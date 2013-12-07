package p6_undo_redo;

import java.awt.geom.AffineTransform;

public class NewTransformationCommand implements NetworkCommand 
{
	private AffineTransform transform;
	private NetworkModel networkModel;
	
	public NewTransformationCommand(AffineTransform at, NetworkModel networkModel) 
	{
		this.transform = at;
		this.networkModel = networkModel;
	}

	@Override
	public void doit() 
	{
		this.networkModel.addTransformationToList(transform);
		this.networkModel.transformChanged();
	}

	@Override
	public void undo() 
	{
		this.networkModel.removeTransformation(transform);
		this.networkModel.transformChanged();
	}

}
