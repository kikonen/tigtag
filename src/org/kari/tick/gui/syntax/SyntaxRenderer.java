package org.kari.tick.gui.syntax;

import java.awt.Rectangle;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.kari.tick.gui.TickDocument;
import org.kari.util.FileUtil;

/**
 * Base class of syntax renderers
 *
 * @author kari
 * 
 * @see http://htmlhelp.com/reference/html40/entities/special.html
 * @see http://www.computerhope.com/htmcolor.htm
 */
public final class SyntaxRenderer {
    public static final Logger LOG = Logger.getLogger("tick.syntax");
    
    /**
     * Map of (File extension, mimeType)
     */
    protected static final Map<String, String> mMimes = new HashMap<String, String>();

    /**
     * FROM jsyntaxpane.kitsfortypes.properties:
     * 
    text/c=jsyntaxpane.syntaxkits.CSyntaxKit
    text/cpp=jsyntaxpane.syntaxkits.CppSyntaxKit
    text/java=jsyntaxpane.syntaxkits.JavaSyntaxKit
    text/groovy=jsyntaxpane.syntaxkits.GroovySyntaxKit
    text/javascript=jsyntaxpane.syntaxkits.JavaScriptSyntaxKit
    text/xml=jsyntaxpane.syntaxkits.XmlSyntaxKit
    text/sql=jsyntaxpane.syntaxkits.SqlSyntaxKit
    text/properties=jsyntaxpane.syntaxkits.PropertiesSyntaxKit
    text/python=jsyntaxpane.syntaxkits.PythonSyntaxKit
    text/tal=jsyntaxpane.syntaxkits.TALSyntaxKit
    text/jflex=jsyntaxpane.syntaxkits.JFlexSyntaxKit
    text/ruby=jsyntaxpane.syntaxkits.RubySyntaxKit
    text/scala=jsyntaxpane.syntaxkits.ScalaSyntaxKit
    text/clojure=jsyntaxpane.syntaxkits.ClojureSyntaxKit
    text/dosbatch=jsyntaxpane.syntaxkits.DOSBatchSyntaxKit
    text/bash=jsyntaxpane.syntaxkits.BashSyntaxKit
     */
    static {
        mMimes.put("bash", "bash");
        mMimes.put("cmd", "dosbatch");
        mMimes.put("bat", "dosbatch");
        
        mMimes.put("cls", "closure");
        
        mMimes.put("c", "c");
        mMimes.put("h", "c");
        
        mMimes.put("cpp", "cpp");
        mMimes.put("hpp", "cpp");
        mMimes.put("h++", "cpp");
        mMimes.put("c++", "cpp");

        mMimes.put("java", "java");
        mMimes.put("js", "javascript");

        mMimes.put("properties", "properties");
        mMimes.put("py", "python");

        mMimes.put("rb", "ruby");
        mMimes.put("ruby", "ruby");

        mMimes.put("sql", "sql");
        
        mMimes.put("html", "xml");
        mMimes.put("tml", "xml");
        mMimes.put("xhtml", "xml");
        mMimes.put("xml", "xml");
    }
    
    public static final SyntaxRenderer getRenderer(File pFile) {
        return new SyntaxRenderer();
    }
    
    public static String getMimeType(String pFilename) {
        String ext = FileUtil.getExtension(pFilename);
        String mime = mMimes.get(ext);
        return mime != null
            ? "text/" + mime
            : "text/plain";
    }

    public SyntaxRenderer() {
        // Nothing
    }

    public void render(
        final JEditorPane pTextPane, 
        final TickDocument pTickDocument) 
    {
        try {
            String filename = pTickDocument.getFile().getName();
            pTextPane.setContentType(getMimeType(filename));
            pTextPane.setText(pTickDocument.getText());

            pTextPane.setFont(pTextPane.getFont().deriveFont(13.0f));
            
            // Ensure top of the document is focused after load (otherwise
            // caret jumps to end)
            final Rectangle start = pTextPane.modelToView(0);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    pTextPane.scrollRectToVisible(start);
                    pTextPane.setCaretPosition(0);
                    pTextPane.requestFocus();
                }
            });
        } catch (Exception e) {
            LOG.error("render failed", e);
        }
    }
    
}
