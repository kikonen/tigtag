package org.kari.tick;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.kari.tick.gui.painter.HighlightPainter;
import org.kari.tick.gui.painter.TickPainter;

/**
 * Definition of the tick
 * 
 * @author kari
 */
public final class TickDefinition {
    /**
     * Name of default definition
     */
    public static final String DEF_NAME = "DEFAULT";
    private static final String STYLE = "style";
    private static final String COLOR = "color";
    private static final Logger LOG = Logger.getLogger("tick.definition");
//    public static final TickPainter DEF_PAINTER = new HighlightPainter();
    
    public static final Comparator<TickDefinition> NAME_COMPARATOR = new Comparator<TickDefinition>() {
        @Override
        public int compare(TickDefinition def1, TickDefinition def2) {
            return def1.getName().compareTo(def2.getName());
        }
    };
    
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
         * Use hightlight pen on area
         */
        HIGHLIGHT("Highlight"),
        /**
         * Show underline for selected text
         */
        UNDERLINE("Underline"),
        /**
         * Lines highlighted in sidebar
         */
        SIDEBAR("Sidebar");
        
        private final String mName;
        private String mPainterClassName;

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
        
        public TickPainter createPainter() {
            TickPainter painter = null;
            if (mPainterClassName == null) {
                mPainterClassName = "org.kari.tick.gui.painter." + mName + "Painter";
            }
            try {
                Class cls = Class.forName(mPainterClassName);
                painter = (TickPainter)cls.newInstance();
            } catch (Exception e) {
                LOG.error("Invalid style: " + mName, e);
                mPainterClassName = HighlightPainter.class.getName();
                painter = new HighlightPainter();
            } 
            return painter;
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
    
    private Color mColor;
    private ImageIcon mIcon;
    
    public TickDefinition() {
        // Nothing
    }
    
    @Override
    public boolean equals(Object pObj) {
        if (pObj == this) {
            return true;
        }
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
     * @return All property keys
     */
    public Collection<String> getKeys() {
        return mProperties.keySet();
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
            mBlockMode = BlockMode.getMode(getString(STYLE, BlockMode.BLOCK.getName()));
        }
        return mBlockMode;
    }
    
    public Color getColor() {
        if (mColor == null) {
            Color color = Color.GREEN;
            String colorName = mProperties.get(COLOR);
            if (colorName != null) {
                colorName = colorName.toUpperCase();
                try {
                    Field field = Color.class.getField(colorName);
                    color = (Color)field.get(null);
                } catch (Exception e) {
                    // not valid color name: try RGB
                    try {
                        String[] split = colorName.split(",");
                        if (split.length == 3) {
                            color = new Color(
                                Integer.parseInt(split[0]),
                                Integer.parseInt(split[1]),
                                Integer.parseInt(split[2]));
                        } else {
                            int rgb = Integer.parseInt(colorName, 16);
                            color = new Color(rgb);
                        }
                    } catch (Exception e2) {
                        LOG.error("Invalid color:" + colorName, e2);
                    }
                }
            }
            mColor = color;
        }
        return mColor;
    }

    /**
     * @return Identity icon
     */
    public ImageIcon getIcon() {
        if (mIcon == null) {
            int SIZE = 16;
            BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics g = image.getGraphics();
            g.setColor(getColor());
            g.fillRoundRect(1, 1, SIZE - 2, SIZE - 2, SIZE, SIZE);
            g.setColor(Color.BLACK);
            g.drawRoundRect(1, 1, SIZE - 2, SIZE - 2, SIZE, SIZE);
            mIcon = new ImageIcon(image);
        }
        return mIcon;
    }

    
}
 
