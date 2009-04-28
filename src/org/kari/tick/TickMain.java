package org.kari.tick;

import java.util.ArrayList;
import java.util.List;

import org.kari.base.AppUtil;
import org.kari.base.Application;
import org.kari.base.CommandLine;
import org.kari.resources.ResourceAdapter;
import org.kari.tick.gui.TickResources;

/**
 * Main of TigTag
 * 
 * @author kari
 */
public class TickMain 
    implements
        Application
{
    @Override
    public void start(CommandLine pArgs)
        throws Exception
    {
        List<String> filenames = new ArrayList<String>();
        for (String arg : pArgs.getArgs()) {
            filenames.add(arg);
        }
        
        if (!filenames.isEmpty()) {
            for (String filename : filenames) {
                new TickEditorStarter(filename).start();
            }
        } else {
            new TickEditorStarter(null).start();
        }
    }

    public static void main(String[] pArgs) {
        try {
            System.setProperty("swing.plaf.metal.controlFont", "Dialog");
            ResourceAdapter.getInstance().addBundle(TickResources.class);
            
            TickRegistry.getInstance().loadDefinitions();
            AppUtil.start(TickMain.class, pArgs);
        } catch (Exception e) {
            System.exit(-1);
        }
    }
}
