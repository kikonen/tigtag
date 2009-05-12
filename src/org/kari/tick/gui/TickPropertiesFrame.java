package org.kari.tick.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.kari.layout.KGrid;
import org.kari.properties.KPropertiesFrame;
import org.kari.tick.Tick;
import org.kari.tick.TickDefinition;

/**
 * Properties for tick
 *
 * @author kari
 */
public class TickPropertiesFrame extends KPropertiesFrame {
    private final JLabel mNameField = new JLabel();
    private final JTextField mCommentField = new JTextField();

    public TickPropertiesFrame() {
        setTitle("Tick Properties");
        
        JPanel generalPage = new JPanel();
        KGrid grid = new KGrid(generalPage);
        
        int y = 0;
        grid.label(0, y, "Name:");
        grid.fillX(1, y++, mNameField);

        grid.label(0, y, "Comment:");
        grid.fillX(1, y++, mCommentField);
        
        grid.gridwidth = 2;
        grid.fillXY(0, y);

        addPage("General", generalPage);

        setFocusedComponent(mCommentField);
    }

    @Override
    public Object getContent()
        throws Exception
    {
        Tick tick = (Tick)super.getContent();
        tick.setComment(mCommentField.getText());
        return tick;
    }

    @Override
    public void setContent(Object pContent)
        throws Exception
    {
        super.setContent(pContent);
        Tick tick = (Tick)pContent;
        
        TickDefinition def = tick.getDefinition();
        mNameField.setText(def.getName());
        mNameField.setIcon(def.getIcon());
        
        mCommentField.setText(tick.getComment());
    }
    
}
