package org.kari.tick.gui.painter;

import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Paint underline for tick
 * 
 * @author kari
 */
public class UnderlinePainter extends HighlightPainter {

    @Override
    protected void paintLine(
        Graphics2D g2d, 
        Rectangle start, 
        Rectangle end)
    {
        g2d.drawLine(
                start.x - GAP_H, 
                start.y + start.height, 
                start.x + end.x - start.x, 
                start.y + start.height);
    }
}
