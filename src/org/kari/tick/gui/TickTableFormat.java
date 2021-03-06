package org.kari.tick.gui;

import java.awt.Component;
import java.awt.Font;
import java.util.Comparator;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.kari.tick.Tick;
import org.kari.tick.TickDefinition;
import org.kari.tick.TickLocation;

import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;

public class TickTableFormat 
    implements 
        AdvancedTableFormat<Object>,
        WritableTableFormat<Object>
{
    private static final TickCommentComparator COMMENT_COMPARATOR = new TickCommentComparator();
    
    public static final int IDX_NAME = 0;
    public static final int IDX_MODE  = 12;
    public static final int IDX_LINE  = 13;
    public static final int IDX_TEXT = 14;
    public static final int IDX_COMMENT = 1;

    /**
     * Comparator for grouping ticks by definition
     */
    public static final class DefinitionGroupComparator implements Comparator<Tick> {
        @Override
        public int compare(Tick pO1, Tick pO2) {
            return pO1.getDefinition().getName().compareTo( pO2.getDefinition().getName() );
        }
    }

    /**
     * Comparator for sorting based into comments
     */
    public static final class TickCommentComparator implements Comparator<Tick> {
        @Override
        public int compare(Tick pO1, Tick pO2) {
            int result = pO1.getComment().compareTo( pO2.getComment() );
            return result == 0
                ? TickDocument.TICK_RANK_COMPARATOR.compare(pO1, pO2)
                : result;
        }
    }

    /**
     * Render tick definition name
     *
     * @author kari
     */
    final class NameTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
            JTable pTable,
            Object pValue,
            boolean pIsSelected,
            boolean pHasFocus,
            int pRow,
            int pColumn)
        {
            super.getTableCellRendererComponent(pTable, pValue, pIsSelected,
                    pHasFocus, pRow, pColumn);

            TickTable table = (TickTable)pTable;
            Tick tick = (Tick)pValue;
            TickLocation loc = tick.getLocation();
            TickDefinition def = tick.getDefinition();

            if (loc.intersectLines(
                    table.getHighlightStartLine(), 
                    table.getHighlightEndLine())) 
            {
                setFont(getFont().deriveFont(Font.BOLD));
            }

            String text = loc.mStartLine != loc.mEndLine
                ? (loc.mStartLine + 1) + " .. " + (loc.mEndLine + 1)
                : Integer.toString(loc.mStartLine + 1);
                
            setText(text);
            setBackground(def.getColor());

            return this;
        }
    }

    /**
     * Render tick comment
     *
     * @author kari
     */
    final class CommentTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
            JTable pTable,
            Object pValue,
            boolean pIsSelected,
            boolean pHasFocus,
            int pRow,
            int pColumn)
        {
            super.getTableCellRendererComponent(pTable, pValue, pIsSelected,
                    pHasFocus, pRow, pColumn);
            
            TickTable table = (TickTable)pTable;
            Tick tick = (Tick)pValue;
            TickLocation loc = tick.getLocation();
            TickDefinition def = tick.getDefinition();

            if (loc.intersectLines(
                    table.getHighlightStartLine(), 
                    table.getHighlightEndLine())) 
            {
                setFont(getFont().deriveFont(Font.BOLD));
            }
            
            setText(tick.getComment());
//            setBackground(def.getColor());

            return this;
        }
    }

    
    private final TableColumn[] mColumns = {
//        new TableColumn(TickTableModel.IDX_LINE),
        new TableColumn(TickTableFormat.IDX_NAME),
//        new TableColumn(TickTableModel.IDX_MODE),
        new TableColumn(TickTableFormat.IDX_COMMENT),
        };

    
    public TickTableFormat() {
        TableColumn nameColumn = mColumns[0];
        TableColumn commentColumn = mColumns[1];
        nameColumn.setCellRenderer(new NameTableCellRenderer());
        nameColumn.setMinWidth(60);
        nameColumn.setPreferredWidth(160);
//        nameColumn.setMaxWidth(300);
        
        commentColumn.setPreferredWidth(800);
        commentColumn.setCellRenderer(new CommentTableCellRenderer());
    }

    public TableColumn[] getColumns() {
        return mColumns;
    }

    @Override
    public Class getColumnClass(int pColumn) {
//        for (TableColumn column : mColumns) {
//            if (column.getModelIndex() == pColumn) {
//                return column.getColumnClass();
//            }
//        }
        return Tick.class;
    }

    @Override
    public Comparator getColumnComparator(int pColumn) {
        switch (pColumn) {
        case IDX_NAME:
            return TickDocument.TICK_RANK_COMPARATOR;
        case IDX_COMMENT:
            return COMMENT_COMPARATOR;
        }
        return null;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int pColumn) {
        switch (pColumn) {
            case IDX_NAME:
                return "Line";
            case IDX_MODE:
                return "Style";
            case IDX_LINE:
                return "Line";
            case IDX_TEXT:
                return "Text";
            case IDX_COMMENT:
                return "Comment";
        }
        return null;
    }

    @Override
    public Object getColumnValue(Object baseObject, int pColumnIndex) {
        if (baseObject == null) return null;
        if (baseObject instanceof SeparatorList.Separator) {
            SeparatorList.Separator<Tick> separator = (SeparatorList.Separator<Tick>)baseObject;
            if (pColumnIndex == 1) {
                return separator.first().getDefinition().getName();
            } else {
                return "------";
            }
        }
        
        Object result = null;
        Tick tick = (Tick)baseObject;
        TickLocation loc = tick.getLocation();
        switch (pColumnIndex) {
        case IDX_NAME:
            result = tick;
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
        case IDX_COMMENT:
            result = tick;
            break;
        default:
            break;
        }
        return result;
    }

    @Override
    public boolean isEditable(Object pBaseObject, int pColumn) {
        return pBaseObject instanceof SeparatorList.Separator;
    }

    @Override
    public Object setColumnValue(
        Object pBaseObject,
        Object pEditedValue,
        int pColumn)
    {
        return null;
    }
    
}
