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
    private File mFile;

    public TickEditorStarter() {
        // Nothing
    }

    public TickEditorStarter(File pFile) {
        mFile = pFile;
    }
    
    public void start() {
        SwingUtilities.invokeLater(this);
    }

    @Override
    public void run() {
        TickFrame tickFrame = new TickFrame();
        tickFrame.setVisible(true);
        tickFrame.toFront();
        tickFrame.setFile(mFile);
    }
}
