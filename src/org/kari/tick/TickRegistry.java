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
    public static final String FILE_EXT = ".properties";
    public static final String DEF_REGISTRY_NAME = "markers";

    private final String mRegistryName;
    private final Map<String, TickDefinition> mDefinitions = new HashMap<String, TickDefinition>();

    /**
     * Default registry
     */
    public TickRegistry() {
        this(DEF_REGISTRY_NAME);
    }
    
    /**
     * @param pRegistryName Marker registry
     */
    public TickRegistry(String pRegistryName) {
        mRegistryName = pRegistryName;
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
        
        File defFile = getRegistryFile();
        if (defFile.exists()) {
            reader = new BufferedReader(new FileReader(defFile));
        } else {
            reader = new BufferedReader(
                new InputStreamReader(
                    TickRegistry.class.getResourceAsStream("/" + DEF_REGISTRY_NAME + FILE_EXT)));
        }

        String line;
        TickDefinition def = null;
        
        while ( (line = reader.readLine()) != null) {
            String str = line.trim();
            if (str.length() == 0 || str.startsWith("#")) {
                // Skip comment
            } else if (str.startsWith("[")) {
                // start new definition
                String name = str.substring(1, str.length() - 1);
                def = new TickDefinition();
                def.setName(name);
                mDefinitions.put(def.getName(), def);
            } else if (def != null) {
                // definition parameters
                int valueSep = str.indexOf('=');
                if (valueSep != -1) {
                    String key = str.substring(0, valueSep).trim();
                    String value = str.substring(valueSep + 1).trim();
                    def.setString(key, value);
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

        String lineSeparator = System.getProperty("line.separator");
        sb.append("#");
        sb.append(lineSeparator);
        sb.append("# FORMAT");
        sb.append(lineSeparator);
        sb.append("# style=Block | Highlight | Sidebar | Underline");
        sb.append(lineSeparator);
        sb.append("# color=RED | GREEN | etc. | ff00ee | 127,127,127");
        sb.append(lineSeparator);
        sb.append("#");
        sb.append(lineSeparator);
        
        for (TickDefinition def : defs) {
            sb.append("[");
            sb.append(def.getName());
            sb.append("]");
            sb.append(lineSeparator);
            
            List<String> keys = new ArrayList<String>(def.getKeys());
            Collections.sort(keys);
            for (String key : keys) {
                String value = def.getString(key, null);
                sb.append(key);
                sb.append("=");
                sb.append(value);
                sb.append(lineSeparator);
            }
            sb.append(lineSeparator);
        }
        return sb.toString();
    }

    /**
     * Save definitions into pFile
     */
    public void saveDefinitions(File pFile)
        throws IOException
    {
        pFile.getParentFile().mkdirs();
        String formatted = formatDefinitions();
        FileUtil.save(pFile, formatted.getBytes());
    }

    /**
     * @return persistent file for tick definitions
     */
    public File getRegistryFile() {
        return new File(TextUtil.expand(FileAccessBase.TICKS_DIR + "/" + mRegistryName + FILE_EXT));
    }
    
    /**
     * Create new set by collecting ticks belonging into it
     * 
     * @return definitions for pGroupName
     */
    public TickSet getSet(String pGroupName) {
        TickSet set = new TickSet(pGroupName);
        for (TickDefinition tick : mDefinitions.values()) {
            if (pGroupName.equals(tick.getString("group", null))) {
                set.addDefinition(tick);
            }
        }
        return set;
    }
    
}
