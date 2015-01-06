/*
 * Base File from YFiles Java Demo
 * 
 * Unmodified
 * 
 */

/****************************************************************************
 **
 ** This file is part of yFiles-2.9.0.2. 
 ** 
 ** yWorks proprietary/confidential. Use is subject to license terms.
 **
 ** Redistribution of this file or of an unauthorized byte-code version
 ** of this file is strictly forbidden.
 **
 ** Copyright (c) 2000-2012 by yWorks GmbH, Vor dem Kreuzberg 28, 
 ** 72070 Tuebingen, Germany. All rights reserved.
 **
 ***************************************************************************/


import y.io.GraphMLIOHandler;
import y.io.IOHandler;
import y.module.YModule;
import y.option.DefaultEditorFactory;
import y.option.Editor;
import y.option.EditorFactory;
import y.option.GuiFactory;
import y.option.OptionHandler;
import y.util.D;
import y.view.AutoDragViewMode;
import y.view.CreateEdgeMode;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DPrinter;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.Graph2DViewMouseWheelZoomListener;
import y.view.MoveLabelMode;
import y.view.MovePortMode;
import y.view.MoveSelectionMode;
import y.view.MoveSnapContext;
import y.view.OrthogonalMoveBendsMode;
import y.view.HotSpotMode;
import y.view.TooltipMode;
import y.view.View2DConstants;
import y.view.DropSupport;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Abstract base class for GUI- and <code>Graph2DView</code>-based demos.
 * Provides useful callback methods. <p>To avoid problems with
 * "calls to overwritten method in constructor", do not initialize the demo
 * within the constructor of the subclass, use the method {@link #initialize()}
 * instead.</p>
 */
public abstract class Base {
  /**
   * The dimension of a small separator of the toolbar without divider line. This can be used to separate non-icon
   * components of toolbars.
   */
  public static final Dimension TOOLBAR_SMALL_SEPARATOR = new Dimension(3, 3);

  public static final Icon SHARED_LAYOUT_ICON = getIconResource("resource/layout.png");

  private static final Pattern PATH_SEPARATOR_PATTERN = Pattern.compile("/");

  /**
   * Initializes to a "nice" look and feel.
   */
  public static void initLnF() {
    Defaults.initLnF();
  }

  /**
   * The view component of this demo.
   */
  protected Graph2DView view;

  protected final JPanel contentPane;

  /**
   * This constructor creates the {@link #view} and calls,
   * {@link #createToolBar()} {@link #registerViewModes()},
   * {@link #registerViewActions()}, and {@link #registerViewListeners()}
   */
  protected Base() {
    view = createGraphView();
    configureDefaultRealizers();

    contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());

    initialize();

    registerViewModes();
    registerViewActions();

    contentPane.add(view, BorderLayout.CENTER);
    final JToolBar jtb = createToolBar();
    if (jtb != null) {
      contentPane.add(jtb, BorderLayout.NORTH);
    }

    registerViewListeners();
  }

  /**
   * Callback used by the default constructor {@link #DemoBase()} ()} to create the default graph view
   * @return an instance of {@link y.view.Graph2DView} with activated  "FitContentOnResize" behaviour.
   */
  protected Graph2DView createGraphView() {
    Graph2DView view = new Graph2DView();
    view.setFitContentOnResize(true);
    return view;
  }

  /**
   * Configures the default node realizer and default edge realizer used by subclasses
   * of this demo. The default implementation delegates to {@link Defaults#configureDefaultRealizers(Graph2DView)}.
   */
  protected void configureDefaultRealizers() {
    Defaults.configureDefaultRealizers(view);
  }

  /**
   * This method is called before the view modes and actions are registered and
   * the menu and toolbar is build.
   */
  protected void initialize() {
  }

  public void dispose() {
  }

  protected void loadGraph(URL resource) {

    if (resource == null) {
      String message = "Resource \"" + resource + "\" not found in classpath";
      D.showError(message);
      throw new RuntimeException(message);
    }

    try {
      IOHandler ioh = createGraphMLIOHandler();
      view.getGraph2D().clear();
      ioh.read(view.getGraph2D(), resource);
    } catch (IOException e) {
      String message = "Unexpected error while loading resource \"" + resource + "\" due to " + e.getMessage();
      D.bug(message);
      throw new RuntimeException(message, e);
    }
    view.getGraph2D().setURL(resource);
    view.fitContent();
    view.updateView();

  }

  protected GraphMLIOHandler createGraphMLIOHandler() {
    return new GraphMLIOHandler();
  }

  protected void loadGraph(Class aClass, String resourceString) {
    final URL resource = aClass.getResource(resourceString);
    if (resource == null) {
      String message = "Resource \"" + resourceString + "\" not found in classpath of " + aClass;
      D.showError(message);
      throw new RuntimeException(message);
    }
    loadGraph(resource);
  }

  protected void loadGraph(String resourceString) {
    loadGraph(getClass(), resourceString);
  }

  /**
   * Creates an application frame for this demo and displays it. The simple class name
   * is the title of the displayed frame.
   */
  public final void start() {
    start(getSimpleClassName());
  }

  /**
   * Returns the simple class name. This works only for top-level classes which shouldn't be a problem for demos.
   */
  private String getSimpleClassName() {
    final String className = getClass().getName();
    return className.substring(className.lastIndexOf('.') + 1);
  }

  /**
   * Creates an application frame for this demo and displays it. The given
   * string is the title of the displayed frame.
   */
  public final void start(String title) {
    JFrame frame = new JFrame(title);
    JOptionPane.setRootFrame(frame);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.addContentTo(frame.getRootPane());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  public void addContentTo(final JRootPane rootPane) {
    final JMenuBar jmb = createMenuBar();
    if (jmb != null) {
      rootPane.setJMenuBar(jmb);
    }
    rootPane.setContentPane(contentPane);
  }

  protected void addHelpPane( final String helpFilePath ) {
    if (helpFilePath != null) {
      final URL url = getClass().getResource(helpFilePath);
      if (url == null) {
        System.err.println("Could not locate help file: " + helpFilePath);
      } else {
        final JComponent helpPane = createHelpPane(url);
        if (helpPane != null) {
          contentPane.add(helpPane, BorderLayout.EAST);
        }
      }
    }
  }

  /**
   * Creates the application help pane.
   * @param helpURL the URL of the HTML help page to display.
   */
  protected JComponent createHelpPane( final URL helpURL ) {
    try {
      JEditorPane editorPane = new JEditorPane(helpURL);
      editorPane.setEditable(false);
      editorPane.setPreferredSize(new Dimension(250, 250));
      return new JScrollPane(editorPane);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  protected void registerViewActions() {
    // register keyboard actions
    Graph2DViewActions actions = new Graph2DViewActions(view);
    ActionMap amap = view.getCanvasComponent().getActionMap();
    if (amap != null) {
      InputMap imap = actions.createDefaultInputMap(amap);
      if (!isDeletionEnabled()) {
        amap.remove(Graph2DViewActions.DELETE_SELECTION);
      }
      view.getCanvasComponent().setInputMap(JComponent.WHEN_FOCUSED, imap);
    }
  }

  /**
   * Adds the view modes to the view. This implementation adds a new EditMode
   * created by {@link #createEditMode()}, a new TooltipMode created by {@link #createTooltipMode()}
   * and a new {@link AutoDragViewMode}.
   */
  protected void registerViewModes() {
    EditMode editMode = createEditMode();
    if (editMode != null) {
      view.addViewMode(editMode);
    }

    TooltipMode tooltipMode = createTooltipMode();
    if(tooltipMode != null) {
      view.addViewMode(tooltipMode);
    }

    view.addViewMode(new AutoDragViewMode());
  }

  /**
   * Callback used by {@link #registerViewModes()} to create the default
   * EditMode.
   *
   * @return an instance of {@link EditMode} with showNodeTips enabled.
   */
  protected EditMode createEditMode() {
    EditMode editMode = new EditMode();
    // show the highlighting which is turned off by default
    if (editMode.getCreateEdgeMode() instanceof CreateEdgeMode) {
      ((CreateEdgeMode) editMode.getCreateEdgeMode()).setIndicatingTargetNode(true);
    }
    if (editMode.getMovePortMode() instanceof MovePortMode) {
      ((MovePortMode) editMode.getMovePortMode()).setIndicatingTargetNode(true);
    }

    //allow moving view port with right drag gesture
    editMode.allowMovingWithPopup(true);

    return editMode;
  }

  /**
   * Callback used by {@link #registerViewModes()} to create a default
   * <code>TooltipMode</code>.
   *
   * @return an instance of {@link TooltipMode} where only node tooltips are enabled.
   */
  protected TooltipMode createTooltipMode() {
    TooltipMode tooltipMode = new TooltipMode();
    tooltipMode.setEdgeTipEnabled(false);
    return tooltipMode;
  }

  /**
   * Instantiates and registers the listeners for the view (e.g.
   * {@link y.view.Graph2DViewMouseWheelZoomListener}).
   */
  protected void registerViewListeners() {
    Graph2DViewMouseWheelZoomListener wheelZoomListener = new Graph2DViewMouseWheelZoomListener();
    //zoom in/out at mouse pointer location 
    wheelZoomListener.setCenterZooming(false);
    view.getCanvasComponent().addMouseWheelListener(wheelZoomListener);
  }

  /**
   * Determines whether default actions for deletions will be added to the view
   * and toolbar.
   */
  protected boolean isDeletionEnabled() {
    return true;
  }

  /**
   * Creates a toolbar for this demo.
   */
  protected JToolBar createToolBar() {
    JToolBar toolBar = new JToolBar();
    toolBar.add(new Zoom(1.25));
    toolBar.add(new Zoom(0.8));
    toolBar.add(new ResetZoom());
    toolBar.add(new FitContent(view));
    if (isDeletionEnabled()) {
      toolBar.add(createDeleteSelectionAction());
    }
    return toolBar;
  }

  /**
   * Create a menu bar for this demo.
   */
  protected JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu menu = new JMenu("File");
    Action action;
    action = createLoadAction();
    if (action != null) {
      menu.add(action);
    }
    action = createSaveAction();
    if (action != null) {
      menu.add(action);
    }
    menu.addSeparator();
    menu.add(new PrintAction());
    menu.addSeparator();
    menu.add(new ExitAction());
    menuBar.add(menu);
    
    if (getExampleResources() != null && getExampleResources().length != 0) {
      createExamplesMenu(menuBar);
    }
    
    return menuBar;
  }

  protected Action createLoadAction() {
    return new LoadAction();
  }

  protected Action createSaveAction() {
    return new SaveAction();
  }

  protected Action createDeleteSelectionAction() {
    return new DeleteSelection(view);
  }

  /**
   * Creates a menu to load the example graphs returned by {@link #getExampleResources} and adds it to the given menu
   * bar.
   * @param menuBar the menu bar to which the created menu is added.
   */
  protected void createExamplesMenu(JMenuBar menuBar) {
    final String[] fileNames = getExampleResources();
    if (fileNames == null) {
      return;
    }

    final JMenu menu = new JMenu("Example Graphs");
    menuBar.add(menu);

    for (int i = 0; i < fileNames.length; i++) {
      final String filename = fileNames[i];
      final String[] path = PATH_SEPARATOR_PATTERN.split(filename);
      menu.add(new AbstractAction(path[path.length - 1]) {
        public void actionPerformed(ActionEvent e) {
          loadGraph(filename);
        }
      });
    }
  }

  public JPanel getContentPane() {
    return contentPane;
  }

  /**
   * Returns the list of example graph resources of this demo.
   */
  protected String[] getExampleResources() {
    return null;
  }
  
  /**
   * Action that prints the contents of the view
   */
  public class PrintAction extends AbstractAction {
    PageFormat pageFormat;

    OptionHandler printOptions;

    public PrintAction() {
      super("Print");

      // setup option handler
      printOptions = new OptionHandler("Print Options");
      printOptions.addInt("Poster Rows", 1);
      printOptions.addInt("Poster Columns", 1);
      printOptions.addBool("Add Poster Coords", false);
      final String[] area = {"View", "Graph"};
      printOptions.addEnum("Clip Area", area, 1);
    }

    public void actionPerformed(ActionEvent e) {
      Graph2DPrinter gprinter = new Graph2DPrinter(view);

      // show custom print dialog and adopt values
      if (!printOptions.showEditor(view.getFrame())) {
        return;
      }
      gprinter.setPosterRows(printOptions.getInt("Poster Rows"));
      gprinter.setPosterColumns(printOptions.getInt("Poster Columns"));
      gprinter.setPrintPosterCoords(printOptions.getBool("Add Poster Coords"));
      if ("Graph".equals(printOptions.get("Clip Area"))) {
        gprinter.setClipType(Graph2DPrinter.CLIP_GRAPH);
      } else {
        gprinter.setClipType(Graph2DPrinter.CLIP_VIEW);
      }

      // show default print dialogs
      PrinterJob printJob = PrinterJob.getPrinterJob();
      if (pageFormat == null) {
        pageFormat = printJob.defaultPage();
      }
      PageFormat pf = printJob.pageDialog(pageFormat);
      if (pf == pageFormat) {
        return;
      } else {
        pageFormat = pf;
      }

      // setup print job.
      // Graph2DPrinter is of type Printable
      printJob.setPrintable(gprinter, pageFormat);

      if (printJob.printDialog()) {
        try {
          printJob.print();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  /**
   * Action that terminates the application
   */
  public static class ExitAction extends AbstractAction {
    public ExitAction() {
      super("Exit");
    }

    public void actionPerformed(ActionEvent e) {
      System.exit(0);
    }
  }

  /**
   * Action that saves the current graph to a file in GraphML format.
   */
  public class SaveAction extends AbstractAction {
    JFileChooser chooser;

    public SaveAction() {
      super("Save...");
      chooser = null;
    }

    public void actionPerformed(ActionEvent e) {
      if (chooser == null) {
        chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".graphml");
          }

          public String getDescription() {
            return "GraphML Format (.graphml)";
          }
        });
      }

      URL url = view.getGraph2D().getURL();
      if (url != null && "file".equals(url.getProtocol())) {
        try {
          chooser.setSelectedFile(new File(new URI(url.toString())));
        } catch (URISyntaxException e1) {
          // ignore
        }
      }

      if (chooser.showSaveDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
        String name = chooser.getSelectedFile().toString();
        if(!name.endsWith(".graphml")) {
          name += ".graphml";
        }
        IOHandler ioh = createGraphMLIOHandler();

        try {
          ioh.write(view.getGraph2D(), name);
        } catch (IOException ioe) {
          D.show(ioe);
        }
      }
    }
  }

  /**
   * Action that loads the current graph from a file in GraphML format.
   */
  public class LoadAction extends AbstractAction {
    JFileChooser chooser;

    public LoadAction() {
      super("Load...");
      chooser = null;
    }

    public void actionPerformed(ActionEvent e) {
      if (chooser == null) {
        chooser = new JFileChooser();
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.isDirectory() || f.getName().endsWith(".graphml");
          }

          public String getDescription() {
            return "GraphML Format (.graphml)";
          }
        });
      }
      if (chooser.showOpenDialog(contentPane) == JFileChooser.APPROVE_OPTION) {
        URL resource = null;
        try {
          resource = chooser.getSelectedFile().toURI().toURL();
        } catch (MalformedURLException urlex) {
          urlex.printStackTrace();
        }
        loadGraph(resource);
      }
    }
  }

  /**
   * Action that deletes the selected parts of the graph.
   */
  public static class DeleteSelection extends AbstractAction {
    private final Graph2DView view;

    public DeleteSelection(final Graph2DView view) {
      super("Delete Selection");
      this.view = view;
      this.putValue(Action.SMALL_ICON, getIconResource("resource/delete.png"));
      this.putValue(Action.SHORT_DESCRIPTION, "Delete Selection");
    }

    public void actionPerformed(ActionEvent e) {
      view.getGraph2D().removeSelection();
      view.getGraph2D().updateViews();
    }
  }

  /**
   * Action that resets the view's zoom level to <code>1.0</code>.
   */
  public class ResetZoom extends AbstractAction {
    public ResetZoom() {
      super("Reset Zoom");
      this.putValue(Action.SMALL_ICON, getIconResource("resource/zoomOriginal.png"));
      this.putValue(Action.SHORT_DESCRIPTION, "Reset Zoom");
    }

    public void actionPerformed( final ActionEvent e ) {
      view.setZoom(1.0);
      // optional code that adjusts the size of the
      // view's world rectangle. The world rectangle
      // defines the region of the canvas that is
      // accessible by using the scroll bars of the view.
      Rectangle box = view.getGraph2D().getBoundingBox();
      view.setWorldRect(box.x - 20, box.y - 20, box.width + 40, box.height + 40);

      view.updateView();
    }
  }

  /**
   * Action that applies a specified zoom level to the view.
   */
  public class Zoom extends AbstractAction {
    double factor;

    public Zoom(double factor) {
      super("Zoom " + (factor > 1.0 ? "In" : "Out"));
      String resource = factor > 1.0d ? "resource/zoomIn.png" : "resource/zoomOut.png";
      this.putValue(Action.SMALL_ICON, getIconResource(resource));
      this.putValue(Action.SHORT_DESCRIPTION, "Zoom " + (factor > 1.0 ? "In" : "Out"));
      this.factor = factor;
    }

    public void actionPerformed(ActionEvent e) {
      view.setZoom(view.getZoom() * factor);
      // optional code that adjusts the size of the
      // view's world rectangle. The world rectangle
      // defines the region of the canvas that is
      // accessible by using the scroll bars of the view.
      Rectangle box = view.getGraph2D().getBoundingBox();
      view.setWorldRect(box.x - 20, box.y - 20, box.width + 40, box.height + 40);

      view.updateView();
    }
  }

  /**
   * Action that fits the content nicely inside the view.
   */
  public static class FitContent extends AbstractAction {
    private final Graph2DView view;

    public FitContent(final Graph2DView view) {
      super("Fit Content");
      this.view = view;
      this.putValue(Action.SMALL_ICON, getIconResource("resource/zoomFit.png"));
      this.putValue(Action.SHORT_DESCRIPTION, "Fit Content");
    }

    public void actionPerformed(ActionEvent e) {
      view.fitContent();
      view.updateView();
    }
  }

  /**
   * This is a convenience class which configures a view, a drop support and/or an edit mode for
   * snapping and/or grid snapping.<br>
   * Note that {@link Graph2DView#setGridMode(boolean) enabling the grid on the view} has the effect that nodes
   * can only be placed on grid positions, thus it prevents the other snapping rules from being applied. However,
   * the {@link MoveSnapContext#setUsingGridSnapping(boolean) newer grid snapping feature} which is used here
   * coexists nicely with other snapping rules.
   */
  public static class SnappingConfiguration {
    private boolean snappingEnabled;
    private double snapDistance;
    private double snapLineExtension;
    private Color snapLineColor;
    private boolean removingInnerBends;

    private double nodeToNodeDistance;
    private double nodeToEdgeDistance;
    private double edgeToEdgeDistance;

    private boolean gridSnappingEnabled;
    private double gridSnapDistance;
    private double gridDistance;
    private int gridType;


    public double getEdgeToEdgeDistance() {
      return edgeToEdgeDistance;
    }

    public void setEdgeToEdgeDistance(double edgeToEdgeDistance) {
      this.edgeToEdgeDistance = edgeToEdgeDistance;
    }

    public double getGridSnapDistance() {
      return gridSnapDistance;
    }

    public void setGridSnapDistance(double gridSnapDistance) {
      this.gridSnapDistance = gridSnapDistance;
    }

    public boolean isGridSnappingEnabled() {
      return gridSnappingEnabled;
    }

    public void setGridSnappingEnabled(boolean gridSnappingEnabled) {
      this.gridSnappingEnabled = gridSnappingEnabled;
    }

    public double getNodeToEdgeDistance() {
      return nodeToEdgeDistance;
    }

    public void setNodeToEdgeDistance(double nodeToEdgeDistance) {
      this.nodeToEdgeDistance = nodeToEdgeDistance;
    }

    public double getNodeToNodeDistance() {
      return nodeToNodeDistance;
    }

    public void setNodeToNodeDistance(double nodeToNodeDistance) {
      this.nodeToNodeDistance = nodeToNodeDistance;
    }

    public boolean isRemovingInnerBends() {
      return removingInnerBends;
    }

    public void setRemovingInnerBends(boolean removingInnerBends) {
      this.removingInnerBends = removingInnerBends;
    }

    public double getSnapDistance() {
      return snapDistance;
    }

    public void setSnapDistance(double snapDistance) {
      this.snapDistance = snapDistance;
    }

    public Color getSnapLineColor() {
      return snapLineColor;
    }

    public void setSnapLineColor(Color snapLineColor) {
      this.snapLineColor = snapLineColor;
    }

    public double getSnapLineExtension() {
      return snapLineExtension;
    }

    public void setSnapLineExtension(double snapLineExtension) {
      this.snapLineExtension = snapLineExtension;
    }

    public boolean isSnappingEnabled() {
      return snappingEnabled;
    }

    public void setSnappingEnabled(boolean snappingEnabled) {
      this.snappingEnabled = snappingEnabled;
    }

    public double getGridDistance() {
      return gridDistance;
    }

    public void setGridDistance(double gridDistance) {
      this.gridDistance = gridDistance;
    }

    public int getGridType() {
      return gridType;
    }

    public void setGridType(int gridType) {
      this.gridType = gridType;
    }

    public void configureView(final Graph2DView view) {
      // Do not use the grid mode of the view. Use grid snapping instead (see configureSnapContext()).
      view.setGridMode(false);

      if (isGridSnappingEnabled()) {
        // Use the normal grid for the display.
        view.setGridVisible(true);
        view.setGridResolution(getGridDistance());
        view.setGridType(getGridType());
        view.setGridColor(getSnapLineColor());
      } else {
        view.setGridVisible(false);
      }

      view.updateView();
    }

    public void configureDropSupport(final DropSupport dropSupport) {
      dropSupport.setSnappingEnabled(isSnappingEnabled());
      configureSnapContext(dropSupport.getSnapContext());
    }

    /**
     * Configures the snap context of the given <code>EditMode</code> and its children according to the given
     * parameters.
     *
     * @noinspection JavaDoc
     */
    public void configureEditMode(final EditMode editMode) {
      if (editMode.getHotSpotMode() instanceof HotSpotMode) {
        HotSpotMode hotSpotMode = (HotSpotMode) editMode.getHotSpotMode();
        hotSpotMode.setSnappingEnabled(isSnappingEnabled());
        hotSpotMode.getSnapContext().setSnapLineColor(getSnapLineColor());
      }
      {
        MoveSelectionMode moveSelectionMode = (MoveSelectionMode) editMode.getMoveSelectionMode();
        MoveSnapContext snapContext = moveSelectionMode.getSnapContext();
        moveSelectionMode.setSnappingEnabled(isGridSnappingEnabled() || isSnappingEnabled());

        configureSnapContext(snapContext);
        moveSelectionMode.setRemovingInnerBends(isRemovingInnerBends());
      }
      {
        OrthogonalMoveBendsMode moveBendsMode = (OrthogonalMoveBendsMode) editMode.getOrthogonalMoveBendsMode();
        MoveSnapContext snapContext = moveBendsMode.getSnapContext();
        moveBendsMode.setSnappingEnabled(isGridSnappingEnabled() || isSnappingEnabled());

        configureSnapContext(snapContext);
        moveBendsMode.setRemovingInnerBends(isRemovingInnerBends());
      }
      {
        CreateEdgeMode createEdgeMode = (CreateEdgeMode) editMode.getCreateEdgeMode();
        MoveSnapContext snapContext = createEdgeMode.getSnapContext();
        if (isSnappingEnabled()) {
          createEdgeMode.setSnapToOrthogonalSegmentsDistance(5.0);
          createEdgeMode.setUsingNodeCenterSnapping(true);
          createEdgeMode.setSnappingOrthogonalSegments(true);
        } else {
          createEdgeMode.setSnapToOrthogonalSegmentsDistance(0.0);
          createEdgeMode.setUsingNodeCenterSnapping(false);
          createEdgeMode.setSnappingOrthogonalSegments(false);
        }

        configureSnapContext(snapContext);
      }
      if (editMode.getMovePortMode() instanceof MovePortMode) {
        MovePortMode movePortMode = (MovePortMode) editMode.getMovePortMode();
        movePortMode.setUsingRealizerPortCandidates(!isSnappingEnabled());
        movePortMode.setSegmentSnappingEnabled(isSnappingEnabled());
        MoveSnapContext snapContext = movePortMode.getSnapContext();
        configureSnapContext(snapContext);
      }
      if(editMode.getMoveLabelMode() instanceof MoveLabelMode) {
        final MoveLabelMode moveLabelMode = (MoveLabelMode) editMode.getMoveLabelMode();
        moveLabelMode.setSnappingEnabled(isSnappingEnabled());
      }
    }

    /**
     * Configures the given <code>MoveSnapContext</code>.
     *
     * @noinspection JavaDoc
     */
    public void configureSnapContext(final MoveSnapContext snapContext) {

      snapContext.setSnapLineColor(getSnapLineColor());
      snapContext.setSnapDistance(getSnapDistance());
      snapContext.setGridSnapDistance(getGridSnapDistance());
      snapContext.setSnapLineExtension(getSnapLineExtension());

      snapContext.setUsingGridSnapping(isGridSnappingEnabled());
      snapContext.setRenderingSnapLines(isGridSnappingEnabled() || isSnappingEnabled());

      if (isGridSnappingEnabled() && !isSnappingEnabled()) {
        snapContext.setSnappingBendsToSnapLines(false);
        snapContext.setSnappingSegmentsToSnapLines(false);
        snapContext.setUsingCenterSnapLines(false);
        snapContext.setUsingEquidistantSnapLines(false);
        snapContext.setUsingFixedNodeSnapLines(false);
        snapContext.setUsingOrthogonalBendSnapping(false);
        snapContext.setUsingOrthogonalMovementConstraints(false);
        snapContext.setUsingOrthogonalPortSnapping(false);
        snapContext.setUsingSegmentSnapLines(false);

        // Use "null" values if just the grid, but no snap lines are enabled.
        snapContext.setEdgeToEdgeDistance(0.0);
        snapContext.setNodeToEdgeDistance(-1.0);
        snapContext.setNodeToNodeDistance(0.0);
      } else {
        snapContext.setSnappingBendsToSnapLines(false);
        snapContext.setSnappingSegmentsToSnapLines(true);
        snapContext.setUsingCenterSnapLines(false);
        snapContext.setUsingEquidistantSnapLines(true);
        snapContext.setUsingFixedNodeSnapLines(true);
        snapContext.setUsingOrthogonalBendSnapping(true);
        snapContext.setUsingOrthogonalMovementConstraints(true);
        snapContext.setUsingOrthogonalPortSnapping(true);
        snapContext.setUsingSegmentSnapLines(true);

        snapContext.setEdgeToEdgeDistance(getEdgeToEdgeDistance());
        snapContext.setNodeToEdgeDistance(getNodeToEdgeDistance());
        snapContext.setNodeToNodeDistance(getNodeToNodeDistance());
      }
    }
  }

  /**
   * @return a configuration for snapping with default parameters.
   */
  public static SnappingConfiguration createDefaultSnappingConfiguration() {
    SnappingConfiguration result = new SnappingConfiguration();
    result.setSnappingEnabled(true);
    result.setGridSnappingEnabled(false);
    result.setSnapLineColor(Color.LIGHT_GRAY);
    result.setRemovingInnerBends(true);
    result.setNodeToNodeDistance(30.0);
    result.setNodeToEdgeDistance(20.0);
    result.setEdgeToEdgeDistance(20.0);
    result.setSnapDistance(5.0);
    result.setGridSnapDistance(10.0);
    result.setSnapLineExtension(40.0);
    result.setGridDistance(50.0);
    result.setGridType(View2DConstants.GRID_CROSS);
    return result;
  }

  /**
   * Creates a control for triggering the specified action from the demo toolbars and displays the action's text and
   * icon, if present.
   *
   * @param action the <code>Action</code> that is triggered by the created control.
   * @return a control for triggering the specified action from the demo toolbars.
   */
  public static JComponent createActionControl(final Action action) {
    return createActionControl(action, true);
  }

  /**
   * Creates a control for triggering the specified action from the demo toolbars.
   *
   * @param action         the <code>Action</code> that is triggered by the created control.
   * @param showActionText <code>true</code> if the control should display the action name; <code>false</code>
   *                       otherwise.
   * @return a control for triggering the specified action from the demo toolbars.
   */
  public static JComponent createActionControl(final Action action, final boolean showActionText) {
    final JButton jb = new JButton();
    if (action.getValue(Action.SMALL_ICON) != null) {
      jb.putClientProperty("hideActionText", Boolean.valueOf(!showActionText));
    }
    jb.setAction(action);
    return jb;
  }

  /**
   * Finds an <code>Icon</code> resource with the specified name.
   * This methods uses
   * <blockquote>
   * <code>DemoBase.class.getResource(resource)</code>
   * </blockquote>
   * to resolve resources.
   * @param resource the name/path of the desired resource.
   * @return the resolved <code>Icon</code> of <code>null</code> if no icon
   * with the specified name could be found.
   */
  public static Icon getIconResource( final String resource ) {
    try {
      final URL icon = Base.class.getResource(resource);
      if (icon == null) {
        return null;
      } else {
        return new ImageIcon(icon);
      }
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Creates dialogs for OptionHandlers.
   */
  public static class OptionSupport {
    private final EditorFactory editorFactory;

    /**
     * Displays the OptionHandler of the given module in a dialog and runs the module on the given graph if either the
     * <code>Ok</code> or <code>Apply</code> button were pressed. The creation of the dialog is delegated to {@link
     * #createDialog(y.option.OptionHandler, java.awt.event.ActionListener, java.awt.Frame)}
     *
     * @param module the option handler.
     * @param graph  an optional ActionListener that is added to each button of the dialog.
     * @param owner  the owner of the created dialog.
     */
    public static void showDialog(final YModule module, final Graph2D graph, final boolean runOnOk, Frame owner) {
      if (module.getOptionHandler() == null) {
        module.start(graph);
      } else {
        final ActionListener listener = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            module.start(graph);
          }
        };

        showDialog(module.getOptionHandler(), listener, runOnOk, owner);
      }
    }

    /**
     * Displays the given OptionHandler in a dialog and invokes the given ActionListener if either the <code>Ok</code>
     * or <code>Apply</code> button were pressed. The creation of the dialog is delegated to {@link
     * #createDialog(y.option.OptionHandler, java.awt.event.ActionListener, java.awt.Frame)}
     *
     * @param oh       the option handler.
     * @param listener the ActionListener that is invoked if either the <code>Ok</code> or <code>Apply</code> button
     *                 were pressed.
     * @param owner    the owner of the created dialog.
     */
    public static void showDialog(final OptionHandler oh, final ActionListener listener,
                                  final boolean runOnOk, Frame owner) {
      final ActionListener delegatingListener = new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          final String actionCommand = e.getActionCommand();
          if ("Apply".equals(actionCommand) || (runOnOk && "Ok".equals(actionCommand))) {
            EventQueue.invokeLater(new Runnable() {
              public void run() {
                listener.actionPerformed(e);
              }
            });
          }
        }
      };

      final JDialog dialog = new OptionSupport().createDialog(oh, delegatingListener, owner);
      dialog.setVisible(true);
    }

    public OptionSupport() {
      editorFactory = new DefaultEditorFactory();
    }

    /**
     * Returns an dialog for the editor of the given option handler which contains four control buttons: Ok, Apply,
     * Reset, and Cancel. An optional ActionListener can be used to add custom behavior to these buttons.
     *
     * @param oh       the option handler.
     * @param listener an optional ActionListener that is added to each button of the dialog.
     * @param owner    the owner of the created dialog.
     */
    public JDialog createDialog(final OptionHandler oh, ActionListener listener, Frame owner) {
      final JDialog dialog = new JDialog(owner, getTitle(oh), true);

      final AbstractAction applyAction = new AbstractAction("Apply") {
        public void actionPerformed(ActionEvent e) {
          if (oh.checkValues()) {
            oh.commitValues();
          }
        }
      };
      final JButton applyButton = createButton(applyAction, listener);
      applyButton.setDefaultCapable(true);
      applyButton.requestFocus();

      final AbstractAction closeAction = new AbstractAction("Cancel") {
        public void actionPerformed(ActionEvent e) {
          dialog.dispose();
        }
      };

      final AbstractAction okAction = new AbstractAction("Ok") {
        public void actionPerformed(ActionEvent e) {
          if (oh.checkValues()) {
            oh.commitValues();
            dialog.dispose();
          }
        }
      };

      final AbstractAction resetAction = new AbstractAction("Reset") {
        public void actionPerformed(ActionEvent e) {
          oh.resetValues();
        }
      };

      final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
      buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 11, 5));
      buttonPanel.add(createButton(okAction, listener));
      buttonPanel.add(createButton(closeAction, listener));
      buttonPanel.add(applyButton);
      buttonPanel.add(createButton(resetAction, listener));

      final Editor editor = getEditorFactory().createEditor(oh);
      final JComponent editorComponent = editor.getComponent();
      if (editorComponent instanceof JPanel) {
        editorComponent.setBorder(BorderFactory.createEmptyBorder(11, 5, 5, 5));
      } else {
        editorComponent.setBorder(BorderFactory.createEmptyBorder(9, 9, 15, 9));
      }

      final JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
      contentPanel.add(editorComponent, BorderLayout.CENTER);
      contentPanel.add(buttonPanel, BorderLayout.SOUTH);

      dialog.setContentPane(contentPanel);
      dialog.getRootPane().getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "APPLY_ACTION");
      dialog.getRootPane().getActionMap().put("APPLY_ACTION", applyAction);
      dialog.getRootPane().getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "CANCEL_ACTION");
      dialog.getRootPane().getActionMap().put("CANCEL_ACTION", closeAction);
      dialog.getRootPane().setDefaultButton(applyButton);
      dialog.pack();
      dialog.setSize(dialog.getPreferredSize());
      dialog.setResizable(false);
      dialog.setLocationRelativeTo(owner);

      return dialog;
    }

    protected EditorFactory getEditorFactory() {
      return editorFactory;
    }

    String getTitle(OptionHandler oh) {
      final Object title = oh.getAttribute(OptionHandler.ATTRIBUTE_TITLE);
      if (title instanceof String) {
        return (String) title;
      }

      final Object titleKey = oh.getAttribute(OptionHandler.ATTRIBUTE_TITLE_KEY);
      if (titleKey instanceof String) {
        return getTitle(oh, (String) titleKey);
      } else {
        return getTitle(oh, oh.getName());
      }
    }

    static String getTitle(OptionHandler oh, String title) {
      final GuiFactory guiFactory = oh.getGuiFactory();
      return guiFactory == null ? title : guiFactory.getString(title);
    }

    static JButton createButton(AbstractAction action, ActionListener listener) {
      final JButton button = new JButton(action);
      button.addActionListener(listener);
      return button;

    }
  }
}
