package org.kari.tick.gui;

import java.util.Collections;
import java.util.Set;

import javax.swing.ActionMap;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.kari.action.ActionConstants;
import org.kari.action.ActionContext;
import org.kari.action.KAction;
import org.kari.action.KMenu;
import org.kari.properties.Apply;
import org.kari.properties.KPropertiesFrame;
import org.kari.properties.PropertiesViewer;
import org.kari.tick.Tick;

/**
 * Table listing all existing ticks in model
 * 
 * @author kari
 */
public final class TickTable extends JTable 
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
        
        new KAction(ActionConstants.R_PROPERTIES, this) {
            @Override
            public void actionPerformed(ActionContext pCtx) {
                try {
                    final int selectedRow = getSelectedRow();
                    final Tick origTick = getTickTableModel().getRowElement(selectedRow);
                    
                    Apply apply = new Apply() {
                        @Override
                        public void apply(KPropertiesFrame pDialog)
                            throws Exception
                        {
                            Tick tick = (Tick)pDialog.getContent();
                            TickDocument doc = getTickTableModel().getDocument();
                            doc.removeTick(origTick);
                            doc.addTick(tick);
                        }
                    };
                    
                    new PropertiesViewer(origTick, apply).show();
                } catch (Exception e) {
                    TickConstants.LOG.error("Failed to edit tick", e);
                }
            }
        };
    }
    
    public TickTable() {
        super(new TickTableModel(null));
        ActionMap actionMap = getActionMap();
        KMenu menu = new KMenu(
                ActionConstants.R_MENU_CONTEXT,
                actionMap.get(ActionConstants.R_REMOVE),
                actionMap.get(ActionConstants.R_PROPERTIES));
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
        if (selectedRow != -1 && isFocusOwner()) {
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
