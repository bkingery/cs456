package p6_undo_redo;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import p6_undo_redo.NetworkView.Mode;


public class NetworkViewContainer extends JPanel 
{	
	NetworkView networkView;
	final JFileChooser fileChooser;
	FileNameExtensionFilter filter;
	
	
	ImageIcon selectIcon;
	ImageIcon nodeIcon;
	ImageIcon connectionIcon;
	ImageIcon rotateIcon;
	
	JMenuItem undo;
	JMenuItem redo;
	
	JButton selectModeButton;
	JButton nodeDrawModeButton;
	JButton connectionDrawModeButton;
	JButton rotateModeButton;
	
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
		
		JMenu edit = new JMenu("Edit");
		undo = new JMenuItem("Undo");
		redo = new JMenuItem("Redo");
		
		open.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { open(); }
		});
		
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { save();}
		});
		
		saveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { saveAs(); }
		});
		
		edit.addMenuListener(new MenuListener() {
			public void menuCanceled(MenuEvent e) { return; }
			public void menuDeselected(MenuEvent e) { return; }
			public void menuSelected(MenuEvent e) { editMenu(); }
		});
		
		undo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { undo(); }
		});
		
		redo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { redo(); }
		});
		
		file.add(open);
		file.add(save);
		file.add(saveAs);
		edit.add(undo);
		edit.add(redo);
		menuBar.add(file);
		menuBar.add(edit);
		
		
		//Set up left button pallete
		JPanel buttonPanel = new JPanel(new GridLayout(4,1));
		ButtonGroup buttonGroup = new ButtonGroup();
		
		selectIcon = new ImageIcon(getClass().getResource("/icons/selectionIcon.png"));
		nodeIcon = new ImageIcon(getClass().getResource("/icons/nodeIcon.png"));
		connectionIcon = new ImageIcon(getClass().getResource("/icons/connectionIcon.png"));
		rotateIcon = new ImageIcon(getClass().getResource("/icons/rotateIcon.png"));
		
		selectModeButton = new JButton(selectIcon);
		nodeDrawModeButton = new JButton(nodeIcon);
		connectionDrawModeButton = new JButton(connectionIcon);
		rotateModeButton = new JButton(rotateIcon);
		
		selectModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { selectMode(e); }
		});
		
		nodeDrawModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { nodeDrawMode(e); }
		});
		
		connectionDrawModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { connectionDrawMode(e); }
		});
		
		rotateModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { rotateMode(e); }
		});
		
		Dimension d = new Dimension(87,87);
		selectModeButton.setPreferredSize(d);
		selectModeButton.setMinimumSize(d);
		selectModeButton.setMaximumSize(d);
		nodeDrawModeButton.setPreferredSize(d);
		nodeDrawModeButton.setMinimumSize(d);
		nodeDrawModeButton.setMaximumSize(d);
		connectionDrawModeButton.setPreferredSize(d);
		connectionDrawModeButton.setMinimumSize(d);
		connectionDrawModeButton.setMaximumSize(d);
		rotateModeButton.setPreferredSize(d);
		rotateModeButton.setMinimumSize(d);
		rotateModeButton.setMaximumSize(d);

		selectModeButton.doClick();
		
		buttonGroup.add(selectModeButton);
		buttonGroup.add(nodeDrawModeButton);
		buttonGroup.add(connectionDrawModeButton);
		buttonPanel.add(selectModeButton);
		buttonPanel.add(nodeDrawModeButton);
		buttonPanel.add(connectionDrawModeButton);
		buttonPanel.add(rotateModeButton);
		
		JPanel west = new JPanel(new GridLayout(0,1));
		GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weighty = 1;
        
        JPanel spacer = new JPanel();
        spacer.setMinimumSize(new Dimension(0,0));
		west.add(buttonPanel, gbc);
//		west.add(spacer);
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
	
	public void editMenu()
	{
		undo.setEnabled(networkView.canUndo());
		redo.setEnabled(networkView.canRedo());
	}
	
	public void undo()
	{
		networkView.undo();
	}
	
	public void redo()
	{
		networkView.redo();
	}
	
	public void selectMode(ActionEvent e)
	{
		selectModeButton.setEnabled(false);
		nodeDrawModeButton.setEnabled(true);
		connectionDrawModeButton.setEnabled(true);
		rotateModeButton.setEnabled(true);
		
		networkView.setMode(Mode.SELECT);
		networkView.requestFocusInWindow();
	}
	
	public void nodeDrawMode(ActionEvent e)
	{
		nodeDrawModeButton.setEnabled(false);
		selectModeButton.setEnabled(true);
		connectionDrawModeButton.setEnabled(true);
		rotateModeButton.setEnabled(true);
		
		networkView.setMode(Mode.NODE);
		networkView.requestFocusInWindow();
	}
	
	public void connectionDrawMode(ActionEvent e)
	{
		connectionDrawModeButton.setEnabled(false);
		nodeDrawModeButton.setEnabled(true);
		selectModeButton.setEnabled(true);
		rotateModeButton.setEnabled(true);
		
		networkView.setMode(Mode.CONNECTION);
		networkView.requestFocusInWindow();
	}
	
	public void rotateMode(ActionEvent e)
	{
		selectModeButton.setEnabled(true);
		connectionDrawModeButton.setEnabled(true);
		nodeDrawModeButton.setEnabled(true);
		rotateModeButton.setEnabled(false);
		
		networkView.setMode(Mode.ROTATE);
		networkView.requestFocusInWindow();
	}
	
}
















