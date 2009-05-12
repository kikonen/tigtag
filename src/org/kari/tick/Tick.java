package org.kari.tick;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.text.BadLocationException;

import org.kari.tick.gui.TickTextPane;
import org.kari.tick.gui.TickHighlighter.Highlight;
import org.kari.tick.gui.painter.TickPainter;

/**
 * Tick in the document
 * 
 * @author kari
 */
public final class Tick {
    public static final String P_LOCATION = "location";
    public static final String P_TICK = "tick";
    public static final String P_LINK = "link";
    public static final String P_COMMENT = "comment";
    public static final String P_MODE = "mode";
    public static final String P_COLOR = "color";

    private TickDefinition mDefinition;
    private TickLocation mLocation;
    
    private String mLink;
    private String mComment;
    private boolean mValid;
    private Color mColor;
    private String mText;
    private TickPainter mPainter;
    
    /**
     * For persistency
     */
    public Tick() {
        // Nothing
    }
    
    public Tick(
        TickDefinition pTickDefinition,
        TickLocation pLocation,
        String pText)
    {
        mDefinition = pTickDefinition;
        mLocation = pLocation;
        mColor = mDefinition.getColor();
        setText(pText);
        mValid = true;
    }
    
    @Override
    public boolean equals(Object pObj) {
        boolean result = false;
        if (pObj == this) {
            result = true;
        } else if (pObj instanceof Tick) {
            Tick tick = (Tick)pObj;
            result = mDefinition.equals(tick.mDefinition)
                && mLocation.equals(tick.mLocation);
            
            mLocation.equals(tick.mLocation);
        }
        
        return result;
    }

    @Override
    public int hashCode() {
        return mDefinition.hashCode() ^ mLocation.hashCode();
    }
    
    @Override
    public String toString() {
        return "Tick=" + mDefinition + ",loc=" +mLocation;
    }

    public TickDefinition getDefinition() {
        return mDefinition;
    }
    
    public TickLocation getLocation() {
        return mLocation;
    }
    
    public String getText() {
        return mText;
    }

    public void setText(String pText) {
        mText = pText;
        if (mText != null) {
            mText = mText.trim();
        }
    }

    public String getComment() {
        return mComment;
    }

    public void setComment(String pComment) {
        mComment = pComment;
    }

    public Color getColor() {
        return mColor;
    }

    public void setColor(Color pColor) {
        mColor = pColor;
    }

    /**
     * @return Link identification name, null if none
     */
    public String getLink() {
        return mLink;
    }

    public void setLink(String pLink) {
        mLink = pLink;
    }

    public boolean isValid() {
        return mValid;
    }

    public void setValid(boolean pInvalid) {
        mValid = pInvalid;
    }
    
    /**
     * Save tick in to persistent form
     */
    public Map<String, String> save() {
        Map<String, String> result = new HashMap<String, String>();
        result.put(P_TICK, mDefinition.getName());
        if (mComment != null) {
            result.put(P_COMMENT, mComment);
        }
        if (mLink != null) {
            result.put(P_LINK, mLink);
        }
        result.put(P_LOCATION, mLocation.toString());
//        result.put(P_MODE, mLocation.mBlockMode.getName());
//        result.put(P_COLOR, Integer.toString(mColor.getRGB()));
        return result;
    }

    /**
     * Restore tick from persistent form
     */
    public void restore(TickRegistry pRegistry, Map<String, String> pProperties) {
        mDefinition = pRegistry.getDefinition(pProperties.get(P_TICK));
        if (mDefinition == null) {
            mDefinition = pRegistry.getDefinition(TickDefinition.DEF_NAME);
        }
        mLocation = new TickLocation(pProperties.get(P_LOCATION));
        mLocation.mBlockMode = null;//BlockMode.getMode(pProperties.get(P_MODE));
        if (mLocation.mBlockMode == null) {
            mLocation.mBlockMode = mDefinition.getBlockMode();
        }
        mLink = pProperties.get(P_LINK);
        mComment = pProperties.get(P_COMMENT);
        String colorStr = null;//pProperties.get(P_COLOR);
        if (colorStr != null) {
            mColor = new Color(Integer.parseInt(colorStr));
        } else {
            mColor = mDefinition.getColor();
        }
        mValid = true;
    }


    /**
     * Paint this tick
     */
    public void paint(
            JComponent pComponent,
            TickTextPane pEditor,
            Graphics2D g2d,
            int pYOffset,
            Highlight pHighlight)
        throws BadLocationException
    {
        Composite origComposite = g2d.getComposite();
        Stroke origStroke = g2d.getStroke();
        Color origColor = g2d.getColor();
        
        getPainter().paint(
                pComponent, 
                pEditor, 
                g2d, 
                pYOffset, 
                this, 
                pHighlight);
        g2d.setComposite(origComposite);
        g2d.setStroke(origStroke);
        g2d.setColor(origColor);
    }

    public TickPainter getPainter() {
        if (mPainter == null) {
            mPainter = mLocation.mBlockMode.createPainter();
        }
        return mPainter;
    }

    
    /**
     * @return true if this pTick can be merged into this tick
     */
    public boolean canMerge(Tick pTick) {
        return mDefinition == pTick.mDefinition
            && (mLocation.intersect(pTick.mLocation)
                || mLocation.adjacent(pTick.mLocation));
    }
    
    /**
     * Merge pTick into this tick if possible. Merge requires that ticks
     * either intersect or are adjacent
     */
    public void merge(Tick pTick) {
        TickLocation loc = mLocation.merge(pTick.mLocation);
        mLocation = loc;
        refresh();
    }

    public void refresh() {
        mPainter = null;
    }

}
