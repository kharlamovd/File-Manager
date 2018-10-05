import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class lesson43 {

    /**
     * @param args the command line arguments
     */
    
    JFrame f = new JFrame();
    JTree tree;

    public void start(){
        f.setSize(600, 400);
        f.getContentPane().setLayout(null);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode ("Cars");

        root.add(new DefaultMutableTreeNode("BMW"));
        root.add(new DefaultMutableTreeNode("Audi"));
        root.add(new DefaultMutableTreeNode("Mers"));
        
        tree = new JTree(root);
        TreeModel model = tree.getModel();
        tree.setRootVisible(true);

        JScrollPane s = new JScrollPane();
        s.setBounds(20,20,300,300);
        s.getViewport().add(tree);

        tree.addTreeSelectionListener(new TreeSelectionListener(){

            public void valueChanged(TreeSelectionEvent e) 
            {
                TreePath path = e.getPath();
                //JOptionPane.showMessageDialog(f, path.getLastPathComponent().toString());
                //((DefaultMutableTreeNode)path.getLastPathComponent()).setUserObject("testing");
                ((DefaultMutableTreeNode)path.getLastPathComponent()).add(new DefaultMutableTreeNode("ok"));
            }
        });
        //tree.setBounds(20,20,300,300);
        f.getContentPane().add(s);

        //double stime = System.currentTimeMillis();
        
        MouseListener ml = new MouseAdapter() 
        {
        	ActionListener ac = new ActionListener() {
        		@Override
                public void actionPerformed(ActionEvent e) {
                 System.out.println("Single click!");
                 timer.stop();
                }
            };
        	
         Timer timer = new Timer(300, ac);
         
            public void mousePressed(MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if(selRow != -1) 
                {
                    if(e.getClickCount() == 1) {
                     timer.start();
                    }
                    else if(e.getClickCount() == 2) {
                     System.out.println("Double click!");
                     timer.stop();
                    }
                }
            }
        };
        
        f.addMouseListener(ml);
        /*f.addMouseListener(new MouseListener(){

            public void mouseClicked(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet.");
                TreePath path = tree.getSelectionPath();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) ((DefaultMutableTreeNode)path.getLastPathComponent());
                node.setUserObject("NEW!!!");
                tree.updateUI();
            }

            public void mousePressed(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            public void mouseReleased(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            public void mouseEntered(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

            public void mouseExited(MouseEvent e) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }
        });*/
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
    }

    public static void main(String[] args) {
        // TODO code application logic here
    	lesson43 m = new lesson43();
        m.start();
    }

}