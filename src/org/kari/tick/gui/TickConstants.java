package org.kari.tick.gui;

import org.apache.log4j.Logger;

public interface TickConstants {
    String S = "tick.";
    
    String VERSION = "0.2";
    Logger LOG = Logger.getLogger("tick");
    
    String R_NEW_VIEW = S + "New";
    
    String TICK_FILE_EXT = ".zip";
    /**
     * file entry inside tick file containing ticks
     */
    String TICK_ENTRY_EXT = ".ticks";

}
