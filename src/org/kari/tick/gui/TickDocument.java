package org.kari.tick.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kari.tick.FileLoader;
import org.kari.tick.Tick;
import org.kari.tick.TickRankComparator;

/**
 * Document model for tigtag. Maintains program code, and associated ticks
 * 
 * @author kari
 *
 */
public class TickDocument {
    private static final TickRankComparator TICK_RANK_COMPARATOR = new TickRankComparator();

    /**
     * File name extension for ticks file
     */
    public static final String EXT_TICKS = ".ticks";
    
    private String mFilename;
    private String mText;
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
     * Display pText, and render it according to code highlighter
     * 
     * @param pFileLoader Represents file contents
     */
    public void setFileContents(FileLoader pLoader) 
    {
        mFilename = pLoader.getFile().getAbsolutePath();
        mText = pLoader.getText();
        setTicks(pLoader.getTicks());
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
