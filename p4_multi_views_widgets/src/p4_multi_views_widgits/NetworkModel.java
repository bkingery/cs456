package p4_multi_views_widgits;

import java.io.*;
import java.net.URL;
import java.util.*;

import p4_multi_views_widgits.NetworkConnection.Side;

/**
 * Objects of this class contain information about a network nodes and their connections.  
 */
public class NetworkModel 
{
	private String fileName;
	private boolean unsavedChanges;
	private ArrayList<NetworkNode> nodeList = new ArrayList<NetworkNode>();
	private ArrayList<NetworkConnection> conList = new ArrayList<NetworkConnection>();
	
	private ArrayList<NetworkListener> listeners = new ArrayList<NetworkListener>();
	
	/**
	 * Creates an empty network model that has a unique default file name and no contents
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	public NetworkModel() throws FileNotFoundException, UnsupportedEncodingException
	{
		long timestamp = System.currentTimeMillis()/1000;
		setFileName("defaultNetwork_"+timestamp+".network");
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
		this.unsavedChanges = false;
	}
	
	/**
	 * Parses a Network Model description file
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	private void parseFile(String fileName) throws FileNotFoundException
	{
		File f = new File(fileName);
		Scanner s = new Scanner(f);
		
		while (s.hasNext())
		{
			String line = s.nextLine();
			if (line.charAt(0) == 'N')
				parseNetworkNode(line);
			else if (line.charAt(0) == 'C')
				parseNetworkConnection(line);
		}
		
		s.close();
	}
	
	private void parseNetworkNode(String line)
	{
		String [] tokens  = line.split("\"*\"");
		String [] coordinates = tokens[0].split(" ");
		String nodeName = tokens[1].trim();
		double xCenter	= Double.parseDouble(coordinates[1]);
		double yCenter = Double.parseDouble(coordinates[2]);
		NetworkNode n = new NetworkNode(nodeName, xCenter, yCenter);
		this.addNode(n);
	}
	
	private void parseNetworkConnection(String line)
	{
		String [] tokens = line.split("\"*\"");
		String node1 = tokens[1].trim();
		Side side1 = Side.valueOf(tokens[2].trim());
		String node2 = tokens[3].trim();
		Side side2 = Side.valueOf(tokens[4].trim());
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
	}
	
	/**
	 * Saves the contents of this model to its file.
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	public void save() throws FileNotFoundException, UnsupportedEncodingException
	{
		saveAs(this.getFileName());
	}
	
	/**
	 * Saves the contents of this model to the file specified by path
	 * @param path
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public void saveAs(String path) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter writer = new PrintWriter(path, "UTF-8");
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
		newNode.setNetwork(this);
		this.nodeList.add(newNode);
		this.unsavedChanges = true;
		this.nodeChanged(newNode);
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
	 * @param nodeName : the name of the node you want
	 * @return the specified NetworkNode if it exists, null otherwise.
	 */
	public NetworkNode getNode(String nodeName)
	{
		for (int i=0; i<this.nodeList.size(); i++)
		{
			NetworkNode n = this.nodeList.get(i);
			if (n.getName().compareTo(nodeName) == 0)
				return n;
		}
		
		return null;
	}

	/**
	 * Removes the specified object from the list of nodes.
	 * Also removes any connections to the removed node.
	 * @param i the index of the object to be removed.
	 */
	public void removeNode(int i)
	{
		NetworkNode n = this.nodeList.remove(i);
		
		for (int x=0; x<this.conList.size(); x++)
		{
			NetworkConnection c = this.conList.get(x);
			if (c.getNode1().compareTo(n.getName())==0 || c.getNode2().compareTo(n.getName())==0)
			{
				removeConnection(x);
			}
		}
		
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
	 * Registers a NetworkListener for notifications
	 * @param l The NetworkListener to be notified.
	 */
	public void addNetworkListener(NetworkListener l)
	{
		this.listeners.add(l);
	}
	
	/**
	 * Removes a NetworkListener from notifications
	 * @param l The NetworkListener to be removed.
	 */
	public void removeNetworkListener(NetworkListener l)
	{
		this.listeners.remove(l);
	}
	
	/**
	 * @return The number of listeners listening to this model
	 */
	public int nNetworkListeners()
	{
		return this.listeners.size();
	}
	
	public void nodeChanged(NetworkNode networkNode) 
	{
		this.unsavedChanges = true;
		for (int i=0; i<listeners.size(); i++)
		{
			listeners.get(i).nodeChanged(networkNode);
		}
	}
	
	//************************************************************************
	//**************************** TEST CASES ********************************
	//************************************************************************
	
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
		boolean result = true;
		System.out.println("testing NetworkModel");
		
		result &= testConstructors();
		
		try {
			result &= testGetFileName();
			result &= testSetFileName();
			result &= testSave();
			result &= testUnsavedChanges();
			result &= testAddNode();
			result &= testnNodes();
			result &= testGetNode();
			result &= testRemoveNode();
			result &= testAddConnection();
			result &= testnConnections();
			result &= testGetConnection();
			result &= testRemoveConnection();
			
			//TODO
			
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
			result = false;
		}
		
		if (result)
			System.out.println("NetworkModel OK");
	}

	private static boolean testConstructors()
	{
		boolean result = true;
		
		try {
			NetworkModel nm1 = new NetworkModel("test\\test.txt");
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		
		try {
			NetworkModel nm2 = new NetworkModel("bad_file");
			System.out.println("Exception not thrown for bad filename");
			result = false;
		} catch (FileNotFoundException e) {
			// Should throw this exception.
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			result = false;
		}
		
		return result;
	}

	private static boolean testGetFileName() throws FileNotFoundException, UnsupportedEncodingException
	{
		boolean result = true;
		
		NetworkModel nm1 = new NetworkModel("test\\test.txt");
		if (nm1.getFileName().compareTo("test\\test.txt")!=0)
		{
			System.out.println("Failed: getFileName");
			System.out.println("Should be: test.txt");
			System.out.println("Actual: "+nm1.getFileName());
			result = false;
		}
		
		return result;
	}

	private static boolean testSetFileName() throws FileNotFoundException, UnsupportedEncodingException
	{
		boolean result = true;

		NetworkModel nm1 = new NetworkModel("test\\test.txt");
		nm1.setFileName("new.txt");
		if (nm1.getFileName().compareTo("new.txt")!=0)
		{
			System.out.println("Failed: setFileName");
			result = false;
		}

		return result;
	}
	
	
	private static boolean testSave()
	{
		//TODO
		return true;
	}
	
	
	private static boolean testUnsavedChanges() throws FileNotFoundException, UnsupportedEncodingException
	{
		boolean result = true;
		NetworkModel nm1 = new NetworkModel("test\\test.txt");
		
		if (nm1.unsavedChanges())
		{
			System.out.println("Failed: unsavedChanges");
			result = false;
		}
		NetworkNode n = new NetworkNode("test", 0, 0);
		nm1.addNode(n);
		if (!nm1.unsavedChanges())
		{
			System.out.println("Failed: unsavedChanges");
			result = false;
		}
		
		return result;
	}

	private static boolean testAddNode() throws FileNotFoundException, UnsupportedEncodingException
	{
		boolean result = true;
		NetworkModel nm1 = new NetworkModel();
		NetworkNode n1 = new NetworkNode("test1", 0,0);
		NetworkNode n2 = new NetworkNode("test2", 10,10);
		nm1.addNode(n1);
		nm1.addNode(n2);
		if (nm1.nNodes() != 2)
		{
			System.out.println("Failed: addNode");
			result = false;
		}
		
		return result;
	}

	private static boolean testnNodes() throws FileNotFoundException, UnsupportedEncodingException
	{
		boolean result = true;
		NetworkModel nm1 = new NetworkModel("test\\test.txt");
		if (nm1.nNodes() != 3)
		{
			System.out.println("Failed: nNodes");
			result = false;
		}
		nm1.addNode(new NetworkNode("test1", 0,0));
		if (nm1.nNodes() != 4)
		{
			System.out.println("Failed: nNodes");
			result = false;
		}
		return result;
	}

	private static boolean testGetNode() throws FileNotFoundException, UnsupportedEncodingException
	{
		boolean result = true;
		NetworkModel nm1 = new NetworkModel("test\\test.txt");
		if (nm1.getNode(0).getName().compareTo("Central")!=0)
		{
			System.out.println("Failed: getNode");
			result = false;
		}
		return result;
	}

	private static boolean testRemoveNode() throws FileNotFoundException, UnsupportedEncodingException
	{
		boolean result = true;
		NetworkModel nm1 = new NetworkModel("test\\test.txt");
		nm1.removeNode(0);
		if (nm1.nNodes() != 2 || nm1.getNode(0).getName().compareTo("Authentication server")!=0)
		{
			System.out.println("Failed: removeNode");
			result = false;
		}
		if (nm1.nConnections() != 3)
		{
			System.out.println("Failed: removeNode Didn't remove connection");
			result = false;
		}
		return result;
	}

	private static boolean testAddConnection() throws FileNotFoundException, UnsupportedEncodingException
	{
		boolean result = true;
		NetworkModel nm1 = new NetworkModel("test\\test.txt");
		nm1.addNode(new NetworkNode("test", 0, 0));
		nm1.addConnection(new NetworkConnection("test", Side.T, "Central", Side.B));
		if (nm1.nConnections() != 6)
		{
			System.out.println("Failed: addConnection");
			result = false;
		}
		return result;
	}

	private static boolean testnConnections() throws FileNotFoundException, UnsupportedEncodingException
	{
		boolean result = true;
		NetworkModel nm1 = new NetworkModel("test\\test.txt");
		if (nm1.nConnections() != 5)
		{
			System.out.println("Failed: nConnections");
			result = false;
		}
		return result;
	}

	private static boolean testGetConnection() throws FileNotFoundException, UnsupportedEncodingException
	{
		boolean result = true;
		NetworkModel nm1 = new NetworkModel("test\\test.txt");
		NetworkConnection c = nm1.getConnection(0);
		if (c.getNode1().compareTo("Central") !=0 && c.getNode2().compareTo("Authentication server") !=0)
		{
			System.out.println("Failed: getConnection");
		}
		
		try {
			nm1.getConnection(5);
			System.out.println("Failed: getConnection");
		} catch (IndexOutOfBoundsException e) {
			//Should throw exception
		}
		
		return result;
	}
	
	private static boolean testRemoveConnection() throws FileNotFoundException, UnsupportedEncodingException
	{
		boolean result = true;
		NetworkModel nm1 = new NetworkModel("test\\test.txt");
		nm1.removeConnection(0);
		if (nm1.nConnections() != 4)
		{
			System.out.println("Failed: removeConnection");
			result = false;
		}
		nm1.removeConnection(3);
		if (nm1.nConnections() != 3)
		{
			System.out.println("Failed: removeConnection");
			result = false;
		}
		nm1.removeConnection(1);
		if (nm1.nConnections() != 2)
		{
			System.out.println("Failed: removeConnection");
			result = false;
		}
		return result;
	}
}












