package org.kari.tick.gui;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.kari.action.ActionConstants;
import org.kari.action.ActionContext;
import org.kari.action.KAction;
import org.kari.action.KMenu;
import org.kari.tick.Tick;

/**
 * Table listing all existing ticks in model
 * 
 * @author kari
 */
public class TickTable extends JTable {
    private Action mRemoveAction = new KAction(ActionConstants.R_REMOVE) {
        @Override
        public void actionPerformed(ActionContext pCtx) {
            int selectedRow = getSelectedRow();
            Tick tick = getTickTableModel().getRowElement(selectedRow);
            getTickTableModel().getDocument().removeTick(tick);
        }
    };

    public TickTable() {
        super(new TickTableModel(null));
        KMenu menu = new KMenu(
                ActionConstants.R_MENU_CONTEXT,
                mRemoveAction);
        menu.createContextMenu(this).start();
    }

    @Override
    public void createDefaultColumnsFromModel() {
        TableModel m = getModel();
        if (m != null) {
            // Remove any current columns
            TableColumnModel cm = getColumnModel();
            while (cm.getColumnCount() > 0) {
                cm.removeColumn(cm.getColumn(0));
            }

            for (TableColumn column : getTickTableModel().getColumns()) {
                addColumn(column);
            }
        }
    }
    
    public TickTableModel getTickTableModel() {
        return (TickTableModel)getModel();
    }
}
