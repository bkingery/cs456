package p6_undo_redo;

public class ChangeNodePositionCommand implements NetworkCommand 
{
	private NetworkNode node;
	private NetworkModel networkModel;
	private double newX;
	private double newY;
	private double oldX;
	private double oldY;
	
	public ChangeNodePositionCommand(NetworkNode n, double x, double y)
	{
		this.node = n;
		this.networkModel = node.getNetwork();
		this.oldX = node.getX();
		this.oldY = node.getY();
		this.newX = x;
		this.newY = y;
	}
	
	@Override
	public void doit() 
	{
		node.setLocation(newX, newY);
		networkModel.nodeChanged(node);
	}

	@Override
	public void undo() 
	{
		node.setLocation(oldX, oldY);
		networkModel.nodeChanged(node);
	}

}
