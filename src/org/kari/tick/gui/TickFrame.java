package org.kari.tick.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

import org.kari.action.ActionConstants;
import org.kari.action.ActionContainer;
import org.kari.action.ActionContext;
import org.kari.action.ActionGroup;
import org.kari.action.KAction;
import org.kari.action.KMenu;
import org.kari.action.KMenuImpl;
import org.kari.action.KToolbar;
import org.kari.action.std.CloseWindowAction;
import org.kari.action.std.ExitAction;
import org.kari.perspective.KApplicationFrame;
import org.kari.resources.ResKey;
import org.kari.resources.ResourceAdapter;
import org.kari.resources.WidgetResources;
import org.kari.tick.FileSaver;
import org.kari.tick.Tick;
import org.kari.tick.TickDefinition;
import org.kari.tick.TickEditorStarter;
import org.kari.tick.TickSet;

/**
 * TigTag window
 * 
 * @author kari
 */
public class TickFrame extends KApplicationFrame 
    implements
        TickListener
{
    public static final int HEIGHT = 800;

    public static final String APP_NAME = "TigTag";

    private TickEditorPanel mEditor;
    
    
    /**
     * Allow DnD of files into editor
     */
    public class TickTransferHandler extends TransferHandler {
        @Override
        public boolean canImport(JComponent pComp, DataFlavor[] pTransferFlavors)
        {
            return true;
        }

        @Override
        public boolean importData(JComponent pComp, Transferable pData) {
            boolean result = false;
            try {
                List<String> validURIs = new ArrayList<String>();
                List<File> validFiles = new ArrayList<File>();

                DataFlavor[] flavors = pData.getTransferDataFlavors();

                for (int flavorIter=0; flavorIter<flavors.length; flavorIter++) {
                    DataFlavor flavor = flavors[flavorIter];
                    LOG.debug(flavor.getMimeType());
                    Class cls = flavor.getRepresentationClass();

                    if (flavor.getMimeType().indexOf("text/uri-list")!=-1 && cls==String.class) {
                        String data = (String)pData.getTransferData(flavor);
                        StringTokenizer st = new StringTokenizer(data, "\r\n");
                        while (st.hasMoreElements()) {
                            String uri = st.nextToken().trim();
                            if (!"".equals(uri)) {
                                validURIs.add(uri);
                            }
                        }
                    } else if (flavor.getMimeType().indexOf("application/x-java-file-list")!=-1) {
                        List<File> files = (List<File>)pData.getTransferData(flavor);
                        validFiles.addAll(files);
                    }
                }

                if (!validURIs.isEmpty()) {
                    Collections.sort(validURIs, new Comparator<String>() {
                        public int compare(String pO1, String pO2) {
                            // TODO KI suffix sort
                            return pO1.compareTo(pO2);
                        }
                    });
                    Collections.reverse(validURIs);

                    boolean usedCurrent = false;
                    for (String uri : validURIs) {
                        try {
                            if (uri.startsWith("zip:")) {
                                // TODO KI Nicer approach would be to install "zip:/" URI handler
                                // KDE support...
//                                mOpenAction.openZipFile(uri);
                                LOG.error("NY! " + uri);
                            } else {
                                URL url = new URL(uri);
                                File file = new File(url.getFile());
                                LOG.info("Opening: " + url);
                                if (file.exists()) {
                                    if (!usedCurrent) {
                                        setFile(file);
                                        usedCurrent = true;
                                    } else {
                                        new TickEditorStarter(file).start();
                                    }
                                } else {
                                    LOG.error("File not found: " + url);
                                }
                            }
                        } catch (Exception e1) {
                            LOG.error("Failed to open: " + uri, e1);
                        }
                    }
                } else {
                    Collections.sort(validFiles, new Comparator<File>() {
                        public int compare(File pO1, File pO2) {
                            // TODO KI suffix sort
                            return pO1.compareTo(pO2);
                        }
                    });
                    Collections.reverse(validFiles);
                    
                    for (File file : validFiles) {
                        try {
                            LOG.info("Opening: " + file);
                            new TickEditorStarter(file).start();
                        } catch (Exception e1) {
                            LOG.error("Failed to open: " + file, e1);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("Failed ot insert", e);
            }
            return result;
        }
    }
    
    
    /**
     * Selects predefine tick 
     */
    final class TickAction extends KAction {
        private TickDefinition mDefinition;
        
        public TickAction(TickDefinition pDefinition, ActionGroup pGroup) {
            super(pDefinition.getName() + " - " + pDefinition.getBlockMode().getName(), pGroup);
            mDefinition = pDefinition;
            putValue(SMALL_ICON, mDefinition.getIcon());
        }
        
        @Override
        public void actionPerformed(ActionContext pCtx) {
            getEditor().getTextPane().getTickSet().setCurrent(mDefinition);
        }
    }

    /**
     * Adjust editor font size
     * 
     * @author kari
     */
    final class FontSizeAction extends KAction {
        private final int mAdjustment;
        
        private FontSizeAction(int pAdjustment) {
            super(pAdjustment > 0 ? TickConstants.R_GROW_FONT : TickConstants.R_SHRINK_FONT);
            mAdjustment = pAdjustment;
        }

        @Override
        public void actionPerformed(ActionContext pCtx) {
            TickTextPane textPane = getEditor().getTextPane();
            Font font = textPane.getFont();
            int size = font.getSize();
            size += mAdjustment;
            if (size < 8) {
                size = 8;
            } else if (size > 20) {
                size = 20;
            }
            
            if (size != font.getSize()) {
                textPane.refresh();
                Font newFont = font.deriveFont((float)size);
                textPane.setFont(newFont);
            }
        }
    }
    
    private final ActionGroup mDefinitionGroup = new ActionGroup();

    private final Action mEditTicksAction = new KAction("&Edit Markers") {
        @Override
        public void actionPerformed(ActionContext pCtx) {
            try {
                new EditDefinitions(TickFrame.this).edit();
            } catch (Exception e) {
                LOG.error("Failed to save", e);
            }
        }
    };

    private final Action mDuplicateViewAction = new KAction(TickConstants.R_DUPLICATE) {
        @Override
        public void actionPerformed(ActionContext pCtx) {
            File file = getEditor().getTextPane().getTickDocument().getFile();
            new TickEditorStarter(file).start();
        }
    };
    
    private final Action mAboutAction = new KAction("&About") {
        @Override
        public void actionPerformed(ActionContext pCtx) {
            String msg = "<html>"
                + "<b>" 
                + TickConstants.APP_NAME
                + " v" + TickConstants.VERSION + "</b><br><br>"
                + "The Code Review Editor.<br><br>"
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

    /**
     * Open new file to be ticked or tick file
     */
    private final Action mOpenAction = new KAction(ActionConstants.R_OPEN) {
        private File mDir;
        
        @Override
        public void actionPerformed(ActionContext pCtx) {
            try {
                TickDocument oldDoc = getEditor().getTextPane().getTickDocument();
                if (mDir == null) {
                    File currentFile = oldDoc.getFile();
                    if (currentFile != null) {
                        mDir = currentFile.getParentFile();
                    }
                }
                
                JFileChooser chooser = new JFileChooser(mDir);
                int retVal = chooser.showOpenDialog(pCtx.getWindow());
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    setFile(file);
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
                TickDocument doc = getEditor().getTextPane().getTickDocument();
                new FileSaver(doc).save();
                doc.setModified(false);
                updateActions();
            } catch (Exception e) {
                LOG.error("Failed to load", e);
            }
        }
    };
    
    
    public TickFrame() {
        ActionContainer ac = getActionContainer();
        
        ac.addMenu(new KMenu(
                ActionConstants.R_MENU_FILE,
                mDuplicateViewAction,
                KAction.SEPARATOR,
                mOpenAction,
                mSaveAction,
                KAction.SEPARATOR,
                new CloseWindowAction(),
                new ExitAction()));
        
        ac.addMenu(new KMenu(
                ActionConstants.R_MENU_VIEW,
                new FontSizeAction(1),
                new FontSizeAction(-1)));

        
        ac.addMenu(new KMenu(
                ActionConstants.R_MENU_EDIT,
                getEditor().getActionMap().get(ActionConstants.R_CLEAR),
                KAction.SEPARATOR,
                mEditTicksAction
            ));
        
        ac.addMenu(new KMenu(
                ActionConstants.R_MENU_HELP,
                mAboutAction));

        KToolbar mainTb = new KToolbar(
            ActionConstants.R_TB_MAIN,
            mDuplicateViewAction,
            mOpenAction,
            KAction.SEPARATOR,
            mSaveAction 
            );
        
        ac.addToolbar(mainTb);
        
        setSize(new Dimension(800, HEIGHT));

        setIcon(TickConstants.R_APP); 
        setAppTitle(null);
    }
    
    /**
     * @param title Title, null to set empty title
     */
    public void setAppTitle(String title) {
        if (title == null || title.length() == 0) {
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
     * @param pFilename, null clears current content
     */
    public void setFile(File pFile) {
        TickEditorPanel editor = getEditor();
        try {
            if (pFile != null) {
                TickDocument oldDoc = editor.getTextPane().getTickDocument();
                editor.setFile(pFile, true);
                TickDocument newDoc = editor.getTextPane().getTickDocument();
                setAppTitle(newDoc.getFile().getAbsolutePath());
                
                oldDoc.removeTickListener(TickFrame.this);
                newDoc.addTickListener(TickFrame.this);
            } else {
                setAppTitle(null);
                TickDocument tickDoc = editor.getTextPane().getTickDocument();
                tickDoc.clearTicks();
                editor.getTextPane().setText("");
            }
        } catch (Exception e) {
            LOG.error("Failed to load: " + pFile, e);
            editor.getTextPane().setText("Failed to load:\n \"" + pFile + "\"");
        } finally {
            createMarkerMenu();
            updateActions();
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
            
            TransferHandler th = new TickTransferHandler();
            mEditor.setTransferHandler(th);
            mEditor.getTextPane().setTransferHandler(th);
            mEditor.getTickTable().setTransferHandler(th);
        }
        return mEditor;
    }

    /**
     * Create/recrete markers menu
     */
    public void createMarkerMenu() {
        TickDocument doc = getEditor().getTextPane().getTickDocument();
        TickSet set = doc.getRegistry().getSet("Set 1");
        getEditor().getTextPane().setTickSet(set);
        
        KAction[] tickActions = createTickActions();
        tickActions[0].setSelected(true);
        set.setCurrent( ((TickAction)tickActions[0]).mDefinition );
        
        KMenu markerMenu = new KMenu(
                TickConstants.R_MARKERS_MENU,
                tickActions);
        JMenu menu = markerMenu.create(this);
        
        JMenuBar menubar = getJMenuBar();
        KMenuImpl oldMenu = (KMenuImpl)menubar.getComponent(menubar.getComponentCount() - 2);
        if (oldMenu != null
            && TickConstants.R_MARKERS_MENU.equals(oldMenu.getActionMenu().getName())) 
        {
            menubar.remove(oldMenu);
        }
        menubar.add(menu, menubar.getComponentCount() - 1);
        menubar.revalidate();
    }
    
    /**
     * Create/Update tick actions
     */
    private KAction[] createTickActions() {
        mDefinitionGroup.clear();
        
        List<KAction> result = new ArrayList<KAction>();
        TickDocument doc = getEditor().getTextPane().getTickDocument();
        List<TickDefinition> definitions = new ArrayList<TickDefinition>(
                doc.getRegistry().getDefinitions());
        Collections.sort(definitions, TickDefinition.NAME_COMPARATOR);
        
        for (TickDefinition def : definitions) {
            result.add(new TickAction(def, mDefinitionGroup));
        }
        
        return result.toArray(new KAction[result.size()]);
    }

    /**
     * Update actions
     */
    protected void updateActions() {
        TickDocument doc = getEditor().getTextPane().getTickDocument();
        mSaveAction.setEnabled(!doc.isEmpty() && doc.isModified());
    }
    
    @Override
    public void tickAdded(TickDocument pDocument, Tick pTick) {
        updateActions();
    }

    @Override
    public void tickRemoved(TickDocument pDocument, Tick pTick) {
        updateActions();
    }
   
}
