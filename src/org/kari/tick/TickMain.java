package org.kari.tick;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.UIManager;

import jsyntaxpane.DefaultSyntaxKit;

import org.kari.base.AppUtil;
import org.kari.base.Application;
import org.kari.base.CommandLine;
import org.kari.properties.PropertiesRegistry;
import org.kari.resources.ResourceAdapter;
import org.kari.tick.gui.TickConstants;
import org.kari.tick.gui.TickPropertiesFrame;
import org.kari.tick.gui.TickResources;
import org.kari.util.SystemUtil;

/**
 * Main of TigTag
 * 
 * @author kari
 */
public class TickMain 
    implements
        Application
{
    public static final String LF = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
    
    @Override
    public void start(CommandLine pArgs)
        throws Exception
    {
        // Initialize look and feel; if preferred is not available then use default
        try {
            UIManager.setLookAndFeel(LF);
        } catch (Exception e) {
            TickConstants.LOG.error(LF + " not available", e);
            if (SystemUtil.isWindows()) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } else {
                System.setProperty("swing.plaf.metal.controlFont", "Dialog");
            }
        }
        
        DefaultSyntaxKit.initKit();
        
        List<String> filenames = new ArrayList<String>();
        for (String arg : pArgs.getArgs()) {
            filenames.add(arg);
        }
        
        if (!filenames.isEmpty()) {
            for (String filename : filenames) {
                new TickEditorStarter(new File(filename)).start();
            }
        } else {
            new TickEditorStarter().start();
        }
    }

    public static void main(String[] pArgs) {
        try {
            ResourceAdapter.getInstance().addBundle(TickResources.class);
            PropertiesRegistry.getInstance().register(Tick.class, TickPropertiesFrame.class);
            AppUtil.start(TickMain.class, pArgs);
        } catch (Exception e) {
            System.exit(-1);
        }
    }
}
