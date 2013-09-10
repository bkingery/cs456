package p1_model_view;

import java.awt.event.*;
import javax.swing.*;

public class Main 
{
	public static void main(String[] args) 
	{
		System.out.println("Model View");
		
		//TODO parse the file and create NetworkModel object from it.
				//TODO open JFrame that contains a NetworkView object that displays the Network Model.
		
		//Create the frame
		JFrame F = new JFrame("Model View");

		//Set the position and the size of frame's window
		F.setBounds(100, 100, 300, 400);
		
		//Set up quitting on close of window
		F.addWindowListener(
				new WindowAdapter()
				{
					public void windowClosing(WindowEvent evt)
					{ System.exit(0); }
				});

		//F.getContentPane().add(new); //TODO what goes here
		F.setVisible(true);
	}

}
