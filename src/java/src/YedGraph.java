/*
 * Author: Peijin Zhang
 * 
 * Modified from YFiles Yed Demo
 * 
 */

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.EdgeMap;
import y.base.Node;
import y.io.GraphMLIOHandler;
import y.io.IOHandler;
import y.io.ImageOutputHandler;
import y.io.JPGIOHandler;
import y.io.ViewPortConfigurator;
import y.io.YGFIOHandler;
import y.layout.organic.OrganicLayouter;
import y.module.GRIPModule;
import y.module.OrganicEdgeRouterModule;
import y.module.OrganicLayoutModule;
import y.module.SmartOrganicLayoutModule;
import y.module.YModule;
import y.option.OptionHandler;
import y.view.DefaultGraph2DRenderer;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.PopupMode;
import y.view.YLabel;
import yext.export.io.EMFOutputHandler;
import yext.export.io.PDFOutputHandler;
import yext.export.io.SWFOutputHandler;
import yext.svg.io.SVGIOHandler;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
public class YedGraph extends Base 
{
	private EdgeMap preferredEdgeLengthMap;
	private YModule module;
	public File input;

	public YedGraph(File input) throws Exception
	{
		this.input = input;
		preferredEdgeLengthMap = view.getGraph2D().createEdgeMap();
		view.getGraph2D().addDataProvider(OrganicLayouter.PREFERRED_EDGE_LENGTH_DATA, preferredEdgeLengthMap);
		
		//Default layout to Smart Organic
		module = new SmartOrganicLayoutModule();
		Defaults.applyRealizerDefaults(view.getGraph2D(), true, true);
		
		Graph2D graph = view.getGraph2D();
		
		//Default edge is gray
		EdgeRealizer er = graph.getDefaultEdgeRealizer();
		er.setLineColor(Color.GRAY);
		graph.setDefaultEdgeRealizer(er);
		//er.setTargetArrow(Arrow.NONE);
		
		//Default node is blue
		NodeRealizer nr = graph.getDefaultNodeRealizer();
		nr.setFillColor(new Color(145, 200, 255));
		graph.setDefaultNodeRealizer(nr);
		
		// Organic Layout options
		OptionHandler options = module.getOptionHandler();
		options.set("VISUAL", "PREFERRED_EDGE_LENGTH", 50);
		options.set("VISUAL", "ALLOW_NODE_OVERLAPS", false);
		options.set("VISUAL", "MINIMAL_NODE_DISTANCE", 5.0);
		options.set("VISUAL", "AVOID_NODE_EDGE_OVERLAPS", true);
		options.set("VISUAL", "COMPACTNESS", 0.4);
		
		DefaultGraph2DRenderer g2dr = new DefaultGraph2DRenderer();
		g2dr.setDrawEdgesFirst(true);
		view.setGraph2DRenderer(g2dr);
		
		loadGraph();
	}
	
	public void loadGraph() throws Exception
	{
		Graph2D graph = view.getGraph2D();
		BufferedReader in = new BufferedReader(new FileReader(input));
		
		ArrayList<Node> nodes = new ArrayList<Node>();
		ArrayList<String> indecies = new ArrayList<String>();
		
		Font font = new Font("Dialog", Font.PLAIN, 12);  
        FontMetrics metrics = new FontMetrics(font){};

		String nextLine = in.readLine();
		
		//Load nodes and edges from edge list
		
		while(nextLine != null && nextLine.length() > 1)
		{
			String extension = input.getName().substring(input.getName().lastIndexOf(".") + 1).toLowerCase();
			String[] tempnodes;
			if(extension.equals("txt"))
				tempnodes = nextLine.split(">");
			else
				tempnodes = nextLine.split(",");
			String node1 = tempnodes[0].trim();
			String node2 = tempnodes[1].trim();
			Node a, b;
			if(!indecies.contains(node1))
			{
				indecies.add(node1);
				a = graph.createNode(0, 0, metrics.getStringBounds(node1, null).getBounds().getWidth() + 15, 30, node1);
				nodes.add(a);
			}
			else
			{
				a = nodes.get(indecies.indexOf(node1));
			}
			if(!indecies.contains(node2))
			{
				indecies.add(node2);
				b = graph.createNode(0, 0, metrics.getStringBounds(node2, null).getBounds().getWidth() + 15, 30, node2);
				nodes.add(b);
			}
			else
			{
				b = nodes.get(indecies.indexOf(node2));
			}
			graph.createEdge(a, b);
			nextLine = in.readLine();
		}
		
		// Find parent node and make it bigger as well as change background color to gray
		for(int x = 0; x < nodes.size(); x++)
		{
			if(nodes.get(x).inDegree() == 0)
			{
				NodeRealizer nr = graph.getRealizer(nodes.get(x));
				nr.setWidth(nr.getWidth() + 15);
				nr.setHeight(nr.getHeight() + 15);
				nr.setFillColor(new Color(200, 200, 200));
				NodeLabel label = nr.getLabel();
				label.setFontStyle(Font.BOLD);
				label.setFontSize(18);
				nr.setLabel(label);
				graph.setRealizer(nodes.get(x), nr);
			}
		}
		
		for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) 
		{
			Edge edge = ec.edge();
			String eLabel = graph.getLabelText(edge);
			preferredEdgeLengthMap.set(edge, null);
			try 
			{
				preferredEdgeLengthMap.setInt(edge, (int) Double.parseDouble(eLabel));
			}
			catch (Exception ex) 
			{
			}
		}
		module.start(view.getGraph2D());
	}
	
	// GUI 

	protected void registerViewModes() 
	{
		EditMode editMode = new EditMode();
		view.addViewMode(editMode);

		editMode.setPopupMode(new PopupMode() {
			public JPopupMenu getEdgePopup(Edge e) {
				JPopupMenu pm = new JPopupMenu();
				pm.add(new EditLabel(e));
				return pm;
			}
		});
	}

	protected JToolBar createToolBar() {
		JToolBar bar = super.createToolBar();
		bar.add(createActionControl(new LayoutAction()));
		bar.add(createActionControl(new OptionAction()));
		bar.add(new Save(view.getGraph2D(), this));
		return bar;
	}

	protected JMenuBar createMenuBar() {
		JMenuBar mb = super.createMenuBar();
		JMenu layoutMenu = new JMenu("Style");
		ButtonGroup bg = new ButtonGroup();
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				module = new OrganicLayoutModule();
			}
		};
		JRadioButtonMenuItem item = new JRadioButtonMenuItem("Classic");
		item.addActionListener(listener);
		bg.add(item);
		layoutMenu.add(item);
		listener = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				module = new SmartOrganicLayoutModule();
			}
		};
		item = new JRadioButtonMenuItem("Smart");
		item.addActionListener(listener);
		item.setSelected(true);
		bg.add(item);
		layoutMenu.add(item);

		listener = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				module = new GRIPModule();
			}
		};
		item = new JRadioButtonMenuItem("GRIP");
		item.addActionListener(listener);
		bg.add(item);
		layoutMenu.add(item);
		listener = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				module = new OrganicEdgeRouterModule();
			}
		};
		item = new JRadioButtonMenuItem("EdgeRouting");
		item.addActionListener(listener);
		bg.add(item);
		layoutMenu.add(item);
		mb.add(layoutMenu);
		return mb;
	}
	
	// Manual save to JPG and GraphML
	protected static class Save extends AbstractAction 
	{
		Graph2D graph;
		YedGraph yedgraph;

		public Save(Graph2D graph, YedGraph yedgraph) 
		{
			super("Save");
			this.graph = graph;
			this.yedgraph = yedgraph;
			this.putValue(Action.SHORT_DESCRIPTION, "Save");
		}

		public void actionPerformed(ActionEvent e) 
		{
			JFileChooser save = new JFileChooser();
			save.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnVal = save.showOpenDialog(yedgraph.contentPane);
			File outputdirectory = new File("");
			if (returnVal == JFileChooser.APPROVE_OPTION) 
			{
				outputdirectory = save.getSelectedFile();
				for(int x = 0; x < 7; x++)
				{
					switch(x)
					{
						case 0:		yedgraph.saveGraphML(new File(outputdirectory, YedRunner.filename(yedgraph.input.getName(), x))); break;
						case 1:		yedgraph.saveYGF(new File(outputdirectory, YedRunner.filename(yedgraph.input.getName(), x))); break;
						case 2:		yedgraph.saveJPG(new File(outputdirectory, YedRunner.filename(yedgraph.input.getName(), x))); break;
						case 3:		yedgraph.saveSVG(new File(outputdirectory, YedRunner.filename(yedgraph.input.getName(), x))); break;
						case 4:		yedgraph.savePDF(new File(outputdirectory, YedRunner.filename(yedgraph.input.getName(), x))); break;
						case 5:		yedgraph.saveSWF(new File(outputdirectory, YedRunner.filename(yedgraph.input.getName(), x))); break;
						case 6:		yedgraph.saveEMF(new File(outputdirectory, YedRunner.filename(yedgraph.input.getName(), x)));
					}
				}
				yedgraph.saveJPG(new File(outputdirectory, yedgraph.input.getName().substring(0, yedgraph.input.getName().lastIndexOf(".")) + ".jpg"));
				yedgraph.saveGraphML(new File(outputdirectory, yedgraph.input.getName().substring(0, yedgraph.input.getName().lastIndexOf(".")) + ".graphml"));
				
				JOptionPane.showMessageDialog(new JFrame(), "Files Saved", "Message", JOptionPane.PLAIN_MESSAGE);
			}
	
		}
	}

	public void writeGraphToFile(Graph2D graph, IOHandler ioh, File outFile)
	{
		try 
		{
			ioh.write(graph, new FileOutputStream(outFile));
		}
		catch (IOException ioEx) 
		{
			System.out.println("Cannot write graph to file '" + outFile + "'.");
		}
	}

	public void exportGraphToImageFileFormat(Graph2D graph, ImageOutputHandler ioh, File outFile)
	{
		Graph2DView originalView = replaceCurrentWithExportView(graph, ioh);
		configureExportView((Graph2DView)graph.getCurrentView());
		writeGraphToFile(graph, ioh, outFile);
		restoreOriginalView(graph, originalView);
	}

	public Graph2DView replaceCurrentWithExportView(Graph2D graph, ImageOutputHandler ioh)
	{
		Graph2DView originalView = (Graph2DView)graph.getCurrentView();
		Graph2DView exportView = ioh.createDefaultGraph2DView(graph);
		exportView.setGraph2DRenderer(originalView.getGraph2DRenderer());
		exportView.setRenderingHints(originalView.getRenderingHints());
		graph.setCurrentView(exportView);
		return originalView;
	}

	public void restoreOriginalView(Graph2D graph, Graph2DView originalView)
	{
		graph.removeView(graph.getCurrentView());
		graph.setCurrentView(originalView);
	}

	public void configureExportView(Graph2DView exportView)
	{
		ViewPortConfigurator vpc = new ViewPortConfigurator();
		vpc.setGraph2D(exportView.getGraph2D());
		vpc.setClipType(ViewPortConfigurator.CLIP_GRAPH);
		vpc.setMargin(100);
		vpc.setScalingFactor(2);
		vpc.configure(exportView);
	}

	//Saving methods
	
	public void saveJPG(File file)
	{
		JPGIOHandler ioh = new JPGIOHandler();
		((JPGIOHandler)ioh).setQuality(1f);
		exportGraphToImageFileFormat(view.getGraph2D(), ioh, file);
	}
	
	public void saveGraphML(File file)
	{
		try 
		{
			IOHandler ioh = new GraphMLIOHandler();
			ioh.write(view.getGraph2D(), new FileOutputStream(file));
		}
		catch (IOException ioEx) 
		{
			
		}
	}
	
	public void saveYGF(File file)
	{
		try 
		{
			IOHandler ioh = new YGFIOHandler();
			ioh.write(view.getGraph2D(), new FileOutputStream(file));
		}
		catch (IOException ioEx) 
		{
			
		}
	}
	
	public void saveSVG(File file)
	{
		try 
		{	
			IOHandler ioh = new SVGIOHandler();
			double tmpPDT = view.getPaintDetailThreshold();
			view.setPaintDetailThreshold(0.0);
			ioh.write(view.getGraph2D(), new FileOutputStream(file));
			view.setPaintDetailThreshold(tmpPDT);
		}
		catch (IOException ioEx) 
		{
			
		}
	}
	
	public void savePDF(File file)
	{
		try
		{
			IOHandler ioh = new PDFOutputHandler();
			ioh.write(view.getGraph2D(), new FileOutputStream(file));
		}
		catch (IOException ioEx)
		{
			
		}

	}
	
	public void saveSWF(File file)
	{
		try
		{
			IOHandler ioh = new SWFOutputHandler();
			ioh.write(view.getGraph2D(), new FileOutputStream(file));
		}
		catch (IOException ioEx)
		{
			
		}
	}
	
	public void saveEMF(File file)
	{
		try
		{
			IOHandler ioh = new EMFOutputHandler();
			ioh.write(view.getGraph2D(), new FileOutputStream(file));
		}
		catch (IOException ioEx)
		{
			
		}
	}

	
	class OptionAction extends AbstractAction {
		OptionAction() {
			super("Settings...", getIconResource("resource/Properties.png"));
		}

		public void actionPerformed(ActionEvent e) {
			OptionSupport.showDialog(module, view.getGraph2D(), false, view.getFrame());
		}
	}

	/**
	 *  Launches the OrganicLayouter.
	 */
	class LayoutAction extends AbstractAction {
		LayoutAction() {
			super("Layout", SHARED_LAYOUT_ICON);
		}

		public void actionPerformed(ActionEvent e) {
			//update preferredEdgeLengthData before launching the module
			Graph2D graph = view.getGraph2D();
			for (EdgeCursor ec = graph.edges(); ec.ok(); ec.next()) {
				Edge edge = ec.edge();
				String eLabel = graph.getLabelText(edge);
				preferredEdgeLengthMap.set(edge, null);
				try {
					preferredEdgeLengthMap.setInt(edge, (int) Double.parseDouble(eLabel));
				}
				catch (Exception ex) {
				}
			}

			//start the module
			module.start(view.getGraph2D());
		}
	}

	class EditLabel extends AbstractAction {
		Edge e;

		EditLabel(Edge e) 
		{
			super("Edit Preferred Length");
			this.e = e;
		}

		public void actionPerformed(ActionEvent ev) 
		{

			final EdgeRealizer r = view.getGraph2D().getRealizer(e);
			final YLabel label = r.getLabel();

			view.openLabelEditor(label,
					label.getBox().getX(),
					label.getBox().getY(),
					null, true);
		}
	}
}



