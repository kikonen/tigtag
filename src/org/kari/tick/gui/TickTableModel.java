package org.kari.tick.gui;

import javax.swing.table.TableColumn;

import org.kari.tick.Tick;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.util.concurrent.Lock;

/**
 * Table model listing ticks
 * 
 * @author kari
 */
public final class TickTableModel extends EventTableModel<Tick> 
    implements TickListener
{
    private final SeparatorList<Tick> mSeparatorList;
    private final SortedList<Tick> mSortedList;
    private final EventList<Tick> mTickList;
    private final Lock mWriteLock;
    private TickDocument mTickDocument;


    /**
     * Construct new new model
     */
    public static TickTableModel create() {
        EventList<Tick> tickList = new BasicEventList<Tick>();
        SortedList<Tick> sortedList = new SortedList<Tick>(tickList, null);
        SeparatorList<Tick> groupList = new SeparatorList<Tick>(
                sortedList, 
                new TickTableFormat.DefinitionGroupComparator(),
                0,
                Integer.MAX_VALUE);
        
        return new TickTableModel(groupList, sortedList, tickList);
    }
    
    private TickTableModel(
            SeparatorList<Tick> pSeparatorList,
            SortedList<Tick> pSortedList, 
            EventList<Tick> pTickList) 
    {
        super(pSeparatorList, new TickTableFormat());
        mSeparatorList = pSeparatorList;
        mSortedList = pSortedList;
        mTickList = pTickList;
        mWriteLock = mTickList.getReadWriteLock().writeLock();
    }
    
    /**
     * Get main level source list for table model
     */
    public EventList<Tick> getList() {
        return source;
    }

    public SeparatorList<Tick> getSeparatorList() {
        return mSeparatorList;
    }

    public SortedList<Tick> getSortedList() {
        return mSortedList;
    }
    
    public TableColumn[] getColumns() {
        return ((TickTableFormat)getTableFormat()).getColumns();
    }

    public TickDocument getTickDocument() {
        return mTickDocument;
    }

    public void setTickDocument(TickDocument pDocument) {
        if (mTickDocument != null) {
            mTickDocument.removeTickListener(this);
        }
        mTickDocument = pDocument;
        if (mTickDocument != null) {
            mTickDocument.addTickListener(this);
        }
        
        mWriteLock.lock();
        mTickList.clear();
        mTickList.addAll(pDocument.getTicks());
        mWriteLock.unlock();
    }
    
    public void tickAdded(TickDocument pDocument, Tick pTick) {
        int index = pDocument.getTicks().indexOf(pTick);
        mWriteLock.lock();
        mTickList.add(index, pTick);
        mWriteLock.unlock();
    }

    public void tickRemoved(TickDocument pDocument, Tick pTick) {
        mWriteLock.lock();
        mTickList.remove(pTick);
        mWriteLock.unlock();
    }

}
