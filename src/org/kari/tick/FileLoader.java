package org.kari.tick;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.kari.tick.gui.TickConstants;
import org.kari.util.FileUtil;

/**
 * Utility for loading file into editor
 * 
 * @author kari
 *
 */
public class FileLoader extends FileAccessBase {
    private boolean mLoadTicks;

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
     * Load file, assuming UTF-8 encoding
     */
    public void load() 
        throws IOException
    {
        mText = null;
        mTicks.clear();
        
        if (mLoadTicks && isAlreadyTicked()) {
            File tickFile = getTickFile(false);
            ZipFile zip = new ZipFile(tickFile);

            String basename = null;
            
            // Find ticked file name from ZIP
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                int idx = name.indexOf(TickConstants.TICK_ENTRY_EXT);
                if (idx != -1) {
                    basename = name.substring(0, idx); 
                }
            }
            
            // file
            ZipEntry fileEntry = zip.getEntry(basename);
            byte[] data = FileUtil.load(zip.getInputStream(fileEntry));
            mText = new String(data, "UTF-8");

            // registry
            mRegistry = new TickRegistry();
            mRegistry.loadDefinitions();

            // ticks
            ZipEntry tickEntry = zip.getEntry(basename + TickConstants.TICK_ENTRY_EXT);
            mTicks.addAll(loadTicks(zip.getInputStream(tickEntry)));
            
            mBasename = basename;
        } else {
            byte[] data = FileUtil.load(mFile);
            mText = new String(data, "UTF-8");
            mBasename = mFile.getName();
            
            // registry
            mRegistry = new TickRegistry();
            mRegistry.loadDefinitions();
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
                    tick.restore(mRegistry, properties);
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
            tick.restore(mRegistry, properties);
            result.add(tick);
        }

        return result;
    }
    
}
