package p3_simple_interaction;

public class GeometryDescriptor 
{
	private int charIndex;
	private int nodeIndex;
	private int connIndex;
	public GeometryDescriptor()
	{
		this.charIndex = -1;
		this.nodeIndex = -1;
		this.connIndex = -1;
	}
	public int getCharIndex() {
		return this.charIndex;
	}
	public void setCharIndex(int charIndex) {
		this.charIndex = charIndex;
	}
	public int getNodeIndex() {
		return this.nodeIndex;
	}
	public void setNodeIndex(int nodeIndex) {
		this.nodeIndex = nodeIndex;
	}
	public int getConnIndex() {
		return this.connIndex;
	}
	public void setConnIndex(int connIndex) {
		this.connIndex = connIndex;
	}
}
