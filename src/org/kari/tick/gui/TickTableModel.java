package org.kari.tick.gui;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.kari.tick.Tick;
import org.kari.tick.TickLocation;

/**
 * Table model listing ticks
 * 
 * @author kari
 */
public class TickTableModel extends AbstractTableModel 
    implements TickListener
{
    public static final int IDX_NAME = 10;
    public static final int IDX_MODE  = 12;
    public static final int IDX_LINE  = 13;
    public static final int IDX_TEXT = 14;
    
    private final TableColumn[] mColumns = {
        new TableColumn(TickTableModel.IDX_NAME),
        new TableColumn(TickTableModel.IDX_MODE),
        new TableColumn(TickTableModel.IDX_LINE),
        new TableColumn(TickTableModel.IDX_TEXT)};

    private TickDocument mTickDocument;
    
    public TickTableModel(TickDocument pDocument) {
        mTickDocument = pDocument;
    }
    
    public TableColumn[] getColumns() {
        return mColumns;
    }

    public TickDocument getDocument() {
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
        fireTableDataChanged();
    }
    
    public Tick getRowElement(int pRow) {
        return mTickDocument.getTicks().get(pRow);
    }

    @Override
    public void tickAdded(TickDocument pDocument, Tick pTick) {
        fireTableDataChanged();
    }

    @Override
    public void tickRemoved(TickDocument pDocument, Tick pTick) {
        fireTableDataChanged();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }
    
    @Override
    public String getColumnName(int pColumn) {
        switch (pColumn) {
            case IDX_NAME:
                return "Name";
            case IDX_MODE:
                return "Style";
            case IDX_LINE:
                return "Line";
            case IDX_TEXT:
                return "Text";
        }
        return null;
   }

    @Override
    public int getRowCount() {
        return mTickDocument != null
            ? mTickDocument.getTicks().size()
            : 0;
    }

    @Override
    public Object getValueAt(int pRowIndex, int pColumnIndex) {
        Object result = null;
        Tick tick = mTickDocument.getTicks().get(pRowIndex);
        TickLocation loc = tick.getLocation();
        switch (pColumnIndex) {
        case IDX_NAME:
            result = tick.getDefinition().getName();
            break;
        case IDX_MODE:
            result = loc.mBlockMode.getName();
            break;
        case IDX_LINE:
            if (loc.mStartLine != loc.mEndLine) {
                result = (loc.mStartLine + 1) + " .. " + (loc.mEndLine + 1);
            } else {
                result = Integer.toString(loc.mStartLine + 1);
            }
            break;
        case IDX_TEXT:
            result = tick.getText();
            break;
        default:
            break;
    }
        return result;
    }

}
