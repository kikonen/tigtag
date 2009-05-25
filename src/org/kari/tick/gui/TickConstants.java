package org.kari.tick.gui;

import org.apache.log4j.Logger;

/**
 * Constants for tigtag
 *
 * @author kari
 */
public interface TickConstants {
    String S = "tick.";
    
    String VERSION = "0.39 Alpha";
    String APP_NAME = "TigTag";
    
    Logger LOG = Logger.getLogger("tick");
    
    String R_DUPLICATE = S + "New";
    String R_APP = S + "App";
    String R_MARKERS_MENU = S + "Markers";
    String R_TICK_PROPERTIES = S + "Properties";

    String R_GROW_FONT = S + "GrowFont";
    String R_SHRINK_FONT = S + "ShrinkFont";

    String TICK_FILE_EXT = ".tigtag";
    
    /**
     * file entry inside tick file containing ticks
     */
    String TICK_ENTRY_EXT = ".ticks";
    
    int FRAME_WIDTH = 1000;
    int FRAME_HEIGHT = 800;
}
