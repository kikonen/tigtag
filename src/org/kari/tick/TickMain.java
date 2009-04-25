package org.kari.tick;

import org.kari.base.AppUtil;
import org.kari.resources.ResourceAdapter;
import org.kari.tick.gui.TickFrame;
import org.kari.tick.gui.TickResources;

/**
 * Main of TigTag
 * 
 * @author kari
 */
public class TickMain {

    public static void main(String[] pArgs) {
        try {
            System.setProperty("swing.plaf.metal.controlFont", "Dialog");
            ResourceAdapter.getInstance().addBundle(TickResources.class);
            
            TickRegistry.getInstance().loadDefinitions();
            AppUtil.start(TickFrame.class, pArgs);
        } catch (Exception e) {
            System.exit(-1);
        }
    }
}
