package org.kari.tick;

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
    private final String mFilename;

    public TickEditorStarter(String pFilename) {
        mFilename = pFilename;
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
