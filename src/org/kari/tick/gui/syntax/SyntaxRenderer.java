package org.kari.tick.gui.syntax;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Logger;
import org.kari.tick.gui.TickDocument;
import org.kari.util.DirectByteArrayOutputStream;

import sun.awt.motif.MFileDialogPeer;

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
    
    protected static final Map<String, Class<?>> mSupportedRenderers =
        new HashMap<String, Class<?>>();
        
    protected final Map<String, String> mPatternReplaces = new HashMap<String, String>();
    protected final Map<String, MutableAttributeSet> mStyles = new HashMap<String, MutableAttributeSet>();
    
    static {
        mSupportedRenderers.put("java", JavaSyntaxRenderer.class);
        mSupportedRenderers.put("cpp", CPPSyntaxRenderer.class);
//        mSupportedRenderers.put("xml", XMLSyntaxRenderer.class);
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
        } else if ("js".equals(ext)) {
            ext = "java";
        } else if ("html".equals(ext) ||"htm".equals(ext)) {
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
        MutableAttributeSet plainAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(plainAttr, Color.BLACK);
        
        MutableAttributeSet keywordAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(keywordAttr, Color.BLACK);
        StyleConstants.setBold(keywordAttr, true);
        
        MutableAttributeSet separatorAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(separatorAttr, new Color(0,33,255));

        MutableAttributeSet commentAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(commentAttr, Color.LIGHT_GRAY);

        MutableAttributeSet operatorAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(operatorAttr, new Color(0,124,31));

        MutableAttributeSet literalAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(literalAttr, new Color(188,0,0));

        MutableAttributeSet typeAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(typeAttr, new Color(0,44,221));

        mStyles.put("comment",          commentAttr);
        mStyles.put("keyword",          keywordAttr);
        mStyles.put("literal",          literalAttr);
        mStyles.put("operator",         operatorAttr);
        mStyles.put("plain",            plainAttr);
        mStyles.put("separator",        separatorAttr);
        mStyles.put("type",             typeAttr);
        
        // http://htmlhelp.com/reference/html40/entities/special.html
        try {
            Field field = StringUtils.class.getDeclaredField("mHtmlEncodeMap");
            field.setAccessible(true);
            CharKeyOpenHashMap map = (CharKeyOpenHashMap)field.get(null);
            CharIterator iter = map.keySet().iterator();
            while (iter.hasNext()) {
                char ch = iter.next();
                String encoded = (String)map.get(ch);
                mPatternReplaces.put(encoded, "" + ch);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        String[][] PATTERNS = {
            {"<br />", ""},
            {"<h1>", ""},
            {"</h1>", ""},
            {"<code>", ""},
            {"</code>", ""},
        };
        for (String[] str : PATTERNS) {
            mPatternReplaces.put(str[0], str[1]);
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
        final JTextPane pTextPane, 
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
        final JTextPane pTextPane, 
        final TickDocument pTickDocument,
        String pHTML) 
        throws Exception
    {
        StyledDocument doc = pTextPane.getStyledDocument();
        doc.remove(0, doc.getLength());
        MutableAttributeSet attr = null;
        
        BufferedReader input = new BufferedReader(new StringReader(pHTML));
        
        String line;
        int offset = 0;
        while ((line = input.readLine()) != null) {
            if (line.indexOf("<span") == -1) {
                continue;
            }
            line = decode(line);
            
            int prevIdx = 0;
            while (prevIdx < line.length()) {
                int startIdx = line.indexOf("<span", prevIdx); 
                int endIdx = line.indexOf(">", startIdx + 5);
                {
                    int startParen = line.indexOf('\"', startIdx + 1);
                    int endParen = line.indexOf('\"', startParen + 1);
                    String style = line.substring(startParen + 1, endParen);
                    attr = getAttr(style);
                }
                
                int endTagIdx = line.indexOf("</span>", endIdx); 
                String str = line.substring(endIdx + 1, endTagIdx);

                doc.insertString(offset, str, attr);
                offset = doc.getLength();
                
                prevIdx = endTagIdx + 7;
            }
            doc.insertString(offset, "\n", null);
            offset = doc.getLength();
        }
    }


    /**
     * Unescape all HTML escapes
     */
    protected String decode(String line) {
        for (String pattern : mPatternReplaces.keySet()) {
            String str = mPatternReplaces.get(pattern);
//            line = pattern.matcher(line).replaceAll(str);
            line = StringUtils.replace(line, pattern, str);
        }
        return line;
    }


}
