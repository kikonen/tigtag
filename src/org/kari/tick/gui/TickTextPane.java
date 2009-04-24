package org.kari.tick.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

import org.apache.log4j.Logger;
import org.kari.tick.Tick;
import org.kari.tick.TickDefinition;
import org.kari.tick.TickSet;
import org.kari.tick.gui.TickEditorPanel.LineNumberPanel;
import org.kari.tick.gui.painter.TickPainter;

/**
 * Text pane for tigtag
 * 
 * @author kari
 */
public class TickTextPane extends JTextPane 
    implements 
        CaretListener,
        KeyListener
{
    public static final Logger LOG = Logger.getLogger("tick.editor");
    
    private Rectangle mOldRect = new Rectangle(0, 0, 0 ,0);
    private TickSet mTickSet;
    
    private Action mLeft = new AbstractAction() {
        public void actionPerformed(ActionEvent pE) {
            moveCaret(KeyEvent.VK_LEFT);
        }
    };
    private Action mRight = new AbstractAction() {
        public void actionPerformed(ActionEvent pE) {
            moveCaret(KeyEvent.VK_RIGHT);
        }
    };
    
    private LineNumberPanel mLineNumberPanel;
    
    
    public TickTextPane() {
        super(new TickDocument());
        setEditable(false);
        addCaretListener(this);
        addKeyListener(this);
        
        getActionMap().put("caret-backward", mLeft);
        getActionMap().put("caret-forward", mRight);
        
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }

    /**
     * For disabling line wrapping
     */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    /**
     * For disabling line wrapping
     */
    @Override
    public void setSize(Dimension d) {
        if (d.width < getParent().getSize().width) {
            d.width = getParent().getSize().width;
        }
        super.setSize(d);
    }
    
    public TickDocument getTickDocument() {
        return (TickDocument)super.getDocument();
    }

    /**
     * Jump caret "word by word"
     */
    public void caretUpdate(CaretEvent pEvent) {
        if (false) {
            Element root = getDocument().getDefaultRootElement();
            int line = root.getElementIndex(getCaretPosition()) + 1;
            LOG.info("dot=" + pEvent.getDot() 
                + ", mark=" + pEvent.getMark()
                + ",line=" + line);
        }
        repaint();
    }
    
    public void keyPressed(KeyEvent pE) {
        // TODO KI add TICK
        int keyCode = pE.getKeyCode();
        if (keyCode == KeyEvent.VK_SPACE) {
            if (mTickSet != null) {
                TickDefinition current = mTickSet.getCurrent();
                if (current != null) {
                    try {
                        int caretLocation = getCaretPosition();
                        int startPos = findWordStart(caretLocation);
                        int endPos = findWordEnd(caretLocation);
                        if (startPos != endPos) {
                            Tick tick = new Tick(current, startPos, endPos);
                            TickDocument doc = getTickDocument();
                            if (doc.getTicks().contains(tick)) {
                                doc.removeTick(tick);
                            } else {
                                doc.addTick(tick);
                            }
                            repaint();
                        }
                    } catch (BadLocationException e) {
                        // ignore
                    }
                }
            }
        }
    }

    public void keyReleased(KeyEvent pE) {
        // Ignore
    }

    public void keyTyped(KeyEvent pE) {
        // Ignore
    }
    
    public void moveCaret(int pKeyCode) {
        try {
            Document doc = getDocument();
            int docLen = getDocument().getLength();
            
            int caretLocation = getCaretPosition();
            int newLoc = caretLocation;

            if (pKeyCode == KeyEvent.VK_LEFT) {
                if (caretLocation > 0) {
                    boolean found = false;
                    int pos = caretLocation - 1;
                    
                    while (!found && pos > 0) {
                        String ch = doc.getText(pos, 1);
                        found = !isWordSeparator(ch);
                        if (!found) {
                            pos--;
                        } else {
                            pos = findWordStart(pos);
                        }
                    }
                    
                    newLoc = pos;
                }
            } else if (pKeyCode == KeyEvent.VK_RIGHT) {
                if (caretLocation < docLen) {
                    boolean found = false;
                    int pos = findWordEnd(caretLocation);
                    
                    while (!found && pos < docLen) {
                        String ch = doc.getText(pos, 1);
                        found = !isWordSeparator(ch);
                        if (!found) {
                            pos++;
                        }
                    }
                    newLoc = pos;
                }
            }
        
            if (caretLocation != newLoc) {
                setCaretPosition(newLoc);
            }
        } catch (BadLocationException e) {
            // ignore
        }

    }

    public static int getCaretRowPosition(TickTextPane comp) {
        try {
            int y = comp.modelToView(comp.getCaretPosition()).y;
            int line = y/8;//comp.getRowHeight(comp);
            return ++line;
        } catch (BadLocationException e) {
            LOG.warn("invalid", e);
        }
        return -1;
    }
 
    public static int getCaretColumnPosition(TickTextPane comp) {
        int offset = comp.getCaretPosition();
        int column;
        try {
            column = offset - Utilities.getRowStart(comp, offset);
        } catch (BadLocationException e) {
            LOG.warn("invalid", e);
            column = -1;
        }
        return column;
    }
    
    /**
     *  Return the current line number at the Caret position.
     */
    public static int getLineAtCaret(JTextComponent component)
    {
        int caretPosition = component.getCaretPosition();
        Element root = component.getDocument().getDefaultRootElement();
        return root.getElementIndex( caretPosition ) + 1;
    }
    
    @Override
    public void paint(Graphics pG) {
        super.paint(pG);
        if (mLineNumberPanel != null) {
            mLineNumberPanel.repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics pG) {
        super.paintComponent(pG);

        paintCaret((Graphics2D)pG);
        paintTicks((Graphics2D)pG);
    }

    private void paintCaret(Graphics2D g2d) {
        Rectangle rect = null;
        try {
            int caretPosition = getCaretPosition();
            
            int startPos = findWordStart(caretPosition);
            int endPos = findWordEnd(caretPosition);
            
            Rectangle begin = modelToView(startPos);
            Rectangle end = modelToView(endPos);
            int width = end.x - begin.x;
            rect = new Rectangle(begin.x, begin.y, width, begin.height);
        } catch (BadLocationException e) {
            LOG.warn("invalid", e);
        }
        
        // TODO KI Paint current ticks
        if (rect != null) {
            g2d.setColor(Color.BLUE);
            if (rect.width == 0) {
                g2d.drawLine(
                        rect.x, 
                        rect.y,
                        rect.x,
                        rect.y + rect.height);
            } else {
                int GAP_H = 4;
                int GAP_V = 2;
                g2d.drawRoundRect(
                        rect.x - GAP_H, 
                        rect.y - GAP_V, 
                        rect.width + GAP_H * 2, 
                        rect.height + GAP_V * 2,
                        10,
                        10);
            }
        }

//        if (!mOldRect.equals(rect)) {
//            repaint();
//        }
        if (rect != null) {
            mOldRect = rect;
        }
    }
    
    private void paintTicks(Graphics2D g2d) {
        TickDocument doc = getTickDocument();
        for (String tickName : doc.getTickNames()) {
            for (Tick tick : doc.getTicks(tickName)) {
                TickPainter painter = tick.getTickDefinition().getPainter();
                painter.paint(this, g2d, tick);
            }
        }
    }
            
    /**
     * Find beginning of the current word
     */
    public int findWordStart(int pStartPosition) 
        throws BadLocationException
    {
        Document doc = getDocument();
        boolean found = false;
        int pos = pStartPosition;
        while (!found && pos > 0) {
            String ch = doc.getText(pos - 1, 1);
            found = isWordSeparator(ch);
            if (!found) {
                pos--;
            }
        }
        if (pos < 0) {
            pos = 0;
        }
        return pos;
    }
     
    /**
     * Find end of the current word
     */
    public int findWordEnd(int pStartPosition) 
        throws BadLocationException
    {
        Document doc = getDocument();
        int docLen = getDocument().getLength();
        
        boolean found = false;
        int pos = pStartPosition;
        
        while (!found && pos <= docLen) {
            String ch = doc.getText(pos, 1);
            found = isWordSeparator(ch);
            if (!found) {
                pos++;
            }
        }
        if (pos > docLen) {
            pos = docLen;
        }
        return pos;
    }

    private boolean isWordSeparator(String pText) {
        char ch = pText.charAt(0);
        return ch == '('
            || ch == ')'
            || ch == '['
            || ch == ']'
            || ch == '{'
            || ch == '}'
            || ch == ','
            || ch == ';'
            || Character.isWhitespace(ch);
    }
    
    /**
     * Is document empty
     */
    public boolean isEmpty() {
        return getTickDocument().getLength() == 0;
    }

    /**
     * @return Currently used tickset, null if none
     */
    public TickSet getTickSet() {
        return mTickSet;
    }

    public void setTickSet(TickSet pTickSet) {
        mTickSet = pTickSet;
    }

    public LineNumberPanel getLineNumberPanel() {
        return mLineNumberPanel;
    }

    public void setLineNumberPanel(LineNumberPanel pLineNumberPanel) {
        mLineNumberPanel = pLineNumberPanel;
    }
    
}
