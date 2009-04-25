package org.kari.tick.gui;

import org.kari.tick.Tick;

/**
 * Listener for tick changes in {@link TickDocument}
 * 
 * @author kari
 *
 */
public interface TickListener {
    void tickAdded(TickDocument pDocument, Tick pTick);
    void tickRemoved(TickDocument pDocument, Tick pTick);
}
