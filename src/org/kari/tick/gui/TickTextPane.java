package org.kari.tick.gui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

import org.apache.log4j.Logger;
import org.kari.perspective.ViewUtil;
import org.kari.properties.Apply;
import org.kari.properties.KPropertiesFrame;
import org.kari.properties.PropertiesViewer;
import org.kari.tick.Tick;
import org.kari.tick.TickDefinition;
import org.kari.tick.TickLocation;
import org.kari.tick.TickSet;
import org.kari.tick.TickDefinition.BlockMode;
import org.kari.tick.gui.syntax.SyntaxRenderer;

/**
 * Text pane for tigtag
 * 
 * @author kari
 */
public class TickTextPane extends JEditorPane {
    public static final Logger LOG = Logger.getLogger("tick.editor");
    public static final int DEF_MAX_LINELEN = 80;
    private static final BasicStroke MAX_LINELEN_STROKE = new BasicStroke(
            1.0f, 
            BasicStroke.CAP_SQUARE, 
            BasicStroke.JOIN_BEVEL, 
            1.0f, 
            new float[]{8f, 8f},
            0);
    
    private static final int CARET_GAP_H = 0;
    private static final int CARET_GAP_V = 0;
    private static final AlphaComposite CARET_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f);

    
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
            mCaretRect = calculateCaretRect();
            repaintFull();
        }
        
        public void keyPressed(KeyEvent pEvent) {
            int keyCode = pEvent.getKeyCode();
            char ch = pEvent.getKeyChar();
            boolean canTick = false;
            String str = null;
            if (keyCode == KeyEvent.VK_SPACE) {
                canTick = true;
            } else {
                if (keyCode == KeyEvent.VK_V && pEvent.isControlDown()) {
                    canTick = true;
                    try {
                        str = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                    } catch (Exception e) {
                        LOG.error("Failed to access clipboard");
                    }
                } else {
                    canTick = (Character.isLetter(ch) || Character.isDigit(ch))
                        && !(pEvent.isAltDown()
                            || pEvent.isAltGraphDown()
                            || pEvent.isControlDown() 
                            || pEvent.isMetaDown()
                            || pEvent.isActionKey()
                            );
                    if (canTick) {
                        str = new String(new char[]{ch});
                    }
                }
            }
            if (canTick) {
                tick(keyCode, str);
            }
        }

        public void keyReleased(KeyEvent pE) {
            // Ignore
        }

        public void keyTyped(KeyEvent pE) {
            // Ignore
        }

        public void tickAdded(TickDocument pDocument, Tick pTick) {
            repaintFull();
        }

        public void tickRemoved(TickDocument pDocument, Tick pTick) {
            repaintFull();
        }
    }
    
    private final EventHandler mEventHandler = new EventHandler();
    
    private int mCharWidth;
    private Rectangle mCaretRect;
    
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
    private TickHighlighter mTickHighlighter;
    private TickDocument mTickDocument;
    
    public TickTextPane() {
        setEditable(false);
        addCaretListener(mEventHandler);
        addKeyListener(mEventHandler);
        
        getActionMap().put("caret-backward", mLeft);
        getActionMap().put("caret-forward", mRight);
        
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        setTickDocument(new TickDocument(true));
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
        return mTickDocument;
    }

    public void setTickDocument(TickDocument pTickDocument) 
    {
        TickDocument oldDoc = mTickDocument;
        if (oldDoc != null) {
            oldDoc.removeTickListener(mEventHandler);
        }
        mTickDocument = pTickDocument;
        
        try {
            if (mTickDocument != null && !mTickDocument.isEmpty()) {
                SyntaxRenderer sr = SyntaxRenderer.getRenderer(mTickDocument.getFile());
                sr.render(this, pTickDocument);
                mTickDocument.addTickListener(mEventHandler);
            }
        } catch (Exception e) {
            LOG.error("Failed to show: " + pTickDocument.getFile(), e);
        }
        refresh();
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
    
    /**
     * Refresh view
     */
    public void refresh() {
        mCharWidth = 0;
        mCaretRect = null;
        getTickDocument().refresh();
    }
    
    @Override
    public void paint(Graphics pG) {
        super.paint(pG);
        
        Graphics2D g2d = (Graphics2D)pG;
        // max line limit and acceptable 20% exceeding of it
        paintLineLimit(g2d, mMaxLineLen, Color.LIGHT_GRAY);
        paintLineLimit(g2d, mMaxLineLen + 20, Color.ORANGE);
        paintLineLimit(g2d, mMaxLineLen + 40, Color.RED);
        paintTicks(g2d);
        paintCaret(g2d);
    }

    @Override
    protected void paintComponent(Graphics pG) {
        super.paintComponent(pG);
    }

    /**
     * Paint maximum line length limit
     * 
     * @param pLimit Limit in characters
     */
    private void paintLineLimit(
        Graphics2D g2d, 
        int pLimit,
        Color pColor) 
    {
        Color origColor = g2d.getColor();
        Stroke origStroke = g2d.getStroke();

        if (mCharWidth == 0) {
            Font font = getFont();
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics(font);
            mCharWidth = fm.getWidths()[0];
        }
        int x = mCharWidth * pLimit;
        
        g2d.setColor(pColor);
        g2d.setStroke(MAX_LINELEN_STROKE);
        g2d.drawLine(x, 0, x, getHeight());
        
        g2d.setColor(origColor);
        g2d.setStroke(origStroke);
    }

    private void paintCaret(Graphics2D g2d) {
        if (mCaretRect == null) {
            mCaretRect = calculateCaretRect();
        }
        Rectangle rect = mCaretRect;
        
        if (rect != null) {
            Color origColor = g2d.getColor();
            g2d.setColor(Color.BLUE);
            if (rect.width == 0) {
                g2d.drawLine(
                        rect.x, 
                        rect.y,
                        rect.x,
                        rect.y + rect.height);
            } else {
                Composite origComposite = g2d.getComposite();
                g2d.setComposite(CARET_COMPOSITE);
                
                g2d.drawRect(
                        rect.x - CARET_GAP_H, 
                        rect.y - CARET_GAP_V, 
                        rect.width + CARET_GAP_H * 2, 
                        rect.height + CARET_GAP_V * 2);
                g2d.setComposite(origComposite);
            }
            g2d.setColor(origColor);
        }
    }

    private void paintTicks(Graphics2D g2d) {
        TickDocument doc = getTickDocument();
        for (Tick tick : doc.getTicks()) {
            if (tick.isValid()) {
                BlockMode mode = tick.getLocation().mBlockMode;
                if (mode != BlockMode.SIDEBAR) {
                    try {
                        tick.paint(this, this, g2d, 0, mTickHighlighter.getHighlight(tick));
                    } catch (BadLocationException e) {
                        LOG.error("Invalid tick: " + tick, e);
                        tick.setValid(false);
                    }
                }
            }
        }
    }

    /**
     * Calculate current "caret" location rectangle for ticking
     * 
     * @return Caret rectangle, null if position is invalid
     */
    private Rectangle calculateCaretRect() {
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
        return rect;
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
        if (false) {
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
        } else {
            return Utilities.getWordStart(this, pStartPosition);
        }
    }
     
    /**
     * Find end of the current word
     */
    public int findWordEnd(int pStartPosition) 
        throws BadLocationException
    {
        if (true) {
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
        } else {
            return Utilities.getWordEnd(this, pStartPosition);
        }
    }

    private boolean isWordSeparator(String pText) {
        char ch = pText.charAt(0);
        return ch == '('
            || ch == ')'
            || ch == '['
            || ch == ']'
            || ch == '{'
            || ch == '}'
            || ch == '.'
            || ch == ','
            || ch == ';'
            || Character.isWhitespace(ch);
    }
    
    /**
     * Is document empty
     */
    public boolean isEmpty() {
        return getDocument().getLength() == 0;
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
    
    public TickHighlighter getTickHighlighter() {
        return mTickHighlighter;
    }

    public void setTickHighlighter(TickHighlighter pTickHighlighter) {
        mTickHighlighter = pTickHighlighter;
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
     * 
     * @param pKeyCode key triggering tick
     * @param pText Initial text for tick
     */
    public void tick(int pKeyCode, String pText) {
        if (mTickSet != null) {
            TickDefinition current = mTickSet.getCurrent();
            if (current != null) {
                TickLocation loc = getTickLocation(mTickSet.getCurrentMode(), false);
                
                if (loc != null) {
                    Tick tick = new Tick(current, loc, getText(loc));
                    final TickDocument doc = getTickDocument();
                    final List<Tick> ticks = doc.getTicks();
                    final int idx = ticks.indexOf(tick);
                    
                    if (pKeyCode == KeyEvent.VK_SPACE) {
                        if (idx != -1) {
                            doc.removeTick(tick);
                        } else {
                            doc.addTick(tick);
                        }
                    } else {
                        String comment = null;
                        if (idx != -1) {
                            tick = ticks.get(idx);
                            comment = tick.getComment();
                        }
                        if (comment == null) {
                            tick.setComment(pText);
                        }
                    
                        final Tick origTick = tick;
                        Apply apply = new Apply() {
                            @Override
                            public void apply(KPropertiesFrame pDialog)
                                throws Exception
                            {
                                Tick newTick = (Tick)pDialog.getContent();
                                doc.removeTick(origTick);
                                doc.addTick(newTick);
                            }
                        };

                        try {
                            new PropertiesViewer(ViewUtil.getFrame(this), tick, apply).show();
                        } catch (Exception e) {
                            LOG.error("Failed to edit");
                        }
                    }
                }
            }
        }
    }
    
    
    /**
     * Get tick position based into current tick mode
     * 
     * @return location, null if current location is invalid for ticking
     */
    public TickLocation getTickLocation(BlockMode pBlockMode, boolean pAllowEmpty) {
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
            if (startPos != endPos || pAllowEmpty) {
                final Document doc = getDocument();
                final Element rootElement = doc.getDefaultRootElement();
                int startLine = rootElement.getElementIndex(startPos);
                int endLine = rootElement.getElementIndex(endPos);
                
                if (pBlockMode.isLineMode()) {
                    startPos = findLineStart(startPos);
                    endPos = findLineEnd(endPos);
                }
                
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

    /**
     * Repaint both text area & line numbers
     */
    public void repaintFull() {
        repaint();
        if (mLineNumberPanel != null) {
            mLineNumberPanel.repaint();
        }
    }
    
}
