package org.kari.tick;

import java.util.ArrayList;
import java.util.List;

import org.kari.tick.TickDefinition.BlockMode;


/**
 * Currently active tick set
 * 
 * @author kari
 */
public class TickSet {
    private final String mName;
    private TickDefinition mCurrent;
    private BlockMode mBlockMode;
    private List<TickDefinition> mDefinitions = new ArrayList<TickDefinition>();

    public TickSet(String pName) {
        mName = pName;
    }
    
    /**
     * Get currently active tick
     * 
     * @return definition, null if none
     */
    public TickDefinition getCurrent() {
        return mCurrent;
    }

    public void setCurrent(TickDefinition pCurrent) {
        mCurrent = pCurrent;
    }

    /**
     * Get current active block mode
     */
    public BlockMode getCurrentMode() {
        return mBlockMode != null
            ? mBlockMode
            : mCurrent.getBlockMode();
    }
    
    /**
     * @return Blockmode, if null then default of current will be used
     */
    public BlockMode getBlockMode() {
        return mBlockMode;
    }

    /**
     * @param pBlockMode Blockmode, null to use default
     */
    public void setBlockMode(BlockMode pBlockMode) {
        mBlockMode = pBlockMode;
    }

    public String getName() {
        return mName;
    }

    public List<TickDefinition> getDefinitions() {
        return mDefinitions;
    }

    public void setDefinitions(List<TickDefinition> pDefinitions) {
        mDefinitions.clear();
        mDefinitions.addAll(pDefinitions);
        if (!mDefinitions.contains(mCurrent)) {
            mCurrent = null;
            if (!mDefinitions.isEmpty()) {
                mCurrent = mDefinitions.get(0);
            }
        }
    }

    public void addDefinition(TickDefinition pDefinition) {
        if (mCurrent == null) {
            mCurrent = pDefinition;
        }
        mDefinitions.add(pDefinition);
    }
}
