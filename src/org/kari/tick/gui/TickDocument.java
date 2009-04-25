package org.kari.tick.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.text.DefaultStyledDocument;

import org.kari.tick.Tick;

/**
 * Document model for tigtag. Maintains program code, and associated ticks
 * 
 * @author kari
 *
 */
public class TickDocument extends DefaultStyledDocument {
    /**
     * File name extension for ticks file
     */
    public static final String EXT_TICKS = ".ticks";
    
    private String mFilename;
    /**
     * Map of (tickdef, Set of (ticks-for-def))
     */
    private final Map<String, Set<Tick>> mTicks = new HashMap<String, Set<Tick>>();
    private final List<TickListener> mTickListeners = new ArrayList<TickListener>();
    
    private boolean mModified;

    
    public TickDocument() {
        super();
    }

    /**
     * Get ticks defined for given tick name
     * 
     * @return Ticks, empty if none
     */
    public Set<Tick> getTicks(String pTickName) {
        Set<Tick> ticks = mTicks.get(pTickName);
        if (ticks == null) {
            ticks = Collections.EMPTY_SET;
        }
        return ticks;
    }

    /**
     * @return Set of existing ticks, empty if none
     */
    public Set<String> getTickNames() {
        return mTicks.keySet();
    }

    /**
     * Add new tick, if duplicate then existing one is overridden
     */
    public void addTick(Tick pTick) {
        String tickName = pTick.getTickDefinition().getName();
        Set<Tick> ticks = mTicks.get(tickName);
        if (ticks == null) {
            ticks = new HashSet<Tick>();
            mTicks.put(tickName, ticks);
        }
        ticks.add(pTick);
        fireTickChanged(pTick, true);
        setModified(true);
    }

    /**
     * Remove new tick
     */
    public void removeTick(Tick pTick) {
        String tickName = pTick.getTickDefinition().getName();
        Set<Tick> ticks = mTicks.get(tickName);
        if (ticks != null) {
            ticks.remove(pTick);
            fireTickChanged(pTick, false);
            setModified(true);
        }
    }

    public boolean isModified() {
        return mModified;
    }

    public void setModified(boolean pModified) {
        mModified = pModified;
    }

    public String getFilename() {
        return mFilename;
    }

    public void setFilename(String pFilename) {
        mFilename = pFilename;
    }

    /**
     * @return All ticks from the document, empty if none
     */
    public List<Tick> getTicks() {
        List<Tick> ticks = new ArrayList<Tick>();
        for (String defName : mTicks.keySet()) {
            ticks.addAll(mTicks.get(defName));
        }
        return ticks;
    }

    /**
     * Set ticks, clearing old ticks
     */
    public void setTicks(List<Tick> pTicks) {
        List<Tick> oldTicks = getTicks();
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
