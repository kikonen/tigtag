package org.kari.tick;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import org.kari.tick.TickDefinition.BlockMode;
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

    private TickDefinition mTickDefinition;
    private TickLocation mLocation;
    
    private String mLink;
    private String mComment;
    private boolean mInvalid;
    private Color mColor;
    private String mText;
    
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
        mTickDefinition = pTickDefinition;
        mLocation = pLocation;
        mColor = mTickDefinition.getColor();
        setText(pText);
    }
    
    @Override
    public boolean equals(Object pObj) {
        boolean result = false;
        if (pObj == this) {
            result = true;
        } else if (pObj instanceof Tick) {
            Tick tick = (Tick)pObj;
            result = mTickDefinition.equals(tick.mTickDefinition)
                && mLocation.equals(tick.mLocation);
            
            mLocation.equals(tick.mLocation);
        }
        
        return result;
    }

    @Override
    public int hashCode() {
        return mTickDefinition.hashCode() ^ mLocation.hashCode();
    }
    
    @Override
    public String toString() {
        return "Tick=" + mTickDefinition + ",loc=" +mLocation;
    }

    public TickDefinition getTickDefinition() {
        return mTickDefinition;
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

    public boolean isInvalid() {
        return mInvalid;
    }

    public void setInvalid(boolean pInvalid) {
        mInvalid = pInvalid;
    }
    
    /**
     * Save tick in to persistent form
     */
    public Map<String, String> save() {
        Map<String, String> result = new HashMap<String, String>();
        result.put(P_TICK, mTickDefinition.getName());
        if (mComment != null) {
            result.put(P_COMMENT, mComment);
        }
        if (mLink != null) {
            result.put(P_LINK, mLink);
        }
        result.put(P_LOCATION, mLocation.toString());
        result.put(P_MODE, mLocation.mBlockMode.getName());
        result.put(P_COLOR, Integer.toString(mColor.getRGB()));
        return result;
    }

    /**
     * Restore tick from persistent form
     */
    public void restore(Map<String, String> pProperties) {
        mTickDefinition = TickRegistry.getInstance().getDefinition(pProperties.get(P_TICK));
        mLocation = new TickLocation(pProperties.get(P_LOCATION));
        mLocation.mBlockMode = BlockMode.getMode(pProperties.get(P_MODE));
        if (mLocation.mBlockMode == null) {
            mLocation.mBlockMode = mTickDefinition.getBlockMode();
        }
        mLink = pProperties.get(P_LINK);
        mComment = pProperties.get(P_COMMENT);
        String colorStr = pProperties.get(P_COLOR);
        if (colorStr != null) {
            mColor = new Color(Integer.parseInt(colorStr));
        } else {
            mColor = Color.GREEN;
        }
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
    {
        Composite origComposite = g2d.getComposite();
        Stroke origStroke = g2d.getStroke();
        Color origColor = g2d.getColor();
        
        TickPainter painter = mLocation.mBlockMode.getPainter();
        painter.paint(
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
}
