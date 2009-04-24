package org.kari.tick;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.kari.util.FileUtil;

/**
 * Utility for loading file into editor
 * 
 * @author kari
 *
 */
public class FileLoader {
    private static final Logger LOG = Logger.getLogger("tick.fileloader");

    private File mFile;
    private boolean mLoadTicks;
    private String mText;
    private final List<Tick> mTicks = new ArrayList<Tick>();

    /**
     * @param pFile Original File which is being ticked
     * @param pLoadTicks If true old tick session for file, instead of current
     * contents, is loaded. If 
     */
    public FileLoader(File pFile, boolean pLoadTicks) {
        mFile = pFile;
        mLoadTicks = pLoadTicks;
    }
    
    /**
     * @return true if ".ticks file already exists
     */
    public boolean isAlreadyTicked() {
        return getTickFile().exists();
    }

    /**
     * Get file containing ticks
     * 
     * @return File, may not exist
     */
    public File getTickFile() {
        return new File(mFile.getAbsolutePath() + ".ticks");
    }

    /**
     * Load file, assuming UTF-8 encoding
     */
    public void load() 
        throws IOException
    {
        mText = null;
        mTicks.clear();
        
        if (mLoadTicks && isAlreadyTicked()) {
            File tickFile = getTickFile();
            LOG.warn("NYI! load ticks");
            byte[] data = FileUtil.load(mFile);
            mText = new String(data, "UTF-8");
        } else {
            byte[] data = FileUtil.load(mFile);
            mText = new String(data, "UTF-8");
        }
        
//        Tick tick = new Tick(TickRegistry.getInstance().getDefinition("ELSE"), 20, 30);
//        mTicks.add(tick);
    }

    /**
     * @return Loaded text, null if not yet loaded (or load failed)
     */
    public String getText() {
        return mText;
    }

    /**
     * @return Loaded ticks, empty if none
     */
    public List<Tick> getTicks() {
        return mTicks;
    }
    
}
