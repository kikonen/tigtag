package org.kari.tick;

import java.util.Comparator;

/**
 * Rank ticks into order based into "start line, startpos"
 * 
 * @author kari
 */
public class TickRankComparator implements Comparator<Tick> {
    @Override
    public int compare(Tick tick1, Tick tick2) {
        TickLocation loc1 = tick1.getLocation();
        TickLocation loc2 = tick2.getLocation();
        
        int result = loc1.mStartLine - loc2.mStartLine;
        if (result == 0) {
            result = loc1.mStartPos - loc2.mStartPos;
        }
        if (result == 0) {
            result = loc1.mEndPos - loc2.mEndPos;
        }
        return result;
    }

}
