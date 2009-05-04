package org.kari.tick;

import org.kari.tick.TickDefinition.BlockMode;


/**
 * Tick location
 * 
 * @author kari
 */
public final class TickLocation {
    /**
     * Inclusive
     */
    public int mStartPos = -1;
    /**
     * Exclusive
     */
    public int mEndPos = -1;
    /**
     * Starts from 0
     */
    public int mStartLine = -1;
    /**
     * Starts from 0
     */
    public int mEndLine = -1;
    public BlockMode mBlockMode;

    public TickLocation() {
        // Nothing
    }
    
    public TickLocation(String pStr) {
        fromString(pStr);
    }

    public TickLocation(
        BlockMode pBlockMode,
        int pStartPos,
        int pEndPos,
        int pStartLine,
        int pEndLine)
    {
        mBlockMode = pBlockMode;
        mStartPos = pStartPos;
        mEndPos = pEndPos;
        mStartLine = pStartLine;
        mEndLine = pEndLine;
    }
    
    @Override
    public boolean equals(Object pObj) {
        boolean result = false;
        if (pObj == this) {
            result = true;
        } else if (pObj instanceof TickLocation) {
            TickLocation loc = (TickLocation)pObj;
            if (mBlockMode == loc.mBlockMode) {
                if (mBlockMode.isLineMode()) {
                    result = mStartLine == loc.mStartLine
                        && mEndLine == loc.mEndLine;
                } else {
                    result = mStartPos == loc.mStartPos
                        && mEndPos == loc.mEndPos;
                }
            }
        }
        
        return result;
    }

    @Override
    public int hashCode() {
        return mBlockMode.isLineMode()
            ? mStartLine ^ mEndLine
            : mStartPos ^ mEndPos;
    }

    /**
     * Save into persistent form
     */
    @Override
    public String toString() {
        return mStartPos 
            + "," 
            + mEndPos 
            + "," 
            + mStartLine 
            + "," 
            + mEndLine;
    }

    /**
     * Restore from persistent form
     */
    public void fromString(String pStr) {
        if (pStr != null) {
            String[] split = pStr.split(",");
            mStartPos = Integer.parseInt(split[0]);
            mEndPos = Integer.parseInt(split[1]);
            mStartLine = Integer.parseInt(split[2]);
            mEndLine = Integer.parseInt(split[3]);
        }
    }
    
    /**
     * @return true if this tick intersects with range of pTick
     */
    public boolean intersect(TickLocation pLoc) {
        TickLocation a = this;
        TickLocation b = pLoc;
        if (mStartPos > pLoc.mStartPos) {
            b = this;
            a = pLoc;
        }
        return a.mEndPos > b.mStartPos;
    }

    /**
     * @return true if this tick intersects with range of pTick
     */
    public boolean adjacent(TickLocation pLoc) {
        TickLocation a = this;
        TickLocation b = pLoc;
        if (mStartPos > pLoc.mStartPos) {
            b = this;
            a = pLoc;
        }
        return a.mEndPos == b.mStartPos;
    }

    /**
     * Merge pTick with this tick and create new location instance.
     */
    public TickLocation merge(TickLocation pLoc) {
        TickLocation loc = new TickLocation();
        loc.mBlockMode = mBlockMode;
        loc.mStartLine = Math.min(mStartLine, pLoc.mStartLine);
        loc.mEndLine = Math.max(mEndLine, pLoc.mEndLine);
        loc.mStartPos = Math.min(mStartPos, pLoc.mStartPos);
        loc.mEndPos = Math.max(mEndPos, pLoc.mEndPos);
        return loc;
    }

}
