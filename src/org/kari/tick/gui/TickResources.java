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
        { ICON + R_APP, "/icon/tigtag.png" }, 

        { TEXT + R_NEW_VIEW, "&New View" }, 
        { ACC + R_NEW_VIEW, "ctrl N" }, 
    };

    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }

}
