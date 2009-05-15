package org.kari.tick.gui;

import java.util.ListResourceBundle;

import org.kari.resources.QuickBundle;
import org.kari.resources.ResKey;

/**
 * Tick app specific resources
 * 
 * @see http://ressources-web.fr/Web-Application-Icons-Set/
 */
public class TickResources
    extends ListResourceBundle
    implements
        QuickBundle,
        TickConstants,
        ResKey
{
    private static final Object[][] CONTENTS = {
        { TEXT + R_APP, "TigTag" }, 
        { ICON + R_APP, "/icon/tigtag_32.png" }, 

        { TEXT + R_DUPLICATE, "&Duplicate View" }, 
        { ICON + R_DUPLICATE, "/icon/sun/New16.gif" },
        { T_ICON + R_DUPLICATE, "/icon/sun/New24.gif" },
        { ACC + R_DUPLICATE, "ctrl N" },

        { TEXT + R_GROW_FONT, "&Increase Font" }, 
        { ACC + R_GROW_FONT, "ctrl PLUS" },
        
        { TEXT + R_SHRINK_FONT, "&Decrease Font" }, 
        { ACC + R_SHRINK_FONT, "ctrl MINUS" },

        { TEXT + R_MARKERS_MENU, "Ma&rker" },
        
        { TEXT + R_TICK_PROPERTIES, "&Properties" },
        { ICON + R_TICK_PROPERTIES, "/icon/sun/Properties16.gif" },
        { T_ICON + R_TICK_PROPERTIES, "/icon/sun/Properties24.gif" },
        { ACC + R_TICK_PROPERTIES, "SPACE" },

    };

    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }

}
