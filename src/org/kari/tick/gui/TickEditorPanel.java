package org.kari.tick.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.MatteBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

import org.kari.tick.TickRegistry;
import org.kari.tick.TickSet;

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
            Dimension SIZE = new Dimension(40, 10);
            setMinimumSize(SIZE);
            setMaximumSize(SIZE);
            setPreferredSize(SIZE);
            setBackground(Color.WHITE);
            setBorder(new MatteBorder(1, 1, 1, 0, Color.GRAY    ));
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
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
            g.setFont(pane.getFont());
            int fontHeight = g.getFontMetrics(pane.getFont()).getHeight();
            int fontDesc = g.getFontMetrics(pane.getFont()).getDescent();
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
                    g.fillRect(1, y - fontHeight + 2, width, fontHeight + 2);
                    g.setColor(selectedTextColor);
                } else {
                    g.setColor(color);
                }
                g.drawString(Integer.toString(line), 1, y);
                if (line == caretLineNumber) {
                    g.drawRect(1, y - fontHeight + 2, width, fontHeight + 2);
                }
            }
        }
    }

    private JScrollPane mScrollPane;

    private TickTextPane mEditor;

    private LineNumberPanel mLineNumberPanel;

    public TickEditorPanel() {
        super(new BorderLayout());
        add(getLineNumberPanel(), BorderLayout.WEST);
        add(getScrollPane(), BorderLayout.CENTER);
        getTextPane().setLineNumberPanel(getLineNumberPanel());
    }

    public LineNumberPanel getLineNumberPanel() {
        if (mLineNumberPanel == null) {
            mLineNumberPanel = new LineNumberPanel();
        }
        return mLineNumberPanel;
    }

    public JScrollPane getScrollPane() {
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
        if (mEditor == null) {
            mEditor = new TickTextPane();
            TickSet set = TickRegistry.getInstance().createSet("Set 1");
            mEditor.setTickSet(set);
        }
        return mEditor;
    }

    public void setFile(File pFile, boolean pLoadTicks)
        throws IOException
    {
        TickDocument doc = TickDocumentManager.getInstance().openDocument(
                pFile, pLoadTicks);
        getTextPane().setDocument(doc);
    }

}
