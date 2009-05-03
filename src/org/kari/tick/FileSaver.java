package org.kari.tick;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
public class FileSaver extends FileAccessBase {
    public FileSaver(TickDocument pDoc) {
        mText = pDoc.getText();
        mTicks = pDoc.getTicks();
        mFile = new File(pDoc.getFilename());
    }
    
    /**
     * Save ticks into file
     */
    public void save() 
        throws IOException 
    {
        File tickFile = getTickFile();
        tickFile.getParentFile().mkdirs();
        
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
