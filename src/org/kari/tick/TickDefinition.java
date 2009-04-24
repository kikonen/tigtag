package org.kari.tick;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.kari.tick.gui.painter.BlockPainter;
import org.kari.tick.gui.painter.TickPainter;
import org.kari.tick.gui.painter.WordPainter;

/**
 * Definition of the tick
 * 
 * @author kari
 */
public class TickDefinition {
    private static final Logger LOG = Logger.getLogger("tick.definition");
    private static final TickPainter DEF_PAINTER = new WordPainter();
    
    private String mName;
    private final Map<String, String> mProperties = new HashMap<String, String>();
    
    private transient TickPainter mPainter;
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
    }

    
    public TickPainter getPainter() {
        if (mPainter == null) {
            String style = getString("style", "word");
            try {
                style = Character.toUpperCase(style.charAt(0)) + style.substring(1);
                String clsName = "org.kari.tick.gui.painter." + style + "Painter";
                Class cls = Class.forName(clsName);
                mPainter = (TickPainter)cls.newInstance();
                mPainter = new BlockPainter();
            } catch (Exception e) {
                LOG.error("Invalid style: " + style, e);
                mPainter = DEF_PAINTER;
            } 
        }
        return mPainter;
    }
    
    public Color getColor() {
        if (mColor == null) {
            mColor = Color.RED;
        }
        return mColor;
    }
}
 
