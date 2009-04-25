package org.kari.tick;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;
import org.kari.tick.gui.TickConstants;
import org.kari.tick.gui.TickDocument;
import org.kari.util.DirectByteArrayOutputStream;
import org.kari.util.FileUtil;

/**
 * Save logic for ticks
 * 
 * @author kari
 *
 */
public class FileSaver {
    private static final Logger LOG = Logger.getLogger("tick.fileloader");
    
    private String mText;
    private List<Tick> mTicks;
    private File mFile;
    
    public FileSaver(TickDocument pDoc) {
        try {
            mText = pDoc.getText(0, pDoc.getLength());
        } catch (BadLocationException e) {
            // cannot happen
        }
        mTicks = pDoc.getTicks();
        mFile = new File(pDoc.getFilename());
    }
    
    /**
     * @return true if ".ticks file already exists
     */
    public boolean isAlreadyTicked() {
        return getTickFile().exists();
    }

    /**
     * Get file containing ticks
     * 
     * @return File, may not exist
     */
    public File getTickFile() {
        return new File(mFile.getAbsolutePath() + TickConstants.TICK_FILE_EXT);
    }

    /**
     * Save ticks into file
     */
    public void save() 
        throws IOException 
    {
        File tickFile = getTickFile();
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(tickFile));
        try {
            String basename = mFile.getName();
            // file
            {
                ZipEntry fileEntry = new ZipEntry(basename);
                zip.putNextEntry(fileEntry);
                zip.write(mText.getBytes());
                zip.closeEntry();
            }        
            // ticks
            {
                ZipEntry tickEntry = new ZipEntry(basename + TickConstants.TICK_ENTRY_EXT);
                zip.putNextEntry(tickEntry);
                zip.write(saveTicks());
                zip.closeEntry();
            }
        } finally {
            FileUtil.close(zip);
        }
    }
    
    /**
     * Save ticks into file
     */
    public byte[] saveTicks() 
        throws
            IOException
    {
        DirectByteArrayOutputStream buffer = new DirectByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer);
        
        for (Tick tick : mTicks) {
            Map<String, String> properties = tick.save();
            String name = properties.get(Tick.P_TICK);
            out.println("[" + name + "]");
            
            List<String> keys = new ArrayList<String>(properties.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                out.println(key + "=" + properties.get(key));
            }
            out.println();
        }
        
        out.close();
        return buffer.toByteArray();
   }

}
