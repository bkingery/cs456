package p1_model_view;

import java.io.*;
import java.util.*;

import p1_model_view.NetworkConnection.Side;

/**
 * Objects of this class contain information about a network nodes and their connections.  
 */
public class NetworkModel 
{
	private String fileName;
	private boolean unsavedChanges;
	private ArrayList<NetworkNode> nodeList = new ArrayList<NetworkNode>();
	private ArrayList<NetworkConnection> conList = new ArrayList<NetworkConnection>();
	
	/**
	 * Creates an empty network model that has a unique default file name and no contents
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	public NetworkModel() throws FileNotFoundException, UnsupportedEncodingException
	{
		long timestamp = System.currentTimeMillis()/1000;
		setFileName("defaultNetwork_"+timestamp+".txt");
		save();
	}
	
	/**
	 * Reads the specific file and creates a new NetworkModel object that contains all of the 
	 * information in the file. If there is no such file then an exception should be thrown.
	 * @param fileName the name of the file to be read.
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 */
	public NetworkModel(String fileName) throws FileNotFoundException, UnsupportedEncodingException
	{
		setFileName(fileName);
		parseFile(fileName);
		save();
	}
	
	private void parseFile(String fileName) throws FileNotFoundException
	{
		File f = new File(fileName);
		Scanner s = new Scanner(f);
		
		while (s.hasNext())
		{
			String line = s.nextLine();
			String[] tokens = line.split(" ");
			if (tokens[0].compareTo("N") == 0)
				parseNetworkNode(tokens);
			else if (tokens[0].compareTo("C") == 0)
				parseNetworkConnection(tokens);
		}
		
		s.close();
	}
	
	private void parseNetworkNode(String[] tokens)
	{
		String nodeName = tokens[3];
		double xCenter	= Double.parseDouble(tokens[1]);
		double yCenter = Double.parseDouble(tokens[2]);
		NetworkNode n = new NetworkNode(nodeName, xCenter, yCenter);
		this.addNode(n);
	}
	
	private void parseNetworkConnection(String[] tokens)
	{
		String node1 = tokens[1];
		Side side1 = Side.fromString(tokens[2]);
		String node2 = tokens[3];
		Side side2 = Side.fromString(tokens[4]);
		NetworkConnection c = new NetworkConnection(node1, side1, node2, side2);
		this.addConnection(c);
	}
	
	/**
	 * Returns the name of the file associated with this model.
	 */
	public String getFileName()
	{
		return this.fileName;
	}
	
	/**
	 * Changes the file name associated with this model
	 * @param newFileName the new file name
	 */
	public void setFileName(String newFileName)
	{
		this.fileName = newFileName;
		System.out.println("Set fileName to:: "+this.fileName);
	}
	
	/**
	 * Saves the contents of this model to its file.
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	public void save() throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter writer = new PrintWriter(getFileName(), "UTF-8");
		for (int i=0; i<this.nodeList.size(); i++)
		{
			NetworkNode n = this.getNode(i);
			writer.println(n.toString());
		}
		for (int i=0; i<this.conList.size(); i++)
		{
			NetworkConnection c = this.getConnection(i);
			writer.println(c.toString());
		}
		writer.close();
		this.unsavedChanges = false;
	}

	/**
	 * Returns true if there are unsaved changes.
	 */
	public boolean unsavedChanges()
	{
		return this.unsavedChanges;
	}

	/**
	 * Adds the specified NetworkNode to the list of network objects
	 * @param newNode
	 */
	public void addNode(NetworkNode newNode)
	{
		this.nodeList.add(newNode);
		this.unsavedChanges = true;
	}

	/**
	 * Returns the number of network node objects in this model.
	 */
	public int nNodes()
	{
		return this.nodeList.size();
	}
	
	/**
	 * Returns the specified NetworkNode. Indexes begin at zero.
	 * @param i index of the desired object. Must be less than nNodes()
	 */
	public NetworkNode getNode(int i)
	{
		return this.nodeList.get(i);
	}

	/**
	 * Removes the specified object from the list of nodes.
	 * @param i the index of the object to be removed.
	 */
	public void removeNode(int i)
	{
		this.nodeList.remove(i);
		this.unsavedChanges = true;
	}
	
	/**
	 * Adds the specified NetworkConnection to the list of connections
	 * @param newConnection
	 */
	public void addConnection(NetworkConnection newConnection)
	{
		this.conList.add(newConnection);
		this.unsavedChanges = true;
	}
	
	/**
	 * Returns the number of network connections in this model.
	 */
	public int nConnections()
	{
		return this.conList.size();
	}
	
	/**
	 * Returns the specified NetworkConnection. Indexes begin at zero.
	 * @param i index of the desired object. Must be less than nConnections()
	 */
	public NetworkConnection getConnection(int i)
	{
		return this.conList.get(i);
	}
	
	/**
	 * Removes the specified object from the list of connections
	 * @param i the index of the object to be removed.
	 */
	public void removeConnection(int i)
	{
		this.conList.remove(i);
		this.unsavedChanges = true;
	}
	
	/**
	* This method is a regression test to verify that this class is
	* implemented correctly. It should test all of the methods including
	* the exceptions. It should be completely self checking. This 
	* should write "testing NetworkModel" to System.out before it
	* starts and "NetworkModel OK" to System.out when the test
	* terminates correctly. Nothing else should appear on a correct
	* test. Other messages should report any errors discovered.
	**/
	public static void Test()
	{
		System.out.println("testing NetworkModel");
		
	}
}












