package org.kari.tick;

/**
 * Tick in the document
 * 
 * @author kari
 */
public final class Tick {
    private final int mStartPos;
    private final int mEndPos;
    private final TickDefinition mTickDefinition;
    private String mLinkName;
    private boolean mInvalid;
    
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

    /**
     * @return Link identification name, null if none
     */
    public String getLinkName() {
        return mLinkName;
    }

    public void setLinkName(String pLinkName) {
        mLinkName = pLinkName;
    }

    public boolean isInvalid() {
        return mInvalid;
    }

    public void setInvalid(boolean pInvalid) {
        mInvalid = pInvalid;
    }
    
}
