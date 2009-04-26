package org.kari.tick;

import java.util.HashMap;
import java.util.Map;

import org.kari.tick.TickDefinition.BlockMode;

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

    private TickDefinition mTickDefinition;
    private TickLocation mLocation;
    
    private String mLink;
    private String mComment;
    private boolean mInvalid;

    /**
     * For persistency
     */
    public Tick() {
        // Nothing
    }
    
    public Tick(
        TickDefinition pTickDefinition,
        TickLocation pLocation)
    {
        mTickDefinition = pTickDefinition;
        mLocation = pLocation;
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

    public String getComment() {
        return mComment;
    }

    public void setComment(String pComment) {
        mComment = pComment;
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
    }

}
