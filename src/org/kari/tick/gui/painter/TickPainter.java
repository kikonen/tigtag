package org.kari.tick.gui.painter;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.apache.log4j.Logger;
import org.kari.tick.Tick;
import org.kari.tick.TickLocation;
import org.kari.tick.gui.TickTextPane;
import org.kari.tick.gui.TickHighlighter.Highlight;

/**
 * API for tick painters
 * 
 * @author kari
 */
public abstract class TickPainter {
    public static final Logger LOG = Logger.getLogger("tick.painter");

    protected static final BasicStroke BRIGHT_STROKE = new BasicStroke(2);
    protected static final AlphaComposite DIM_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.60f);

    protected List<Rectangle> mRects; 
    protected Font mFont;
    protected Point mNameLoc;
    
    /**
     * @param pComponent Component on which tick is painted
     * @param pYOffset Offset for sidebar (linenumbers), for text area itself 0.
     * @param pHighlight If true then tick should be 
     */
    public abstract void paint(
            JComponent pComponent,
            TickTextPane pEditor, 
            Graphics2D g2d, 
            int pYOffset,
            Tick pTick,
            Highlight pHighlight)
        throws BadLocationException;

    /**
     * Calculate painted rectangle for the tick
     * 
     * @return null if not valid tick
     */
    protected Rectangle calculateTickRect(
            JComponent pComponent,
            TickTextPane pEditor, 
            Tick pTick)
        throws BadLocationException
    {
        Rectangle rect = null;
        Document doc = pEditor.getDocument();
        TickLocation loc = pTick.getLocation();
        int startPos = loc.mStartPos;
        int endPos = loc.mEndPos;
        
        Rectangle start = pEditor.modelToView(startPos);
        Rectangle end = pEditor.modelToView(endPos);
        int width = end.x - start.x;
        int height = start.height;
        int x = start.x;
        int y = start.y;
        
        if (width < 0) {
            width = start.x - end.x;
            x = end.x;
        }
        
        if (end.y > start.y) {
            height = end.y - start.y + end.height;
        }
        
        if (width > 0) {
            rect = new Rectangle(x, y, width, height);
        }
        
        return rect;
    }

}
