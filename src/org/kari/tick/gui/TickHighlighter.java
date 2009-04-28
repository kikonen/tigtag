package org.kari.tick.gui;

import java.util.Set;

import org.kari.tick.Tick;

/**
 * API for indicating which ticks are highlighted
 *
 * @author kari
 */
public interface TickHighlighter {
    public enum Highlight {
        DIM,
        NORMAL,
        BRIGHT
    }
    
    /**
     * Get highlight for given tick
     */
    Highlight getHighlight(Tick pTick);
    
    /**
     * Set of ticks which are currently highlighted
     * 
     * @return Highlighted ticks, empty if none
     */
    Set<Tick> getHightlightedTicks();
}
