//tree-view file manager (evolved into another "File Manager" project with JTable)
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class FileManager {
	JFrame f = new JFrame("File Manager");
	JTree tree;
	
	public void addFolder(DefaultMutableTreeNode node, TreePath path) {
		System.out.println(path);
		String strPath = "";
		Object[] objPath = path.getPath();
		//objPath[0] = "";
		for (int i = 1; i < objPath.length; i++) {
			strPath += objPath[i].toString() + "\\";
		}

		File dir = new File(strPath);
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			
			if (files.length != 0) {
				for (File file: files) {
					node.add(new DefaultMutableTreeNode(file.getName()));
					System.out.println(file.getName());
				}
			} else {
				node.add(new DefaultMutableTreeNode("<empty>"));
			}
		}// else System.out.println("Not a dir!");
	}
	
	public void start() {
		f.setSize(600, 400);
        f.getContentPane().setLayout(null);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode ("My PC");
        tree = new JTree(root);
        DefaultMutableTreeNode cdrive = new DefaultMutableTreeNode("C:\\");
        DefaultMutableTreeNode ddrive = new DefaultMutableTreeNode("D:\\");
        root.add(cdrive);
        root.add(ddrive);
        
        File dir = new File(cdrive.toString());
        System.out.println(dir.getAbsolutePath());
        
        JScrollPane s = new JScrollPane();
        s.setBounds(20,20,600,600);
        s.getViewport().add(tree);
        f.getContentPane().add(s);
        
        /*addFolder(cdrive, "C:\\");
        addFolder(ddrive, "D:\\");*/
        tree.addTreeSelectionListener(new TreeSelectionListener(){

            public void valueChanged(TreeSelectionEvent e) 
            {
                TreePath path = e.getPath();
                //JOptionPane.showMessageDialog(f, path.getLastPathComponent().toString());
                //((DefaultMutableTreeNode)path.getLastPathComponent()).setUserObject("testing");
                addFolder((DefaultMutableTreeNode)path.getLastPathComponent(), path);
            }
        });
        
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static void main(String[] args) {
		FileManager fm = new FileManager();
		fm.start();
	}
}
