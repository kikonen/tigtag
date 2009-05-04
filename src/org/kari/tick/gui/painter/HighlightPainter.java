package org.kari.tick.gui.painter;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.kari.tick.Tick;
import org.kari.tick.TickLocation;
import org.kari.tick.gui.TickTextPane;
import org.kari.tick.gui.TickHighlighter.Highlight;

/**
 * Code word painter. Paints tick around word (or set of words), if necessary,
 * block is drawn around multiple code lines (outlining only selected text
 * area)
 * 
 * @author kari
 */
public class HighlightPainter extends TickPainter {
    protected final AlphaComposite PEN_DIM_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.10f);
    protected final AlphaComposite NORMAL_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.20f);
    protected final AlphaComposite BRIGHT_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.40f);
    protected final int GAP_H = 0;
    protected final int GAP_V = 0;

    @Override
    public void paint(
        JComponent pComponent,
        TickTextPane pEditor,
        Graphics2D g2d,
        int pYOffset,
        Tick pTick,
        Highlight pHighlight) 
    {
        try {
            final Document doc = pEditor.getDocument();
            final TickLocation loc = pTick.getLocation();
            int locStartPos = loc.mStartPos;
            final int len = loc.mEndPos - locStartPos;
            final String txt = doc.getText(locStartPos, len);

            Color origColor = g2d.getColor();
            g2d.setColor(pTick.getColor());
            
            // TODO KI cache calculated info into Tick; it WONT change
            
            Rectangle rect = null;
            int startPos = 0;
            while (startPos < len) {
                int endPos = startPos;
                boolean found = false;
                while (!found && endPos < len) {
                    found = txt.charAt(endPos) == '\n';
                    if (!found) {
                        endPos++;
                    }
                }
                {
                    Rectangle start = pEditor.modelToView(locStartPos + startPos);
                    Rectangle end = pEditor.modelToView(locStartPos + endPos);
                    paintLine(g2d, start, end, pHighlight);
                    if (rect == null) {
                        rect = start;
                    }
                }
                startPos = endPos + 1;
            }
            
            {
                Font font = g2d.getFont().deriveFont(8.0f);
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();
                String text = pTick.getDefinition().getName();
                g2d.setColor(Color.GRAY);
                Rectangle2D textBounds = font.getStringBounds(text, 0, text.length(), fm.getFontRenderContext());
                int x = rect.x - GAP_H + rect.width + GAP_H * 2 - (int)textBounds.getWidth();
                int y = rect.y - GAP_V + (int)textBounds.getHeight()/2 + pYOffset;
                g2d.drawString(
                        text,
                        x, 
                        y);
            }
            
            g2d.setColor(origColor);
            
            pTick.setInvalid(false);
        } catch (BadLocationException e) {
            LOG.error("Invalid: " + pTick, e);
            pTick.setInvalid(true);
        }
    }

    protected void paintLine(
        Graphics2D g2d, 
        Rectangle start, 
        Rectangle end,
        Highlight pHighlight)
    {
        Composite origComposite = g2d.getComposite();
        if (pHighlight == Highlight.DIM) {
            g2d.setComposite(PEN_DIM_COMPOSITE);
        } else if (pHighlight == Highlight.BRIGHT) {
            g2d.setComposite(BRIGHT_COMPOSITE);
        } else{
            g2d.setComposite(NORMAL_COMPOSITE);
        }
        g2d.fillRect(
            start.x - GAP_H, 
            start.y - GAP_V, 
            end.x - start.x + GAP_H * 2, 
            start.height + GAP_V * 2);
        if (pHighlight != Highlight.DIM) {
            g2d.setComposite(origComposite);
        }
    }

}
