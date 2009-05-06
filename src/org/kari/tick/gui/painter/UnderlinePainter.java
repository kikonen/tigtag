package org.kari.tick.gui.painter;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.kari.tick.gui.TickHighlighter.Highlight;

/**
 * Paint underline for tick
 * 
 * @author kari
 */
public class UnderlinePainter extends HighlightPainter {

    @Override
    protected void paintLine(
        Graphics2D g2d, 
        Rectangle rect, 
        Highlight pHighlight)
    {
        if (pHighlight == Highlight.DIM) {
            g2d.setComposite(DIM_COMPOSITE);
        } else if (pHighlight == Highlight.BRIGHT) {
            g2d.setStroke(BRIGHT_STROKE);
        }
        g2d.drawLine(
                rect.x - GAP_H, 
                rect.y + rect.height, 
                rect.x + rect.width, 
                rect.y + rect.height);
    }
}
