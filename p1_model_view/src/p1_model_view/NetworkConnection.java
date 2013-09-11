package p1_model_view;

/**
* This class describes a connection between two network nodes
*/
public class NetworkConnection
{
	public enum Side {Left("L"), Right("R"), Top("T"), Bottom("B");
		
		private String text;

	  	Side(String text) {this.text = text;}

	  	public String getText() {return this.text;}

	  	public static Side fromString(String text) 
	  	{
	  		if (text != null) 
	  		{
	  			for (Side s : Side.values()) 
	  			{
	  				if (text.equalsIgnoreCase(s.text)) 
	  				{
	  					return s;
	  				}
	  			}
	  		}
	  		return null;
	  	}
	  }
	
	private String node1;
	private String node2;
	private Side side1;
	private Side side2;
	
	/**
	* Creates a new connection
	* @param node1 the name of the first node to be connected
	* @param side1 specifies the side of node1 to which the connection is to be attached
	* @param node2 the name of the second node to be connected
	* @param side2 specifies the side of node2 to which the connection is to be attached
	*/
	public NetworkConnection(String node1, Side side1, String node2, Side side2)
	{
		this.node1 = node1;
		this.side1 = side1;
		this.node2 = node2;
		this.side2 = side2;
	}
	
	/**
	 * @return "C node1 side1 node2 side2"
	 */
	public String toString()
	{
		return "C "+node1+" "+side1.getText()+" "+node2+" "+side2.getText();
	}
}