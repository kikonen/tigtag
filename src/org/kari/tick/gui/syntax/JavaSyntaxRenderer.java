package org.kari.tick.gui.syntax;

import java.awt.Color;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Renderer of java syntax
 * 
 * @author kari
 */
public class JavaSyntaxRenderer
    extends GenericSyntaxRenderer
{
    public JavaSyntaxRenderer() {
        MutableAttributeSet attr;

        attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, new Color(147, 147, 147));
        StyleConstants.setItalic(attr, true);
        mStyles.put("javadoc_comment", attr);

        attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, new Color(147, 147, 147));
        StyleConstants.setItalic(attr, true);
        StyleConstants.setBold(attr, true);
        mStyles.put("javadoc_tag", attr);
    }
}
