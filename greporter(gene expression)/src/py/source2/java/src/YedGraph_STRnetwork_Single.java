/*
 * Author: Peijin Zhang
 * Modified by: Liz Ji
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
import y.view.Arrow;
import y.view.DefaultGraph2DRenderer;
import y.view.EdgeRealizer;
import y.view.EditMode;
import y.view.GenericNodeRealizer;
import y.view.Graph2D;
import y.view.Graph2DView;
import y.view.Graph2DViewActions;
import y.view.LineType;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.PopupMode;
import y.view.ProxyShapeNodeRealizer;
import y.view.YLabel;
import y.view.DefaultGraph2DRenderer;
import y.view.hierarchy.DefaultGenericAutoBoundsFeature;
import y.view.hierarchy.DefaultHierarchyGraphFactory;
import y.view.hierarchy.GenericGroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Hashtable;

public class YedGraph_STRnetwork_Single extends Base 
{
	
	private EdgeMap preferredEdgeLengthMap;
	private YModule module;
	public File input;
	public int InfoOpt;

	public YedGraph_STRnetwork_Single(File input, int InfoOpt) throws Exception
	{
		this.input = input;
		this.InfoOpt = InfoOpt;
		
		preferredEdgeLengthMap = view.getGraph2D().createEdgeMap();
		view.getGraph2D().addDataProvider(OrganicLayouter.PREFERRED_EDGE_LENGTH_DATA, preferredEdgeLengthMap);
		
		//Default layout to Smart Organic
		module = new SmartOrganicLayoutModule();
		Defaults.applyRealizerDefaults(view.getGraph2D(), true, true);
		//Make nodes cover the edges
		DefaultGraph2DRenderer thisgraph = new DefaultGraph2DRenderer(); 
		thisgraph.setDrawEdgesFirst(true);
		view.setGraph2DRenderer(thisgraph);
		
		Graph2D graph = view.getGraph2D();
		
		// Default node color
		NodeRealizer nr = graph.getDefaultNodeRealizer();	
		nr.setLineColor(Color.BLACK);
		
		graph.setDefaultNodeRealizer(nr);
		
		// Oganic Layout options
		OptionHandler options = module.getOptionHandler();
		options.set("VISUAL", "PREFERRED_EDGE_LENGTH", 50);
		options.set("VISUAL", "ALLOW_NODE_OVERLAPS", false);
		options.set("VISUAL", "MINIMAL_NODE_DISTANCE", 5.0);
		options.set("VISUAL", "AVOID_NODE_EDGE_OVERLAPS", true);
		options.set("VISUAL", "COMPACTNESS", 0.8);
		
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
			String[] items = nextLine.split(":");
			String[] tempnodes = items[0].split(",");
			String[] edges = items[1].split(","); 
			
			String node1 = tempnodes[0].trim();
			String node2 = tempnodes[1].trim();
			String type1 = tempnodes[2].trim();
			String type2 = tempnodes[3].trim();
			
			Node a, b;
			if(!indecies.contains(node1))
			{
				indecies.add(node1);
				a = graph.createNode(0, 0, metrics.getStringBounds(node1, null).getBounds().getWidth() + 15, 30, node1);
				nodes.add(a);
				setNodeType(graph,a,type1);
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
				setNodeType(graph,b,type2);
			}
			else
			{
				b = nodes.get(indecies.indexOf(node2));
			}
			if(node1.equals(node2) == false){
				createMultiEdge(graph,a,b,edges,InfoOpt);
			}
			nextLine = in.readLine();
		}
		
		if(indecies.isEmpty()){
			indecies.add("No nodes");
			Node emptynode = graph.createNode(0, 0, metrics.getStringBounds("No nodes", null).getBounds().getWidth() + 15, 30, "No nodes");
			nodes.add(emptynode);
			NodeRealizer nr = graph.getRealizer(emptynode);
			nr.setFillColor(Color.GRAY);
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

	private void setNodeType(Graph2D graph, Node node, String type) {
		NodeRealizer nr = graph.getRealizer(node);
		if(type.equals("hit")){
			nr.setFillColor(Color.yellow);
		}
		else if(type.equals("external")){
			nr.setFillColor(new Color(135,206,250));
		}
		
		graph.setRealizer(node, nr);
	}
	
	private void createMultiEdge(Graph2D graph, Node a, Node b, String[] edges, int InfoOpt) {
		int x;
		int y;
		for ( int i = 0; i < edges.length; i++ ) {
			float edgewidth = Float.valueOf(edges[i]).floatValue();		
			if(edgewidth!=0.00){
				EdgeRealizer er = graph.getDefaultEdgeRealizer();
				er.setTargetArrow(Arrow.NONE);			
				//0-neighborhood-green, 1-genefusion-red, 2-cooccurrence-blue
	            //4-coexpression-black, 5-experiments-magenta, 6-databases-cyan, 7-textmining-orange
				switch(i) {
					case 0: er.setLineColor(Color.green);
							edgewidth = edgewidth*3;
							break;
					case 1: er.setLineColor(Color.red);
							edgewidth = edgewidth*3;
							break;
					case 2: er.setLineColor(Color.blue);
							edgewidth = edgewidth*5;
							break;
					case 4: er.setLineColor(Color.black);
							edgewidth = edgewidth*2;
							break;
					case 5: er.setLineColor(Color.magenta);
							edgewidth = edgewidth*2;
							break;
					case 6: er.setLineColor(Color.cyan);
							edgewidth = edgewidth*2;
							break;
					case 7: er.setLineColor(Color.orange);
							edgewidth = edgewidth*2;
							break;
				}
				float[] dash = {(float) 1,0};
				er.setLineType(LineType.createLineType( edgewidth, 1, 1, (float) 1 , dash , (float) 1));	
				graph.setDefaultEdgeRealizer(er);				
				Edge thisedge = graph.createEdge(a, b);
				if(i != InfoOpt) {
					EdgeRealizer hideer = graph.getRealizer(thisedge);
					hideer.setVisible(false);
				}
			}
		}
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
		YedGraph_STRnetwork_Single yedgraph;

		public Save(Graph2D graph, YedGraph_STRnetwork_Single yedgraph) 
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
				for(int x = 0; x < 6; x++)
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

	//Nodes judge
	
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



