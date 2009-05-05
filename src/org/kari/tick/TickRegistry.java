package org.kari.tick;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kari.util.FileUtil;
import org.kari.util.TextUtil;

/**
 * Registry for managing tick definitions
 * 
 * @author kari
 */
public final class TickRegistry {
    public static final String DEFAULT = "DEFAULT";
    public static final String TICK_DEFINITIONS = "/ticks.properties";

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
            System.out.println("Create: " + defFile + " to redefine markers");
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
     * Render all definitions as text file for editing/saving
     */
    public String formatDefinitions() 
    {
        StringBuilder sb = new StringBuilder();
        List<TickDefinition> defs = new ArrayList<TickDefinition>();
        defs.addAll(mDefinitions.values());
        Collections.sort(defs, TickDefinition.NAME_COMPARATOR);
        
        sb.append("#\n");
        sb.append("# FORMAT\n");
        sb.append("# style=Block | Highlight | Sidebar | Underline\n");
        sb.append("# color=RED | GREEN | etc. | ff00ee | 127,127,127\n");
        sb.append("#\n");
        
        for (TickDefinition def : defs) {
            sb.append("[");
            sb.append(def.getName());
            sb.append("]\n");
            
            List<String> keys = new ArrayList<String>(def.getKeys());
            Collections.sort(keys);
            for (String key : keys) {
                String value = def.getString(key, null);
                sb.append(key);
                sb.append("=");
                sb.append(value);
                sb.append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Save formatted ticks into file
     */
    public void saveDefinitions(String pText)
        throws IOException
    {
        File defFile = new File(TextUtil.expand(FileAccessBase.TICKS_DIR + TICK_DEFINITIONS));
        defFile.getParentFile().mkdirs();
        FileUtil.save(defFile, pText.getBytes());
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
