package p6_undo_redo;

public interface NetworkListener {

	void nodeChanged(NetworkNode n);

	void connectionChanged(NetworkConnection c);

	void transformChanged();

}
