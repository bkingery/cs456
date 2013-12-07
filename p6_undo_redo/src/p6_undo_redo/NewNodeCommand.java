package p6_undo_redo;


public class NewNodeCommand implements NetworkCommand 
{
	private NetworkNode node;
	private NetworkModel networkModel;
	
	public NewNodeCommand(NetworkNode n, NetworkModel networkModel)
	{
		this.node = n;
		this.networkModel = networkModel;
	}
	
	@Override
	public void doit() 
	{
		this.node.setNetwork(this.networkModel);
		this.networkModel.addNodeToList(this.node);
		this.networkModel.nodeChanged(node);
	}

	@Override
	public void undo() 
	{
		this.node.setNetwork(null);
		this.networkModel.removeNode(node);
	}

}
