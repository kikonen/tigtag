package org.kari.tick.experiment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;

public class TestGraphics {

    public void render() throws Exception {
        File file = new File("/Users/joshy/splats.pdf");

        // set up the PDF reading
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        FileChannel channel = raf.getChannel();
        ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel
                .size());
        PDFFile pdffile = new PDFFile(buf);

        // get the first page
        PDFPage page = pdffile.getPage(0);

        // create and configure a graphics object
        BufferedImage img = new BufferedImage(100,
            100,
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // do the actual drawing
        PDFRenderer renderer = new PDFRenderer(page, g2, new Rectangle(0,
            0,
            500,
            500), null, Color.RED);
        page.waitForFinish();
        renderer.run();
    }
}
