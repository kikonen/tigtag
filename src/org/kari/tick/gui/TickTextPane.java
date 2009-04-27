package org.kari.tick.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
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
import org.kari.tick.TickLocation;
import org.kari.tick.TickSet;
import org.kari.tick.TickDefinition.BlockMode;
import org.kari.tick.gui.TickEditorPanel.LineNumberPanel;
import org.kari.tick.gui.painter.TickPainter;

/**
 * Text pane for tigtag
 * 
 * @author kari
 */
public class TickTextPane extends JTextPane {
    public static final Logger LOG = Logger.getLogger("tick.editor");
    public static final int DEF_MAX_LINELEN = 80;
    private static final BasicStroke MAX_LINELEN_STROKE = new BasicStroke(
            1.0f, 
            BasicStroke.CAP_SQUARE, 
            BasicStroke.JOIN_BEVEL, 
            0, 
            new float[]{8f, 8f},
            0);
    
    private class EventHandler 
        implements 
            CaretListener,
            KeyListener,
            TickListener
    {
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
                tick();
            }
        }

        public void keyReleased(KeyEvent pE) {
            // Ignore
        }

        public void keyTyped(KeyEvent pE) {
            // Ignore
        }

        public void tickAdded(TickDocument pDocument, Tick pTick) {
            repaint();
        }

        public void tickRemoved(TickDocument pDocument, Tick pTick) {
            repaint();
        }
    }
    
    private final EventHandler mEventHandler = new EventHandler();
    private Rectangle mOldRect = new Rectangle(0, 0, 0 ,0);
    private TickSet mTickSet;
    private int mMaxLineLen = DEF_MAX_LINELEN;
    
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
        addCaretListener(mEventHandler);
        addKeyListener(mEventHandler);
        
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
    
    @Override
    public void setDocument(Document pDoc) {
        Document oldDoc = getDocument();
        if (oldDoc instanceof TickDocument) {
            ((TickDocument)oldDoc).removeTickListener(mEventHandler);
        }
        super.setDocument(pDoc);
        if (pDoc instanceof TickDocument) {
            ((TickDocument)pDoc).addTickListener(mEventHandler);
        }
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
// -----------------------------------------------------------------------------123-----------------
        super.paintComponent(pG);
        
        Graphics2D g2d = (Graphics2D)pG;
        paintLineLimit(g2d);
        paintTicks(g2d);
        paintCaret(g2d);
    }

    /**
     * Paint maximum line length limit
     */
    private void paintLineLimit(Graphics2D g2d) {
        Color origColor = g2d.getColor();
        Stroke origStroke = g2d.getStroke();
        
        Font font = getFont();
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics(font);
        int fontWidth = fm.getWidths()[0];
        int x = fontWidth * mMaxLineLen;
        
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(MAX_LINELEN_STROKE);
        g2d.drawLine(x, 0, x, getHeight());
        
        g2d.setColor(origColor);
        g2d.setStroke(origStroke);
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
        for (Tick tick : doc.getTicks()) {
            BlockMode mode = tick.getLocation().mBlockMode;
            if (mode != BlockMode.SIDEBAR) {
                TickPainter painter = mode.getPainter();
                painter.paint(this, this, g2d, 0, tick);
            }
        }
    }
    
    /**
     * Find location for the line start
     */
    public int findLineStart(int pStartPosition) 
        throws BadLocationException
    {
        final Document doc = getDocument();
        Element rootElement = doc.getDefaultRootElement();
        int line = rootElement.getElementIndex(pStartPosition);
        Element elem = rootElement.getElement(line);

        return elem.getStartOffset();
    }

    public int findLineEnd(int pStartPosition) 
        throws BadLocationException
    {
        final Document doc = getDocument();
        Element rootElement = doc.getDefaultRootElement();
        int line = rootElement.getElementIndex(pStartPosition);
        Element elem = rootElement.getElement(line);
        return elem.getEndOffset();
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

    public int getMaxLineLen() {
        return mMaxLineLen;
    }

    public void setMaxLineLen(int pMaxLineLen) {
        mMaxLineLen = pMaxLineLen;
    }

    /**
     * Toggle tick at current caret/selection. If there is already tick, then
     * it's removed, otherwise new tick is created.
     */
    public void tick() {
        if (mTickSet != null) {
            TickDefinition current = mTickSet.getCurrent();
            if (current != null) {
                TickLocation loc = getTickLocation(mTickSet.getCurrentMode());
                
                if (loc != null) {
                    Tick tick = new Tick(current, loc, getText(loc));
                    TickDocument doc = getTickDocument();
                    if (doc.getTicks().contains(tick)) {
                        doc.removeTick(tick);
                    } else {
                        doc.addTick(tick);
                    }
                    repaint();
                }
            }
        }
    }
    
    
    /**
     * Get tick position based into current tick mode
     * 
     * @return location, null if current location is invalid for ticking
     */
    public TickLocation getTickLocation(BlockMode pBlockMode) {
        TickLocation result = null;
        try {
            int caretLocation = getCaretPosition();
            
            int selStart = -1;
            int selEnd= -1;
            {
                selStart = getSelectionStart();
                selEnd = getSelectionEnd();
            }

            int startPos;
            int endPos;
            if (selStart != selEnd) {
                startPos = selStart;
                endPos = selEnd;
            } else {
                startPos = findWordStart(caretLocation);
                endPos = findWordEnd(caretLocation);
            }
            
            // TODO KI startPos/endPos depend from tick def
            // BLOCK/SIDEBAR = lines
            // WORD = word boundaries
            // POINT = exact selection range (or word from caret)
            if (startPos != endPos) {
                final Document doc = getDocument();
                final Element rootElement = doc.getDefaultRootElement();
                int startLine = rootElement.getElementIndex(startPos);
                int endLine = rootElement.getElementIndex(endPos);
                
                result = new TickLocation(
                        pBlockMode,
                        startPos, 
                        endPos, 
                        startLine, 
                        endLine);
            }
        } catch (BadLocationException e) {
            // ignore
        }
        
        return result;
    }
    
    /**
     * Get text for tick location
     */
    public String getText(TickLocation pLoc) {
        String text = null;
        try {
            text = getDocument().getText(pLoc.mStartPos, pLoc.mEndPos - pLoc.mStartPos);
        } catch (BadLocationException e) {
            // ignore
        }
        return text;
    }
    
}
