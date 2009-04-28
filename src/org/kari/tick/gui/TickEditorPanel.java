package org.kari.tick.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.TransferHandler;
import javax.swing.border.MatteBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

import org.kari.tick.Tick;
import org.kari.tick.TickEditorStarter;
import org.kari.tick.TickRegistry;
import org.kari.tick.TickSet;
import org.kari.tick.TickDefinition.BlockMode;
import org.kari.tick.gui.painter.TickPainter;

/**
 * Tick editor
 * 
 * @author kari
 */
public class TickEditorPanel
    extends JPanel
{
    /**
     * Panel painting linenumbers
     */
    public class LineNumberPanel
        extends JPanel
    {
        public LineNumberPanel() {
            Dimension SIZE = new Dimension(80, 10);
            setMinimumSize(SIZE);
            setMaximumSize(SIZE);
            setPreferredSize(SIZE);
            setBackground(Color.WHITE);
            setBorder(new MatteBorder(1, 1, 1, 0, Color.GRAY    ));
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            
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
                g.drawString(Integer.toString(line), 4, y);
                if (line == caretLineNumber) {
                    g.drawRect(2, y - fontHeight + 2, width, fontHeight + 2);
                }
            }
            
            paintTicks(g2d, -viewPosition.y);
        }
        
        private void paintTicks(Graphics2D g2d, int pYOffset) {
            TickTextPane pane = getTextPane();
            TickDocument doc = pane.getTickDocument();
            for (Tick tick : doc.getTicks()) {
                BlockMode mode = tick.getLocation().mBlockMode;
                if (mode == BlockMode.SIDEBAR) {
                    TickPainter painter = mode.getPainter();
                    painter.paint(this, pane, g2d, pYOffset, tick);
                }
            }
        }
    }

    /**
     * Allow DnD of files into editor
     */
    public class TickTransferHandler extends TransferHandler {
        @Override
        public boolean canImport(JComponent pComp, DataFlavor[] pTransferFlavors)
        {
            return true;
        }

        @Override
        public boolean importData(JComponent pComp, Transferable pTransferable) {
            boolean result = false;
            try {
                DataFlavor[] flavors = pTransferable.getTransferDataFlavors();
                TickConstants.LOG.info(flavors);
                // TODO KI this is *NOT* pretty
                String wanted = "java.awt.datatransfer.DataFlavor[mimetype=text/uri-list;representationclass=java.lang.String]";
                DataFlavor flavor = null;//new DataFlavor(String.class, "text/uri-list;");
                for (DataFlavor tmp : flavors) {
                    if (wanted.equals(tmp.toString())) {
                        flavor = tmp;
                        break;
                    }
                }
//                TickConstants.LOG.info(flavor);
                if (pTransferable.isDataFlavorSupported(flavor)) {
                    String value = (String)pTransferable.getTransferData(flavor);
                    TickConstants.LOG.info(value);
                    String[] filenames = value.split("\n");
                    for (String filename : filenames) {
                        filename = filename.trim();
                        if (filename.startsWith("file://")) {
                            filename = filename.substring(7);
                        } else  if (filename.startsWith("file:")) {
                            filename = filename.substring(5);
                        }
                        new TickEditorStarter(filename).start();
                    }
                }
            } catch (Exception e) {
                TickConstants.LOG.error("Failed ot insert", e);
            }
            return result;
        }
    }
    

    private JSplitPane mSplitPane;

    private JPanel mTopPanel;
    private JScrollPane mScrollPane;
    private TickTextPane mTextPane;
    private LineNumberPanel mLineNumberPanel;
    
    private TickTable mTickTable;
    
    public TickEditorPanel() {
        super(new BorderLayout());
        add(getSplitPane(), BorderLayout.CENTER);
        getTickTable().getTickTableModel().setDocument(getTextPane().getTickDocument());
        TransferHandler th = new TickTransferHandler();
        getTickTable().setTransferHandler(th);
        getTextPane().setTransferHandler(th);
        getLineNumberPanel().setTransferHandler(th);
        setTransferHandler(th);
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
            mSplitPane.setDividerLocation(400);
            mSplitPane.setResizeWeight(1.0);
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
            TickSet set = TickRegistry.getInstance().createSet("Set 1");
            mTextPane.setTickSet(set);
        }
        return mTextPane;
    }
    
    public TickTable getTickTable() {
        if (mTickTable == null) {
            mTickTable = new TickTable();
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
            getTextPane().setDocument(doc);
            getTickTable().getTickTableModel().setDocument(doc);
        } else {
            docMgr.closeDocument(doc);
        }
    }

}
