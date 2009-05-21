package org.kari.tick.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

import org.apache.log4j.Logger;
import org.kari.action.ActionConstants;
import org.kari.action.ActionContext;
import org.kari.action.KAction;
import org.kari.tick.Tick;
import org.kari.tick.TickDefinition;
import org.kari.tick.TickLocation;
import org.kari.tick.TickSet;
import org.kari.tick.TickDefinition.BlockMode;

import ca.odell.glazedlists.swing.EventSelectionModel;

/**
 * Tick editor
 * 
 * @author kari
 */
public class TickEditorPanel
    extends JPanel
{
    private static final KeyStroke[] TO_DETAILS_KEY = {
        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_GRAPH_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_GRAPH_DOWN_MASK)
    };

    private static final KeyStroke[] TO_EDITOR_KEY = {
        KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_GRAPH_DOWN_MASK),
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_GRAPH_DOWN_MASK),
    };

    static final Logger LOG = TickConstants.LOG;

    /**
     * Viewport for linenumbers
     * 
     * <li>Paint sidebar ticks
     * <li>Paint caret location
     * 
     * @author kari
     */
    public class TickLineViewport extends JViewport {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            paintExtra(g);
        }

        private void paintExtra(Graphics g) {
            boolean PAINT_SELECTION = false;
            final Graphics2D g2d = (Graphics2D)g;
            final int width = getWidth();

            final TickTextPane pane = getTextPane();
            final Document doc = pane.getDocument();
            final Element rootElement = doc.getDefaultRootElement();
            final int caretLineNumber = TickTextPane.getLineAtCaret(pane);
            
            int selStartLine = -1;
            int selEndLine = -1;
            Color selectedTextColor = null;
            Color selectionColor = null;
            if (PAINT_SELECTION) {
                int selStart = pane.getSelectionStart();
                int selEnd = pane.getSelectionEnd();
                if (selStart != selEnd) {
                    selectedTextColor = pane.getSelectedTextColor();
                    selectionColor = pane.getSelectionColor();
                    selStartLine = rootElement.getElementIndex(selStart) + 1;
                    selEndLine = rootElement.getElementIndex(selEnd) + 1;
                }
            }
            
            final JScrollPane scrollPane = getScrollPane();
            final Point viewPosition = scrollPane.getViewport().getViewPosition();

            // We need to properly convert the points to match the viewport
            // Read docs for viewport
            
            // starting pos in document
            int start = pane.viewToModel(viewPosition);
            
            // end pos in doc
            int end = pane.viewToModel(new Point(
                    viewPosition.x + pane.getWidth(), 
                    viewPosition.y + pane.getHeight()));

            // translate offsets to lines
            int startline = rootElement.getElementIndex(start) + 1;
            int endline = rootElement.getElementIndex(end) + 1;
            
            // font height
            Font font = pane.getFont();
            Font boldFont = font.deriveFont(Font.BOLD);
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics(font);
            int fontHeight = fm.getHeight();
            int fontDesc = fm.getDescent();
            int starting_y = -1;

            try {
                starting_y = pane.modelToView(start).y
                    - viewPosition.y
                    + fontHeight - fontDesc;
            } catch (BadLocationException e) {
                // ignore
            }

//            // font height
//            int fontHeight = g.getFontMetrics(pane.getFont()).getHeight();
            Color color = g.getColor();
            
            // draw selection
            int caretY = -1;
            for (int line = startline, y = starting_y; line <= endline; line++, y += fontHeight) {
                if (line >= selStartLine && line <= selEndLine) {
                    g.setColor(selectionColor);
                    g.fillRect(2, y - fontHeight + 2, width, fontHeight + 2);
                    g.setColor(selectedTextColor);
                } else {
                    g.setColor(color);
                }
                if ((line % 10 == 0)) {
                    g2d.setFont(boldFont);
                } else {
                    g2d.setFont(font);
                }
                if (line == caretLineNumber) {
                    caretY = y;
                }
            }
            
            // draw caret in top of selection
            if (caretY != -1) {
                g.setColor(color);
                g.drawRect(2, caretY - fontHeight + 2, width - 4, fontHeight + 2);
            }
            
            paintTicks(g2d, -viewPosition.y);
        }

        private void paintTicks(Graphics2D g2d, int pYOffset) {
            TickTextPane pane = getTextPane();
            TickDocument doc = pane.getTickDocument();
            TickHighlighter highlighter = getTextPane().getTickHighlighter();
            for (Tick tick : doc.getTicks()) {
                if (tick.isValid()) {
                    BlockMode mode = tick.getLocation().mBlockMode;
                    if (mode == BlockMode.SIDEBAR) {
                        try {
                            tick.paint(this, pane, g2d, pYOffset, highlighter.getHighlight(tick));
                        } catch (BadLocationException e) {
                            LOG.error("Invalid tick: " + tick, e);
                            tick.setValid(false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Scrollpane allowing own custom tick painting
     */
    public class TickScrollPane extends JScrollPane {
        private boolean mIsRowHeader;

        public TickScrollPane(Component pView) {
            super(pView);
        }

        @Override
        protected JViewport createViewport() {
            return mIsRowHeader
                ? new TickLineViewport()
                : super.createViewport();
        }
        
        @Override
        public void setRowHeaderView(Component view) {
            mIsRowHeader = true;
            try {
                super.setRowHeaderView(view);
            } finally {
                mIsRowHeader = false;
            }
        }
    }
    

    /**
     * Drop target
     */
    protected final class DropTargetHandler implements DropTargetListener {
        public void drop(DropTargetDropEvent pEvent) {
            pEvent.acceptDrop(DnDConstants.ACTION_COPY);
            TransferHandler handler = getTransferHandler();
            handler.importData(TickEditorPanel.this, pEvent.getTransferable());
        }
        
        public void dragEnter(DropTargetDragEvent pEvent) {
            pEvent.acceptDrag(DnDConstants.ACTION_COPY);
        }

        public void dragExit(DropTargetEvent pDte) {
            // do nothing
        }

        public void dragOver(DropTargetDragEvent pEvent) {
            pEvent.acceptDrag(DnDConstants.ACTION_COPY);
        }

        public void dropActionChanged(DropTargetDragEvent pDtde) {
            // do nothing
        }
    }
    

    private JSplitPane mSplitPane;

    private JPanel mTopPanel;
    private JScrollPane mScrollPane;
    private TickTextPane mTextPane;
    private LineNumberPanel mLineNumberPanel = new LineNumberPanel() {
        @Override
        public void repaint() {
            JViewport rowHeader = mScrollPane.getRowHeader();
            if (rowHeader != null) {
                rowHeader.repaint();
            }
        }
    };
    
    private TickTable mTickTable;
    
    private boolean mTableChanged;
    private boolean mEditorChanged;
    
    private final Border mFocusedBorder = new MatteBorder(1, 1, 1, 1, Color.BLUE);
    private final Border mUnFocusedBorder = new EmptyBorder(1, 1, 1, 1);
    
    {
        Action clear = new KAction(ActionConstants.R_CLEAR) {
            @Override
            public void actionPerformed(ActionContext pCtx) {
                getTextPane().getTickDocument().clearTicks();
            }
        };
        getActionMap().put(ActionConstants.R_CLEAR, clear);
    }
    
    private final TickHighlighter mTickHighlighter = new TickHighlighter() {
        @Override
        public Highlight getHighlight(Tick pTick) {
            Highlight result = Highlight.NORMAL;
            if (getTickTable().isFocusOwner()) {
                result = getTickTable().getHighlight(pTick);
            } else {
                TickDefinition current = getTextPane().getTickSet().getCurrent();
                
                TickTable table = getTickTable();
                if (pTick.getLocation().intersectLines(
                        table.getHighlightStartLine(),
                        table.getHighlightEndLine())) {
                    result = Highlight.BRIGHT;
                } else if (!pTick.getDefinition().equals(current)) {
                    result = Highlight.DIM;
                }
            }
            return result;
        }

        @Override
        public Set<Tick> getHightlightedTicks() {
            return Collections.<Tick>emptySet();
        }
    };
    
    
    public TickEditorPanel() {
        super(new BorderLayout());
        add(getSplitPane(), BorderLayout.CENTER);
        
        TickTable tickTable = getTickTable();
        TickTextPane textPane = getTextPane();
        
        tickTable.getTickTableModel().setTickDocument(textPane.getTickDocument());
        textPane.setTickHighlighter(mTickHighlighter);
        
        // Draw highlight for focused editor area
        FocusListener fl = new FocusListener() {
            @Override
            public void focusGained(FocusEvent pEvent) {
                setFocusBorder((JComponent)pEvent.getComponent(), true);
            }
            @Override
            public void focusLost(FocusEvent pEvent) {
                setFocusBorder((JComponent)pEvent.getComponent(), false);
            }
        };
        setFocusBorder(textPane, false);
        setFocusBorder(tickTable, false);
        tickTable.addFocusListener(fl);
        textPane.addFocusListener(fl);
        
        DropTargetHandler dh = new DropTargetHandler();
        
        // DnD & Clipboard
//        setDragEnabled(true);
        tickTable.setDragEnabled(true);
        textPane.setDragEnabled(true);
        new DropTarget(this, dh);
        new DropTarget(tickTable, dh);
        new DropTarget(textPane, dh);
    }
    
    public JPanel getTopPanel() {
        if (mTopPanel== null) {
            mTopPanel = new JPanel(new BorderLayout());
            mTopPanel.add(getScrollPane(), BorderLayout.CENTER);
            getTextPane().setLineNumberPanel(mLineNumberPanel);
        }
        return mTopPanel;
    }

    protected JSplitPane getSplitPane() {
        if (mSplitPane == null) {
            mSplitPane = new JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT,
                    getTopPanel(),
                    new JScrollPane(getTickTable()));
            mSplitPane.setDividerLocation(TickConstants.FRAME_WIDTH - 200);
            mSplitPane.setResizeWeight(1.0);
            mSplitPane.setContinuousLayout(true);
        }
        return mSplitPane;
    }

    protected JScrollPane getScrollPane() {
        if (mScrollPane == null) {
            mScrollPane = new TickScrollPane(getTextPane());
        }
        return mScrollPane;
    }

    public TickTextPane getTextPane() {
        if (mTextPane == null) {
            mTextPane = new TickTextPane();
            
            // Focus travelsal
            InputMap inputMap = mTextPane.getInputMap();
            for (KeyStroke keyStoke : TO_DETAILS_KEY) {
                inputMap.put(keyStoke, "activateTable");
            }
            
            mTextPane.getActionMap().put(
                    "activateTable", 
                    new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent pE) {
                            getTickTable().requestFocus();
                        }});
            
            mTextPane.addCaretListener(new CaretListener() {
                @Override
                public void caretUpdate(CaretEvent pEvent) {
                    mEditorChanged = true;
                    try {
                        TickSet tickSet = getTextPane().getTickSet();
                        if (tickSet != null) {
                            BlockMode mode = tickSet.getCurrentMode();
                            TickLocation loc = getTextPane().getTickLocation(mode, true);
                            if (loc != null) {
                                getTickTable().setHighlight(
                                        loc.mStartLine, 
                                        loc.mEndLine,
                                        !mTableChanged);
                            }
                            ensureRangeVisibility(pEvent.getDot(), pEvent.getDot(), 10);
                        }
                    } finally {
                        mEditorChanged = false;
                    }
                }
            });
        }
        return mTextPane;
    }
    
    public TickTable getTickTable() {
        if (mTickTable == null) {
            mTickTable = new TickTable();
            final EventSelectionModel<Tick> sm = mTickTable.getEventSelectionModel();
            
            sm.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent pEvent) {
                    mTableChanged = true;
                    try {
                        Tick tick = mTickTable.getSelectedTick();
                        if (tick != null) {
                            TickLocation loc = tick.getLocation();
                            ensureRangeVisibility(
                                    loc.mStartPos, 
                                    loc.mEndPos,
                                    5);
                            
                            TickTextPane textPane = getTextPane();
                            try {
                                textPane.setCaretPosition(loc.mStartPos);
                            } catch (Exception e) {
                                LOG.warn("invalid loc: " + loc, e);
                            }
                            textPane.repaint();
                        }
                    } finally {
                        mTableChanged = false;
                    }
                }
            });
            
            // Focus travelsal
            InputMap inputMap = mTickTable.getInputMap();
            for (KeyStroke keyStoke : TO_EDITOR_KEY) {
                inputMap.put(keyStoke, "activateEditor");
            }

            mTickTable.getActionMap().put(
                    "activateEditor", 
                    new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent pE) {
                            getTextPane().requestFocus();
                        }});
        }
        return mTickTable;
    }

    public void setFile(File pFile, boolean pLoadTicks)
        throws IOException
    {
        TickDocumentManager docMgr = TickDocumentManager.getInstance();
        TickDocument doc = docMgr.openDocument(
                pFile, 
                pLoadTicks);
        TickDocument oldDoc = getTextPane().getTickDocument();
        if (doc != oldDoc) {
            docMgr.closeDocument(oldDoc);
            getTextPane().setTickDocument(doc);
            getTickTable().getTickTableModel().setTickDocument(doc);
        } else {
            docMgr.closeDocument(doc);
        }
    }

    private void setFocusBorder(JComponent pComp, boolean pFocused) {
        
        if (pComp.getParent() instanceof JViewport) {
            JScrollPane scrollPane = (JScrollPane)pComp.getParent().getParent(); 
            scrollPane.setBorder(pFocused ? mFocusedBorder : mUnFocusedBorder);
        }
    }

    /**
     * Ensure that given start .. end position area has enough context visible
     * around them
     * 
     * @param pLineCount Line count of extra lines before and after range
     */
    public void ensureRangeVisibility(
        int pStartPos, 
        int pEndPos,
        int pLineCount)
    {
        try {
            TickTextPane textPane = getTextPane();
            Rectangle rect = textPane.modelToView(pStartPos);
            {
                Rectangle endRect = textPane.modelToView(pEndPos);
                if (endRect.y > rect.y) {
                    rect.y = endRect.y;
                }
            }
            
            // Provide limited line context around tick
            int lineHeight = rect.height;
            int lineCount = pLineCount;
            rect.y -= lineHeight * lineCount;
            rect.height += lineHeight * lineCount * 2;
            
            textPane.scrollRectToVisible(rect);
        } catch (BadLocationException e) {
            // Ignore
        }
    }

}
