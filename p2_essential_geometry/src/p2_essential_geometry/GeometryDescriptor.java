package p2_essential_geometry;

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
		return charIndex;
	}
	public void setCharIndex(int charIndex) {
		this.charIndex = charIndex;
	}
	public int getNodeIndex() {
		return nodeIndex;
	}
	public void setNodeIndex(int nodeIndex) {
		this.nodeIndex = nodeIndex;
	}
	public int getConnIndex() {
		return connIndex;
	}
	public void setConnIndex(int connIndex) {
		this.connIndex = connIndex;
	}
}
