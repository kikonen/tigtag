package org.kari.tick.gui;

import java.util.ListResourceBundle;

import org.kari.resources.QuickBundle;
import org.kari.resources.ResKey;

/**
 * Tick app specific resources
 */
public class TickResources
    extends ListResourceBundle
    implements
        QuickBundle,
        TickConstants,
        ResKey
{
    private static final Object[][] CONTENTS = { 
        { TEXT + R_NEW_VIEW, "&New View" }, 
        { ACC + R_NEW_VIEW, "ctrl N" }, 
    };

    @Override
    public Object[][] getContents() {
        return CONTENTS;
    }

}
