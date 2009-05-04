package org.kari.tick.gui.syntax;

import java.awt.Color;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * C/C++ renderer
 */
public class CPPSyntaxRenderer
    extends GenericSyntaxRenderer
{

    public CPPSyntaxRenderer() {
        MutableAttributeSet attr;

        attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, new Color(147, 147, 147));
        StyleConstants.setItalic(attr, true);
        mStyles.put("doxygen_comment", attr);

        attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, new Color(147, 147, 147));
        StyleConstants.setItalic(attr, true);
        StyleConstants.setBold(attr, true);
        mStyles.put("doxygen_tag", attr);

        attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, new Color(0x8E35EF));
        mStyles.put("preproc", attr);

    }

}
