package org.kari.tick.gui.painter;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import org.kari.tick.gui.TickHighlighter.Highlight;

/**
 * Paint rubberband around text
 * 
 * @author kari
 */
public class RubberbandPainter extends HighlightPainter {
    @Override
    protected void paintLines(
        Graphics2D g2d,
        Highlight pHighlight,
        List<Rectangle> pLines)
    {
        Composite origComposite = g2d.getComposite();
        if (pHighlight == Highlight.DIM) {
            g2d.setComposite(DIM_COMPOSITE);
        } else if (pHighlight == Highlight.BRIGHT) {
            g2d.setStroke(BRIGHT_STROKE);
        }
        
        for (Rectangle rect : pLines) {
            g2d.drawRect(
                    rect.x - GAP_H, 
                    rect.y, 
                    rect.width, 
                    rect.height);
        }
        
        g2d.setComposite(origComposite);
    }

}
