package org.kari.tick.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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
import org.kari.action.ActionGroup;
import org.kari.action.KAction;
import org.kari.action.KMenu;
import org.kari.action.KToolbar;
import org.kari.action.std.CloseWindowAction;
import org.kari.action.std.ExitAction;
import org.kari.perspective.KApplicationFrame;
import org.kari.tick.FileSaver;
import org.kari.tick.TickDefinition.BlockMode;

/**
 * TigTag window
 * 
 * @author kari
 */
public class TickFrame extends KApplicationFrame {
    public static final String APP_NAME = "TigTag";

    private TickEditorPanel mEditor;
    
    /**
     * Selects ticking mode (word/block/...)
     */
    final class ModeAction extends KAction {
        private BlockMode mBlockMode;
        
        public ModeAction(BlockMode pBlockMode, ActionGroup pGroup) {
            super(pBlockMode.getName(), pGroup);
            mBlockMode = pBlockMode;
        }

        @Override
        public void actionPerformed(ActionContext pCtx) {
            getEditor().getTextPane().getTickSet().setBlockMode(mBlockMode);
        }
    }
    
    private WindowListener mWindowListener = new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent pE) {
            dispose();
        }

        @Override
        public void windowClosed(WindowEvent pE) {
            // Nothing
        }
    };
    
    private final ActionGroup mModeGroup = new ActionGroup();
    private KAction[] mModeActions = {
        new ModeAction(BlockMode.HIGHLIGHT, mModeGroup),
        new ModeAction(BlockMode.UNDERLINE, mModeGroup),
        new ModeAction(BlockMode.BLOCK, mModeGroup),
        new ModeAction(BlockMode.SIDEBAR, mModeGroup),
    };
    

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
                + "<b>TigTag v" + TickConstants.VERSION + "</b><br><br>"
                + "All copyrights owned by Kari Ikonen<br>"
                + "(except for the  concept of the 'ticks')<br>"
                + "<br>"
                + "Contact Info:<br>"
                + "Email: <b>mr.kari.ikonen@gmail.com</b>"
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
    
    /**
     * Save contents of editor in this window
     */
    private final Action mSaveAction = new KAction(ActionConstants.R_SAVE) {
        @Override
        public void actionPerformed(ActionContext pCtx) {
            try {
                TickDocument doc = mEditor.getTextPane().getTickDocument();
                new FileSaver(doc).save();
                doc.setModified(false);
                // TODO KI enable save via doc listener
//                mSaveAction.setEnabled(false);
            } catch (Exception e) {
                LOG.error("Failed to load", e);
            }
        }
    };
    
    public TickFrame() {
        addWindowListener(mWindowListener);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        
        ActionContainer ac = getActionContainer();
        mModeActions[0].setSelected(true);
        
        ac.addMenu(new KMenu(
                ActionConstants.R_MENU_FILE,
                mNewViewAction,
                KAction.SEPARATOR,
                mOpenAction,
                mSaveAction,
                KAction.SEPARATOR,
                new CloseWindowAction(),
                new ExitAction()));
        
        ac.addMenu(new KMenu(
                ActionConstants.R_MENU_VIEW,
                mModeActions[0],
                mModeActions[1],
                mModeActions[2],
                mModeActions[3],
                KAction.SEPARATOR,
                new KAction("Tick Set 1")));
        
        ac.addMenu(new KMenu(
                ActionConstants.R_MENU_HELP,
                mAboutAction));

        KToolbar mainTb = new KToolbar(
            ActionConstants.R_TB_MAIN,
            mSaveAction,
            mOpenAction,
            KAction.SEPARATOR,
            mModeActions[0],
            mModeActions[1],
            mModeActions[2],
            mModeActions[3]
            );
        
        ac.addToolbar(mainTb);
        
        setSize(new Dimension(700, 600));
        
        // Just testing
        if (true) {
            String DEF_FILE = "/home/kari/data/devel/work/tigtag/src/org/kari/tick/TickRegistry.java";
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
    public void dispose() {
        TickDocumentManager.getInstance().closeDocument(getEditor().getTextPane().getTickDocument());
        super.dispose();
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
