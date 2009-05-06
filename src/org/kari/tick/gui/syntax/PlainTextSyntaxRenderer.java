package org.kari.tick.gui.syntax;

import javax.swing.JEditorPane;

import org.kari.tick.gui.TickDocument;

/**
 * Syntax renderer for plain text (or unrecognized syntax)
 * 
 * @author kari
 */
public class PlainTextSyntaxRenderer extends SyntaxRenderer {

    /**
     * @param pHTML null if JHighlight didn't support doc type
     */
    @Override
    protected void internalRender(
        JEditorPane pTextPane, 
        TickDocument pTickDocument,
        String pHTML) 
    {
        if (pHTML != null) {
            LOG.info("Ignored: " + pHTML);
        }
        pTextPane.setContentType("text/plain");
        pTextPane.setText(pTickDocument.getText());
    }

}
