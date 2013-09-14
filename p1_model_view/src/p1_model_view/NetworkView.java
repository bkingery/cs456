package p1_model_view;

import java.awt.*;
import java.util.Arrays;

import javax.swing.*;

import p1_model_view.NetworkConnection.Side;

public class NetworkView extends JPanel 
{
	NetworkModel networkModel;
    Image world;
	public NetworkView(NetworkModel networkModel)
	{
		this.networkModel = networkModel;
	}
	
	/**
	 * @param n : the network node being connected to
	 * @param s : the side of the node to connect to
	 * @param FM : the Font Metrix to use
	 * @return : int [2] with x and y coordinates
	 */
	private int[] getConnectionCoordinates(NetworkNode n, Side s, FontMetrics FM)
	{
		int[] xy = new int[2];
		int x = 0;
		int y = 0;
		switch (s)
		{
		case T:
			x = (int)n.getX();
			y = (int)n.getY()-FM.getHeight()/2;
			break;
		case B:
			x = (int)n.getX();
			y = (int)n.getY()+FM.getHeight()/2;
			break;
		case L:
			x = (int)n.getX()-FM.stringWidth(n.getName())/2;
			y = (int)n.getY();
			break;
		case R:
			x = (int)n.getX()+FM.stringWidth(n.getName())/2;
			y = (int)n.getY();
			break;
		}
		xy[0] = x;
		xy[1] = y;
		return xy;
	}
	
	public void paint(Graphics g)
    {
		Font helveticaFont = new Font("Helvetica",Font.PLAIN,15);
        g.setFont(helveticaFont);
        FontMetrics FM = g.getFontMetrics();
        
        for (int i=0; i<this.networkModel.nConnections(); i++)
        {
        	NetworkConnection c = this.networkModel.getConnection(i);
        	NetworkNode n1 = this.networkModel.getNode(c.getNode1());
        	NetworkNode n2 = this.networkModel.getNode(c.getNode2());
        	if (n1 != null && n2 != null)
        	{
        		Side s1 = c.getSide1();
        		Side s2 = c.getSide2();
        		int[] x1y1 = getConnectionCoordinates(n1, s1, FM);
        		int[] x2y2 = getConnectionCoordinates(n2, s2, FM);
        		g.drawLine(x1y1[0], x1y1[1], x2y2[0], x2y2[1]);
        	}
        }
        
        for (int i=0; i<this.networkModel.nNodes(); i++)
        {
        	NetworkNode n = this.networkModel.getNode(i);
    		int textWidth = FM.stringWidth(n.getName());
    		int textHeight = FM.getHeight();
    		int textLeft = (int) (n.getX()-textWidth/2);
    		int textBase = (int) (n.getY()+textHeight/4);
    		
    		g.setColor(Color.white);
    		g.fillOval((int)n.getX()-textWidth/2, (int)n.getY()-textHeight/2, textWidth, textHeight);
    		
    		g.setColor(Color.black);
    		g.drawOval((int)n.getX()-textWidth/2, (int)n.getY()-textHeight/2, textWidth, textHeight);
    		g.drawString(n.getName(), textLeft, textBase);
        }
		
            
    }
}








