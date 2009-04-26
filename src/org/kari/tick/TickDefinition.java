package org.kari.tick;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.kari.tick.gui.painter.TickPainter;
import org.kari.tick.gui.painter.WordPainter;

/**
 * Definition of the tick
 * 
 * @author kari
 */
public class TickDefinition {
    private static final Logger LOG = Logger.getLogger("tick.definition");
    public static final TickPainter DEF_PAINTER = new WordPainter();
    
    /**
     * Tick block mode
     * 
     * @author kari
     */
    public enum BlockMode {
        /**
         * Block around lines
         */
        BLOCK("Block"),
        /**
         * Block strictly around selected words
         */
        WORD("Word"),
        /**
         * Lines highlighted in sidebar
         */
        SIDEBAR("Sidebar");
        
        private final String mName;
        private TickPainter mPainter;

        private BlockMode(String pName) {
            mName = pName;
        }

        public String getName() {
            return mName;
        }

        /**
         * @return true if mode uses lines (instead of char positions)
         */
        public boolean isLineMode() {
            return this == BLOCK
                || this == SIDEBAR;
        }
        
        public TickPainter getPainter() {
            if (mPainter == null) {
                try {
                    String clsName = "org.kari.tick.gui.painter." + mName + "Painter";
                    Class cls = Class.forName(clsName);
                    mPainter = (TickPainter)cls.newInstance();
                } catch (Exception e) {
                    LOG.error("Invalid style: " + mName, e);
                    mPainter = DEF_PAINTER;
                } 
            }
            return mPainter;
        }

        /**
         * Find block mode matching name
         * 
         * @return block mode, null if not found
         */
        public static BlockMode getMode(String pName) {
            if (pName != null) {
                pName = pName.toLowerCase();
                pName = Character.toUpperCase(pName.charAt(0)) + pName.substring(1);
                for (BlockMode mode : values()) {
                    if (mode.getName().equals(pName)) {
                        return mode;
                    }
                }
            }
            return null;
        }
    }
    
    
    private String mName;
    private final Map<String, String> mProperties = new HashMap<String, String>();
    private BlockMode mBlockMode;
    
    private transient Color mColor;
    
    public TickDefinition() {
        // Nothing
    }
    
    @Override
    public boolean equals(Object pObj) {
        boolean result = false;
        if (pObj instanceof TickDefinition) {
            result = mName.equals( ((TickDefinition)pObj).mName );
        }
        return result;
    }

    @Override
    public int hashCode() {
        return mName.hashCode();
    }
    
    @Override
    public String toString() {
        return mName + ": " + mProperties;
    }

    /**
     * @return Unique name of tick
     */
    public String getName() {
        return mName;
    }

    public void setName(String pName) {
        mName = pName;
    }

    /**
     * @return Property, pDefault if not found
     */
    public String getString(String pKey, String pDefault) {
        String value = mProperties.get(pKey);
        return value != null
            ? value
            : pDefault;
    }
 
    /**
     * @param pValue value, null to remove property
     */
    public void setString(String pKey, String pValue) {
        mProperties.put(pKey, pValue);
        if (pValue == null) {
            mProperties.remove(pKey);
        }
        mBlockMode = null;
    }

    public BlockMode getBlockMode() {
        if (mBlockMode == null) {
            mBlockMode = BlockMode.getMode(getString("mode", "Block"));
        }
        return mBlockMode;
    }
    
    public Color getColor() {
        if (mColor == null) {
            mColor = Color.RED;
        }
        return mColor;
    }
}
 
