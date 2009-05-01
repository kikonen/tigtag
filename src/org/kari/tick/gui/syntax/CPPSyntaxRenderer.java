package org.kari.tick.gui.syntax;

import java.awt.Color;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * C/C++ renderer
 */
public class CPPSyntaxRenderer extends GenericSyntaxRenderer {

    public CPPSyntaxRenderer() {
        MutableAttributeSet doxygenCommentAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(doxygenCommentAttr, new Color(147,147,147));
        StyleConstants.setItalic(doxygenCommentAttr, true);
        
        MutableAttributeSet doxygenTagAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(doxygenTagAttr, new Color(147,147,147));
        StyleConstants.setItalic(doxygenTagAttr, true);
        StyleConstants.setBold(doxygenTagAttr, true);

        MutableAttributeSet preprocAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(preprocAttr, new Color(0x8E35EF));

        mStyles.put("doxygen_tag",      doxygenTagAttr);
        mStyles.put("doxygen_comment",  doxygenCommentAttr);
        
        mStyles.put("preproc", preprocAttr);
    }

}
