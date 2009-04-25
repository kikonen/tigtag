package org.kari.tick.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.kari.action.ActionConstants;
import org.kari.action.ActionContainer;
import org.kari.action.ActionContext;
import org.kari.action.KAction;
import org.kari.action.KMenu;
import org.kari.action.KToolbar;
import org.kari.action.std.ExitAction;
import org.kari.perspective.KApplicationFrame;

/**
 * TigTag window
 * 
 * @author kari
 */
public class TickFrame extends KApplicationFrame {
    public static final String APP_NAME = "TigTag";

    private TickEditorPanel mEditor;

    private final Action mNewViewAction = new KAction(TickConstants.R_NEW_VIEW) {
        @Override
        public void actionPerformed(ActionContext pCtx) {
            new TickFrame().setVisible(true);
        }
    };
    
    private final Action mAboutAction = new KAction("&About") {
        @Override
        public void actionPerformed(ActionContext pCtx) {
            String msg = "<html>"
                + "All copyrights owned by Kari Ikonen<br>"
                + "(except for the  concept of the 'ticks')<br>"
                + "<br>"
                + "Contact: mr.kari.ikonen@gmail.com"
                + "</html>";
            JOptionPane.showMessageDialog(
                    pCtx.getWindow(), 
                    msg, 
                    APP_NAME, 
                    JOptionPane.INFORMATION_MESSAGE);
        }
    };
    
    private final Action mOpenAction = new KAction(ActionConstants.R_OPEN) {
        private File mDir;
        
        @Override
        public void actionPerformed(ActionContext pCtx) {
            try {
                if (mDir == null) {
                    String currentFilename = mEditor.getTextPane().getTickDocument().getFilename();
                    if (currentFilename != null) {
                        mDir = new File(currentFilename).getParentFile();
                    }
                }
                
                JFileChooser chooser = new JFileChooser(mDir);
                int retVal = chooser.showOpenDialog(pCtx.getWindow());
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    mEditor.setFile(file, true);
                    setTitle(mEditor.getTextPane().getTickDocument().getFilename());
                }
                mDir = chooser.getCurrentDirectory();
            } catch (Exception e) {
                LOG.error("Failed to load", e);
            }
        }
    };
    
    public TickFrame() {
        ActionContainer ac = getActionContainer();
        
        ac.addMenu(new KMenu(
                ActionConstants.R_MENU_FILE,
                mNewViewAction,
                mOpenAction,
                KAction.SEPARATOR,
                new ExitAction()));
        
        ac.addMenu(new KMenu(
                ActionConstants.R_MENU_VIEW,
                new KAction("Tick Set 1")));
        
        ac.addMenu(new KMenu(
                ActionConstants.R_MENU_HELP,
                mAboutAction));

        KToolbar mainTb = new KToolbar(
            ActionConstants.R_TB_MAIN,
            mOpenAction);
        
        ac.addToolbar(mainTb);
        
        setSize(new Dimension(600, 600));
        
        // Just testing
        if (true) {
            String DEF_FILE = "/home/kari/data/devel/work/tigtag/src/org/kari/tick/gui/TickTextPane.java";
            try {
                mEditor.setFile(new File(DEF_FILE), true);
                setTitle(mEditor.getTextPane().getTickDocument().getFilename());
            } catch (IOException e) {
                LOG.error("Failed to load: " + DEF_FILE, e);
                mEditor.getTextPane().setText("Failed to load: " + DEF_FILE);
            }
        }        
    }

    @Override
    protected JComponent createCenterPanel()
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(getEditor(), BorderLayout.CENTER);
        return panel;
    }

    public TickEditorPanel getEditor() {
        if (mEditor == null) {
            mEditor = new TickEditorPanel();
        }
        return mEditor;
    }
    
    
    
}
