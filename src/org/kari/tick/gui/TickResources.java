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
        
        { TEXT + R_MARKERS_MENU, "Ma&rker" }, 

    };

    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }

}
