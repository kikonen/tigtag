package org.kari.tick.gui;

import java.util.Collections;
import java.util.Set;

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
public class TickTable extends JTable 
    implements TickHighlighter
{
    {
        new KAction(ActionConstants.R_REMOVE, this) {
            @Override
            public void actionPerformed(ActionContext pCtx) {
                int selectedRow = getSelectedRow();
                Tick tick = getTickTableModel().getRowElement(selectedRow);
                getTickTableModel().getDocument().removeTick(tick);
            }
        };
    }
    
    public TickTable() {
        super(new TickTableModel(null));
        KMenu menu = new KMenu(
                ActionConstants.R_MENU_CONTEXT,
                getActionMap().get(ActionConstants.R_REMOVE));
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

    @Override
    public Highlight getHighlight(Tick pTick) {
        Highlight result = Highlight.NORMAL;
        int selectedRow = getSelectedRow();
        Tick tick = null;
        if (selectedRow != -1) {
            tick = getTickTableModel().getRowElement(selectedRow);
            if (tick == pTick) {
                result = Highlight.BRIGHT;
            } else {
                if (!pTick.getDefinition().equals(tick.getDefinition())) {
                    result = Highlight.DIM;
                }
            }
        } else {
            // NORMAL
        }
        return result;
    }
    
    @Override
    public Set<Tick> getHightlightedTicks() {
        int selectedRow = getSelectedRow();
        Tick tick = null;
        if (selectedRow != -1) {
            tick = getTickTableModel().getRowElement(selectedRow);
        }
        
        return tick != null
            ? Collections.singleton(tick)
            : Collections.<Tick>emptySet();
    }
    
}
