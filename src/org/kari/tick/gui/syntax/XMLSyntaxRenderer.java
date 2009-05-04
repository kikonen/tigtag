package org.kari.tick.gui.syntax;

import java.awt.Color;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * XML/HTML renderer
 *
 * @author kari
 */
public class XMLSyntaxRenderer extends GenericSyntaxRenderer {

    public XMLSyntaxRenderer() {
        MutableAttributeSet attr;
        
        mStyles.put("char_data", getAttr("plain"));
        
        attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, new Color(0,59,255));
        mStyles.put("tag_symbols", attr);

        attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, new Color(193,0,0));
        mStyles.put("attribute_value", attr);

        attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, new Color(0,0,0));
        StyleConstants.setBold(attr, true);
        mStyles.put("attribute_name", attr);

        attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, new Color(193,0,0));
        StyleConstants.setBold(attr, true);
        StyleConstants.setItalic(attr, true);
        mStyles.put("processing_instruction", attr);

        attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, new Color(0,55,255));
        mStyles.put("tag_name", attr);

        attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, new Color(228,230,160));
        mStyles.put("rife_tag", attr);

        attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, new Color(0,0,196));
        mStyles.put("rife_name", attr);
    }

}
