package org.kari.tick.experiment;

import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

/**
 * An example of drawing a PDF to an image.
 *
 * @author joshua.marinacci@sun.com
 */
public class ImageMain {

    public static void setup() throws IOException {

        //load a pdf from a byte buffer
        File file = new File("test/test.pdf");
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        FileChannel channel = raf.getChannel();
        ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        PDFFile pdffile = new PDFFile(buf);

        // draw the first page to an image
        PDFPage page = pdffile.getPage(0);
        
        //get the width and height for the doc at the default zoom 
        Rectangle rect = new Rectangle(0,0,
                (int)page.getBBox().getWidth(),
                (int)page.getBBox().getHeight());
        
        //generate the image
        Image img = page.getImage(
                rect.width, rect.height, //width & height
                rect, // clip rect
                null, // null for the ImageObserver
                true, // fill background with white
                true  // block until drawing is done
                );
        
        //show the image in a frame
        JFrame frame = new JFrame("PDF Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new JLabel(new ImageIcon(img)));
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    ImageMain.setup();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}