package org.kari.tick.gui;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.kari.tick.FileLoader;

/**
 * Maintain shared model of currently open tick documents
 * 
 * @author kari
 *
 */
public final class TickDocumentManager {
    private Map<File, TickDocument> mDocuments = new HashMap<File, TickDocument>();
    
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
    public TickDocument getDocument(File pFile) {
        return mDocuments.get(pFile);
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
            mDocuments.remove(pDocument.getFile());
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
        TickDocument doc = getDocument(pFile);
        int refCount = 0;
        if (doc == null) {
            FileLoader loader = new FileLoader(pFile, pLoadTicks);
            loader.load();
            
            doc = new TickDocument();
            doc.setFileContents(loader);
            
            mDocuments.put(pFile, doc);
        } else {
            refCount = doc.getReferenceCount();
        }
        refCount++;
        doc.setReferenceCount(refCount);
        return doc;
    }

//    /**
//     * Save document associated with pFile as "Tick file". Both ticked file
//     * and ticks are saved into ZIP file
//     * 
//     * <li>NOTE KI ticked file may change on disc, thus original file must
//     * be saved *WITH* ticks
//     */
//    public void saveDocument(File pFile) 
//        throws 
//            IOException
//    {
//        String filename = pFile.getAbsolutePath();
//        
//        TickDocument doc = getDocument(filename);
//        if (doc != null) {
//            try {
//                String text = doc.getText(0, doc.getLength());
////                new FileSaver(pFile).save();
//            } catch (BadLocationException e) {
//                // cannot happen
//            }
//        }
//    }
}
