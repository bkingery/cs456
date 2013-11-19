package p5_transformations;

/**
* Objects of this class describe a single node in a network.
**/
public class NetworkNode 
{
	private String nodeName;
	private double xCenter;
	private double yCenter;
	private NetworkModel networkModel;
	
	/**
	* Creates a network node
	* @param nodeName the name that the node will be identified by. Names are exact
	*	and case sensitive.
	* @param xCenter the X coordinate of the center of the node in pixels
	* @param yCenter the Y coordinate of the center of the node in pixels
	*/
	public NetworkNode(String nodeName, double xCenter, double yCenter)
	{
		setName(nodeName);
		setLocation(xCenter, yCenter);
	}
	
	/**
	* @return name of the node
	*/
	public String getName()
	{
		return this.nodeName;
	}

	/**
	* Changes the name of the node
	* @param newName
	*/
	public void setName(String newName)
	{
		if (this.networkModel != null)
		{
			for (int i=0; i<networkModel.nConnections(); i++)
			{
				NetworkConnection c = networkModel.getConnection(i);
				if (c.getNode1().compareTo(nodeName)==0)
					c.setNode1(newName);
				if (c.getNode2().compareTo(nodeName)==0)
					c.setNode2(newName);
			}
			this.nodeName = newName;
			this.networkModel.nodeChanged(this);
		}
		else
			this.nodeName = newName;
	}

	/**
	* @return the X coordinate of the center of the node
	*/
	public double getX()
	{
		return this.xCenter;
	}
	
	/**
	* @return the Y coordinate of the center of the node
	*/
	public double getY()
	{
		return this.yCenter;
	}
	
	/**
	* Changes the location of the center of the node
	*/
	public void setLocation(double xCenter, double yCenter)
	{
		this.xCenter = xCenter;
		this.yCenter = yCenter;
		if (this.networkModel != null)
			this.networkModel.nodeChanged(this);
	}

	/**
	* Sets a reference to the network model that this node belongs to
	* @param network
	*/
	public void setNetwork(NetworkModel network)
	{
		this.networkModel = network;
	}
	
	/**
	* @return the network that this node belongs to
	*/
	public NetworkModel getNetwork()
	{
		return this.networkModel;
	}

	/**
	 * @return "N x y name"
	 */
	public String toString()
	{
		return "N "+String.valueOf(xCenter)+" "+String.valueOf(yCenter)+" \""+nodeName+"\"";
	}
}
