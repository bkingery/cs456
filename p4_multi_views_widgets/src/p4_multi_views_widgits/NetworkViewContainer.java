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
	
	
	ImageIcon selectIcon;
	ImageIcon nodeIcon;
	ImageIcon connectionIcon;
	
	JButton selectModeButton;
	JButton nodeDrawModeButton;
	JButton connectionDrawModeButton;
	
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
		
		selectIcon = new ImageIcon("icons\\selectionIcon.png");
		nodeIcon = new ImageIcon("icons\\nodeIcon.png");
		connectionIcon = new ImageIcon("icons\\connectionIcon.png");
		
		selectModeButton = new JButton(selectIcon);
		nodeDrawModeButton = new JButton(nodeIcon);
		connectionDrawModeButton = new JButton(connectionIcon);
		
		selectModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { selectMode(e); }
		});
		
		nodeDrawModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { nodeDrawMode(e); }
		});
		
		connectionDrawModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { connectionDrawMode(e); }
		});
		
		selectModeButton.setPreferredSize(new Dimension(80,80));
		selectModeButton.setMinimumSize(new Dimension(80,80));
		nodeDrawModeButton.setPreferredSize(new Dimension(80,80));
		nodeDrawModeButton.setMinimumSize(new Dimension(80,80));
		connectionDrawModeButton.setPreferredSize(new Dimension(80,80));
		connectionDrawModeButton.setMinimumSize(new Dimension(80,80));

		selectModeButton.doClick();
		
		buttonGroup.add(selectModeButton);
		buttonGroup.add(nodeDrawModeButton);
		buttonGroup.add(connectionDrawModeButton);
		buttonPanel.add(selectModeButton);
		buttonPanel.add(nodeDrawModeButton);
		buttonPanel.add(connectionDrawModeButton);
		
		JPanel west = new JPanel(new GridLayout(0,1));
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
	
	public void selectMode(ActionEvent e)
	{
		selectModeButton.setEnabled(false);
		nodeDrawModeButton.setEnabled(true);
		connectionDrawModeButton.setEnabled(true);
		
		networkView.setMode(Mode.SELECT);
		networkView.requestFocusInWindow();
	}
	
	public void nodeDrawMode(ActionEvent e)
	{
		nodeDrawModeButton.setEnabled(false);
		selectModeButton.setEnabled(true);
		connectionDrawModeButton.setEnabled(true);
		
		networkView.setMode(Mode.NODE);
		networkView.requestFocusInWindow();
	}
	
	public void connectionDrawMode(ActionEvent e)
	{
		connectionDrawModeButton.setEnabled(false);
		nodeDrawModeButton.setEnabled(true);
		selectModeButton.setEnabled(true);
		
		networkView.setMode(Mode.CONNECTION);
		networkView.requestFocusInWindow();
	}
	
}
















