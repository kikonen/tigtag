package org.kari.tick.gui.painter;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import org.kari.tick.gui.TickHighlighter.Highlight;

/**
 * Felt pen over text
 * 
 * @author kari
 */
public class FeltpenPainter extends HighlightPainter {
    @Override
    protected void paintLines(
        Graphics2D g2d,
        Highlight pHighlight,
        List<Rectangle> pLines)
    {
        Composite origComposite = g2d.getComposite();
        if (pHighlight == Highlight.DIM) {
            g2d.setComposite(PEN_DIM_COMPOSITE);
        } else if (pHighlight == Highlight.BRIGHT) {
            g2d.setComposite(BRIGHT_COMPOSITE);
        } else{
            g2d.setComposite(NORMAL_COMPOSITE);
        }
        
        for (Rectangle rect : pLines) {
            g2d.fillRect(
                    rect.x - GAP_H, 
                    rect.y - GAP_V, 
                    rect.width + GAP_H * 2, 
                    rect.height + GAP_V * 2);
        }
        
        g2d.setComposite(origComposite);
    }

}
