package p1_model_view;

import java.awt.event.*;
import javax.swing.*;

public class Network 
{
	public static void main(String[] args) 
	{
//		NetworkModel.Test();
		
		NetworkModel networkModel = null;
		try {
			if (args.length == 0)
				networkModel = new NetworkModel();
			else if (args.length == 1)
				networkModel = new NetworkModel(args[0]);
			else
			{
				System.out.println("Provide a single file path.");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		//Create the frame
		JFrame F = new JFrame("Network");

		//Set the position and the size of frame's window
		F.setBounds(100, 100, 300, 400);
		
		//Set up quitting on close of window
		F.addWindowListener(
				new WindowAdapter()
				{
					public void windowClosing(WindowEvent evt)
					{ System.exit(0); }
				});

		F.getContentPane().add(new NetworkView(networkModel));
		F.setVisible(true);
	}

}
