package org.kari.tick.gui.painter;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

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
    protected static final AlphaComposite PEN_DIM_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.10f);
    protected static final AlphaComposite NORMAL_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.20f);
    protected static final AlphaComposite BRIGHT_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.40f);
    protected static final int GAP_H = 0;
    protected static final int GAP_V = 0;
    
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
        Color origColor = g2d.getColor();
        g2d.setColor(pTick.getColor());
        
        if (mRects == null) {
            calculateRects(pComponent, pEditor, pTick);
        }
        
        for (Rectangle lineRect : mRects) {
            paintLine(g2d, lineRect, pHighlight);
        }
        
        if (!mRects.isEmpty()) {
            Rectangle rect = mRects.get(0);
            
            String tickName = pTick.getDefinition().getName();
            if (mFont == null) {
                mFont = g2d.getFont().deriveFont(9.0f);
            }
            g2d.setFont(mFont);
            if (mNameLoc == null) {
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D textBounds = mFont.getStringBounds(tickName, 0, tickName.length(), fm.getFontRenderContext());
                int x = rect.x - GAP_H + rect.width + GAP_H * 2 - (int)textBounds.getWidth();
                int y = rect.y - GAP_V + (int)textBounds.getHeight()/2 + pYOffset;
                mNameLoc = new Point(x, y);
            }
            
            g2d.setColor(Color.GRAY);
            g2d.drawString(
                    tickName,
                    mNameLoc.x, 
                    mNameLoc.y);
        }
        
        g2d.setColor(origColor);
    }

    protected void calculateRects(
            final JComponent pComponent,
            final TickTextPane pEditor,
            final Tick pTick)
        throws BadLocationException
    {
        mRects = new ArrayList<Rectangle>();
        
        final Document doc = pEditor.getDocument();
        final TickLocation loc = pTick.getLocation();
        int locStartPos = loc.mStartPos;
        final int len = loc.mEndPos - locStartPos;
        final String txt = doc.getText(locStartPos, len);
        
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
                Rectangle lineRect = new Rectangle(
                    start.x, 
                    start.y, 
                    end.x - start.x, 
                    start.height);
                mRects.add(lineRect);
            }
            startPos = endPos + 1;
        }
    }

    protected void paintLine(
        Graphics2D g2d, 
        Rectangle rect, 
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
                rect.x - GAP_H, 
                rect.y - GAP_V, 
                rect.width + GAP_H * 2, 
                rect.height + GAP_V * 2);
        if (pHighlight != Highlight.DIM) {
            g2d.setComposite(origComposite);
        }
    }

}
