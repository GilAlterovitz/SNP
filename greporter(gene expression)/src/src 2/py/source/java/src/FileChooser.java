/*
 * Author: Peijin Zhang
 * 
 * Java GUI for program
 * 
 * 4 buttons:
 * 
 * Select Input Files: Select all CSV/TXT files for graph generation
 * 
 * Select Output Directory: Select directory for .jpg and .graphml to be saved in. Optional
 * 
 * Run: Runs the program
 * 
 * Edit Graph: Selects one txt/csv and generates a graph from that. Allows for manual edit and save.
 * 
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.io.File;

public class FileChooser extends JPanel implements ActionListener 
{
	private JButton openButton, saveButton, editButton, compile;
	private JFileChooser open, save;
	private static JList filetype;
    private JTextArea log;
	private File[] inputfiles = null;
	private File outputdirectory = null;
	private String[] filetypes = {"GraphML", "YGF", "JPG", "SVG", "PDF", "SWF", "EMF"};

	public FileChooser() 
	{
		super(new BorderLayout());

		open = new JFileChooser();

		save = new JFileChooser();
		save.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		save.setMultiSelectionEnabled(false);

		openButton = new JButton("Select input files");
		openButton.setSize(240, 60);
		openButton.addActionListener(this);

		saveButton = new JButton("Select output directory");
		saveButton.setSize(240, 60);
		saveButton.addActionListener(this);

		editButton = new JButton("Edit graph");
		editButton.setSize(240, 60);
		editButton.addActionListener(this);

		compile = new JButton("Run");
		compile.setSize(240, 60);
		compile.addActionListener(this);
		
        filetype = new JList(filetypes);
        filetype.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        filetype.setLayoutOrientation(JList.VERTICAL);
        filetype.setVisibleRowCount(7);
        filetype.setSelectedIndex(0);
		
        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(4, 1, 0, 2));
        buttons.add(openButton);
        buttons.add(saveButton);
        buttons.add(editButton);
        buttons.add(compile);
        
		JPanel leftside = new JPanel();
		leftside.setLayout(new GridLayout(2, 1, 0, 5));
		leftside.add(filetype);
		leftside.add(buttons);
	
        log = new JTextArea(7,20);
        log.setText("No files selected \n\nOutput Filetypes: GraphML");
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        log.setBackground(new Color(237, 237, 237));
        JScrollPane logScrollPane = new JScrollPane(log);
        logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        logScrollPane.setBorder(null);
      
        setLayout(new GridLayout(1, 2, 10, 0));
		add(leftside);
		add(logScrollPane);
	}
	
	public static int[] getFiletype()
	{
		return filetype.getSelectedIndices();
	}

	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == openButton) 
		{
			open.setFileSelectionMode(JFileChooser.FILES_ONLY);
			open.setAcceptAllFileFilterUsed(false);
			open.setFileFilter(new DataFilter());
			open.setMultiSelectionEnabled(true);

			int returnVal = open.showOpenDialog(FileChooser.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) 
			{
				inputfiles = open.getSelectedFiles();
			
            	setLog();
			}
		} 
		else if (e.getSource() == saveButton) 
		{
			int returnVal = save.showSaveDialog(FileChooser.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) 
			{
				outputdirectory = save.getSelectedFile();
			}
		}
		else if(e.getSource() == compile)
		{
			//Run all files
			
			if(inputfiles == null)
				JOptionPane.showMessageDialog(new JFrame(), "Please select input files", "Error", JOptionPane.ERROR_MESSAGE);
			else
			{
				// If no outputdirectory selected, export to same path as input files
				if(outputdirectory == null)
					outputdirectory = new File(inputfiles[0].getPath().substring(0, inputfiles[0].getPath().lastIndexOf("\\")));
				try
				{
					new YedRunner(inputfiles, outputdirectory);
				} 
				catch (Exception er)
				{
					er.printStackTrace();
				}
				JOptionPane.showMessageDialog(new JFrame(), "Graph Generation Complete", "Message", JOptionPane.PLAIN_MESSAGE);
			}
		}
		else if(e.getSource() == editButton)
		{
			//Edit a single file
			
			open.setFileSelectionMode(JFileChooser.FILES_ONLY);
			open.setAcceptAllFileFilterUsed(false);
			open.setFileFilter(new DataFilter());
			open.setMultiSelectionEnabled(false);
			int returnVal = open.showOpenDialog(FileChooser.this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File tempfile = open.getSelectedFile();
				try
				{
					new YedRunner(tempfile);
				} 
				catch (Exception e1)
				{
				}
			}
		}
		else if(e.getSource() == filetype)
		{
			setLog();
		}
	}
	
	private void setLog()
	{
		if(filetype.getSelectedValues().length == 0)
			filetype.setSelectedIndex(0);
		
		int[] indices = filetype.getSelectedIndices();
		String temp = "";
		for(int x = 0; x < inputfiles.length; x++)
			temp += inputfiles[x].getName() + "\n";
		if(inputfiles.length != 0)
			temp += "\nFiles Selected: " + inputfiles.length;
		else
			temp += "No Files Selected";
		
		temp += "\n\nOutput formats: ";
		
		for(int x = 0; x < indices.length; x++)
		{
			if(x != indices.length - 1)
				temp += filetypes[indices[x]] + ", ";
			else
				temp += filetypes[indices[x]];
			if(x == 4)
				temp += "\n";
		}
		log.setText(temp);
		repaint();
	}

	private static void createAndShowGUI() 
	{
		JFrame frame = new JFrame("Yed Graph Generator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.add(new FileChooser());
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) 
	{
		SwingUtilities.invokeLater(new Runnable() 
		{
			public void run() 
			{
				createAndShowGUI();
			}
		});
	}
}