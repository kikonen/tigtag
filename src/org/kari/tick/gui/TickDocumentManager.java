package org.kari.tick.gui;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.text.BadLocationException;

import org.kari.tick.FileLoader;

/**
 * Maintain shared model of currently open tick documents
 * 
 * @author kari
 *
 */
public final class TickDocumentManager {
    private Map<String, TickDocument> mDocuments = new HashMap<String, TickDocument>();
    
    private static TickDocumentManager mInstance;
    
    public static TickDocumentManager getInstance() {
        if (mInstance == null) {
            mInstance = new TickDocumentManager();
        }
        return mInstance;
    }

    /**
     * Get document for given file
     * 
     * @return document, null if it wasn't open
     */
    public TickDocument getDocument(String pFilename) {
        return mDocuments.get(pFilename);
    }

    /**
     * Close document, and possibly release model if last client was closed
     * @param pDocument
     */
    public void closeDocument(TickDocument pDocument) {
        int refCount = pDocument.getReferenceCount();
        refCount--;
        pDocument.setReferenceCount(refCount);
        if (refCount <= 0) {
            mDocuments.remove(pDocument.getFilename());
        }
    }
    
    /**
     * Open tick document for given file. Loads file and all associated saved
     * ticks for it. If document was already opened, then that one is returned
     */
    public TickDocument openDocument(File pFile, boolean pLoadTicks) 
        throws 
            IOException
    {
        String filename = pFile.getAbsolutePath();
        
        TickDocument doc = getDocument(filename);
        int refCount = 0;
        if (doc == null) {
            doc = new TickDocument();
            doc.setFilename(filename);
            
            FileLoader loader = new FileLoader(pFile, pLoadTicks);
            loader.load();
            
            try {
                doc.insertString(0, loader.getText(), null);
            } catch (BadLocationException e) {
                // cannot happen
            }
            doc.setTicks(loader.getTicks());
            
            mDocuments.put(filename, doc);
        } else {
            refCount = doc.getReferenceCount();
        }
        refCount++;
        doc.setReferenceCount(refCount);
        return doc;
    }

    /**
     * Save document associated with pFile as "Tick file". Both ticked file
     * and ticks are saved into ZIP file
     * 
     * <li>NOTE KI ticked file may change on disc, thus original file must
     * be saved *WITH* ticks
     */
    public void saveDocument(File pFile) 
        throws 
            IOException
    {
        String filename = pFile.getAbsolutePath();
        
        TickDocument doc = getDocument(filename);
        if (doc != null) {
            try {
                String text = doc.getText(0, doc.getLength());
//                new FileSaver(pFile).save();
            } catch (BadLocationException e) {
                // cannot happen
            }
        }
    }
}
