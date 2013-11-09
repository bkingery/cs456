package p4_multi_views_widgits;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import p4_multi_views_widgits.NetworkView.Mode;

public class NetworkViewContainer extends JPanel
{	
	NetworkView networkView;
	final JFileChooser fileChooser;
	FileNameExtensionFilter filter;
	
	NetworkViewContainer(NetworkView networkView)
	{
		this.networkView = networkView;
		
		fileChooser = new JFileChooser();
		filter = new FileNameExtensionFilter("network", "network");
		fileChooser.setFileFilter(filter);
		
		//Set up top menu
		JMenuBar menuBar = new JMenuBar();
		JMenu file = new JMenu("File");
		
		JMenuItem open = new JMenuItem("Open");
		JMenuItem save = new JMenuItem("Save");
		JMenuItem saveAs = new JMenuItem("Save As");
		
		open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { open(); }
		});
		
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { save();}
		});
		
		saveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { saveAs(); }
		});
		
		file.add(open);
		file.add(save);
		file.add(saveAs);
		menuBar.add(file);
		
		
		//Set up left button pallete
		JPanel buttonPanel = new JPanel(new GridLayout(3,1));
		ButtonGroup buttonGroup = new ButtonGroup();
		
		JRadioButton selectModeButton = new JRadioButton("Select");
		JRadioButton nodeDrawModeButton = new JRadioButton("Node");
		JRadioButton connectionDrawModeButton = new JRadioButton("Connection");
		
		selectModeButton.setSelected(true);
		selectMode();
		
		selectModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { selectMode(); }
		});
		
		nodeDrawModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { nodeDrawMode(); }
		});
		
		connectionDrawModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { connectionDrawMode(); }
		});
		
		buttonGroup.add(selectModeButton);
		buttonGroup.add(nodeDrawModeButton);
		buttonGroup.add(connectionDrawModeButton);
		buttonPanel.add(selectModeButton);
		buttonPanel.add(nodeDrawModeButton);
		buttonPanel.add(connectionDrawModeButton);
		
		JPanel west = new JPanel(new GridLayout(2,1));
		GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weighty = 1;
        		
		west.add(buttonPanel, gbc);
		west.add(new JPanel());
		west.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		
		this.setLayout(new BorderLayout());
		this.add(menuBar, BorderLayout.NORTH);
		this.add(west, BorderLayout.WEST);
		this.add(networkView);
	}
	
	public void open()
	{
		int returnVal = fileChooser.showOpenDialog(NetworkViewContainer.this);

        if (returnVal == JFileChooser.APPROVE_OPTION) 
        {
            File file = fileChooser.getSelectedFile();
            System.out.println(file.toString());
            NetworkModel networkModel = networkView.getNetworkModel();
			try {
				String path = file.getCanonicalPath();
				if (networkModel.getFileName().equals(path))
	            	Network.createNewWindow(networkModel); //link to existing NetworkModel
				else
					Network.createNewWindow(new NetworkModel(path));
			} catch (IOException e) {
				e.printStackTrace();
			}
            
        } 
	}
	
	public void save()
	{
		networkView.save();
	}
	
	public void saveAs()
	{
		int returnVal = fileChooser.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) 
        {
			try {
				
				File file = fileChooser.getSelectedFile();
				String path = file.getCanonicalPath();
				networkView.saveAs(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
	}
	
	public void selectMode()
	{
		networkView.setMode(Mode.SELECT);
		networkView.requestFocusInWindow();
	}
	
	public void nodeDrawMode()
	{
		networkView.setMode(Mode.NODE);
		networkView.requestFocusInWindow();
	}
	
	public void connectionDrawMode()
	{
		networkView.setMode(Mode.CONNECTION);
		networkView.requestFocusInWindow();
	}
	
}
















