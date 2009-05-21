package org.kari.tick.gui;

import javax.swing.table.TableColumn;

import org.kari.tick.Tick;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
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
    private final SortedList<Tick> mSortedList;
    private final EventList<Tick> mTickList;
    private final Lock mWriteLock;
    private TickDocument mTickDocument;

    /**
     * Construct new new model
     */
    public static TickTableModel create() {
        BasicEventList<Tick> tickList = new BasicEventList<Tick>();
        SortedList<Tick> sortedList = new SortedList<Tick>(tickList, null);
        return new TickTableModel(sortedList, tickList);

    }
    
    private TickTableModel(
            SortedList<Tick> pSortedList, 
            EventList<Tick> pTickList) 
    {
        super(pSortedList, new TickTableFormat());
        mSortedList = pSortedList;
        mTickList = pTickList;
        mWriteLock = mTickList.getReadWriteLock().writeLock();
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
        mTickList.addAll(pDocument.getTicks());
        mWriteLock.unlock();
    }
    
    public Tick getRowElement(int pRow) {
        return getElementAt(pRow);
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
