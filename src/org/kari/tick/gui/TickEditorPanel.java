package org.kari.tick.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
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

/**
 * Tick editor
 * 
 * @author kari
 */
public class TickEditorPanel
    extends JPanel
{
    static final Logger LOG = TickConstants.LOG;

    /**
     * Panel painting linenumbers
     */
    public class LineNumberPanel
        extends JPanel
    {
        public LineNumberPanel() {
            Dimension SIZE = new Dimension(30, 10);
            setMinimumSize(SIZE);
            setMaximumSize(SIZE);
            setPreferredSize(SIZE);
            setBackground(Color.WHITE);
            setBorder(new MatteBorder(1, 1, 1, 0, Color.GRAY    ));
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            paintExtra(g);
        }          
        
        private void paintExtra(Graphics g) {
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
            {
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
    private LineNumberPanel mLineNumberPanel;
    
    private TickTable mTickTable;
    
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
                if (!pTick.getDefinition().equals(current)) {
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
            mTopPanel.add(getLineNumberPanel(), BorderLayout.WEST);
            mTopPanel.add(getScrollPane(), BorderLayout.CENTER);
            getTextPane().setLineNumberPanel(getLineNumberPanel());
        }
        return mTopPanel;
    }

    public LineNumberPanel getLineNumberPanel() {
        if (mLineNumberPanel == null) {
            mLineNumberPanel = new LineNumberPanel();
        }
        return mLineNumberPanel;
    }

    protected JSplitPane getSplitPane() {
        if (mSplitPane == null) {
            mSplitPane = new JSplitPane(
                    JSplitPane.VERTICAL_SPLIT,
                    getTopPanel(),
                    new JScrollPane(getTickTable()));
            mSplitPane.setDividerLocation(TickFrame.HEIGHT - 200);
            mSplitPane.setResizeWeight(1.0);
            mSplitPane.setContinuousLayout(true);
        }
        return mSplitPane;
    }

    protected JScrollPane getScrollPane() {
        if (mScrollPane == null) {
            mScrollPane = new JScrollPane(getTextPane());
            AdjustmentListener al = new AdjustmentListener() {
                public void adjustmentValueChanged(AdjustmentEvent pE) {
                    getLineNumberPanel().repaint();
                }
            };
            mScrollPane.getHorizontalScrollBar().addAdjustmentListener(al);
            mScrollPane.getVerticalScrollBar().addAdjustmentListener(al);
        }
        return mScrollPane;
    }

    public TickTextPane getTextPane() {
        if (mTextPane == null) {
            mTextPane = new TickTextPane();
            
            // Focus travelsal
            mTextPane.getInputMap().put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK), 
                    "activateTable");
            mTextPane.getInputMap().put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_GRAPH_DOWN_MASK), 
                    "activateTable");
            
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
                    TickSet tickSet = getTextPane().getTickSet();
                    if (tickSet != null) {
                        BlockMode mode = tickSet.getCurrentMode();
                        TickLocation loc = getTextPane().getTickLocation(mode, true);
                        if (loc != null) {
                            TickTableModel tableModel = getTickTable().getTickTableModel();
                            tableModel.setHighlightStartLine(loc.mStartLine);
                            tableModel.setHighlightEndLine(loc.mEndLine);
                            getTickTable().repaint();
                        }
                    }
                }
            });
        }
        return mTextPane;
    }
    
    public TickTable getTickTable() {
        if (mTickTable == null) {
            mTickTable = new TickTable();
            mTickTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent pEvent) {
                    int selectedRow = mTickTable.getSelectedRow();
                    TickTextPane textPane = getTextPane();
                    if (selectedRow != -1) {
                        Tick tick = mTickTable.getTickTableModel().getRowElement(selectedRow);
                        try {
                            int pos = tick.getLocation().mStartPos;
                            Rectangle rect = textPane.modelToView(pos);
                            {
                                Rectangle endRect = textPane.modelToView(tick.getLocation().mEndPos);
                                if (endRect.y > rect.y) {
                                    rect.y = endRect.y;
                                }
                            }
                            
                            // Provide limited line context around tick
                            int lineHeight = rect.height;
                            int lineCount = 5;
                            rect.y -= lineHeight * lineCount;
                            rect.height += lineHeight * lineCount * 2;
                            
                            textPane.scrollRectToVisible(rect);
                            textPane.setCaretPosition(pos);
                        } catch (BadLocationException e) {
                            // Ignore
                        }
                    }
                    textPane.repaint();
                }
            });
            
            // Focus travelsal
            mTickTable.getInputMap().put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK), 
                    "activateEditor");
            mTickTable.getInputMap().put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_GRAPH_DOWN_MASK), 
                    "activateEditor");
            
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

}
