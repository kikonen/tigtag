package org.kari.tick.gui.painter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;

import org.kari.tick.Tick;
import org.kari.tick.gui.TickTextPane;

/**
 * Code word painter. Paints tick around word (or set of words), if necessary,
 * block is drawn around multiple code lines (outlining only selected text
 * area)
 * 
 * @author kari
 */
public class WordPainter extends TickPainter {

    @Override
    public void paint(
        JComponent pComponent,
        TickTextPane pEditor,
        Graphics g2d,
        int pYOffset,
        Tick pTick) 
    {
        Rectangle rect = calculateTickRect(pComponent, pEditor, pTick);
        if (rect != null) {
            g2d.setColor(Color.RED);
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

}
