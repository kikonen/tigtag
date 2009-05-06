package org.kari.tick.gui.painter;

import java.awt.Color;
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
 * Code block painter. Paints tick around rectangle around whole code block
 * identifier by tick start/end locations.
 * 
 * @author kari
 */
public class BlockPainter extends TickPainter {
    public static final int GAP_H = 1;
    public static final int GAP_V = 1;
    
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
        if (mRects == null) {
            calculateRects(pComponent, pEditor, pTick);
        }
        Rectangle rect = mRects.get(0);
        if (rect != null) {
            Color color = pTick.getColor();
            g2d.setColor(color);

            // Paint block
            if (pHighlight == Highlight.DIM) {
                g2d.setComposite(DIM_COMPOSITE);
            } else if (pHighlight == Highlight.BRIGHT) {
                g2d.setStroke(BRIGHT_STROKE);
            }
            g2d.drawRoundRect(
                    rect.x - GAP_H, 
                    rect.y - GAP_V + pYOffset, 
                    rect.width + GAP_H * 2, 
                    rect.height + GAP_V * 2,
                    10,
                    10);

            // Paint tick name
            String tickName = pTick.getDefinition().getName();
            if (mFont == null) {
                mFont = g2d.getFont().deriveFont(9.0f);
            }
            g2d.setFont(mFont);
            if (mNameLoc == null) {
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D textBounds = mFont.getStringBounds(tickName, 0, tickName.length(), fm.getFontRenderContext());
                int x = rect.x - GAP_H + rect.width + GAP_H * 2 - (int)textBounds.getWidth();
                int y = rect.y - GAP_V + (int)textBounds.getHeight() + pYOffset;
                mNameLoc = new Point(x, y);
            }            
            g2d.drawString(
                    tickName,
                    mNameLoc.x, 
                    mNameLoc.y);
        }
    }

    protected void calculateRects(
            final JComponent pComponent,
            final TickTextPane pEditor,
            final Tick pTick)
        throws BadLocationException
    {
        mRects = new ArrayList<Rectangle>();
        Rectangle rect = calculateTickRect(pComponent, pEditor, pTick);
        mRects.add(rect);
    }
    
    @Override
    protected Rectangle calculateTickRect(
            JComponent pComponent,
            TickTextPane pEditor, 
            Tick pTick) 
        throws BadLocationException
    {
        Rectangle rect = null;
        final Document doc = pEditor.getDocument();
        final TickLocation loc = pTick.getLocation();

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = -1;
        int maxX = -1;

        int pos = loc.mStartPos;
        while (pos < loc.mEndPos) {
            int startPos = pEditor.findLineStart(pos);
            int endPos = pEditor.findLineEnd(pos);
            pos = endPos + 1;
            
            String line = doc.getText(startPos, endPos - startPos);
            int lineLen = line.length();
            if (line.endsWith("\n")) {
                lineLen--;
                endPos--;
            }
            int startOffset = 0;
            int endOffset = 0;
            while (startOffset < lineLen && Character.isWhitespace(line.charAt(startOffset))) {
                startOffset++;
            }
            while (endOffset < lineLen - startOffset && Character.isWhitespace(line.charAt(lineLen - endOffset - 1))) {
                endOffset++;
            }
            startPos += startOffset;
            endPos -= endOffset;
            
            {
                Rectangle start = pEditor.modelToView(startPos);
                Rectangle end = pEditor.modelToView(endPos);
                if (start.x < minX) {
                    minX = start.x;
                }
                if (start.y < minY) {
                    minY = start.y;
                }
                if (end.x > maxX) {
                    maxX = end.x;
                }
                if (end.y + end.height > maxY) {
                    maxY = end.y + end.height;
                }
            }
        }
        rect = new Rectangle(minX, minY, maxX - minX, maxY - minY);
        return rect;
    }

}
