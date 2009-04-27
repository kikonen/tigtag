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

    private TickDocument mDocument;
    
    public TickTableModel(TickDocument pDocument) {
        mDocument = pDocument;
    }
    
    public TableColumn[] getColumns() {
        return mColumns;
    }

    public TickDocument getDocument() {
        return mDocument;
    }

    public void setDocument(TickDocument pDocument) {
        if (mDocument != null) {
            mDocument.removeTickListener(this);
        }
        mDocument = pDocument;
        if (mDocument != null) {
            mDocument.addTickListener(this);
        }
        fireTableDataChanged();
    }
    
    public Tick getRowElement(int pRow) {
        return mDocument.getTicks().get(pRow);
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
        return mDocument != null
            ? mDocument.getTicks().size()
            : 0;
    }

    @Override
    public Object getValueAt(int pRowIndex, int pColumnIndex) {
        Object result = null;
        Tick tick = mDocument.getTicks().get(pRowIndex);
        TickLocation loc = tick.getLocation();
        switch (pColumnIndex) {
        case IDX_NAME:
            result = tick.getTickDefinition().getName();
            break;
        case IDX_MODE:
            result = loc.mBlockMode.getName();
            break;
        case IDX_LINE:
            if (loc.mStartLine != loc.mEndLine) {
                result = loc.mStartLine + " .. " + loc.mEndLine;
            } else {
                result = Integer.toString(loc.mStartLine);
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
