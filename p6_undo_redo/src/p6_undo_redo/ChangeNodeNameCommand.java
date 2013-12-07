package p6_undo_redo;

public class ChangeNodeNameCommand implements NetworkCommand 
{
	private NetworkNode node;
	private NetworkModel networkModel;
	private String oldName;
	private String newName;

	public ChangeNodeNameCommand(NetworkNode n, String newName)
	{
		this.node = n;
		this.networkModel = n.getNetwork();
		this.oldName = n.getName();
		this.newName = newName;
	}
	
	@Override
	public void doit() 
	{
		node.setName(newName);
		networkModel.nodeChanged(node);
	}

	@Override
	public void undo() 
	{
		node.setName(oldName);
		networkModel.nodeChanged(node);
	}

}
