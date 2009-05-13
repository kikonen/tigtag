package org.kari.tick.gui;

import java.awt.Rectangle;
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
import org.kari.perspective.ViewUtil;
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
                TickTableModel model = getTickTableModel();
                int selectedRow = getSelectedRow();
                Tick tick = model.getRowElement(selectedRow);
                model.getTickDocument().removeTick(tick);
                if (selectedRow > getRowCount()) {
                    selectedRow--;
                }
                if (selectedRow >= 0) {
                    setRowSelectionInterval(selectedRow, selectedRow);
                }
            }
        }.bind(this);
        
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
                            TickDocument doc = getTickTableModel().getTickDocument();
                            int row = getTickTableModel().getTickDocument().getTicks().indexOf(origTick);
                            doc.removeTick(origTick);
                            doc.addTick(tick);
                            if (row != -1) {
                                setRowSelectionInterval(row, row);
                            }
                        }
                    };
                    
                    new PropertiesViewer(ViewUtil.getFrame(TickTable.this), origTick, apply).show();
                } catch (Exception e) {
                    TickConstants.LOG.error("Failed to edit tick", e);
                }
            }
        }.bind(this);
    }
    
    public TickTable() {
        super(new TickTableModel(null));
        ActionMap actionMap = getActionMap();
        KMenu menu = new KMenu(
                ActionConstants.R_MENU_CONTEXT,
                actionMap.get(ActionConstants.R_REMOVE),
                actionMap.get(ActionConstants.R_PROPERTIES));
        menu.createContextMenu(this).start();
        
        setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);
        setRowHeight(20);
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
            TickTableModel model = getTickTableModel();
            tick = model.getRowElement(selectedRow);
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

    
    /**
     * Set highlighted document row range (not table row)
     */
    public void setHighlight(int pStartLine, int pEndLine) {
        TickTableModel model = getTickTableModel();
        if (model.setHighlight(pStartLine, pEndLine)) {
            int firstRow = -1;
            int lastRow = -1;
            int row = 0;
            for (Tick tick : model.getTickDocument().getTicks()) {
                if (tick.getLocation().intersectLines(pStartLine, pEndLine)) {
                    if (firstRow == -1) {
                        firstRow = row;
                    }
                    lastRow = row;
                }
                row++;
            }
            
            if (firstRow != -1) {
                Rectangle firstRect = getCellRect(firstRow, 0, true);
                Rectangle lastRect = getCellRect(lastRow, 0, true);
                scrollRectToVisible(new Rectangle(
                        0,
                        firstRect.y,
                        0,
                        (lastRect.y + lastRect.height) - firstRect.y));
            }
            repaint();
        }
    }

}
