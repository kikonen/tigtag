package org.kari.tick.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kari.tick.FileLoader;
import org.kari.tick.Tick;
import org.kari.tick.TickRankComparator;
import org.kari.tick.TickRegistry;

/**
 * Document model for tigtag. Maintains program code, and associated ticks
 * 
 * @author kari
 *
 */
public final class TickDocument {
    public static final TickRankComparator TICK_RANK_COMPARATOR = new TickRankComparator();

    /**
     * File name extension for ticks file
     */
    public static final String EXT_TICKS = ".ticks";
    
    private File mFile;
    private String mText;
    private final List<TickListener> mTickListeners = new ArrayList<TickListener>();
    private final List<Tick> mTicks = new ArrayList<Tick>();
    
    private boolean mModified;
    
    private int mReferenceCount;

    private TickRegistry mRegistry;

    
    /**
     * @param pBlank True if blank new document is created
     */
    public TickDocument(boolean pBlank) {
        super();
        if (pBlank) {
            try {
                mRegistry = new TickRegistry();
                mRegistry.loadDefinitions();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    public int getReferenceCount() {
        return mReferenceCount;
    }

    public void setReferenceCount(int pReferenceCount) {
        mReferenceCount = pReferenceCount;
    }

    /**
     * Add new tick, if duplicate then existing one is overridden
     * 
     * @param pTick or tick into which pTick was merged
     */
    public Tick addTick(Tick pTick) {
        Tick result = pTick;
        List<Tick> merged = new ArrayList<Tick>();
        
        for (Tick tick : mTicks) {
            if (tick.canMerge(pTick)) {
                merged.add(tick);
            }
        }
        
        if (!merged.isEmpty()) {
            mTicks.removeAll(merged);
            result = merged.get(0);
            for (Tick tick : merged) {
                if (tick != result) {
                    result.merge(tick);
                }
            }
            result.merge(pTick);
        }
        
        mTicks.add(result);
        Collections.sort(mTicks, TICK_RANK_COMPARATOR);
        setModified(true);
        fireTickChanged(result, true);
        return result;
    }

    /**
     * Remove new tick
     */
    public void removeTick(Tick pTick) {
        mTicks.remove(pTick);
        setModified(true);
        fireTickChanged(pTick, false);
    }

    public boolean isModified() {
        return mModified;
    }

    public void setModified(boolean pModified) {
        mModified = pModified;
    }

    /**
     * @return Associated original file, null if not set 
     */
    public File getFile() {
        return mFile;
    }

    public void setFile(File pFile) {
        mFile = pFile;
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
     * @return Registry, null if file contents is not set
     */
    public TickRegistry getRegistry() {
        return mRegistry;
    }

    /**
     * Display pText, and render it according to code highlighter
     * 
     * @param pFileLoader Represents file contents
     */
    public void setFileContents(FileLoader pLoader) 
    {
        mFile = pLoader.getFile();
        mText = pLoader.getText();
        mRegistry = pLoader.getRegistry();
        setTicks(pLoader.getTicks());
    }

    /**
     * Ensure ticks are repainting correctly
     */
    public void refresh() {
        for (Tick tick : mTicks) {
            tick.refresh();
        }
    }
    
    /**
     * Clear all ticks
     */
    public void clearTicks() {
        setTicks(Collections.<Tick>emptyList());
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
