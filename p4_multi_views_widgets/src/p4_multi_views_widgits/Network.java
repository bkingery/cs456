package p4_multi_views_widgits;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;

public class Network 
{
	static ArrayList<NetworkView> networkViewList = new ArrayList<NetworkView>();
	
	public static void createNewWindow(NetworkModel networkModel)
	{
		final NetworkView networkView = new NetworkView(networkModel);
		networkViewList.add(networkView);
		NetworkViewContainer container = new NetworkViewContainer(networkView);
		
		//Create the frame
		final JFrame F = new JFrame("Network");

		//Set the position and the size of frame's window
		F.setBounds(100, 100, 800, 600);
		
		//Set up quitting on close of window
		F.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		F.addWindowListener(
				new WindowAdapter()
				{
					public void windowClosing(WindowEvent evt)
					{ 
						System.out.println(networkView.getNetworkModel().getFileName());
						if (networkView.getNetworkModel().nNetworkListeners() == 1 &&
							networkView.getNetworkModel().unsavedChanges())
						{
							int reply = JOptionPane.showConfirmDialog(null, "Save Changes?", "Unsaved Changes", JOptionPane.YES_NO_CANCEL_OPTION);
							if (reply == JOptionPane.YES_OPTION)
							{
								networkView.save();
								networkView.getNetworkModel().removeNetworkListener(networkView);
								F.dispose();
								if (networkViewList.size() == 1)
									System.exit(0);
							}
							if (reply == JOptionPane.NO_OPTION)
							{
								networkView.getNetworkModel().removeNetworkListener(networkView);
								F.dispose();
								if (networkViewList.size() == 1)
									System.exit(0);
							}
								
						}
						else
						{
							networkView.getNetworkModel().removeNetworkListener(networkView);
							F.dispose();
							if (networkViewList.size() == 1)
								System.exit(0);
						}
					}
				});
		
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
