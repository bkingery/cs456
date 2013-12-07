package p6_undo_redo;

import java.awt.geom.AffineTransform;
import java.util.Stack;

public class ClearTransformsCommand implements NetworkCommand {

	private Stack<AffineTransform> oldTransforms;
	private NetworkModel networkModel;
	
	public ClearTransformsCommand(Stack<AffineTransform> transformations, NetworkModel networkModel) 
	{
		this.oldTransforms = new Stack<AffineTransform>();
		for (int i=0; i< transformations.size(); i++)
			oldTransforms.add(transformations.get(i));
		this.networkModel = networkModel;
	}

	@Override
	public void doit() 
	{
		this.networkModel.clearTransformationList();
		this.networkModel.transformChanged();
	}

	@Override
	public void undo() 
	{
		for (int i=0; i<oldTransforms.size(); i++)
		{
			networkModel.addTransformationToList(oldTransforms.get(i));
		}
		this.networkModel.transformChanged();
	}

}
