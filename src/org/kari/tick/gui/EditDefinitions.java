package org.kari.tick.gui;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.kari.tick.TickRegistry;
import org.kari.util.SystemUtil;

/**
 * Edit definitions
 * 
 * @author kari
 */
public class EditDefinitions {
    static final Logger LOG = TickConstants.LOG;
    
    private final TickFrame mFrame;
    private boolean mRunning;
    private File mFile;
    private long mModified;
    
    public EditDefinitions(TickFrame pFrame) {
        mFrame = pFrame;
    }

    public void edit() 
        throws Exception 
    {
        TickRegistry registry = mFrame.getEditor().getTextPane().getTickDocument().getRegistry();
        mFile = registry.getRegistryFile();
        registry.saveDefinitions(mFile);
        mModified = mFile.lastModified();;
        
        mRunning = true;
        launchMonitor();
        launchProcess();
    }

    private void launchProcess() {
        new Thread("Editor") {
            @Override
            public void run() {
                try {
                    final String[] cmd = {
                        SystemUtil.isLinux() ? "kwrite" : "notepad",
                        mFile.getAbsolutePath()};
                    Process process = Runtime.getRuntime().exec(cmd);
                    process.waitFor();
                    reloadIfNeeded();
                } catch (Exception e) {
                    LOG.error("Failed to edot", e);
                } finally {
                    mRunning = false;
                }
            }}.start();
    }

    private void launchMonitor() {
        new Thread("FileMonitor") {
            @Override
            public void run() {
                try {
                    while (mRunning) {
                        reloadIfNeeded();
                        Thread.sleep(250);
                    }
                } catch (Exception e) {
                    LOG.error("Failed to edot", e);
                }
            }}.start();
    }

    private synchronized void reloadIfNeeded() {
        long modified = mFile.lastModified();
        if (modified != mModified) {
            reload();
            mModified = modified;
        }
    }
    
    private void reload() {
        try {
            TickDocument doc = mFrame.getEditor().getTextPane().getTickDocument();
            TickRegistry registry = doc.getRegistry();
            registry.loadDefinitions();
            mFrame.createMarkerMenu();
        } catch (IOException e) {
            LOG.error("Failed to edit", e);
        }
    }

}
