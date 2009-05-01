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
public class JavaSyntaxRenderer extends GenericSyntaxRenderer {
    public JavaSyntaxRenderer() {
        MutableAttributeSet javadocCommentAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(javadocCommentAttr, new Color(147,147,147));
        StyleConstants.setItalic(javadocCommentAttr, true);
        
        MutableAttributeSet javadocTagAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(javadocTagAttr, new Color(147,147,147));
        StyleConstants.setItalic(javadocTagAttr, true);
        StyleConstants.setBold(javadocTagAttr, true);

        mStyles.put("javadoc_tag",      javadocTagAttr);
        mStyles.put("javadoc_comment",  javadocCommentAttr);
    }
}
