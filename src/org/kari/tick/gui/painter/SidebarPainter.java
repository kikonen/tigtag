package org.kari.tick.gui.painter;

import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.text.BadLocationException;

import org.kari.tick.Tick;
import org.kari.tick.gui.TickTextPane;

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
