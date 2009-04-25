package org.kari.tick;

import java.util.HashMap;
import java.util.Map;

/**
 * Tick in the document
 * 
 * @author kari
 */
public final class Tick {
    public static final String P_START_POS = "startPos";
    public static final String P_END_POS = "endPos";
    public static final String P_TICK = "tick";
    public static final String P_LINK = "link";
    public static final String P_COMMENT = "comment";
    
    private int mStartPos;
    private int mEndPos;
    private TickDefinition mTickDefinition;
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
        int pStartPos, 
        int pEndPos)
    {
        mTickDefinition = pTickDefinition;
        mStartPos = pStartPos;
        mEndPos = pEndPos;
    }
    
    @Override
    public boolean equals(Object pObj) {
        boolean result = false;
        if (pObj == this) {
            result = true;
        } else if (pObj instanceof Tick) {
            Tick tick = (Tick)pObj;
            // TODO KI equality depends from tick mode
            // WORD = start & end pos
            // BLOCK = start/end line
            result = mTickDefinition.equals(tick.mTickDefinition)
                && mStartPos == tick.mStartPos
                && mEndPos == tick.mEndPos;
        }
        
        return result;
    }

    @Override
    public int hashCode() {
        return mTickDefinition.hashCode() ^ mStartPos ^ mEndPos;
    }
    
    @Override
    public String toString() {
        return "Tick: start=" + mStartPos + ", end=" + mEndPos + "def=" + mTickDefinition;
    }

    public TickDefinition getTickDefinition() {
        return mTickDefinition;
    }

    public int getStartPos() {
        return mStartPos;
    }

    public int getEndPos() {
        return mEndPos;
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
        result.put(P_START_POS, Integer.toString(mStartPos));
        result.put(P_END_POS, Integer.toString(mEndPos));
        return result;
    }

    /**
     * Restore tick from persistent form
     */
    public void restore(Map<String, String> pProperties) {
        mTickDefinition = TickRegistry.getInstance().getDefinition(pProperties.get(P_TICK));
        mStartPos = Integer.parseInt(pProperties.get(P_START_POS));
        mEndPos = Integer.parseInt(pProperties.get(P_END_POS));        
        mLink = pProperties.get(P_LINK);
        mComment = pProperties.get(P_COMMENT);
    }

}
