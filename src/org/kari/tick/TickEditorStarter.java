package org.kari.tick;

import java.io.File;

import javax.swing.SwingUtilities;

import org.kari.tick.gui.TickFrame;

/**
 * Starter for tick editor
 *
 * @author kari
 */
public class TickEditorStarter 
    implements
        Runnable
{
    private String mFilename;

    public TickEditorStarter() {
        // Nothing
    }

    public TickEditorStarter(String pFilename) {
        mFilename = pFilename;
    }
    
    public TickEditorStarter(File pFile) {
        mFilename = pFile.getAbsolutePath();
    }
    
    public void start() {
        SwingUtilities.invokeLater(this);
    }

    @Override
    public void run() {
        TickFrame tickFrame = new TickFrame();
        tickFrame.setVisible(true);
        tickFrame.toFront();
        if (mFilename != null) {
            tickFrame.setFile(mFilename);
        }
    }
}
