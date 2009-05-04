package org.kari.tick;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.kari.tick.gui.TickConstants;
import org.kari.util.TextUtil;

/**
 * Base for tick file access
 * 
 * @author kari
 */
public abstract class FileAccessBase {
    protected static final Logger LOG = Logger.getLogger("tick.file");
    protected static final String TICKS_DIR = "${user.home}/tigtag";
    
    protected String mText;
    protected List<Tick> mTicks = new ArrayList<Tick>();
    protected File mFile;
    protected String mBasename;
    
    public FileAccessBase() {
        super();
    }

    /**
     * @return Original file loaded/saved
     */
    public File getFile() {
        return mFile;
    }

    /**
     * @return Identification name for ticked file
     */
    public String getBasename() {
        return mBasename;
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
        File result;
        if (mFile.getName().endsWith(".zip")) {
            result = mFile;
        } else {
            String filename = TICKS_DIR + "/" + mFile.getName() + TickConstants.TICK_FILE_EXT;
            filename = TextUtil.expand(filename);
            result = new File(filename);
        }
        return result;
    }


}
