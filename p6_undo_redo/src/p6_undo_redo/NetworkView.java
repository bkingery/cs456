package p6_undo_redo;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.*;

import p6_undo_redo.NetworkConnection.Side;

public class NetworkView extends JPanel implements MouseListener, MouseMotionListener, KeyListener, NetworkListener
{
	public enum Mode {SELECT, NODE, CONNECTION, ROTATE};
	private Mode mode;
	
	private NetworkModel networkModel;
	private Font font;
	private FontMetrics FM;
	
	private int curNode;
	private Point2D curNodePosition;
	private int curConnection;
	private int curCharIndex;
	
	private boolean snapping;
	private Point2D startConnection;
	private int startNode;
	private Side startSide;
	private Point2D connectionPrompt;
	private CubicCurve2D tmpConnection;
	
	private Point2D transformCenter;
	private Point2D transformStart;
	private Point2D transformEnd;
	
	private AffineTransform midTransform;
	
	private double[][] bezierMatrix = { {-1,  3, -3, 1},
										{ 3, -6,  3, 0},
										{-3,  3,  0, 0},
										{ 1,  0,  0, 0}};
	
	public NetworkView(NetworkModel networkModel)
	{
		this.networkModel = networkModel;
		mode = Mode.SELECT;
		font = new Font("Helvetica",Font.PLAIN,15);
		FM = getFontMetrics(font);
		
		snapping = false;
		startConnection = null;
		connectionPrompt = null;
		tmpConnection = null;
		
		transformCenter = null;
		transformStart = null;
		transformEnd = null;
		midTransform = new AffineTransform();
		
		
		clearCurSelections();
		
		//Register as listener
		this.networkModel.addNetworkListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
	}
	
	public void setMode(Mode m)
	{
		this.mode = m;
		if (m == Mode.ROTATE)
		{
			if (transformCenter == null)
			{
				Point2D p = new Point();
				p.setLocation(this.getSize().getWidth()/2, this.getSize().getHeight()/2);
				transformCenter = p;
			}
		}
		this.repaint();
	}
	
	/**
	 * Tells the model to save
	 */
	public void save()
	{
		try {
			networkModel.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a new model with the contents of the current model,
	 * but this the path specified.  The will be registered with the new model.
	 * @param path
	 */
	public void saveAs(String path)
	{
		try {
			networkModel.saveAs(path);
			networkModel.removeNetworkListener(this);
			networkModel = new NetworkModel(path);
			networkModel.addNetworkListener(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean canUndo()
	{
		return networkModel.canUndo();
	}
	
	public boolean canRedo()
	{
		return networkModel.canRedo();
	}
	
	public void undo()
	{
		networkModel.undo();
	}
	
	public void redo()
	{
		networkModel.redo();
	}
	
	public NetworkModel getNetworkModel() {
		return this.networkModel;
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
		if (curNode >= 0)
		{
			NetworkNode n = networkModel.getNode(curNode);
			this.curNodePosition = new Point();
			this.curNodePosition.setLocation(n.getX(), n.getY());
		}
		else
			this.curNodePosition = null;
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
	private Point2D getConnectionPoint(NetworkNode n, Side s)
	{
		Point2D p = new Point();
		double x = (curNodePosition != null && networkModel.getNodeIndex(n) == curNode) ? curNodePosition.getX() : n.getX();
		double y = (curNodePosition != null && networkModel.getNodeIndex(n) == curNode) ? curNodePosition.getY() : n.getY();
		int nodeWidth = getNodeWidth(FM.stringWidth(n.getName()));
		int nodeHeight = getNodeHeight(nodeWidth);
		switch (s)
		{
		case T:
			y = y-(nodeHeight/2);
			break;
		case B:
			y = y+(nodeHeight/2);
			break;
		case L:
			x = x-(nodeWidth/2);
			break;
		case R:
			x = x+(nodeWidth/2);
			break;
		}
		p.setLocation(x, y);
		return p;
	}
	
	/**
	 * @return an ArrayList containing all possible connection Point2Ds
	 */
	private ArrayList<Point2D> getAllConnectionPoints()
	{
		ArrayList<Point2D> conPointList = new ArrayList<Point2D>();
		for (int i=0; i<networkModel.nNodes(); i++)
		{
			NetworkNode n = networkModel.getNode(i);
			conPointList.add(getConnectionPoint(n, Side.B));
			conPointList.add(getConnectionPoint(n, Side.L));
			conPointList.add(getConnectionPoint(n, Side.R));
			conPointList.add(getConnectionPoint(n, Side.T));
		}
		
		return conPointList;
	}
	
	/**
	 * The main paint method
	 */
	public void paint(Graphics g)
    {
		super.paint(g);
		
		Graphics2D g2 = (Graphics2D) g;
		AffineTransform original = g2.getTransform();
		
		AffineTransform at = networkModel.getCurrentTransformation(midTransform);
		
		g2.transform(at);
		
        g.setFont(font);
        drawConnections(g2);
        drawNodes(g2);
        
        g2.setTransform(original);
        drawTransformCenter(g2);
    }

	private AffineTransform createNewTransformation() 
	{
		AffineTransform toCenter = null;
		if (canTransform())
		{	
			toCenter = new AffineTransform();
			toCenter.translate(transformCenter.getX(), transformCenter.getY());
			toCenter.concatenate(getRotationTransform());
			toCenter.concatenate(getScaleTransform());
			toCenter.translate(-transformCenter.getX(), -transformCenter.getY());
		}
		return toCenter;
	}
	
	private AffineTransform getScaleTransform()
	{
		AffineTransform at = new AffineTransform();
		
		if (canTransform())
		{
			double s = pointDistance(transformEnd, transformCenter) / pointDistance(transformStart, transformCenter);
			at.scale(s,s);
		}
		return at;
	}
	
	private double getHypotenuse(Point2D center, Point2D other)
	{
		return Math.sqrt(Math.pow(other.getX()-center.getX(),2) + Math.pow(other.getY()-center.getY(),2));
	}
	
	private double getSine(Point2D center, Point2D other)
	{
		double H = getHypotenuse(center, other);
		return (other.getY() - center.getY()) / H;
	}
	
	private double getCosine(Point2D center, Point2D other)
	{
		double H = getHypotenuse(center, other);
		return (other.getX() - center.getX()) / H;
	}
	
	private AffineTransform getRotationTransform()
	{
		AffineTransform at = new AffineTransform();
		
		if (canTransform())
		{
			double sinS = getSine(transformCenter, transformStart);
			double cosS = getCosine(transformCenter, transformStart);
			double sinE = getSine(transformCenter, transformEnd);
			double cosE = getCosine(transformCenter, transformEnd);
			
			at.rotate(cosS, -sinS);
			at.rotate(cosE, sinE);
		}
		
		return at;
	}

	private boolean canTransform() 
	{
		boolean result = transformCenter != null && transformStart != null && transformEnd != null;
		result &= transformStart.getX() != transformEnd.getX() || transformStart.getY() != transformEnd.getY(); 
		
		return result;
	}

	private void drawTransformCenter(Graphics2D g) 
	{
		if (this.mode == Mode.ROTATE)
		{
			g.setColor(Color.RED);
			Point2D p = transformCenter;			
	        g.drawOval((int)p.getX()-4, (int)p.getY()-4, 8, 8);
	        g.drawLine((int)p.getX(), (int)p.getY()-8, (int)p.getX(), (int)p.getY()+8);
	        g.drawLine((int)p.getX()-4, (int)p.getY(), (int)p.getX()+4, (int)p.getY());
		}
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
	
	private void drawNodes(Graphics2D g) 
	{
		for (int i=0; i<this.networkModel.nNodes(); i++)
        {
        	NetworkNode n = this.networkModel.getNode(i);
        	
        	double x = (curNodePosition != null && i == getCurNode()) ? curNodePosition.getX() : n.getX();
        	double y = (curNodePosition != null && i == getCurNode()) ? curNodePosition.getY() : n.getY();
        	
    		int textWidth = FM.stringWidth(n.getName());
    		int textHeight = FM.getHeight();
    		int textLeft = (int) (x-textWidth/2);
    		int textBase = (int) (y+(textHeight/4));
    		
    		int ovalWidth = getNodeWidth(textWidth);
    		int ovalHeight = getNodeHeight(ovalWidth);
    		g.setColor(Color.white);
    		g.fillOval((int)x-ovalWidth/2, (int)y-ovalHeight/2, ovalWidth, ovalHeight);
    		
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
    		
    		g.drawOval((int)x-ovalWidth/2, (int)y-ovalHeight/2, ovalWidth, ovalHeight);
    		g.drawString(text, textLeft, textBase);
        }
	}
	
	/**
	 * @param m the mouse point
	 * @return i the index representing the node selected or -1 if none
	 */
	private int getNode(Point2D m)
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
    		Point2D upperLeft = new Point(upperLeft_x, upperLeft_y);
    		Point2D lowerRight  = new Point(upperLeft_x+ovalWidth, upperLeft_y+ovalHeight);
    		Point2D center = new Point((int)n.getX(), (int)n.getY());
    		
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
	
	private int getCharIndex(Point2D m) 
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
			Point2D upperRight = new Point(upperRight_x, upperRight_y);
			Point2D lowerLeft = new Point(textLeft, textBase+FM.getDescent());
			
			if (inBoundingBox(upperRight, lowerLeft, m))
			{
				result = getCharIndex(n.getName(), "", textLeft, 0, (int)m.getX());
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
	
	private Point2D calculateCurveControlPoint(Point2D p1, Side s, double distance)
	{
		Point2D c = new Point();
		double x = p1.getX();
		double y = p1.getY();
		
		distance = (distance > 100) ? 100 : distance;
		
		switch (s)
		{
		case B:
			y += distance;
			break;
		case T:
			y -= distance;
			break;
		case R:
			x += distance;
			break;
		case L:
			x -= distance;
			break;
		}
		
		c.setLocation(x, y);
		return c;
	}


	/**
	 * Draws all of the connections in networkModel
	 * @param g Graphics object
	 */
	private void drawConnections(Graphics2D g) 
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
        		Point2D p1 = getConnectionPoint(n1, s1);
        		Point2D p2 = getConnectionPoint(n2, s2);
        		double distance = Math.sqrt(pointDistance(p1, p2));
        		Point2D c1 = calculateCurveControlPoint(p1, s1, distance);
        		Point2D c2 = calculateCurveControlPoint(p2, s2, distance);
        		CubicCurve2D curve = new CubicCurve2D.Double(p1.getX(), p1.getY(), 
        													 c1.getX(), c1.getY(), 
        													 c2.getX(), c2.getY(), 
        													 p2.getX(), p2.getY());
        		g.draw(curve);
//        		g.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        	}
        }
		
		if (tmpConnection != null)
			g.draw(this.tmpConnection);
		if (connectionPrompt != null)
			g.drawRect((int)connectionPrompt.getX(), (int)connectionPrompt.getY(), 16, 16);
	}
	
	private double[][] computeCoefficientMatrix(double[][] geo)
	{
		double[][] coef = new double[2][4];

		for (int i=0; i<2; i++)
			for (int j=0; j<4; j++)
				for (int k=0; k<4; k++)
					coef[i][j] += geo[i][k] * bezierMatrix[k][j];
		return coef;
	}
	
	public double[][] computeGeometryMatrix(Point2D p1, Point2D c1, Point2D c2, Point2D p2)
	{
		double[][] geo = new double[2][4];
		geo[0][0] = p1.getX();
		geo[0][1] = c1.getX();
		geo[0][2] = c2.getX();
		geo[0][3] = p2.getX();
		geo[1][0] = p1.getY();
		geo[1][1] = c1.getY();
		geo[1][2] = c2.getY();
		geo[1][3] = p2.getY();
		return geo;
	}
	
	/**
	 * @param m : the mouse point
	 * @return i : the index representing the connection selected or -1 if none
	 */
	private int getConnection(Point2D m)
	{
		int result = -1;
		double minDist = Double.POSITIVE_INFINITY;
		for (int i=0; i< this.networkModel.nConnections(); i++)
		{
			NetworkConnection c = this.networkModel.getConnection(i);
			NetworkNode n1 = this.networkModel.getNode(c.getNode1());
			NetworkNode n2 = this.networkModel.getNode(c.getNode2());
			if (n1 != null && n2 != null)
			{
				Side s1 = c.getSide1();
				Side s2 = c.getSide2();
				Point2D p1 = getConnectionPoint(n1, s1);
        		Point2D p2 = getConnectionPoint(n2, s2);
        		double distance = Math.sqrt(pointDistance(p1, p2));
        		Point2D c1 = calculateCurveControlPoint(p1, s1, distance);
        		Point2D c2 = calculateCurveControlPoint(p2, s2, distance);
        		
//				if (inBoundingBox(p1, p2, m))
//				{
					double[][] geo = computeGeometryMatrix(p1, c1, c2, p2);
					double[][] coef = computeCoefficientMatrix(geo);
					
					double dist = pointDistance(nearestPointCurve(coef, m, 0, 1), m);
					if (dist < minDist)
						minDist = dist;
					if (dist <= 25)
						result = i;
//				}
			}
		}
		return result;
	}
	
	/**
	 * @return true if Point p is in the bounding box of line
	 */
	private boolean inBoundingBox(Point2D p1, Point2D p2, Point2D m)
	{
		double max_x = (p1.getX() > p2.getX()) ? p1.getX() : p2.getX();
		double min_x = (p1.getX() < p2.getX()) ? p1.getX() : p2.getX();
		double max_y = (p1.getY() > p2.getY()) ? p1.getY() : p2.getY();
		double min_y = (p1.getY() < p2.getY()) ? p1.getY() : p2.getY();
		
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
		
		if (m.getX() >= min_x && m.getX() <= max_x &&
			m.getY() >= min_y && m.getY() <= max_y)
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
	private boolean insideEllipse(Point2D center, float horizontal_radius, float virtical_radius, Point2D m) 
	{
		double x_component = (m.getX()-center.getX())/horizontal_radius;
		double y_component = (m.getY()-center.getY())/virtical_radius;
		double result = Math.pow(x_component, 2)+Math.pow(y_component, 2)-1;
		return result <= 0;
	}
	
	/**
	 * @param a point a of line
	 * @param b point b of line
	 * @param m point to check against
	 * @param lowerT the lower bound
	 * @param upperT the upper bound
	 * @return Point2D on line nearest to m
	 */
	private Point2D nearestPointLine(Point2D a, Point2D b, Point2D m, float lowerT, float upperT)
	{
		int N = 10;
		float inc = (upperT - lowerT)/N;
		Point2D lowP = computePointLine(a, b, lowerT);
		Point2D highP = computePointLine(a, b, upperT);
		if (pointDistance(lowP, highP) <= 1.0)
			return lowP; //close enough for pixel resolution
		
		float nearT = lowerT;
		Point2D nearP= lowP;
		double nearD = pointDistance(nearP, m);
		
		for (float t=lowerT+inc; t<=upperT; t=t+inc)
		{
			Point2D tp = computePointLine(a, b, t);
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
	
	private Point2D nearestPointCurve(double[][] coef, Point2D m, double lowerT, double upperT)
	{
		int N = 10;
		double inc = (upperT - lowerT)/N;
		Point2D lowP = computePointCurve(coef, lowerT);
		Point2D highP = computePointCurve(coef, upperT);
		if (pointDistance(lowP, highP) <= 1.0)
			return lowP; //close enough for pixel resolution
		
		double nearT = lowerT;
		Point2D nearP= lowP;
		double nearD = pointDistance(nearP, m);
		
		for (double t=lowerT+inc; t<=upperT; t=t+inc)
		{
			Point2D tp = computePointCurve(coef, t);
			if (pointDistance(tp, m) < nearD)
			{
				nearD = pointDistance(tp, m);
				nearT = t;
				nearP = tp;
			}
		}
		double newLow = nearT-inc;
		if (newLow<lowerT) newLow=lowerT;
		double newHigh = nearT+inc;
		if (newHigh>upperT) newHigh=upperT;
		return nearestPointCurve(coef, m, newLow, newHigh);
	}
	
	/**
	 * @return The square of the distance between point a and point b
	 */
	private double pointDistance(Point2D p1, Point2D b) 
	{
		double dx = p1.getX() - b.getX();
		double dy = p1.getY() - b.getY();
		return dx*dx+dy*dy;
	}

	private Point2D computePointCurve(double[][] coef, double t)
	{
		Point2D result = new Point();
		double a = coef[0][0];
		double b = coef[0][1];
		double c = coef[0][2];
		double d = coef[0][3];
		double e = coef[1][0];
		double f = coef[1][1];
		double g = coef[1][2];
		double h = coef[1][3];
		
		double x = (a*Math.pow(t,3) + b*Math.pow(t,2) + c*t + d);
		double y = (e*Math.pow(t,3) + f*Math.pow(t,2) + g*t + h);
		result.setLocation(x,y);
		return result;
	}

	/**
	 * @return The point on a line using the parametric form
	 */
	private Point2D computePointLine(Point2D a, Point2D b, double t) 
	{
		Point2D result = new Point();
		double x = (a.getX()+t*(b.getX()-a.getX()));
		double y = (a.getY()+t*(b.getY()-a.getY()));
		result.setLocation(x,y);
		return result;
	}

	/**
	 * @param mouseLoc : component coordinates
	 * @return descriptor indicating the node or connection object index
	 */
	public GeometryDescriptor pointGeometry(Point2D m)
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
	
	/**
	 * Selects the essential geometry at point p
	 * @param p
	 */
	private void makeSelection(Point2D p) 
	{
		clearCurSelections();
		GeometryDescriptor g = pointGeometry(p);
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
	
	/**
	 * Changes the current node's position to point p
	 * @param p
	 */
	private void changeCurNodePosition(Point2D p) 
	{
		if (this.getCurNode() >= 0)
		{
			NetworkNode n = this.networkModel.getNode(getCurNode());
			double x = curNodePosition.getX();
			double y = curNodePosition.getY();
			if (n.getX() != x || n.getY() != y)
				networkModel.changeNodePosition(n, x, y);
			curNodePosition = null;
		}
	}
	
	private void midNodeDrag(Point2D p)
	{
		double x = (p.getX() < 0) ? 0 : p.getX();
		double y = (p.getY() < 0) ? 0 : p.getY();
		curNodePosition = new Point();
		curNodePosition.setLocation(x,y);
		repaint();
	}
	
	/**
	 * Creates a new NetworkNode at point p
	 * @param p
	 * @return the new NetworkNode
	 */
	private NetworkNode createNode(Point2D p)
	{
		NetworkNode n = new NetworkNode("New node", p.getX(), p.getY());
		networkModel.newNode(n);
		return n;
	}
	
	private Side computeSide(NetworkNode n, Point2D p) 
	{
		int nx = (int)n.getX();
		int ny = (int)n.getY();
		
		Side side = null;
		if (p.getX() > nx)
			side = Side.R;
		if (p.getX() < nx)
			side = Side.L;
		if (p.getY() > ny)
			side = Side.B;
		if (p.getY() < ny)
			side = Side.T;
			
		return side;
	}
	
	private void startConnection(Point2D p)
	{
		ArrayList<Point2D> connectionPoints = getAllConnectionPoints();
		for (int i=0; i<connectionPoints.size(); i++)
		{
			Point2D cp = connectionPoints.get(i);
			if (cp.getX() == p.getX() && cp.getY() == p.getY())
			{
				this.startConnection = p;
				this.startNode = this.getNode(p);
				this.startSide = computeSide(networkModel.getNode(startNode), p);
			}
		}
	}
	
	private void midConnection(Point2D p)
	{
		double distance = Math.sqrt(pointDistance(startConnection, p));
		Point2D c1 = calculateCurveControlPoint(startConnection, startSide, distance);
		tmpConnection = new CubicCurve2D.Double(startConnection.getX(), startConnection.getY(), c1.getX(), c1.getY(), c1.getX(), c1.getY(), p.getX(), p.getY());
		this.repaint();
	}

	private void endConnection(Point2D p)
	{
		ArrayList<Point2D> connectionPoints = getAllConnectionPoints();
		for (int i=0; i<connectionPoints.size(); i++)
		{
			Point2D cp = connectionPoints.get(i);
			if (cp.getX() == p.getX() && cp.getY() == p.getY())
			{
				NetworkNode n1 = networkModel.getNode(startNode);
				NetworkNode n2 = networkModel.getNode(curNode);
				Side s1 = startSide;
				Side s2 = computeSide(n2, p);
				
				NetworkConnection c = new NetworkConnection(n1.getName(), s1, n2.getName(), s2);
				networkModel.addConnection(c);
			}
		}
		startConnection = null;
		startNode = -1;
		startSide = null;
		connectionPrompt = null;
		tmpConnection = null;
	}
	
	/**
	 * Snaps to any of the connection points if m is within 15 pixles
	 * @param m the current mouse point
	 */
	private void snapToConnectionPoint(Point2D m)
	{
		connectionPrompt = null;
		if (!snapping)
		{
			snapping = true;
			ArrayList<Point2D> connectionPoints = getAllConnectionPoints();
			for (int i=0; i<connectionPoints.size(); i++)
			{
				Point2D cp = connectionPoints.get(i);
				Point2D upperLeft = new Point();
				upperLeft.setLocation(cp.getX()-(8), cp.getY()-(8));
				Point2D lowerRight = new Point();
				lowerRight.setLocation(cp.getX()+(8), cp.getY()+(8));
				
				if (inBoundingBox(upperLeft, lowerRight, m))
				{
					this.connectionPrompt = upperLeft;
					try {
						Robot r = new Robot();
						SwingUtilities.convertPointToScreen((Point) cp, this);
						r.mouseMove((int)cp.getX(), (int)cp.getY());
					} catch (AWTException e) {
						e.printStackTrace();
					}
				}
			}
			snapping = false;
			this.repaint();
		}
	}
	
	public void setTransformCenter(Point2D p)
	{
		transformCenter = p;
		this.repaint();
	}
	
	private void startTransform(Point2D p) 
	{
		transformStart = p;
		
	}
	
	private void midTransform(Point2D p) 
	{
		transformEnd = p;
		midTransform = this.createNewTransformation();
		this.repaint();
	}
	
	private void endTransform(Point2D p) 
	{
		transformEnd = p;
		this.midTransform.setToIdentity();
		AffineTransform at = createNewTransformation();
		if (at != null)
		{
			networkModel.addTransformation(at);
//			this.repaint();
		}
	}
	
	private void clearTransformations()
	{
		this.networkModel.clearTransformations();
		this.midTransform.setToIdentity();
		this.transformCenter.setLocation(this.getSize().getWidth()/2, this.getSize().getHeight()/2);
		this.transformStart = null;
		this.transformEnd = null;
//		this.repaint();
	}
	
	//********************************************************
	// Network Listener
	//********************************************************
	@Override
	public void nodeChanged(NetworkNode n)
	{
		this.repaint();
	}
	
	
	@Override
	public void connectionChanged(NetworkConnection c) 
	{
		this.repaint();
	}
	
	@Override
	public void transformChanged()
	{
		this.repaint();
	}

	//********************************************************
	// Mouse Listener
	//********************************************************
	
	@Override
	public void mouseReleased(MouseEvent e) 
	{
		Point2D p = e.getPoint();
		switch (mode)
		{
		case SELECT:
		{
			changeCurNodePosition(curNodePosition);
			try {
				p = networkModel.getCurrentTransformation(midTransform).inverseTransform(p, null);
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
			GeometryDescriptor g = pointGeometry(p);
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
			break;
		}
		case CONNECTION:
		{
			try {
				p = networkModel.getCurrentTransformation(midTransform).inverseTransform(p, null);
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
			makeSelection(p);
			endConnection(p);
			break;
		}
		case ROTATE:
		{
			endTransform(p);
			break;
		}
		default:
			break;
		}
		
	}

	@Override
	public void mousePressed(MouseEvent e) 
	{
		Point2D p = e.getPoint();
		switch (mode)
		{
		case SELECT:
		{
			try {
				p = networkModel.getCurrentTransformation(midTransform).inverseTransform(p, null);
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
			makeSelection(p);
			break;
		}
		case CONNECTION:
		{
			try {
				p = networkModel.getCurrentTransformation(midTransform).inverseTransform(p, null);
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
			makeSelection(p);
			startConnection(p);
			break;
		}
		case ROTATE:
		{
			startTransform(p);
			break;
		}
		default:
			break;
		}
		
	}

	@Override
	public void mouseClicked(MouseEvent e) 
	{
		Point2D p = e.getPoint();
		switch (mode)
		{
		case NODE:
		{
			try {
				p = networkModel.getCurrentTransformation(midTransform).inverseTransform(p, null);
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
			createNode(p);
			makeSelection(p);
			break;
		}
		case ROTATE:
		{
			if(e.getClickCount() == 2)
				clearTransformations();
			else
				setTransformCenter(p);
			break;
		}
		default:
			break;
		}
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
		
		Point2D p = e.getPoint();
		switch (mode)
		{
		case SELECT:
		{
			try {
				p = networkModel.getCurrentTransformation(midTransform).inverseTransform(p, null);
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
			midNodeDrag(p);
			break;
		}
		case CONNECTION:
		{
			try {
				p = networkModel.getCurrentTransformation(midTransform).inverseTransform(p, null);
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
			if(startConnection != null)
			{
				snapToConnectionPoint(p);
				midConnection(p);
			}
			break;
		}
		case ROTATE:
		{
			midTransform(p);
			break;
		}
		default:
			break;
		}
		
	}

	@Override
	public void mouseMoved(MouseEvent e) 
	{
		Point2D p = e.getPoint();
		switch (mode)
		{
		case CONNECTION:
		{
			try {
				p = networkModel.getCurrentTransformation(midTransform).inverseTransform(p, null);
			} catch (NoninvertibleTransformException e1) {
				e1.printStackTrace();
			}
			snapToConnectionPoint(p);
			break;
		}
		default:
			break;
		}
		
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
				networkModel.changeNodeName(n, newName);
				break;
			}
			case KeyEvent.VK_SHIFT:
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
				networkModel.changeNodeName(n, newName);
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








