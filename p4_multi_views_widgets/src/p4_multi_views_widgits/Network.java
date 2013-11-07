package p4_multi_views_widgits;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;

public class Network 
{
	public static void createNewWindow(NetworkModel networkModel)
	{
		//Create the frame
		JFrame F = new JFrame("Network");

		//Set the position and the size of frame's window
		F.setBounds(100, 100, 800, 600);
		
		//Set up quitting on close of window
		F.addWindowListener(
				new WindowAdapter()
				{
					public void windowClosing(WindowEvent evt)
					{ System.exit(0); }
				});
		
		NetworkView networkView = new NetworkView(networkModel);
		NetworkViewContainer container = new NetworkViewContainer(networkView);
		
		F.getContentPane().add(container);
		F.setVisible(true);
		F.setFocusable(true);
		networkView.requestFocusInWindow();
	}
	
	public static void main(String[] args) 
	{
//		NetworkModel.Test();
		
		NetworkModel networkModel = null;
		try {
			if (args.length == 0)
				networkModel = new NetworkModel();
			else if (args.length == 1)
			{
				File f = new File(args[0]);
				networkModel = new NetworkModel(f.getCanonicalPath());
			}
			else
			{
				System.out.println("Provide a single file path.");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		createNewWindow(networkModel);
	}

}
