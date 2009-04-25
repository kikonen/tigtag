package org.kari.tick;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
import org.kari.tick.gui.TickConstants;
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
        return new File(mFile.getAbsolutePath() + TickConstants.TICK_FILE_EXT);
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
            ZipFile zip = new ZipFile(tickFile);
            
            String basename = mFile.getName();
            // file
            ZipEntry fileEntry = zip.getEntry(basename);
            byte[] data = FileUtil.load(zip.getInputStream(fileEntry));
            mText = new String(data, "UTF-8");
            
            // ticks
            ZipEntry tickEntry = zip.getEntry(basename + TickConstants.TICK_ENTRY_EXT);
            mTicks.addAll(loadTicks(zip.getInputStream(tickEntry)));
        } else {
            byte[] data = FileUtil.load(mFile);
            mText = new String(data, "UTF-8");
        }
    }
    
    /**
     * Load ticks from file
     */
    private List<Tick> loadTicks(InputStream pInput) 
        throws
            IOException
    {
        List<Tick> result = new ArrayList<Tick>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(pInput));

        String line;
        final Map<String, String> properties = new HashMap<String, String>();
        
        while ( (line = reader.readLine()) != null) {
            String str = line.trim();
            if (str.length() == 0 || str.startsWith("#")) {
                // Skip comment
            } else if (str.startsWith("[")) {
                // start new tick
                if (!properties.isEmpty()) {
                    Tick tick = new Tick();
                    tick.restore(properties);
                    result.add(tick);
                    properties.clear();
                }
                String name = str.substring(1, str.length() - 1);
                properties.put(Tick.P_TICK, name);
            } else if (properties != null) {
                // tick parameters
                int valueSep = str.indexOf('=');
                if (valueSep != -1) {
                    String key = str.substring(0, valueSep).trim();
                    String value = str.substring(valueSep + 1).trim();
                    properties.put(key, value);
                }
            }
        }
        
        if (!properties.isEmpty()) {
            Tick tick = new Tick();
            tick.restore(properties);
            result.add(tick);
        }

        return result;
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
