package p3_simple_interaction;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import p3_simple_interaction.NetworkConnection.Side;

public class NetworkView extends JPanel implements MouseListener, MouseMotionListener, KeyListener, NetworkListener
{
	private NetworkModel networkModel;
	private Font font;
	private FontMetrics FM;
	
	private int curNode;
	private int curConnection;
	private int curCharIndex;
	
	public NetworkView(NetworkModel networkModel)
	{
		this.networkModel = networkModel;
		font = new Font("Helvetica",Font.PLAIN,15);
		FM = getFontMetrics(font);
		
		clearCurSelections();
		
		//Register as listener
		this.networkModel.addNetworkListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
	}
	
	/**
	 * @return curConnection
	 */
	public int getCurConnection() {
		return curConnection;
	}

	/**
	 * @param curConnection
	 */
	public void setCurConnection(int curConnection) {
		this.curConnection = curConnection;
	}

	/**
	 * @return curNode
	 */
	public int getCurNode() {
		return curNode;
	}

	/**
	 * @param curNode
	 */
	public void setCurNode(int curNode) {
		this.curNode = curNode;
	}
	
	/**
	 * @param charIndex
	 */
	public void setCurCharIndex(int charIndex) {
		this.curCharIndex = charIndex;
	}
	
	public int getCurCharIndex() {
		return curCharIndex;
	}

	/**
	 * Sets all selections to -1
	 * Equivalent to calling
	 * 
	 * setCurNode(-1);
	 * setCurConnection(-1);
	 * setCurCharIndex(-1);
	 */
	private void clearCurSelections() 
	{
		setCurNode(-1);
		setCurConnection(-1);
		setCurCharIndex(-1);
	}
	
	/**
	 * @param n : the network node being connected to
	 * @param s : the side of the node to connect to
	 * @param FM : the Font Metrix to use
	 * @return : int [2] with x and y coordinates
	 */
	private Point getConnectionCoordinates(NetworkNode n, Side s)
	{
		Point p = new Point();
		int x = 0;
		int y = 0;
		int nodeWidth = getNodeWidth(FM.stringWidth(n.getName()));
		int nodeHeight = getNodeHeight(nodeWidth);
		switch (s)
		{
		case T:
			x = (int)n.getX();
			y = (int)n.getY()-nodeHeight/2;
			break;
		case B:
			x = (int)n.getX();
			y = (int)n.getY()+nodeHeight/2;
			break;
		case L:
			x = (int)n.getX()-nodeWidth/2;
			y = (int)n.getY();
			break;
		case R:
			x = (int)n.getX()+nodeWidth/2;
			y = (int)n.getY();
			break;
		}
		p.x = x;
		p.y = y;
		return p;
	}
	
	/**
	 * The main paint method
	 */
	public void paint(Graphics g)
    {
		super.paint(g);
        g.setFont(font);
        drawConnections(g);
        drawNodes(g);
    }

	private int getNodeWidth(int textWidth)
	{
		return textWidth+10;
	}
	
	private int getNodeHeight(int ovalWidth) 
	{
		int textHeight = FM.getHeight();
		return (ovalWidth/4 > textHeight+textHeight/2) ? ovalWidth/4 : textHeight+textHeight/2;
	}
	
	private String insertCharAt(String text, char c, int index) 
	{
		String before = text.substring(0, index);
		String after = text.substring(index);
		text = before + c + after;
		return text;
	}
	
	private String removeCharAt(String text, int index)
	{
		if (index >0)
		{
			String before = text.substring(0, index-1);
			String after = text.substring(index);
			text = before+after;
		}
		return text;
	}
	
	private void drawNodes(Graphics g) 
	{
		for (int i=0; i<this.networkModel.nNodes(); i++)
        {
        	NetworkNode n = this.networkModel.getNode(i);
    		int textWidth = FM.stringWidth(n.getName());
    		int textHeight = FM.getHeight();
    		int textLeft = (int) (n.getX()-textWidth/2);
    		int textBase = (int) (n.getY()+(textHeight/4));
    		
    		int ovalWidth = getNodeWidth(textWidth);
    		int ovalHeight = getNodeHeight(ovalWidth);
    		g.setColor(Color.white);
    		g.fillOval((int)n.getX()-ovalWidth/2, (int)n.getY()-ovalHeight/2, ovalWidth, ovalHeight);
    		
    		String text = n.getName();
    		//Determine if Node should be highlighted
    		if (i == getCurNode())
    		{
    			g.setColor(Color.red);
    			//Determine char insertion point
    			if(getCurCharIndex() >=0)
    				text = insertCharAt(text, '|', getCurCharIndex());
    		}
    		else
    			g.setColor(Color.black);
    		
    		g.drawOval((int)n.getX()-ovalWidth/2, (int)n.getY()-ovalHeight/2, ovalWidth, ovalHeight);
    		g.drawString(text, textLeft, textBase);
        }
	}
	
	/**
	 * @param m the mouse point
	 * @return i the index representing the node selected or -1 if none
	 */
	private int getNode(Point m)
	{
		int result = -1;
		for (int i=0; i<this.networkModel.nNodes(); i++)
		{
			NetworkNode n = this.networkModel.getNode(i);
			int textWidth = FM.stringWidth(n.getName());
    		
    		int ovalWidth = getNodeWidth(textWidth);
    		int ovalHeight = getNodeHeight(ovalWidth);
    		int upperLeft_x = (int) (n.getX()-ovalWidth/2);
    		int upperLeft_y = (int) (n.getY()-ovalHeight/2);
    		Point upperLeft = new Point(upperLeft_x, upperLeft_y);
    		Point lowerRight  = new Point(upperLeft_x+ovalWidth, upperLeft_y+ovalHeight);
    		Point center = new Point((int)n.getX(), (int)n.getY());
    		
    		if (inBoundingBox(upperLeft, lowerRight, m))
    		{
    			float a = ((upperLeft_x+ovalWidth)-upperLeft_x)/2;
    			float b = ((upperLeft_y+ovalHeight)-upperLeft_y)/2;
    			if (insideEllipse(center, a, b, m))
    				result = i;
    		}
		}
		return result;
	}
	
	public void nodeChanged(NetworkNode n)
	{
		this.repaint();
	}
	
	private int getCharIndex(Point m) 
	{
		int result = -1;
		
		for (int i=0; i<this.networkModel.nNodes(); i++)
		{
			NetworkNode n = this.networkModel.getNode(i);
			int textWidth = FM.stringWidth(n.getName());
			int textHeight = FM.getHeight();
			int textLeft = (int) (n.getX()-textWidth/2);
    		int textBase = (int) (n.getY()+(textHeight/4));
			int upperRight_x = (int) (textLeft+textWidth);
			int upperRight_y = (int) (textBase-FM.getAscent());
			Point upperRight = new Point(upperRight_x, upperRight_y);
			Point lowerLeft = new Point(textLeft, textBase+FM.getDescent());
			
			if (inBoundingBox(upperRight, lowerLeft, m))
			{
				result = getCharIndex(n.getName(), "", textLeft, 0, m.x);
			}
		}
		return result;
	}

	/**
	 * @param text the text
	 * @param start_x the text starting x coordinate
	 * @param i index so far.  Typically should be called with 1
	 * @param mx the mouse x coordinate
	 * @return the character index of text indicated by mx
	 */
	private int getCharIndex(String text, String sofar, int start_x, int i, int mx) 
	{
		String nextchar = ""+text.charAt(Math.min(i, text.length()-1));
		int width = FM.stringWidth(sofar)+(FM.stringWidth(nextchar)/2);
		if (i >= text.length() || start_x+width > mx)
			return i;
		
		sofar += nextchar;
		return getCharIndex(text, sofar, start_x, ++i, mx);
	}


	/**
	 * Draws all of the connections in networkModel
	 * @param g Graphics object
	 */
	private void drawConnections(Graphics g) 
	{
		for (int i=0; i<this.networkModel.nConnections(); i++)
        {
			//Determine if connection should be highlighted
			if (i == getCurConnection())
				g.setColor(Color.red);
			else
				g.setColor(Color.black);
			
        	NetworkConnection c = this.networkModel.getConnection(i);
        	NetworkNode n1 = this.networkModel.getNode(c.getNode1());
        	NetworkNode n2 = this.networkModel.getNode(c.getNode2());
        	if (n1 != null && n2 != null)
        	{
        		Side s1 = c.getSide1();
        		Side s2 = c.getSide2();
        		Point p1 = getConnectionCoordinates(n1, s1);
        		Point p2 = getConnectionCoordinates(n2, s2);
        		g.drawLine(p1.x, p1.y, p2.x, p2.y);
        	}
        }
	}
	
	/**
	 * @param m : the mouse point
	 * @return i : the index representing the connection selected or -1 if none
	 */
	private int getConnection(Point m)
	{
		int result = -1;
		float minDist = Float.POSITIVE_INFINITY;
		for (int i=0; i< this.networkModel.nConnections(); i++)
		{
			NetworkConnection c = this.networkModel.getConnection(i);
			NetworkNode n1 = this.networkModel.getNode(c.getNode1());
			NetworkNode n2 = this.networkModel.getNode(c.getNode2());
			if (n1 != null && n2 != null)
			{
				Side s1 = c.getSide1();
				Side s2 = c.getSide2();
				Point p1 = getConnectionCoordinates(n1, s1);
        		Point p2 = getConnectionCoordinates(n2, s2);
				
				if (inBoundingBox(p1, p2, m))
				{
					float dist = pointDistance(nearestPointLine(p1, p2, m, 0, 1), m);
					if (dist < minDist)
						minDist = dist;
					if (dist <= 25)
						result = i;
				}
			}
		}
		return result;
	}
	
	/**
	 * @return true if Point p is in the bounding box of line
	 */
	private boolean inBoundingBox(Point p1, Point p2, Point m)
	{
		int max_x = (p1.x > p2.x) ? p1.x : p2.x;
		int min_x = (p1.x < p2.x) ? p1.x : p2.x;
		int max_y = (p1.y > p2.y) ? p1.y : p2.y;
		int min_y = (p1.y < p2.y) ? p1.y : p2.y;
		
		if (max_x == min_x)
		{
			max_x += 5;
			min_x -=5;
		}
		if (max_y == min_y)
		{
			max_y +=5;
			min_y -=5;
		}
		
		if (m.x >= min_x && m.x <= max_x &&
			m.y >= min_y && m.y <= max_y)
			return true;
		else
			return false;
	}
	
	/**
	 * @param center
	 * @param horizontal_radius
	 * @param virtical_radius
	 * @param m
	 * @return true if point m in inside the ellipse
	 */
	private boolean insideEllipse(Point center, float horizontal_radius, float virtical_radius, Point m) 
	{
		float x_component = (m.x-center.x)/horizontal_radius;
		float y_component = (m.y-center.y)/virtical_radius;
		double result = Math.pow(x_component, 2)+Math.pow(y_component, 2)-1;
		return result <= 0;
	}
	
	/**
	 * @param a point a of line
	 * @param b point b of line
	 * @param m point to check against
	 * @param lowerT the lower bound
	 * @param upperT the upper bound
	 * @return point on line nearest to m
	 */
	private Point nearestPointLine(Point a, Point b, Point m, float lowerT, float upperT)
	{
		int N = 10;
		float inc = (upperT - lowerT)/N;
		Point lowP = computePointLine(a, b, lowerT);
		Point highP = computePointLine(a, b, upperT);
		if (pointDistance(lowP, highP) <= 1.0)
			return lowP; //close enough for pixel resolution
		
		float nearT = lowerT;
		Point nearP= lowP;
		float nearD = pointDistance(nearP, m);
		
		for (float t=lowerT+inc; t<=upperT; t=t+inc)
		{
			Point tp = computePointLine(a, b, t);
			if (pointDistance(tp, m) < nearD)
			{
				nearD = pointDistance(tp, m);
				nearT = t;
				nearP = tp;
			}
		}
		float newLow = nearT-inc;
		if (newLow<lowerT) newLow=lowerT;
		float newHigh = nearT+inc;
		if (newHigh>upperT) newHigh=upperT;
		return nearestPointLine(a, b, m, newLow, newHigh);
	}
	
	/**
	 * @return The square of the distance between point a and point b
	 */
	private float pointDistance(Point a, Point b) 
	{
		float dx = a.x - b.x;
		float dy = a.y - b.y;
		return dx*dx+dy*dy;
	}


	/**
	 * @return The point on a line using the parametric form
	 */
	private Point computePointLine(Point a, Point b, float t) 
	{
		Point result = new Point();
		result.x = (int) (a.x+t*(b.x-a.x));
		result.y = (int) (a.y+t*(b.y-a.y));
		return result;
	}

	/**
	 * @param mouseLoc : component coordinates
	 * @return descriptor indicating the node or connection object index
	 */
	public GeometryDescriptor pointGeometry(Point m)
	{
		GeometryDescriptor g = new GeometryDescriptor();
		int node = getNode(m);
		if (node >= 0)
		{
			g.setNodeIndex(node);
			int charIndex = getCharIndex(m);
			g.setCharIndex(charIndex);
		}
		else
		{
			int con = getConnection(m);
			g.setConnIndex(con);
		}
		
		return g;
	}

	//********************************************************
	// Mouse Listener
	//********************************************************
	
	@Override
	public void mouseReleased(MouseEvent e) 
	{
		GeometryDescriptor g = pointGeometry(e.getPoint());
		if (g.getNodeIndex() >=0)
		{
			System.out.print("Node"+g.getNodeIndex()+": ");
			System.out.print("\""+this.networkModel.getNode(g.getNodeIndex()).getName()+"\"");
			if(g.getCharIndex() >=0)
				System.out.print(", CharIndex: "+g.getCharIndex());
			System.out.println();
		}
		else if (g.getConnIndex() >=0)
		{
			System.out.print("Connection"+g.getConnIndex()+": ");
			System.out.print("\""+this.networkModel.getConnection(g.getConnIndex()).getNode1()+"\"");
			System.out.println(" to "+"\""+this.networkModel.getConnection(g.getConnIndex()).getNode2()+"\"");
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) 
	{
		clearCurSelections();
		GeometryDescriptor g = pointGeometry(e.getPoint());
		if (g.getNodeIndex() >=0)
		{
			setCurNode(g.getNodeIndex());
			if(g.getCharIndex() >=0)
				setCurCharIndex(g.getCharIndex());
		}
		else if (g.getConnIndex() >=0)
		{
			setCurConnection(g.getConnIndex());
		}
		this.repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
	}
	
	//********************************************************
	// Mouse Motion Listener
	//********************************************************
	
	@Override
	public void mouseDragged(MouseEvent e) 
	{
		if (this.getCurNode() >= 0)
		{
			NetworkNode n = this.networkModel.getNode(getCurNode());
			Point p = e.getPoint();
			n.setLocation(p.getX(), p.getY());
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	//********************************************************
	// Key Listener
	//********************************************************
	
	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyReleased(KeyEvent e) 
	{
		if (getCurCharIndex() >=0)
		{
			NetworkNode n = this.networkModel.getNode(getCurNode());
			switch (e.getKeyCode())
			{
			case KeyEvent.VK_BACK_SPACE:
			{
				String newName = removeCharAt(n.getName(), getCurCharIndex());
				int charIndex = getCurCharIndex();
				charIndex = (charIndex-1 <= 0) ? 0 : charIndex-1;
				setCurCharIndex(charIndex);
				n.setName(newName);
				break;
			}
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_UP:
				break;
			case KeyEvent.VK_RIGHT:
//				System.out.println("Right");
				break;
			case KeyEvent.VK_LEFT:
//				System.out.println("Left");
				break;
			default:
			{
				String newName = insertCharAt(n.getName(), e.getKeyChar(), getCurCharIndex());
				setCurCharIndex(getCurCharIndex()+1);
				n.setName(newName);
				break;
			}
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}
}








