package org.kari.tick;

import org.kari.base.AppUtil;
import org.kari.tick.gui.TickFrame;

/**
 * Main of TigTag
 * 
 * @author kari
 */
public class TickMain {

    public static void main(String[] pArgs) {
        try {
            System.setProperty("swing.plaf.metal.controlFont", "Dialog");
            TickRegistry.getInstance().loadDefinitions();
            AppUtil.start(TickFrame.class, pArgs);
        } catch (Exception e) {
            System.exit(-1);
        }
    }
}
