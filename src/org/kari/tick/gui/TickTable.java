package org.kari.tick.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Set;

import javax.swing.AbstractCellEditor;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
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

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.SeparatorList;
import ca.odell.glazedlists.gui.AbstractTableComparatorChooser;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

/**
 * Table listing all existing ticks in model
 * 
 * @author kari
 */
public final class TickTable extends JSeparatorTable 
    implements TickHighlighter
{
    {
        new KAction(ActionConstants.R_REMOVE) {
            @Override
            public void actionPerformed(ActionContext pCtx) {
                TickTableModel model = getTickTableModel();
                int selectedRow = getSelectedRow();
                Tick tick = model.getElementAt(selectedRow);
                model.getTickDocument().removeTick(tick);
                if (selectedRow >= getRowCount()) {
                    selectedRow--;
                }
                if (selectedRow >= 0) {
                    setRowSelectionInterval(selectedRow, selectedRow);
                }
            }
        }.bind(this);
        
        new KAction(TickConstants.R_TICK_PROPERTIES) {
            @Override
            public void actionPerformed(ActionContext pCtx) {
                try {
                    final int selectedRow = getSelectedRow();
                    final Tick origTick = getTickTableModel().getElementAt(selectedRow);
                    
                    Apply apply = new Apply() {
                        @Override
                        public void apply(KPropertiesFrame pDialog)
                            throws Exception
                        {
                            Tick tick = (Tick)pDialog.getContent();
                            TickDocument doc = getTickTableModel().getTickDocument();
                            doc.removeTick(origTick);
                            doc.addTick(tick);
                            
                            int row = getTickTableModel().getSortedList().indexOf(tick);
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
    
    private int mHighlightStartLine = -1;
    private int mHighlightEndLine = -1;
    
    
    public TickTable() {
        super(TickTableModel.create());
//        super(TickTableModel.create(), new EventTableColumnModel(new BasicEventList<TableColumn>()));
        SeparatorList<Tick> separatorList = getTickTableModel().getSeparatorList();
        setSeparatorRenderer(new TickSeparatorTableCell(separatorList));
        setSeparatorEditor(new TickSeparatorTableCell(separatorList));
        
        ListSelectionModel sm = new EventSelectionModel<Tick>(separatorList);
        sm.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION_DEFENSIVE);
        setSelectionModel(sm);
        
        ActionMap actionMap = getActionMap();
        KMenu menu = new KMenu(
                ActionConstants.R_MENU_CONTEXT,
                actionMap.get(ActionConstants.R_REMOVE),
                actionMap.get(TickConstants.R_TICK_PROPERTIES));
        menu.createContextMenu(this).start();
        
        setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);
        setRowHeight(20);
        
        TableComparatorChooser.install(
                this, 
                getTickTableModel().getSortedList(),
                AbstractTableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
    }
    
    public EventSelectionModel<Tick> getEventSelectionModel() {
        return (EventSelectionModel<Tick>)getSelectionModel();
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

    /**
     * Get first tick from selection
     * @return Tick, null if none (or separator)
     */
    public Tick getSelectedTick() {
        EventList<Tick> selected = getEventSelectionModel().getSelected();
        Tick tick = null;
        
        if (!selected.isEmpty()) {
            Object first = selected.get(0);
            if (first instanceof Tick) {
                tick = (Tick)first;
            }
        }
        
        return tick;
    }
    
    @Override
    public Highlight getHighlight(Tick pTick) {
        Highlight result = Highlight.NORMAL;
        Tick tick = getSelectedTick();
        if (tick != null && isFocusOwner()) {
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
            tick = getTickTableModel().getElementAt(selectedRow);
        }
        
        return tick != null
            ? Collections.singleton(tick)
            : Collections.<Tick>emptySet();
    }

    
    /**
     * Set highlighted document row range (not table row)
     */
    public void setHighlight(
        int pStartLine, 
        int pEndLine,
        boolean pScrollVisible) 
    {
        TickTableModel model = getTickTableModel();
        boolean changed = setHighlight2(pStartLine, pEndLine);
        if (changed && pScrollVisible) {
            int firstRow = -1;
            int lastRow = -1;
            int row = 0;
            
            EventList<Tick> list = model.getList();
            for (Object elem : list) {
                if (elem instanceof Tick) {
                    Tick tick = (Tick)elem;
                    if (tick.getLocation().intersectLines(pStartLine, pEndLine)) {
                        if (firstRow == -1) {
                            firstRow = row;
                        }
                        lastRow = row;
                    }
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
        }
        
        if (changed) {
            repaint();
        }
    }

    /**
     * Get first highlighted document row (not table row)
     */
    public int getHighlightStartLine() {
        return mHighlightStartLine;
    }

    /**
     * Get last highlighted document row (not table row)
     */
    public int getHighlightEndLine() {
        return mHighlightEndLine;
    }

    /**
     * Set highlighted document row range (not table row)
     * 
     * @return true if changed
     */
    private boolean setHighlight2(int pStartLine, int pEndLine) {
        boolean changed = false;
        if (mHighlightStartLine != pStartLine || mHighlightEndLine != pEndLine) {
            mHighlightStartLine = pStartLine;
            mHighlightEndLine = pEndLine;
            changed = true;
        }
        return changed;
    }
    
    
    /** application appearance */
    public static final Color GLAZED_LISTS_DARK_BROWN = new Color(36, 23, 10);
    public static final Color GLAZED_LISTS_MEDIUM_BROWN = new Color(69, 64, 56);
    public static final Color GLAZED_LISTS_MEDIUM_LIGHT_BROWN = new Color(150, 140, 130);
    public static final Color GLAZED_LISTS_LIGHT_BROWN = new Color(246, 237, 220);
    public static final Color GLAZED_LISTS_LIGHT_BROWN_DARKER = new Color(231, 222, 205);
//    public static final Icon THROBBER_ACTIVE = loadIcon("resources/throbber-active.gif");
//    public static final Icon THROBBER_STATIC = loadIcon("resources/throbber-static.gif");
    public static final Icon EXPANDED_ICON = Icons.triangle(9, SwingConstants.EAST, GLAZED_LISTS_MEDIUM_LIGHT_BROWN);
    public static final Icon COLLAPSED_ICON = Icons.triangle(9, SwingConstants.SOUTH, GLAZED_LISTS_MEDIUM_LIGHT_BROWN);
    public static final Icon X_ICON = Icons.x(10, 5, GLAZED_LISTS_MEDIUM_LIGHT_BROWN);
    public static final Border EMPTY_ONE_PIXEL_BORDER = BorderFactory.createEmptyBorder(1, 1, 1, 1);
    public static final Border EMPTY_TWO_PIXEL_BORDER = BorderFactory.createEmptyBorder(2, 2, 2, 2);
    
    /**
     * Render the issues separator.
     */
    public static class TickSeparatorTableCell
        extends AbstractCellEditor
        implements
            TableCellRenderer,
            TableCellEditor,
            ActionListener
    {
        private final MessageFormat nameFormat = new MessageFormat("{0} ({1})");

        /** the separator list to lock */
        private final SeparatorList separatorList;

        private final JPanel panel = new JPanel(new BorderLayout());
        private final JButton expandButton;
        private final JLabel nameLabel = new JLabel();

        private SeparatorList.Separator<Tick> separator;

        public TickSeparatorTableCell(SeparatorList separatorList) {
            this.separatorList = separatorList;

            this.expandButton = new JButton(EXPANDED_ICON);
            this.expandButton.setOpaque(false);
            this.expandButton.setBorder(EMPTY_TWO_PIXEL_BORDER);
            this.expandButton.setIcon(EXPANDED_ICON);
            this.expandButton.setContentAreaFilled(false);

//            this.nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
            this.nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

            this.expandButton.addActionListener(this);

            this.panel.setBackground(GLAZED_LISTS_LIGHT_BROWN);
            this.panel.add(expandButton, BorderLayout.WEST);
            this.panel.add(nameLabel, BorderLayout.CENTER);
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            configure(value);
            return panel;
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            configure(value);
            return panel;
        }

        public Object getCellEditorValue() {
            return this.separator;
        }

        private void configure(Object value) {
            this.separator = (SeparatorList.Separator<Tick>)value;
            Tick tick = separator.first();
            if (tick == null) {
                // handle 'late' rendering calls after this separator is invalid
                return; 
            }
            expandButton.setIcon(separator.getLimit() == 0 ? EXPANDED_ICON : COLLAPSED_ICON);
            nameLabel.setText(nameFormat.format(new Object[] {
                tick.getDefinition().getName(), 
                new Integer(separator.size())}));
        }

        public void actionPerformed(ActionEvent e) {
            separatorList.getReadWriteLock().writeLock().lock();
            boolean collapsed;
            try {
                collapsed = separator.getLimit() == 0;
                separator.setLimit(collapsed ? Integer.MAX_VALUE : 0);
            } finally {
                separatorList.getReadWriteLock().writeLock().unlock();
            }
            expandButton.setIcon(collapsed ? COLLAPSED_ICON : EXPANDED_ICON);
        }
    }

}



