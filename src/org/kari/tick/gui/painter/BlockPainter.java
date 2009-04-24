package org.kari.tick.gui.painter;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.kari.tick.Tick;
import org.kari.tick.gui.TickTextPane;

/**
 * Code block painter. Paints tick around rectangle around whole code block
 * identifier by tick start/end locations.
 * 
 * @author kari
 */
public class BlockPainter extends TickPainter {
    public static final int GAP_H = 4;
    public static final int GAP_V = 2;

    @Override
    public void paint(
        TickTextPane pEditor,
        Graphics g2d, 
        Tick pTick) 
    {
        Rectangle rect = calculateTickRect(pEditor, pTick);
        if (rect != null) {
            String text = pTick.getTickDefinition().getName();
            Color color = pTick.getTickDefinition().getColor();
            g2d.setColor(color);
            g2d.drawRoundRect(
                    rect.x - GAP_H, 
                    rect.y - GAP_V, 
                    rect.width + GAP_H * 2, 
                    rect.height + GAP_V * 2,
                    10,
                    10);
            
            Font font = g2d.getFont().deriveFont(9.0f);
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics();
            Rectangle2D textBounds = font.getStringBounds(text, 0, text.length(), fm.getFontRenderContext());
            int x = rect.x - GAP_H + rect.width + GAP_H * 2 - (int)textBounds.getWidth();
            int y = rect.y - GAP_V + (int)textBounds    .getHeight();
            g2d.drawString(
                    text,
                    x, 
                    y);
        }
    }

    @Override
    protected Rectangle calculateTickRect(TickTextPane pEditor, Tick pTick) {
        Rectangle rect = null;
        try {
            Document doc = pEditor.getDocument();
            int docLen = doc.getLength();
            int startPos = pTick.getStartPos();
            int endPos = pTick.getEndPos();
            
            boolean valid = startPos >= 0
                && startPos <= docLen
                && endPos >= 0
                && endPos <= docLen
                && startPos <= endPos;
                
            if (valid) {
                Rectangle start = pEditor.modelToView(startPos);
                Rectangle end = pEditor.modelToView(endPos);
                int width = pEditor.getWidth() - GAP_H * 3;
                int height = end.y - start.y + end.height;
                int x = GAP_H;
                int y = start.y;
                
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
