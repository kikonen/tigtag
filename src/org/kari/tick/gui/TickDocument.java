package org.kari.tick.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kari.tick.FileLoader;
import org.kari.tick.Tick;
import org.kari.util.DirectByteArrayOutputStream;

/**
 * Document model for tigtag. Maintains program code, and associated ticks
 * 
 * @author kari
 *
 */
public class TickDocument {
    /**
     * File name extension for ticks file
     */
    public static final String EXT_TICKS = ".ticks";
    
    private String mFilename;
    private String mText;
    private String mRenderedText;
    private final List<TickListener> mTickListeners = new ArrayList<TickListener>();
    private final List<Tick> mTicks = new ArrayList<Tick>();
    
    private boolean mModified;
    
    private int mReferenceCount;

    
    public TickDocument() {
        super();
    }
    
    public int getReferenceCount() {
        return mReferenceCount;
    }

    public void setReferenceCount(int pReferenceCount) {
        mReferenceCount = pReferenceCount;
    }

    /**
     * Add new tick, if duplicate then existing one is overridden
     */
    public void addTick(Tick pTick) {
        mTicks.add(pTick);
        fireTickChanged(pTick, true);
        setModified(true);
    }

    /**
     * Remove new tick
     */
    public void removeTick(Tick pTick) {
        mTicks.remove(pTick);
        fireTickChanged(pTick, false);
        setModified(true);
    }

    public boolean isModified() {
        return mModified;
    }

    public void setModified(boolean pModified) {
        mModified = pModified;
    }

    /**
     * @return Associated original filename, null if not set 
     */
    public String getFilename() {
        return mFilename;
    }

    public void setFilename(String pFilename) {
        mFilename = pFilename;
    }
    
    /**
     * @return true if document is currently empty
     */
    public boolean isEmpty() {
        return mText == null;
    }
    
    /**
     * @return Current loaded text, null if none loaded
     */
    public String getText() {
        return mText;
    }
    
    /**
     * @return Currently displayed HTML rendered text, null if none loaded
     */
    public String getRenderedText() {
        return mRenderedText;
    }

    /**
     * Display pText, and render it according to code highlighter
     * 
     * @param pFileLoader Represents file contents
     */
    public void setFileContents(FileLoader pLoader) 
        throws IOException
    {
        String filename = pLoader.getFile().getAbsolutePath();
        String text = pLoader.getText();

        if (false) {
//            ByteArrayInputStream inputBuffer = new ByteArrayInputStream(text.getBytes());
//            DirectByteArrayOutputStream buffer = new DirectByteArrayOutputStream();
//            XhtmlRendererFactory.getRenderer(FileUtils.getExtension(filename))
//                .highlight(filename,
//                       inputBuffer,
//                       buffer,
//                       "UTF-8",
//                       false);
//            mRenderedText = new String(buffer.toByteArray());
        } else {
            mRenderedText = text;
        }
        mFilename = filename;
        mText = text;
        setTicks(pLoader.getTicks());
    }

    /**
     * @return All ticks from the document, empty if none
     */
    public List<Tick> getTicks() {
        return mTicks;
    }

    /**
     * Set ticks, clearing old ticks
     */
    public void setTicks(List<Tick> pTicks) {
        Tick[] oldTicks = mTicks.toArray(new Tick[mTicks.size()]);
        mTicks.clear();
        for (Tick tick : oldTicks) {
            fireTickChanged(tick, false);
        }
        for (Tick tick : pTicks) {
            addTick(tick);
        }
    }
    
    public TickListener[] getTickListeners() {
        return mTickListeners.toArray(new TickListener[mTickListeners.size()]);
    }
    
    public void addTickListener(TickListener pTickListener) {
        mTickListeners.add(pTickListener);
    }
    
    public void removeTickListener(TickListener pTickListener) {
        mTickListeners.remove(pTickListener);
    }
    
    protected void fireTickChanged(Tick pTick, boolean pAdded) {
        for (TickListener listener : getTickListeners()) {
            if (pAdded) {
                listener.tickAdded(this, pTick);
            } else {
                listener.tickRemoved(this, pTick);
            }
        }
    }

}
