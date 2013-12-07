package p6_undo_redo;

public class NewConnectionCommand implements NetworkCommand 
{
	private NetworkConnection connection;
	private NetworkModel networkModel;
	
	public NewConnectionCommand(NetworkConnection con, NetworkModel nm)
	{
		this.connection = con;
		this.networkModel = nm;
	}

	@Override
	public void doit() 
	{
		this.networkModel.addConnectionToList(connection);
		this.networkModel.connectionChanged(connection);
	}

	@Override
	public void undo() 
	{
		this.networkModel.removeConnection(connection);
		this.networkModel.connectionChanged(connection);
	}

}
