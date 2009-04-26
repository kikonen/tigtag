package org.kari.tick.gui.painter;

import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.apache.log4j.Logger;
import org.kari.tick.Tick;
import org.kari.tick.TickLocation;
import org.kari.tick.gui.TickTextPane;

/**
 * API for tick painters
 * 
 * @author kari
 */
public abstract class TickPainter {
    public static final Logger LOG = Logger.getLogger("tick.painter");

    /**
     * @param pComponent Component on which tick is painted
     * @param pYOffset Offset for sidebar (linenumbers), for text area itself 0.
     */
    public abstract void paint(
        JComponent pComponent,
        TickTextPane pEditor, 
        Graphics g2d, 
        int pYOffset,
        Tick pTick);

    /**
     * Calculate painted rectangle for the tick
     * 
     * @return null if not valid tick
     */
    protected Rectangle calculateTickRect(
        JComponent pComponent,
        TickTextPane pEditor, 
        Tick pTick) {
        Rectangle rect = null;
        try {
            Document doc = pEditor.getDocument();
            int docLen = doc.getLength();
            TickLocation loc = pTick.getLocation();
            int startPos = loc.mStartPos;
            int endPos = loc.mEndPos;
            
            boolean valid = startPos >= 0
                && startPos <= docLen
                && endPos >= 0
                && endPos <= docLen
                && startPos <= endPos;
                
            if (valid) {
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
                
                if (pTick.isInvalid()) {
                    LOG.info("tick is now valid:" + pTick);
                    pTick.setInvalid(false);
                }
            } else {
                if (!pTick.isInvalid()) {
                    LOG.warn("invalid tick:" + pTick);
                    pTick.setInvalid(true);
                }
            }
        } catch (BadLocationException e) {
            if (!pTick.isInvalid()) {
                LOG.warn("invalid tick:" + pTick, e);
                pTick.setInvalid(true);
            }
        }
        return rect;
    }

}
