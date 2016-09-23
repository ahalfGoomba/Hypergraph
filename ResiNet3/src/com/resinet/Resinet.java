package com.resinet;

import com.resinet.controller.MainframeController;
import com.resinet.util.Constants;
import com.resinet.util.GbcBuilder;
import com.resinet.util.Strings;
import com.resinet.util.Util;
import com.resinet.views.NetPanel;
import com.resinet.views.ProbabilitySpinner;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Locale;
import java.util.Map;

public class Resinet implements Constants {
    private final MainframeController controller;

    private final JPanel contentPane;
    private JMenuBar menuBar;

    private JPanel sidePanel;

    private NetPanel netPanel;

    private ProbabilitySpinner edgeEndProbabilityBox;
    private ProbabilitySpinner edgeProbabilityStepSizeBox;
    private ProbabilitySpinner nodeEndProbabilityBox;
    private ProbabilitySpinner nodeProbabilityStepSizeBox;
    private ProbabilitySpinner sameReliabilityEdgeProbBox;
    private ProbabilitySpinner sameReliabilityNodeProbBox;

    private JButton calcReliabilityBtn, calcResilienceBtn;
    private JProgressBar calculationProgressBar;

    private JTextPane outputField;

    private JTextPane statusBarCollapsedLabel;
    private JPanel singleReliabilitiesContainer;
    private JTabbedPane hypergraphTabbedPane;
    private JCheckBox coloredHyperedgesBox;
    private JCheckBox ellipseVisualisation;
    private JSpinner alphaValueSpinner;
    private JLabel alphaLabel;
    JPanel calculatePanel;

    private GUI_STATES guiState;
    private JTabbedPane reliabilitiesTabbedPane;
    private JMenuItem tutorialMenuItem;
    private JMenuItem aboutMenuItem;
    private JMenuItem resetMenuItem;
    private JMenuItem openMenuItem;
    private JMenuItem saveMenuItem;
    private JMenuItem closeMenuItem;
    private JCheckBox calculationSeriesCheckBox;
    private JCheckBox considerNodesBox;
    private JCheckBox considerEdgesBox;
    private JCheckBox considerHyperEdgesBox;
    
    private JScrollPane singleReliabilitiesScrollPane;
    private JPanel expandedOutputPanel;
    private JButton collapseOutputBtn;
    private JCheckBox differentForTerminalCheckBox;
    private ProbabilitySpinner terminalNodeProbBox;
    private JMenuItem undoMenuItem;
    private JMenuItem redoMenuItem;
    private JMenuItem centerGraphMenuItem;
    private JMenuItem alignGraphMenuItem;
    private JMenuItem hypergraphFileMenuItem;
    private JMenuItem graphFileMenuItem;
    private JMenuItem generateGraphMenuItem;
    
    private boolean hypergraphMode; 

    public Resinet(MainframeController controller) {
        this.controller = controller;
        contentPane = new JPanel(new BorderLayout());
        hypergraphMode = false; 

        //Windows Look-and-Feel setzen
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        initMenu();
        initNetPanel();
        initSideBar();
        initStatusBar();

        setGuiState(GUI_STATES.ENTER_GRAPH, false);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainframeController controller = new MainframeController();

            Resinet resinet = new Resinet(controller);
            controller.setMainFrame(resinet);

            JFrame mainFrame = new JFrame(Strings.getLocalizedString("title"));
            mainFrame.addWindowListener(controller);

            mainFrame.setContentPane(resinet.getContentPane());
            mainFrame.setJMenuBar(resinet.getMenuBar());

            //Beim Klicken auf X das Programm beenden
            mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

            //Minimale Größe setzen
            mainFrame.setMinimumSize(new Dimension(700, 500));

            //Größe setzen
            mainFrame.setSize(1200, 700);

            //Auf dem Bildschirm zentrieren und sichtbar machen
            mainFrame.setLocationRelativeTo(null);
            mainFrame.setVisible(true);
        });
    }

    private void initMenu() {
        menuBar = new JMenuBar();
        initFileMenu();
        initEditMenu();
        initGraphMenu();
        initLanguageMenu();
        initHelpMenu();
    }

    private void initEditMenu() {
        JMenu editMenu = new JMenu(Strings.getLocalizedString("edit"));
        editMenu.addMenuListener(controller);

        undoMenuItem = new JMenuItem(Strings.getLocalizedString("undo"));
        undoMenuItem.addActionListener(controller);
        undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK));
        undoMenuItem.setActionCommand("undo");
        editMenu.add(undoMenuItem);

        redoMenuItem = new JMenuItem(Strings.getLocalizedString("redo"));
        redoMenuItem.addActionListener(controller);
        redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK));
        redoMenuItem.setActionCommand("redo");
        editMenu.add(redoMenuItem);

        editMenu.addSeparator();

        JMenuItem cutMenuItem = new JMenuItem(Strings.getLocalizedString("cut"));
        cutMenuItem.addActionListener(controller);
        cutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
        cutMenuItem.setActionCommand((String) TransferHandler.getCutAction().getValue(Action.NAME));
        editMenu.add(cutMenuItem);

        JMenuItem copyMenuItem = new JMenuItem(Strings.getLocalizedString("copy"));
        copyMenuItem.addActionListener(controller);
        copyMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
        copyMenuItem.setActionCommand((String) TransferHandler.getCopyAction().getValue(Action.NAME));
        editMenu.add(copyMenuItem);

        JMenuItem pasteMenuItem = new JMenuItem(Strings.getLocalizedString("paste"));
        pasteMenuItem.addActionListener(controller);
        pasteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));
        pasteMenuItem.setActionCommand((String) TransferHandler.getPasteAction().getValue(Action.NAME));
        editMenu.add(pasteMenuItem);

        editMenu.addSeparator();

        JMenuItem deleteMenuItem = new JMenuItem(Strings.getLocalizedString("delete"));
        deleteMenuItem.addActionListener(controller);
        deleteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        //Dieser Aktions-String entspricht dem ActionEvent.getActionCommand in der Methode actionPerformed
        deleteMenuItem.setActionCommand("delete");
        editMenu.add(deleteMenuItem);

        editMenu.addSeparator();

        JMenuItem selectAllMenuItem = new JMenuItem(Strings.getLocalizedString("select.all"));
        selectAllMenuItem.addActionListener(controller);
        selectAllMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK));
        selectAllMenuItem.setActionCommand("select_all");
        editMenu.add(selectAllMenuItem);

        menuBar.add(editMenu);
    }

    private void initHelpMenu() {
        //Hilfemenü aufbauen
        JMenu helpMenu = new JMenu(Strings.getLocalizedString("help"));

        tutorialMenuItem = new JMenuItem(Strings.getLocalizedString("show.help"));
        tutorialMenuItem.addActionListener(controller);
        helpMenu.add(tutorialMenuItem);

        helpMenu.addSeparator();

        aboutMenuItem = new JMenuItem(Strings.getLocalizedString("about.resinet"));
        aboutMenuItem.addActionListener(controller);
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);
    }

    private void initLanguageMenu() {
        //Sprachmenü aufbauen
        JMenu languageMenu = new JMenu(Strings.getLocalizedString("language"));

        for (Map.Entry lang : Strings.languages.entrySet()) {
            JMenuItem itm = new JCheckBoxMenuItem((String) lang.getValue());
            itm.addActionListener((e) -> {
                if (!Strings.setLanguageAndRestart(contentPane, (String) lang.getKey())) {
                    itm.setSelected(false);
                }
            });
            //Falls der Menüeintrag der aktuellen Sprache entspricht, abhaken und deaktivieren
            if (Strings.currentLocale.getLanguage().equals(new Locale((String) lang.getKey()).getLanguage())) {
                itm.setSelected(true);
                itm.setEnabled(false);
            }
            languageMenu.add(itm);
        }

        menuBar.add(languageMenu);
    }

    private void initGraphMenu() {
        //Menü Graph aufbauen
        JMenu graphMenu = new JMenu(Strings.getLocalizedString("graph"));

        centerGraphMenuItem = new JMenuItem(Strings.getLocalizedString("center.graph"));
        centerGraphMenuItem.addActionListener(controller);
        graphMenu.add(centerGraphMenuItem);

        alignGraphMenuItem = new JMenuItem(Strings.getLocalizedString("align.graph"));
        alignGraphMenuItem.addActionListener(controller);
        graphMenu.add(alignGraphMenuItem);

        JMenuItem selectOverlappingVerticesMenuItem = new JMenuItem(Strings.getLocalizedString("select.overlapping.vertices"));
        selectOverlappingVerticesMenuItem.addActionListener(controller);
        selectOverlappingVerticesMenuItem.setActionCommand("select overlapping");
        graphMenu.add(selectOverlappingVerticesMenuItem);

        
        graphMenu.addSeparator();

        generateGraphMenuItem = new JMenuItem(Strings.getLocalizedString("generate.graph"));
        generateGraphMenuItem.addActionListener(controller);
        generateGraphMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_MASK));
        graphMenu.add(generateGraphMenuItem);

        menuBar.add(graphMenu);
    }

    private void initFileMenu() {
        //Menü "Datei" aufbauen
        JMenu fileMenu = new JMenu(Strings.getLocalizedString("file"));
        
        
        hypergraphFileMenuItem = new JMenuItem(Strings.getLocalizedString("hypergraph"));
        hypergraphFileMenuItem.addActionListener(controller);
        fileMenu.add(hypergraphFileMenuItem);
        
        graphFileMenuItem = new JMenuItem(Strings.getLocalizedString("graph.graph"));
        graphFileMenuItem.addActionListener(controller);
        fileMenu.add(graphFileMenuItem); 
        
        fileMenu.addSeparator(); 
        
        resetMenuItem = new JMenuItem(Strings.getLocalizedString("reset.network"));
        resetMenuItem.addActionListener(controller);
        //Tastenkombination Strg+R
        resetMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
        fileMenu.add(resetMenuItem);

        fileMenu.addSeparator();

        openMenuItem = new JMenuItem(Strings.getLocalizedString("load.network"));
        openMenuItem.addActionListener(controller);
        //Tastenkombination Strg+O
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
        fileMenu.add(openMenuItem);

        saveMenuItem = new JMenuItem(Strings.getLocalizedString("save.network"));
        saveMenuItem.addActionListener(controller);
        //Tastenkombination Strg+S
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
        fileMenu.add(saveMenuItem);

        fileMenu.addSeparator();

        closeMenuItem = new JMenuItem(Strings.getLocalizedString("close"));
        closeMenuItem.addActionListener(controller);
        //Alt+F4 abfangen
        closeMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_MASK));
        fileMenu.add(closeMenuItem);

        menuBar.add(fileMenu);
    }

    private void initNetPanel() {
        netPanel = new NetPanel(controller);
        netPanel.setBackground(Color.white);

        JScrollPane graphScrollPane = new JScrollPane(netPanel);
        graphScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        graphScrollPane.getHorizontalScrollBar().setUnitIncrement(10);
        contentPane.add(graphScrollPane, BorderLayout.CENTER);
    }

    private void initSideBar() {
        sidePanel = new JPanel(new BorderLayout());
       
        initProbabilitiesPanel();
        initCalculatePanel();
        initHypergraphPanel();

        contentPane.add(sidePanel, BorderLayout.LINE_END);
    }

    private void initProbabilitiesPanel() {
        reliabilitiesTabbedPane = new JTabbedPane(JTabbedPane.TOP);
        reliabilitiesTabbedPane.addChangeListener(controller);
      
        initSingleReliabilitiesTab();

        initSameReliabilityTab();
        
        sidePanel.add(reliabilitiesTabbedPane, BorderLayout.CENTER);
       
    }

    private void initSameReliabilityTab() {
        JPanel sameReliabilityPanel = new JPanel(new GridBagLayout());
        sameReliabilityPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        reliabilitiesTabbedPane.addTab(Strings.getLocalizedString("same.reliabilities"), sameReliabilityPanel);

        //Kantenwahrscheinlichkeiten
        JLabel edgeProbLabel = new JLabel(Strings.getLocalizedString("reliability.of.every.edge"), SwingConstants.RIGHT);
        sameReliabilityPanel.add(edgeProbLabel, GbcBuilder.build(0, 0, 1, 1, 1, 0).bottom(10).left());

        sameReliabilityEdgeProbBox = new ProbabilitySpinner();
        sameReliabilityPanel.add(sameReliabilityEdgeProbBox, GbcBuilder.build(1, 0, 1, 1, 1, 0).bottom(10));

        //Knotenwahrscheinlichkeiten
        JLabel nodeProbLabel = new JLabel(Strings.getLocalizedString("reliability.of.every.vertex"), SwingConstants.RIGHT);
        sameReliabilityPanel.add(nodeProbLabel, GbcBuilder.build(0, 1, 1, 1, 1, 0).bottom(10).left());

        sameReliabilityNodeProbBox = new ProbabilitySpinner();
        sameReliabilityPanel.add(sameReliabilityNodeProbBox, GbcBuilder.build(1, 1, 1, 1, 1, 0).bottom(10));

        //Terminalknotenwahrscheinlichkeiten / CheckBox
        differentForTerminalCheckBox = new JCheckBox(Strings.getLocalizedString("terminal.node.reliability"));
        differentForTerminalCheckBox.addItemListener(controller);
        differentForTerminalCheckBox.setMargin(new Insets(0, 0, 0, 0));
        sameReliabilityPanel.add(differentForTerminalCheckBox, GbcBuilder.build(0, 2, 1, 1, 1, 0).bottom(10).left());

        terminalNodeProbBox = new ProbabilitySpinner();
        sameReliabilityPanel.add(terminalNodeProbBox, GbcBuilder.build(1, 2, 1, 1, 1, 0).bottom(10));

        //Checkbox für Berechnungsserie
        calculationSeriesCheckBox = new JCheckBox(Strings.getLocalizedString("perform.calculation.series"));
        calculationSeriesCheckBox.setMargin(new Insets(0, 0, 0, 0));
        calculationSeriesCheckBox.addItemListener(controller);
        sameReliabilityPanel.add(calculationSeriesCheckBox, GbcBuilder.build(0, 3, 2, 1, 1, 0).bottom(10).left());

        //Eingabefelder für Berechnungsserie
        JLabel edgeProbEndValueLbl = new JLabel(Strings.getLocalizedString("edge.end.value"), SwingConstants.RIGHT);
        sameReliabilityPanel.add(edgeProbEndValueLbl, GbcBuilder.build(0, 4, 1, 1, 1, 0).bottom(10).left());

        edgeEndProbabilityBox = new ProbabilitySpinner();
        edgeEndProbabilityBox.setEnabled(false);
        sameReliabilityPanel.add(edgeEndProbabilityBox, GbcBuilder.build(1, 4, 1, 1, 1, 0).bottom(10));

        JLabel edgeStepSizeLbl = new JLabel(Strings.getLocalizedString("edge.step.size"), SwingConstants.RIGHT);
        sameReliabilityPanel.add(edgeStepSizeLbl, GbcBuilder.build(0, 5, 1, 1, 1, 0).bottom(10).left());

        edgeProbabilityStepSizeBox = new ProbabilitySpinner("0.01");
        edgeProbabilityStepSizeBox.setEnabled(false);
        sameReliabilityPanel.add(edgeProbabilityStepSizeBox, GbcBuilder.build(1, 5, 1, 1, 1, 0).bottom(10));

        JLabel nodeEndValueLbl = new JLabel(Strings.getLocalizedString("vertex.end.value"), SwingConstants.RIGHT);
        sameReliabilityPanel.add(nodeEndValueLbl, GbcBuilder.build(0, 6, 1, 1, 1, 0).bottom(10).left());

        nodeEndProbabilityBox = new ProbabilitySpinner();
        nodeEndProbabilityBox.setEnabled(false);
        sameReliabilityPanel.add(nodeEndProbabilityBox, GbcBuilder.build(1, 6, 1, 1, 1, 0).bottom(10));

        JLabel nodeStepSizeLbl = new JLabel(Strings.getLocalizedString("vertex.step.size"), SwingConstants.RIGHT);
        sameReliabilityPanel.add(nodeStepSizeLbl, GbcBuilder.build(0, 7, 1, 1, 1, 0).bottom(10).left());

        nodeProbabilityStepSizeBox = new ProbabilitySpinner("0.01");
        nodeProbabilityStepSizeBox.setEnabled(false);
        sameReliabilityPanel.add(nodeProbabilityStepSizeBox, GbcBuilder.build(1, 7, 1, 1, 1, 0).bottom(10));
    }

    private void initSingleReliabilitiesTab() {
        JPanel singleReliabilitiesPanel = new JPanel(new BorderLayout());
        reliabilitiesTabbedPane.addTab(Strings.getLocalizedString("different.reliabilities"), singleReliabilitiesPanel);

        JPanel considerComponentsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        considerNodesBox = new JCheckBox(Strings.getLocalizedString("consider.vertex.reliabilities"));
        considerNodesBox.addItemListener(controller);
        considerComponentsPanel.add(considerNodesBox);
        considerNodesBox.setSelected(true);

        considerEdgesBox = new JCheckBox(Strings.getLocalizedString("consider.edge.reliabilities"));
        considerEdgesBox.addItemListener(controller);
        considerComponentsPanel.add(considerEdgesBox);
        considerEdgesBox.setSelected(true);
        

        singleReliabilitiesContainer = new JPanel(new GridBagLayout());
        singleReliabilitiesContainer.setBorder(new EmptyBorder(0, 0, 0, 15));
        singleReliabilitiesScrollPane = new JScrollPane(singleReliabilitiesContainer);
        singleReliabilitiesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        singleReliabilitiesScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        singleReliabilitiesScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        singleReliabilitiesScrollPane.getHorizontalScrollBar().setUnitIncrement(10);

        singleReliabilitiesPanel.add(considerComponentsPanel, BorderLayout.PAGE_START);
        singleReliabilitiesPanel.add(singleReliabilitiesScrollPane, BorderLayout.CENTER);
    }

    private void initCalculatePanel() {
        calculatePanel = new JPanel();

        calculatePanel.setBorder(BorderFactory.createTitledBorder(Strings.getLocalizedString("start.calculation")));
        calculatePanel.setLayout(new GridBagLayout());

        calcReliabilityBtn = new JButton(Strings.getLocalizedString("calculate.reliability"));
        calcReliabilityBtn.addActionListener(controller);
        calculatePanel.add(calcReliabilityBtn, GbcBuilder.build(0, 0, 1, 1, 1, 0).fillBoth());

        calcResilienceBtn = new JButton(Strings.getLocalizedString("calculate.resilience"));
        calcResilienceBtn.addActionListener(controller);
        calculatePanel.add(calcResilienceBtn, GbcBuilder.build(0, 1, 1, 1, 1, 0).fillBoth());

        calculationProgressBar = new JProgressBar();
        calculationProgressBar.setStringPainted(true);
        calculatePanel.add(calculationProgressBar, GbcBuilder.build(0, 2, 2, 1, 1, 0).fillBoth());

        sidePanel.add(calculatePanel, BorderLayout.PAGE_END);
    }
    
    private void initHypergraphPanel(){
    	hypergraphTabbedPane = new JTabbedPane(JTabbedPane.TOP);
        hypergraphTabbedPane.addChangeListener(controller);
    	JPanel hypergraphPanel = new JPanel((new GridLayout(15,1)));
    	    	
    	coloredHyperedgesBox = new JCheckBox("colored Hyperedges");
    	coloredHyperedgesBox.addItemListener(controller);
    	hypergraphPanel.add(coloredHyperedgesBox);
    	
    	ellipseVisualisation = new JCheckBox("visualisation as ellipses       ");
    	ellipseVisualisation.addItemListener(controller);
    	hypergraphPanel.add(ellipseVisualisation);
    	
    	alphaLabel = new JLabel(Strings.getLocalizedString("alpha.value"));
    	alphaLabel.setVisible(false);
    	hypergraphPanel.add(alphaLabel);
    	
    	SpinnerModel sm = new SpinnerNumberModel(185, 0, 255, 1);
    	alphaValueSpinner = new JSpinner(sm);
    	alphaValueSpinner.setSize(5, 5);
    	alphaValueSpinner.setVisible(false);
    	alphaValueSpinner.addChangeListener(controller);
    	hypergraphPanel.add(alphaValueSpinner);
 	
    	hypergraphTabbedPane.add("Hypergraph", hypergraphPanel);
    	
    	
    	hypergraphTabbedPane.setVisible(false);
 	
    	contentPane.add(sidePanel, BorderLayout.LINE_END);
    }


    private void initStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));

        outputField = new JTextPane();
        outputField.setEditable(false);
        outputField.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        outputField.setBorder(new EmptyBorder(5, 5, 5, 5));
        outputField.setBackground(null);
        outputField.setFont(new Font("Tahoma", Font.PLAIN, 13));

        collapseOutputBtn = new JButton(Strings.getLocalizedString("hide"));
        collapseOutputBtn.addActionListener(controller);

        expandedOutputPanel = new JPanel(new BorderLayout());
        expandedOutputPanel.add(collapseOutputBtn, BorderLayout.LINE_END);
        expandedOutputPanel.add(outputField, BorderLayout.CENTER);

        statusBar.add(expandedOutputPanel, BorderLayout.CENTER);

        statusBarCollapsedLabel = new JTextPane();
        statusBarCollapsedLabel.setEditable(false);
        statusBarCollapsedLabel.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        statusBarCollapsedLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        statusBarCollapsedLabel.setBackground(null);
        statusBarCollapsedLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
        statusBar.add(statusBarCollapsedLabel, BorderLayout.PAGE_END);

        contentPane.add(statusBar, BorderLayout.PAGE_END);

        setResultText("");
    }

    /**
     * Setzt den aktuellen Status der Benutzeroberfläche, aktiviert und deaktiviert also Bedienelemente nach Bedarf
     *
     * @param state Der gewünschte Status
     */
    public void setGuiState(GUI_STATES state) {
        setGuiState(state, false);
    }

    /**
     * Setzt den aktuellen Status der Benutzeroberfläche, aktiviert und deaktiviert also Bedienelemente nach Bedarf
     *
     * @param state Der gewünschte Status
     * @param force Überspringt die Prüfung, ob der neue Status gleich dem alten Status ist und erzwingt somit die
     *              Neuzuweisung der Aktivierung
     */
    public void setGuiState(GUI_STATES state, boolean force) {
        if (guiState == state && !force)
            return;

        guiState = state;

        calcReliabilityBtn.setEnabled(state != GUI_STATES.CALCULATION_RUNNING);
        calcResilienceBtn.setEnabled(state != GUI_STATES.CALCULATION_RUNNING);

        reliabilitiesTabbedPane.setEnabled(state != GUI_STATES.CALCULATION_RUNNING);

        boolean reliabilityBoxesEnabled = (state != GUI_STATES.CALCULATION_RUNNING);
        Util.setChildrenEnabled(reliabilitiesTabbedPane, reliabilityBoxesEnabled);

        if (reliabilityBoxesEnabled && !calculationSeriesCheckBox.isSelected()) {
            edgeProbabilityStepSizeBox.setEnabled(false);
            nodeProbabilityStepSizeBox.setEnabled(false);
            edgeEndProbabilityBox.setEnabled(false);
            nodeEndProbabilityBox.setEnabled(false);
        }

        if (reliabilityBoxesEnabled && !differentForTerminalCheckBox.isSelected()) {
            terminalNodeProbBox.setEnabled(false);
        }
    }

    public RELIABILITY_MODES getReliabilityMode() {
        if (reliabilitiesTabbedPane.getSelectedIndex() == 0) {
            return RELIABILITY_MODES.SINGLE;
        } else {
            return RELIABILITY_MODES.SAME;
        }
    }

    public void setReliabilityMode(RELIABILITY_MODES mode) {
        if (mode == RELIABILITY_MODES.SAME) {
            reliabilitiesTabbedPane.setSelectedIndex(1);
        } else {
            reliabilitiesTabbedPane.setSelectedIndex(0);
        }
    }

    private void setResultText(String longText, String shortText) {
        outputField.setText(longText);
        statusBarCollapsedLabel.setText(shortText);

        //wenn der lange Text gesetzt ist, soll die StatusBar groß dargestellt werden
        setStatusBarCollapsed(longText.length() == 0);
    }

    public void setResultText(String text) {
        String s[] = text.split("\r\n|\r|\n");
        if (s.length > 1) {
            //Text ist mehrzeilig
            setResultText(text, "");
        } else {
            setResultText("", text);
        }
    }

    public void setStatusBarCollapsed(boolean collapsed) {
        expandedOutputPanel.setVisible(!collapsed);
        statusBarCollapsedLabel.setVisible(collapsed);
    }
    
    public void setHypergraphMode(boolean status){
    	hypergraphMode = status;
    	netPanel.getController().setHypergraphMode(hypergraphMode);
    }

    public int getLastSingleReliabilityComponentX() {
        int componentCount = singleReliabilitiesContainer.getComponentCount();
        if (componentCount == 0) {
            return 1;
        }

        JComponent lastComponent = (JComponent)
                singleReliabilitiesContainer.getComponent(componentCount - 1);
        GridBagLayout gbl = (GridBagLayout) singleReliabilitiesContainer.getLayout();

        return gbl.getConstraints(lastComponent).gridx;
    }

    public JPanel getContentPane() {
        return contentPane;
    }

    private JMenuBar getMenuBar() {
        return menuBar;
    }

    public JCheckBox getCalculationSeriesCheckBox() {
        return calculationSeriesCheckBox;
    }

    public JCheckBox getConsiderNodesBox() {
        return considerNodesBox;
    }

    public JCheckBox getConsiderEdgesBox() {
        return considerEdgesBox;
    }
    
    public JCheckBox getConsiderHyperEdgesBox(){
    	return considerHyperEdgesBox;
    }

    public JScrollPane getSingleReliabilitiesScrollPane() {
        return singleReliabilitiesScrollPane;
    }

    public ProbabilitySpinner getEdgeEndProbabilityBox() {
        return edgeEndProbabilityBox;
    }

    public ProbabilitySpinner getEdgeProbabilityStepSizeBox() {
        return edgeProbabilityStepSizeBox;
    }

    public ProbabilitySpinner getNodeEndProbabilityBox() {
        return nodeEndProbabilityBox;
    }

    public ProbabilitySpinner getNodeProbabilityStepSizeBox() {
        return nodeProbabilityStepSizeBox;
    }

    public ProbabilitySpinner getSameReliabilityEdgeProbBox() {
        return sameReliabilityEdgeProbBox;
    }

    public ProbabilitySpinner getSameReliabilityNodeProbBox() {
        return sameReliabilityNodeProbBox;
    }

    public JButton getCalcReliabilityBtn() {
        return calcReliabilityBtn;
    }

    public JButton getCalcResilienceBtn() {
        return calcResilienceBtn;
    }

    public JProgressBar getCalculationProgressBar() {
        return calculationProgressBar;
    }

    public JMenuItem getTutorialMenuItem() {
        return tutorialMenuItem;
    }

    public JMenuItem getAboutMenuItem() {
        return aboutMenuItem;
    }

    public JMenuItem getResetMenuItem() {
        return resetMenuItem;
    }

    public JMenuItem getOpenMenuItem() {
        return openMenuItem;
    }

    public JMenuItem getSaveMenuItem() {
        return saveMenuItem;
    }

    public JMenuItem getCloseMenuItem() {
        return closeMenuItem;
    }

    public JPanel getSingleReliabilitiesContainer() {
        return singleReliabilitiesContainer;
    }

    public NetPanel getNetPanel() {
        return netPanel;
    }

    public JButton getCollapseOutputBtn() {
        return collapseOutputBtn;
    }

    public JCheckBox getDifferentForTerminalCheckBox() {
        return differentForTerminalCheckBox;
    }

    public ProbabilitySpinner getTerminalNodeProbBox() {
        return terminalNodeProbBox;
    }

    public JMenuItem getUndoMenuItem() {
        return undoMenuItem;
    }

    public JMenuItem getRedoMenuItem() {
        return redoMenuItem;
    }

    public JMenuItem getCenterGraphMenuItem() {
        return centerGraphMenuItem;
    }

    public JMenuItem getAlignGraphMenuItem() {
        return alignGraphMenuItem;
    }
    
    public JMenuItem getCreateHypergraphGraphMenuItem(){
    	return hypergraphFileMenuItem;
    }
    
    public JMenuItem getCreateGraphGraphMenuItem(){
    	return graphFileMenuItem;
    }

    public JMenuItem getGenerateGraphMenuItem() {
        return generateGraphMenuItem;
    }
    
    public boolean getHypergraphMode(){
    	return hypergraphMode;
    }
    
    public JTabbedPane getTabbedPane(){
    	return reliabilitiesTabbedPane;
    }
    
    public JPanel getSidePanel(){
    	return sidePanel;
    }
    
    public JTabbedPane getHypergraphTabbedPane(){
    	return hypergraphTabbedPane;
    }
    
    public JPanel getCalculatePanel(){
    	return calculatePanel;
    }
    
    public JCheckBox getColoredHyperedgeCheckBox(){
    	return coloredHyperedgesBox;
    }
    
    public JLabel getAlphaValueLabel(){
    	return alphaLabel;
    }
    
    public JSpinner getAlphaValueSpinner(){
    	return alphaValueSpinner;
    }
    
    public JCheckBox getEllipseVisualisationCheckBox(){
    	return ellipseVisualisation;
    }
}
