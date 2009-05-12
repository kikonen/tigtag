package org.kari.tick.gui.painter;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import javax.swing.JComponent;
import javax.swing.text.BadLocationException;

import org.kari.tick.Tick;
import org.kari.tick.gui.TickTextPane;
import org.kari.tick.gui.TickHighlighter.Highlight;

/**
 * Same logic than in BlockPAinter, except this painter is used
 * in side bar only
 * 
 * @author kari
 */
public class SidebarPainter extends BlockPainter {
    public SidebarPainter() {
        super();
    }

    @Override
    public void paint(
        JComponent pComponent,
        TickTextPane pEditor,
        Graphics2D g2d,
        int pYOffset,
        Tick pTick,
        Highlight pHighlight)
        throws BadLocationException
    {
        mNameLoc = null;
        super.paint(pComponent, pEditor, g2d, pYOffset, pTick, pHighlight);
    }


    @Override
    protected Rectangle calculateTickRect(
            JComponent pComponent,
            TickTextPane pEditor, 
            Tick pTick) 
        throws BadLocationException
    {
        Rectangle rect = super.calculateTickRect(pComponent, pEditor, pTick);
        if (rect != null) {
            rect.x = GAP_H;
            rect.width = pComponent.getWidth() - GAP_H * 3;
        }
        return rect;
    }
    
}
