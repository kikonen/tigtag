package org.kari.tick.gui.syntax;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Logger;
import org.kari.tick.gui.TickDocument;
import org.kari.util.DirectByteArrayOutputStream;
import org.kari.util.TextUtil;

import com.uwyn.jhighlight.pcj.CharIterator;
import com.uwyn.jhighlight.pcj.map.CharKeyOpenHashMap;
import com.uwyn.jhighlight.renderer.Renderer;
import com.uwyn.jhighlight.renderer.XhtmlRendererFactory;
import com.uwyn.jhighlight.tools.FileUtils;
import com.uwyn.jhighlight.tools.StringUtils;

/**
 * Base class of syntax renderers
 *
 * @author kari
 * 
 * @see http://htmlhelp.com/reference/html40/entities/special.html
 * @see http://www.computerhope.com/htmcolor.htm
 */
public abstract class SyntaxRenderer {
    public static final Logger LOG = Logger.getLogger("tick.syntax");
    
    protected static final String[][] SIMPLE_REPLACES = {
        {"<br />", ""},
        {"<h1>", ""},
        {"</h1>", ""},
        {"<code>", ""},
        {"</code>", ""},
    };
    
    protected static final Map<String, Class<?>> mSupportedRenderers =
        new HashMap<String, Class<?>>();
        
    protected final Map<String, String> mEncodingReplaces = new HashMap<String, String>();
    protected final Map<String, MutableAttributeSet> mStyles = new HashMap<String, MutableAttributeSet>();
    
   
    static {
        mSupportedRenderers.put("java", JavaSyntaxRenderer.class);
        mSupportedRenderers.put("cpp", CPPSyntaxRenderer.class);
        mSupportedRenderers.put("xml", XMLSyntaxRenderer.class);
    }

    public static final SyntaxRenderer getRenderer(String pFilename) {
        SyntaxRenderer result = null;
        String ext = getExtension(pFilename);
        Class<?> cls = mSupportedRenderers.get(ext);
        if (cls != null) {
            try {
                result = (SyntaxRenderer)cls.newInstance();
            } catch (Exception e) {
                LOG.error("failed to create", e);
            }
        }
        return result != null
            ? result
            : new PlainTextSyntaxRenderer();
    }


    /**
     * Get filename ext used for mapping syntax renderer. This can be different
     * than actual file name extension of file
     */
    public static String getExtension(String pFilename) {
        String ext = FileUtils.getExtension(pFilename);
        ext = ext.toLowerCase();
        if ("c".equals(ext) || "h".equals(ext) || "hpp".equals(ext)) {
            ext = "cpp";
        } else if ("js".equals(ext) || "bsh".equals(ext)) {
            ext = "java";
        } else if ("html".equals(ext) || "xhtml".equals(ext) ||"htm".equals(ext)) {
            ext = "xml";
        }
        return ext;
    }

    /**
     * @return True if syntax highlight is supported for given file type
     */
    public static boolean isSupported(String pFilename) {
        String ext = getExtension(pFilename);
        return mSupportedRenderers.containsKey(ext);
    }

    
    
    public SyntaxRenderer() {
        MutableAttributeSet attr;

        attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, Color.BLACK);
        mStyles.put("plain", attr);

        attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, Color.BLACK);
        StyleConstants.setBold(attr, true);
        mStyles.put("keyword", attr);

        attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, new Color(0, 33, 255));
        mStyles.put("separator", attr);

        attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, Color.LIGHT_GRAY);
        mStyles.put("comment", attr);

        attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, new Color(0, 124, 31));
        mStyles.put("operator", attr);
        
        attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, new Color(188, 0, 0));
        mStyles.put("literal", attr);

        attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, new Color(0, 44, 221));
        mStyles.put("type", attr);

        
        // http://htmlhelp.com/reference/html40/entities/special.html
        try {
            Field field = StringUtils.class.getDeclaredField("mHtmlEncodeMap");
            field.setAccessible(true);
            CharKeyOpenHashMap map = (CharKeyOpenHashMap)field.get(null);
            CharIterator iter = map.keySet().iterator();
            while (iter.hasNext()) {
                char ch = iter.next();
                String encoded = (String)map.get(ch);
                mEncodingReplaces.put(encoded.substring(1, encoded.length() - 1), "" + ch);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * @return Attributes for pTag, null for plain text
     */
    public MutableAttributeSet getAttr(String pTag) {
        MutableAttributeSet attr = mStyles.get(pTag);
        if (attr == null) {
            int idx = pTag.indexOf('_');
            if (idx != -1) {
                String tag = pTag.substring(idx + 1);
                attr = mStyles.get(tag);
            }
        }
        if (attr == null) {
            LOG.info("Unknown tag: " + pTag);
        }

        return attr;
    }
    
    public void render(
        final JEditorPane pTextPane, 
        final TickDocument pTickDocument) 
    {
        if (false) {
            oldRender(pTextPane, pTickDocument);
        } else {
            newRender(pTextPane, pTickDocument);
        }
    }


    private void newRender(
        final JEditorPane pTextPane,
        final TickDocument pTickDocument)
    {
        try {
            String filename = pTickDocument.getFilename();
            String ext = getExtension(filename);
            String text = pTickDocument.getText();
            pTextPane.setContentType("text/" + ext);
            pTextPane.setText(text);
            

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
    
    public void oldRender(
        final JEditorPane pTextPane, 
        final TickDocument pTickDocument) 
    {
        try {
            String filename = pTickDocument.getFilename();
            String text = pTickDocument.getText();
            
            String renderedText = null;
            Renderer renderer = isSupported(filename)
                ? XhtmlRendererFactory.getRenderer(getExtension(filename))
                : null;
            if (renderer != null) {
                ByteArrayInputStream inputBuffer = new ByteArrayInputStream(text.getBytes());
                DirectByteArrayOutputStream buffer = new DirectByteArrayOutputStream();
                renderer.highlight(null,
                           inputBuffer,
                           buffer,
                           "UTF-8",
                           false);
                renderedText = new String(buffer.toByteArray());
            }
            
            internalRender(pTextPane, pTickDocument, renderedText);

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
    
    /**
     * @param pHTML JHighlight formatted HTML
     */
    protected void internalRender(
        final JEditorPane pTextPane, 
        final TickDocument pTickDocument,
        final String pHTML) 
        throws Exception
    {
        StyledDocument doc = (StyledDocument)pTextPane.getEditorKit().createDefaultDocument();
        
        MutableAttributeSet attr = null;
        
        String decoded = decode(pHTML);
        BufferedReader input = new BufferedReader(new StringReader(decoded));
        
        String line;
        int offset = 0;
        while ((line = input.readLine()) != null) {
            if (line.indexOf("<span class=") == -1) {
                continue;
            }
            
            int prevIdx = 0;
            while (prevIdx < line.length()) {
                int startIdx = line.indexOf("<span class=", prevIdx);
                if (startIdx != -1) {
                    int endIdx = line.indexOf(">", startIdx + 12);
                    {
                        int startParen = line.indexOf('\"', startIdx + 1);
                        int endParen = line.indexOf('\"', startParen + 1);
                        String style = line.substring(startParen + 1, endParen);
                        attr = getAttr(style);
                    }
                    
                    int endTagIdx = line.indexOf("</span>", endIdx);
                    if (endTagIdx - endIdx + 1 > 0) {
                        String str = line.substring(endIdx + 1, endTagIdx);
                        doc.insertString(offset, str, attr);
                        offset = doc.getLength();
                    }
                    
                    prevIdx = endTagIdx + 7;
                } else {
                    doc.insertString(offset, line.substring(prevIdx), null);
                    offset = doc.getLength();
                    prevIdx = line.length();
                }
            }
            doc.insertString(offset, "\n", null);
            offset = doc.getLength();
        }
        pTextPane.setDocument(doc);
    }


    /**
     * Unescape all HTML escapes
     */
    protected String decode(String line) {
        int idx = line.indexOf('&');
        if (idx != -1) {
            line = TextUtil.expand(line, "&", ";", mEncodingReplaces, false);
        }
        for (String[] str : SIMPLE_REPLACES) {
            line = StringUtils.replace(line, str[0], str[1]);
        }
        return line;
    }


}
