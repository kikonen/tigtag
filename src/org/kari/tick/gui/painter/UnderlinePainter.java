package org.kari.tick.gui.painter;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import org.kari.tick.gui.TickHighlighter.Highlight;

/**
 * Underline for text
 * 
 * @author kari
 */
public class UnderlinePainter extends HighlightPainter {
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
            g2d.drawLine(
                    rect.x - GAP_H, 
                    rect.y + rect.height, 
                    rect.x + rect.width, 
                    rect.y + rect.height);
        }
        
        g2d.setComposite(origComposite);
    }
}
