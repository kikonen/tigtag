package org.kari.tick;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.kari.tick.gui.TickConstants;
import org.kari.util.TextUtil;

/**
 * Registry for managing tick definitions
 * 
 * @author kari
 */
public final class TickRegistry {
    private static final String TICK_DEFINITIONS = "/ticks.properties";

    private static TickRegistry mInstance;
    
    private final Map<String, TickDefinition> mDefinitions = new HashMap<String, TickDefinition>();

    public static synchronized TickRegistry getInstance() {
        if (mInstance == null) {
            mInstance = new TickRegistry();
        }
        return mInstance;
    }
    
    private TickRegistry() {
        // Nothing
    }

    public synchronized Collection<TickDefinition> getDefinitions() {
        return mDefinitions.values();
    }
    
    public synchronized TickDefinition getDefinition(String pName) {
        return mDefinitions.get(pName);
    }
    
    /**
     * Load/reload tick definitions from the disc
     */
    public void loadDefinitions() 
        throws
            IOException
    {
        mDefinitions.clear();
        
        BufferedReader reader;
        
        File defFile = new File(TextUtil.expand(FileAccessBase.TICKS_DIR + TICK_DEFINITIONS));
        if (defFile.exists()) {
            reader = new BufferedReader(new FileReader(defFile));
        } else {
            reader = new BufferedReader(
                    new InputStreamReader(TickRegistry.class.getResourceAsStream(TICK_DEFINITIONS)));
        }

        String line;
        TickDefinition tick = null;
        
        while ( (line = reader.readLine()) != null) {
            String str = line.trim();
            if (str.length() == 0 || str.startsWith("#")) {
                // Skip comment
            } else if (str.startsWith("[")) {
                // start new tick
                String name = str.substring(1, str.length() - 1);
                tick = new TickDefinition();
                tick.setName(name);
                mDefinitions.put(tick.getName(), tick);
            } else if (tick != null) {
                // tick parameters
                int valueSep = str.indexOf('=');
                if (valueSep != -1) {
                    String key = str.substring(0, valueSep).trim();
                    String value = str.substring(valueSep + 1).trim();
                    tick.setString(key, value);
                }
            }
        }
    }
    
    /**
     * Create new set by collecting ticks belonging into it
     * 
     * @return new tick set, which can be empty
     */
    public TickSet createSet(String pName) {
        TickSet set = new TickSet(pName);
        for (TickDefinition tick : mDefinitions.values()) {
            if (pName.equals(tick.getString("group", null))) {
                set.addDefinition(tick);
            }
        }
        return set;
    }
    
}
