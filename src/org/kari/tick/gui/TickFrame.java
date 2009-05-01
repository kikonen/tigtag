package org.kari.tick.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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
import org.kari.resources.ResKey;
import org.kari.resources.ResourceAdapter;
import org.kari.resources.WidgetResources;
import org.kari.tick.FileSaver;
import org.kari.tick.TickDefinition;
import org.kari.tick.TickEditorStarter;
import org.kari.tick.TickRegistry;
import org.kari.tick.TickSet;
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
     * Selects mode (word/block/...)
     */
    final class ModeAction extends KAction {
        private BlockMode mBlockMode;
        
        public ModeAction(BlockMode pBlockMode, ActionGroup pGroup) {
            super(pBlockMode != null ? pBlockMode.getName() : "&Default", pGroup);
            mBlockMode = pBlockMode;
        }

        @Override
        public void actionPerformed(ActionContext pCtx) {
            getEditor().getTextPane().getTickSet().setBlockMode(mBlockMode);
        }
    }
    
    /**
     * Selects predefine tick 
     */
    final class TickAction extends KAction {
        private TickDefinition mDefinition;
        
        public TickAction(TickDefinition pDefinition, ActionGroup pGroup) {
            super(pDefinition.getName(), pGroup);
            mDefinition = pDefinition;
        }
        
        @Override
        public void actionPerformed(ActionContext pCtx) {
            getEditor().getTextPane().getTickSet().setCurrent(mDefinition);
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
        new ModeAction(null, mModeGroup),
        new ModeAction(BlockMode.HIGHLIGHT, mModeGroup),
        new ModeAction(BlockMode.UNDERLINE, mModeGroup),
        new ModeAction(BlockMode.BLOCK, mModeGroup),
        new ModeAction(BlockMode.SIDEBAR, mModeGroup),
    };
    
    private final ActionGroup mDefinitionGroup = new ActionGroup();

    private final Action mNewViewAction = new KAction(TickConstants.R_NEW_VIEW) {
        @Override
        public void actionPerformed(ActionContext pCtx) {
            String filename = getEditor().getTextPane().getTickDocument().getFilename();
            new TickEditorStarter(filename).start();
        }
    };
    
    private final Action mAboutAction = new KAction("&About") {
        @Override
        public void actionPerformed(ActionContext pCtx) {
            String msg = "<html>"
                + "<b>" 
                + TickConstants.APP_NAME
                + " v" + TickConstants.VERSION + "</b><br><br>"
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
                    setAppTitle(file.getAbsolutePath());
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
        
        KAction[] tickActions = createTickActions();
        mModeActions[0].setSelected(true);
        tickActions[0].setSelected(true);
        
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
                mModeActions[4]));
        
        ac.addMenu(new KMenu(
                "&Ticks",
                tickActions));
        
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
            mModeActions[3],
            mModeActions[4]
            );
        
        ac.addToolbar(mainTb);
        
        setSize(new Dimension(700, 600));
        
        TickSet tickSet = getEditor().getTextPane().getTickSet();
        tickSet.setBlockMode(null);
        tickSet.setCurrent( ((TickAction)tickActions[0]).mDefinition );
        
        WidgetResources wr = ResourceAdapter.getInstance().getWidget(
                TickConstants.R_APP, 
                ResKey.MENU);
        Icon icon = wr.getIcon();
        if (icon instanceof ImageIcon) {
            setIconImage( ((ImageIcon)icon).getImage() );
        }
        setAppTitle("");
    }
    
    public void setAppTitle(String title) {
        if (title.length() == 0) {
            title = "<No name>";
        }
        title +=
            " - " 
            + TickConstants.APP_NAME;
        
        super.setTitle(title);
    }
    

    /**
     * Show given file in editor
     * 
     * @param pFilename
     */
    public void setFile(String pFilename) {
        try {
            File file = new File(pFilename);
            mEditor.setFile(file, true);
            setAppTitle(file.getAbsolutePath());
        } catch (IOException e) {
            LOG.error("Failed to load: " + pFilename, e);
            mEditor.getTextPane().setText("Failed to load: \"" + pFilename + "\"");
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

    /**
     * Create/Update tick actions
     */
    private KAction[] createTickActions() {
        mDefinitionGroup.clear();
        
        List<KAction> result = new ArrayList<KAction>();
        List<TickDefinition> definitions = new ArrayList<TickDefinition>(TickRegistry.getInstance().getDefinitions());
        Collections.sort(definitions, TickDefinition.NAME_COMPARATOR);
        
        for (TickDefinition def : definitions) {
            result.add(new TickAction(def, mDefinitionGroup));
        }
        
        return result.toArray(new KAction[result.size()]);
    }
    
}
