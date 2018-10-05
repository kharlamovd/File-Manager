import java.util.HashMap;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

@SuppressWarnings("serial")
public class FileTree extends JTree {
	HashMap<String, DefaultMutableTreeNode> nodes = new HashMap<String, DefaultMutableTreeNode>(); 
	
	public FileTree(DefaultMutableTreeNode root) {
		super(root);
	}
	
	public void addNodeToMap(DefaultMutableTreeNode node) {
		nodes.put(node.toString(), node);
	}
	
	public DefaultMutableTreeNode getNode(String node) {
		return nodes.get(node);
	}
}
