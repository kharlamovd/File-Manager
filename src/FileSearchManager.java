//file manager created with the help of two panels (JTree+JTable)
/* things to perform:
 * -normal dimension of right component of split panel when resized
 * -synchronized tab paths
 * -moving tabs from left panel to right (fix exp)
 * -debug tabbed pane popup actions
 * -not to highlight ext when renaming
 * -tree expand for table operations
 * -pop-up menu for jtree
 * -key shortcuts
 * -??enum string constants
 * -xml/json settings
 */
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.Highlighter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

	  
class TabThread extends Thread implements ChangeListener, ActionListener/*, DragGestureListener, DropTargetListener*/ {
	JFrame parentFrame;
	JTabbedPane parentPane;
	JPanel currentPanel = new JPanel();
	JSplitPane splitPane;
	FileTree tree;
	JTable table, dragSource;
	DefaultTableModel tableModel;
	DefaultTableModel drivesModel;
	CustomCellRenderer cellRenderer = new CustomCellRenderer();
	JButton jumpButton;
	JButton searchButton;
	JButton closeSearchButton;
	JButton waitSearchButton;
	File dir;
	JTextField path;
	JPopupMenu popupMenu = new JPopupMenu();
	JPopupMenu tabTitleMenu;
	ArrayList<File> copyBuffer = new ArrayList<File>();
	File cutBuffer = null;
    int popupRow = -1;
    int tabId, dragRow;
    int enterNum = 0;
    boolean copyOperation = true;
    ArrayList<TabThread> threads; 
    FileSearchManager fileMgr;
    FileTab fileTab;
    SearchThread searchThread;
    SearchThread ssdSearchThread;
    ActionListener stopSearchAction, continueSearchAction;
    public volatile boolean terminated = false;
    
	public TabThread(FileSearchManager fileMgr, FileTab fileTab, int numOfTabs, ArrayList<TabThread> threads, File dirTo) throws UnsupportedEncodingException {
		parentFrame = fileMgr.f;
		this.tree = fileMgr.tree;
		parentPane = fileTab.tabbedPane;
		tabId = numOfTabs;
		this.copyBuffer = fileTab.copyBuffer;
		this.path = fileTab.path;
		this.jumpButton = fileTab.jumpButton;
		this.searchButton = fileTab.searchButton;
		waitSearchButton = new JButton(new String(FileSearchManager.prop.getProperty("stopSearch").getBytes(FileSearchManager.PROPERTY_ENCODING)));
		this.threads = threads;
		this.fileMgr = fileMgr;
		this.fileTab = fileTab;
		this.closeSearchButton = fileTab.closeSearchButton;
		
		if (dirTo != null && dirTo.isDirectory()) {
			dir = dirTo;
		}
		//currentPanel.setLayout(null);
	}
	
	public TreePath getPath(TreeNode[] nodePath) {
    	List<Object> path = new ArrayList<Object>();
        if (nodePath != null) {
          for (TreeNode node: nodePath) {
        	  path.add(node);
          }
        }
        return path.isEmpty() ? null : new TreePath(path.toArray());
    }
    
    public static String getStringPath(TreePath path) {
    	String strPath = "";
		Object[] objPath = path.getPath();
		//objPath[0] = "";
		for (int i = 1; i < objPath.length; i++) {
			strPath += objPath[i].toString() + "\\";
		}
		return strPath;
    }
   
    public static TreePath getTreePath(String path) throws UnsupportedEncodingException {
    	//ArrayList<DefaultMutableTreeNode> nodes = new ArrayList<DefaultMutableTreeNode>();
    	//String anotherNode = "";
    	
    	/*String[] strNodes = path.split("\\\\");
    	DefaultMutableTreeNode root = new DefaultMutableTreeNode("My PC"), last;
    	root.add(last = new DefaultMutableTreeNode(strNodes[0]));
    	
    	for (int i = 1; i < strNodes.length; i++) {
    		DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(strNodes[i]);
    		last.add(newNode);
    		last = newNode;
    	}*/
    	/*ArrayList<DefaultMutableTreeNode> nodes = new ArrayList<DefaultMutableTreeNode>();
    	DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
    	nodes.add(root);
    	int chldCnt = root.getChildCount();
    	for (int i = 0; i < chldCnt; i++) {
    		DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
    		if (path.contains(root.getChildAt(i).toString())) {
    			nodes.add(child);
    		}
    	}*/
    	String[] strNodes = path.split("\\\\");
    	ArrayList<DefaultMutableTreeNode> root = new ArrayList<DefaultMutableTreeNode>();
    	//root.add(last = new DefaultMutableTreeNode(strNodes[0]));
    	root.add(new DefaultMutableTreeNode(new String(FileSearchManager.prop.getProperty("myPc").getBytes(FileSearchManager.PROPERTY_ENCODING))));
    	root.add(new DefaultMutableTreeNode(strNodes[0]+'\\'));
    	
    	for (int i = 1; i < strNodes.length; i++) {
    		root.add(new DefaultMutableTreeNode(strNodes[i]));
    	}
    	return new TreePath(root.toArray());
    	
    	/*for (int i = path.length()-1; i >= 0; i--) {
    		char curr = path.charAt(i);
    		if (curr == '\\' && !anotherNode.equals("")) {
    			nodes.add(new DefaultMutableTreeNode(new StringBuilder(anotherNode).reverse().toString()));
    			anotherNode = "";
    		} else if (curr == ':' && path.charAt(i+1) == '\\') {
    			anotherNode += "\\" + curr; 
    		} else if (curr != ':' && curr != '\\') {
    			anotherNode += curr;
    		}
    	}*/
    	//nodes.add(new DefaultMutableTreeNode(new StringBuilder(anotherNode).reverse().toString()));
    	//nodes.add(new DefaultMutableTreeNode("My PC"));
    	//Collections.reverse(nodes);
    	
    	/*System.out.println(root.toString());
    	return new TreePath(root);*/
    }
    
    public void repaintPath(String path) throws UnsupportedEncodingException {
    	TreePath treePath = getTreePath(path);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
        
        TreeModel model = tree.getModel();
        int childCnt = model.getChildCount(node);
        for (int i = 0; i < childCnt; i++) {
        	DefaultMutableTreeNode children = (DefaultMutableTreeNode) model.getChild(node, i);
        	if (children.isLeaf()) {
	        	File child = new File(TabThread.getStringPath(treePath)+"\\"+children.toString()); 
	        	if (child.isDirectory()) {
	        		File[] childDirs = child.listFiles();
	        		if (childDirs != null && childDirs.length != 0) {
		        		for (File childDir: childDirs) {
		        			if (childDir.isDirectory()) {
		        				children.add(new DefaultMutableTreeNode(childDir.getName()));
		        			}
		        		}
	        		}
	        	}
        	}
        }
        tree.repaint();
    }
    
    public static String getExt(String str) {
    	String ext = "";
    	for (int i = str.length()-1; i >= 0; i--) {
    		if (str.charAt(i) != '.') {
    			ext += str.charAt(i);
    		} else break;
    	}
    	
    	return new StringBuilder(ext).reverse().toString();
    }
    
    public void copyMultipleFiles(Path from, Path to, boolean copyOperaiton) throws IOException {
    	File fromFile = from.toFile();
    	if (fromFile.isDirectory()) {
    		File fromFiles[] = fromFile.listFiles();
    		if (copyOperaiton) {
	    		for (File file: fromFiles) {
	    			if (file.isFile()) {
	    				Files.copy(file.toPath(), Paths.get(to.toString()+"\\"+file.getName()), StandardCopyOption.REPLACE_EXISTING);
	    			} else if (file.isDirectory()) {
	    				File newDir = new File(to.toString()+"\\"+file.getName());
	    				newDir.mkdirs();
	    				copyMultipleFiles(file.toPath(), newDir.toPath(), copyOperaiton);
	    			}
	    		}
    		} else {
    			for (File file: fromFiles) {
	    			if (file.isFile()) {
	    				Files.move(file.toPath(), Paths.get(to.toString()+"\\"+file.getName()), StandardCopyOption.REPLACE_EXISTING);
	    			} else if (file.isDirectory()) {
	    				File newDir = new File(to.toString()+"\\"+file.getName());
	    				newDir.mkdirs();
	    				copyMultipleFiles(file.toPath(), newDir.toPath(), copyOperaiton);
	    			}
	    		}
    		}
    	}
    }
    
    public void deleteMulitpleFiles(File dir) {
    	if (dir.isDirectory()) {
    		File[] files = dir.listFiles();
    		for (File file: files) {
    			if (file.isFile()) {
    				file.delete();
    			} else if (file.isDirectory()) {
    				deleteMulitpleFiles(file);
    				file.delete();
    			}
    		}
    	}
    }
    
    public void jumpToPath(File inputFile, DefaultTableModel tableModel) throws UnsupportedEncodingException {
    	if (inputFile != null && inputFile.exists()) {
			dir = inputFile;
            File[] dirFiles = dir.listFiles();
            
            if (dirFiles != null && dirFiles.length != 0) {
            	if (table.getColumnCount() < 4) table.setModel(tableModel);
            	table.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
            	path.setText(dir.getAbsolutePath());
            	parentPane.setTitleAt(tabId, dir.getAbsolutePath());
            	tableModel.setRowCount(0);
            	tableModel.addRow(new Object[]{"..", "", "", ""});
            	for (File f: dirFiles) {
            		BasicFileAttributes attr;
            		try {
            			attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
            			String name = f.getName();
            			Object rowObj[] = {name, "", "", convertDate(attr.creationTime())};
            			if (f.isFile()) {
            				rowObj[1] = getExt(name); 
            				rowObj[2] = convertFileSize(attr.size()); 
            			}
            			tableModel.addRow(rowObj);
            		} catch (IOException e1) {
            			e1.printStackTrace();
            		}
            	}
            	//expandTreePath(dir.getName());
            	//DefaultMutableTreeNode node = new DefaultMutableTreeNode(dir.getName());
            	//System.out.println(node.get);
            	//tree.setSelectionPath(new TreePath(node.getPath()));
            }	
            else if (dirFiles.length == 0) {
            	path.setText(dir.getAbsolutePath());
            	parentPane.setTitleAt(tabId, dir.getAbsolutePath());
            	tableModel.setRowCount(0);
            	tableModel.addRow(new Object[]{"..", "", "", ""});
            	//DefaultMutableTreeNode node = new DefaultMutableTreeNode(dir.getName());
            	//System.out.println(node.toString());
            	expandTreePath(dir.getName());
            	//tree.setSelectionPath(new TreePath(node.getPath()));
    		}
		} else if (inputFile == null || inputFile.getName().equals("") || inputFile.getName().equals("My PC")) {
			tableModel.setRowCount(0);
			table.setModel(drivesModel);
			table.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
			drivesModel.setRowCount(0);
        	dir = null;
        	path.setText("");
        	parentPane.setTitleAt(tabId, "\\");
        	Drive drives[] = FileSearchManager.LOCAL_DRIVES;
        	for (Drive drive: drives) {
        		tableModel.addRow(new Object[]{drive.drive.getAbsolutePath(), convertFileSize(drive.drive.getFreeSpace()), convertFileSize(drive.drive.getTotalSpace())});
        	}
        	//tree.expandPath(new TreePath(new DefaultMutableTreeNode[] {new DefaultMutableTreeNode("My PC")}));
        	//expandTreePath(new String(FileSearchManager.prop.getProperty("myPc").getBytes(FileSearchManager.PROPERTY_ENCODING)));
		} else {
			JOptionPane.showMessageDialog(parentFrame, new String(FileSearchManager.prop.getProperty("invalidPath").getBytes(FileSearchManager.PROPERTY_ENCODING)));
		}
    }
	
    public String[] copyBufferToStringArray(ArrayList<File> copyBuffer) {
    	if (!copyBuffer.isEmpty()) {
    		int size = copyBuffer.size();
    		File[] fileArr = new File[size];
	    	copyBuffer.toArray(fileArr);
	    	String[] result = new String[size];
	    	
	    	for (int i = 0; i < size; i++) {
	    		result[i] = fileArr[i].getAbsolutePath();
	    	}
	    	
	    	return result;
    	} else {
    		return null;
    	}
    }
    
    /*public static List<Component> getAllComponents(final Container c) {
        Component[] comps = c.getComponents();
        List<Component> compList = new ArrayList<Component>();
        for (Component comp : comps) {
            compList.add(comp);
            if (comp instanceof Container)
                compList.addAll(getAllComponents((Container) comp));
        }
        return compList;
    }*/
    public boolean hasJMenuItem(String item, JMenu menu) {
    	Component[] comps = menu.getMenuComponents();
    	for (Component comp: comps) {
    		if (comp instanceof JMenuItem) {
    			JMenuItem itemComp = (JMenuItem) comp;
    			if (itemComp.getText().equals(item)) {
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    public String convertDate(FileTime dateTime) {
    	String strDate = dateTime.toString();
    	strDate = strDate.replace("T", " ").replace("Z", "").replace("-", ".");
    	String[] dateTimeRaw = strDate.split("\\s");
    	String[] date = dateTimeRaw[0].split("\\.");
    	String[] time = dateTimeRaw[1].split("\\."); 
    	strDate = date[2] + "." + date[1] + "." + date[0] + " " + time[0];
    	return strDate;
    }
    
    public static long calcFolderSize(File folder) {
    	long size = 0;
    	System.out.println(folder.getAbsolutePath());
    	if (folder.isDirectory()) {
	    	File[] folderFiles = folder.listFiles();
	    	
	    	if (folderFiles == null) return 0;
	    	for (File file: folderFiles) {
	    		size += file.length();
	    		if (file.isDirectory()) {
	    			size += calcFolderSize(file);
	    		}
	    	}
	    	return size;
    	} else {
    		return folder.length();
    	}
    }
    
    public static String convertFileSize(double size) throws UnsupportedEncodingException {
    	String strSize = null;
    	if (size > 1024*1024*1024) {
    		strSize = Double.toString(BigDecimal.valueOf(size / (1024*1024*1024))
    			    .setScale(2, RoundingMode.HALF_UP)
    			    .doubleValue())+" "+new String(FileSearchManager.prop.getProperty("gb").getBytes(FileSearchManager.PROPERTY_ENCODING));
    	} else if (size > 1024*1024) {
    		strSize = Double.toString(BigDecimal.valueOf(size / (1024*1024))
    			    .setScale(2, RoundingMode.HALF_UP)
    			    .doubleValue())+" "+new String(FileSearchManager.prop.getProperty("mb").getBytes(FileSearchManager.PROPERTY_ENCODING));
    	} else if (size > 1024) {
    		strSize = Double.toString(BigDecimal.valueOf(size / (1024))
    			    .setScale(2, RoundingMode.HALF_UP)
    			    .doubleValue())+" "+new String(FileSearchManager.prop.getProperty("kb").getBytes(FileSearchManager.PROPERTY_ENCODING));
    	} else {
    		strSize = Integer.toString((int) size)+" "+new String(FileSearchManager.prop.getProperty("bytesConst").getBytes(FileSearchManager.PROPERTY_ENCODING));
    	}
    	return strSize;
    }
    
    public void terminate() {
    	tabId = -1;
    	parentPane.removeChangeListener(this);
    	Component[] components = tabTitleMenu.getComponents();
    	for (Component cmp: components) {
    		if (((JMenuItem) cmp).getText().equals("Close tab (Ctrl+W)")) {
    			((JMenuItem) cmp).removeActionListener(this);
    			break;
    		}
    	}
    }
    
    public void expandTreePath(String searchNode) {
    	TreeNode expNode = (TreeNode)tree.getModel().getRoot();
    	TreeNode[] nodes = tree.getNode(searchNode).getPath();
    	DefaultMutableTreeNode tempNode;
    	int count = 0;
    	for(int i = 0; i < nodes.length; i++) {
    		tempNode = (DefaultMutableTreeNode) nodes[i];
    		count += expNode.getIndex(tempNode)+1;
    		tree.expandRow(count);
    		expNode = tempNode;
    	}
    }
    
    /*@Override
	public void dragGestureRecognized(DragGestureEvent event) {
	    Component cmp = event.getComponent();
	    cmp.toString();
	    dragSource = (JTable) event.getComponent();
	    dragRow = dragSource.rowAtPoint(event.getDragOrigin());
	    System.out.println("Drag gesture component: "+dragSource.getValueAt(0, 0));
	}
    
    @Override
	public void dragEnter(DropTargetDragEvent arg0) {}

	@Override
	public void dragExit(DropTargetEvent arg0) {}

	@Override
	public void dragOver(DropTargetDragEvent arg0) {}

	@Override
	public void drop(DropTargetDropEvent arg0) {
		Transferable tr = arg0.getTransferable();
		DataFlavor[] df = tr.getTransferDataFlavors();
		for (DataFlavor fl: df) {
			System.out.println(fl.getRepresentationClass().toString());
			if (fl.equals(DataFlavor.stringFlavor)) {
				try {
					String data = (String) tr.getTransferData(fl);
					String[] draggedRow = data.split("[\t]");
					arg0.acceptDrop(DnDConstants.ACTION_MOVE);
					//System.out.println(tableModel.getValueAt(0, 0));
					//tableModel.arg0.getLocation();
					
					/*int matchNum = 0;
					int colCnt = tableModel.getColumnCount();
					for (int i = 0; i < tableModel.getRowCount(); i++) {
						for (int k = 0; k < colCnt; k++) {
							if (tableModel.getValueAt(i, k).equals(draggedRow[k])) {
								matchNum++;
							}
						}
						if (matchNum == colCnt) {
							tableModel.removeRow(i);
							break;
						}
					}*/
					/*JTable targetTable = (JTable)((DropTarget)arg0.getSource()).getComponent();
					
					((DefaultTableModel) targetTable.getModel()).addRow(draggedRow);
					((DefaultTableModel) dragSource.getModel()).removeRow(dragRow);
					arg0.dropComplete(true);
					
					//System.out.println(tr.getTransferData(fl));
					break;
				} catch (UnsupportedFlavorException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}*/
    
    @Override
	public void actionPerformed(ActionEvent arg0) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
			
		if (tabId == parentPane.getSelectedIndex()/* && !threads.get(tabId).terminated*/) {
			/*try {
				Field stillbornField = this.getClass().getSuperclass().getDeclaredField("stillborn");
				stillbornField.setAccessible(true);
				boolean stillborn = (boolean) stillbornField.get(this);
				if (!stillborn) {*/
					int initTabId = tabId;
					parentPane.removeTabAt(tabId);
					threads.remove(tabId);
					TabThread.this.terminate();//d = true;
					
					int size = threads.size();
					for (int i = initTabId; i < size; i++) {
						threads.get(i).tabId--;
					}

					if (size == 0) {
						fileTab.currentTab = null;
					} else if (initTabId == 0) {
						fileTab.currentTab = threads.get(0);
					} else {
						fileTab.currentTab = threads.get(initTabId-1);
					}

					//if (currentTab == threads.get(tabId)) {
					//}
					fileTab.tabNumber--;
				}
			/*} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}*/
	}
					
		
		});
	}
    
    @Override
	public void stateChanged(ChangeEvent e) {
		if (tabId == parentPane.getSelectedIndex()/* && !threads.get(tabId).terminated*/) {
			fileTab.currentTab = TabThread.this;
			System.out.println("Current tabId: "+fileTab.currentTab.tabId+", state: "+fileTab.currentTab.getState().toString()+", terminated: "+fileTab.currentTab.terminated);
			if (table.getColumnCount() > 1) {
				waitSearchButton.setVisible(false);
				closeSearchButton.setVisible(false);
			} else if (searchThread != null) {
					if (searchThread.isAlive()) {
						waitSearchButton.setVisible(true);
						closeSearchButton.setVisible(true);
					} else {
						waitSearchButton.setVisible(false);
						closeSearchButton.setVisible(true);
					}
			}
			File curDir = fileTab.currentTab.dir;
			if (curDir == null) {
				path.setText("");
			} else {
				path.setText(curDir.getAbsolutePath());
			}
			/*File tabDir = currentTab.dir;
			if (tabDir == null)
				path.setText("");
			else
				path.setText(tabDir.getAbsolutePath());*/
			//notify();
		}
	}
    
    public final DefaultMutableTreeNode findNode(String searchString) {

        List<DefaultMutableTreeNode> searchNodes = getSearchNodes((DefaultMutableTreeNode)tree.getModel().getRoot());
        DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();

        DefaultMutableTreeNode foundNode = null;
        int bookmark = -1;

        if( currentNode != null ) {
            for(int index = 0; index < searchNodes.size(); index++) {
                if( searchNodes.get(index) == currentNode ) {
                    bookmark = index;
                    break;
                }
            }
        }

        for(int index = bookmark + 1; index < searchNodes.size(); index++) {    
            if(searchNodes.get(index).toString().toLowerCase().contains(searchString.toLowerCase())) {
                foundNode = searchNodes.get(index);
                break;
            }
        }

        if( foundNode == null ) {
            for(int index = 0; index <= bookmark; index++) {    
                if(searchNodes.get(index).toString().toLowerCase().contains(searchString.toLowerCase())) {
                    foundNode = searchNodes.get(index);
                    break;
                }
            }
        }
        return foundNode;
    }
    
        private final List<DefaultMutableTreeNode> getSearchNodes(DefaultMutableTreeNode root) {
            List<DefaultMutableTreeNode> searchNodes = new ArrayList<DefaultMutableTreeNode>();

            Enumeration<?> e = root.preorderEnumeration();
            while(e.hasMoreElements()) {
                searchNodes.add((DefaultMutableTreeNode)e.nextElement());
            }
            return searchNodes;
        }
        
	@SuppressWarnings("serial")
	@Override
	public void run() {
		//while (running) {
        //currentPanel.add(panel1);
		try {
			tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				if (tabId == parentPane.getSelectedIndex()) {
					String strPath = getStringPath(e.getPath());
			        dir = new File(strPath);
			        File[] dirFiles = dir.listFiles();
			        //JTree tree = (JTree) e.getSource();
			        
			        if (dirFiles != null) {
			        	if (dirFiles.length != 0) {
			        		if (table.getColumnCount() < 4) table.setModel(tableModel);
			        		table.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
			        		path.setText(dir.getAbsolutePath());
				        	parentPane.setTitleAt(parentPane.getSelectedIndex(), dir.getAbsolutePath());
				        	
				        	/*if (table.getColumnCount() == 1) {
				        		table.setModel(tableModel);
				        	}*/
				        	tableModel.setRowCount(0);
				        	tableModel.addRow(new Object[]{"..", "", "", ""});
				            	for (File f: dirFiles) {
				            		BasicFileAttributes attr;
									try {
										attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
										Object rowObj[] = {f.getName(), "", attr.size(), attr.creationTime()};
										if (f.isFile()) {
											rowObj[1] = TabThread.getExt(f.getName()); 
										}
										else {
											rowObj[1] = "";
										}
										tableModel.addRow(rowObj);
									} catch (IOException e1) {
										e1.printStackTrace();
									}
				            	}
					        } else {
					        	tableModel.setRowCount(0);
				        		path.setText(dir.getAbsolutePath());
				        		tableModel.addRow(new Object[] {"..", "", "", ""});
				        	}
			        }
			        else {
			        	if (strPath.equals("")) {
			        		tableModel.setRowCount(0);
		                	table.setModel(drivesModel);
		                	table.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
		                	drivesModel.setRowCount(0);
			            	dir = null;
			            	path.setText("\\");
			            	parentPane.setTitleAt(parentPane.getSelectedIndex(), "\\");
			            	Drive drives[] = FileSearchManager.LOCAL_DRIVES;
			            	try {
				            	for (Drive drive: drives) {
				            		drivesModel.addRow(new Object[]{drive.drive.getAbsolutePath(), convertFileSize(drive.drive.getFreeSpace()), convertFileSize(drive.drive.getTotalSpace())});
				            	}
			            	} catch (HeadlessException | UnsupportedEncodingException ex) {
								ex.printStackTrace();
							}
			        	} else {
			        		tableModel.setRowCount(0);
			        		path.setText(dir.getAbsolutePath());
			        		tableModel.addRow(new Object[] {"..", "", "", ""});
			        	}
			        	// else {
			            	//JOptionPane.showMessageDialog(parentFrame, "Sorry, you have to be an administrator to get access to this folder.");
			        	//}
			        }
			        
				}
					tree.clearSelection();
				}
			});
			
	        tableModel = new DefaultTableModel(null, new Object [] {new String(FileSearchManager.prop.getProperty("fileName").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("ext").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("sizeConst").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("creationDate").getBytes(FileSearchManager.PROPERTY_ENCODING))});
	        drivesModel = new DefaultTableModel(null, new Object[] {new String(FileSearchManager.prop.getProperty("localDrive").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("freeSpace").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("totalSpace").getBytes(FileSearchManager.PROPERTY_ENCODING))});
	        table = new JTable(drivesModel) {
	        	public boolean isCellEditable(int row, int column) {                
	                return false;               
	        	};
	        };
	        table.setRowSelectionAllowed(false);
	        table.setFont(fileMgr.settings.getFont());
	        table.getTableHeader().setReorderingAllowed(false);
	        table.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
	        Drive drives[] = FileSearchManager.LOCAL_DRIVES;
	        for (Drive drive: drives) {
	        	drivesModel.addRow(new Object[]{drive.drive.getAbsolutePath(), convertFileSize(drive.drive.getFreeSpace()), convertFileSize(drive.drive.getTotalSpace())});
	    	}
	        table.setDragEnabled(true);
	        new DropTarget(table, new DropTargetListener() {

				@Override
				public void dragEnter(DropTargetDragEvent arg0) {}

				@Override
				public void dragExit(DropTargetEvent arg0) {}

				@Override
				public void dragOver(DropTargetDragEvent arg0) {
					Component row = ((JTable) arg0.getSource()).getComponentAt(arg0.getLocation());
					System.out.println(row.toString());
				}

				@Override
				public void drop(DropTargetDropEvent arg0) {
					Transferable tr = arg0.getTransferable();
					DataFlavor[] df = tr.getTransferDataFlavors();
					for (DataFlavor fl: df) {
						System.out.println(fl.getRepresentationClass().toString());
						if (fl.equals(DataFlavor.stringFlavor)) {
							try {
								String data = (String) tr.getTransferData(fl);
								String[] draggedRow = data.split("[\t]");
								arg0.acceptDrop(DnDConstants.ACTION_MOVE);
								//System.out.println(tableModel.getValueAt(0, 0));
								//tableModel.arg0.getLocation();
								
								/*int matchNum = 0;
								int colCnt = tableModel.getColumnCount();
								for (int i = 0; i < tableModel.getRowCount(); i++) {
									for (int k = 0; k < colCnt; k++) {
										if (tableModel.getValueAt(i, k).equals(draggedRow[k])) {
											matchNum++;
										}
									}
									if (matchNum == colCnt) {
										tableModel.removeRow(i);
										break;
									}
								}*/
								JTable targetTable = (JTable)((DropTarget)arg0.getSource()).getComponent();
								
								((DefaultTableModel) targetTable.getModel()).addRow(draggedRow);
								arg0.dropComplete(true);
								
								break;
							} catch (UnsupportedFlavorException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
				
				@Override
				public void dropActionChanged(DropTargetDragEvent arg0) {}
	        	
	        });
	        //new DragSource().createDefaultDragGestureRecognizer(table, DnDConstants.ACTION_MOVE, this);
	        
	        JScrollPane tableScroll = new JScrollPane(table);
	        //tableScroll.setBounds(240,50,780,645);
	        currentPanel.setLayout(new BorderLayout());
	        currentPanel.add(tableScroll, BorderLayout.CENTER);
	        //panel2.add(tableScroll);
	        //parentFrame.setContentPane(splitPane);
	        
	        ListSelectionModel listModel = table.getSelectionModel();
	        listModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	        listModel.addListSelectionListener(new ListSelectionListener() {
	
	            public void valueChanged(ListSelectionEvent e) 
	            {
		                int row = table.getSelectedRow();
		                int col = table.getSelectedColumn();
		                if (row < 0 || col < 0) return;
		                listModel.removeListSelectionListener(this);
		                if (dir != null)
			                System.out.println("(In fact) Entering ListSelectionListener: "+dir.getAbsolutePath());
		                System.out.println(row+", "+col+", "+table.getValueAt(row, col));
		                
		                if (table.getColumnCount() == 1) {
		                	File foundFile = new File((String) table.getValueAt(row, col)); 
		                	if (foundFile.isDirectory()) {
		                		if (closeSearchButton.isVisible() || waitSearchButton.isVisible()) {
					        		closeSearchButton.setVisible(false);
					        		waitSearchButton.setVisible(false);
		                		}
		                		dir = foundFile;
		                		File[] newFiles = dir.listFiles();
		                		
		                		table.setModel(tableModel);
		                		table.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
			                	tableModel.setRowCount(0);
			                	tableModel.addRow(new Object[]{"..", "", "", ""});
			                	if (newFiles != null && newFiles.length != 0) { 
				                	for (File f: newFiles) {
				                		BasicFileAttributes attr;
										try {
											attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
											String name = f.getName();
											Object rowObj[] = {name, "", convertFileSize(attr.size()), convertDate(attr.creationTime())};
					            			if (f.isFile()) {
					            				rowObj[1] = getExt(name); 
					            			}
											tableModel.addRow(rowObj);
										} catch (IOException e1) {
											e1.printStackTrace();
										}
				                	}
				                	//DefaultMutableTreeNode node = findNode(dir.getName());
				                	//tree.setSelectionPath(new TreePath(node.getPath()));
			                	}
		                	}
		                } else {
			                if (dir != null) {
				                if (col == 0 && row != 0 && table.getValueAt(row, col+1).equals("")) {
		                			dir = new File(dir.getAbsolutePath()+"\\"+table.getValueAt(row, col));
				                	path.setText(dir.getAbsolutePath());
				                	parentPane.setTitleAt(tabId, dir.getAbsolutePath());
				                	File[] newFiles = dir.listFiles();
				                	
				                	tableModel.setRowCount(0);
				                	tableModel.addRow(new Object[]{"..", "", "", ""});
				                	if (newFiles != null && newFiles.length != 0) { 
					                	for (File f: newFiles) {
					                		BasicFileAttributes attr;
					                		String name = f.getName();
											try {
												attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
												Object rowObj[] = {name, "", null, convertDate(attr.creationTime())};
						            			if (f.isFile()) {
													rowObj[1] = getExt(name); 
													rowObj[2] = convertFileSize(attr.size());
												}
												tableModel.addRow(rowObj);
											} catch (IOException e1) {
												e1.printStackTrace();
											}
					                	}
					                	/*DefaultMutableTreeNode node = findNode(dir.getName());
					                	tree.setSelectionPath(new TreePath(node.getPath()));*/
				                	}
				                } else
				                if (table.getValueAt(row, col).equals("..") && FileSearchManager.isDrive(dir)) {
				                	System.out.println("Getting away from "+dir.getAbsolutePath()+" to MyPC");
				                	if (table.getColumnCount() == 4) table.setModel(drivesModel);
				                	drivesModel.setRowCount(0);
				                	tableModel.setRowCount(0);
				                	dir = null;
				                	path.setText("\\");
				                	parentPane.setTitleAt(tabId, "\\");
				                	try {
					                	for (Drive drive: drives) {
					                		drivesModel.addRow(new Object[]{drive.drive.getAbsolutePath(), convertFileSize(drive.drive.getFreeSpace()), convertFileSize(drive.drive.getTotalSpace())});
					                	}
				                	} catch (HeadlessException | UnsupportedEncodingException ex) {
										ex.printStackTrace();
									}
				                	//tree.setSelectionPath(new TreePath(((DefaultMutableTreeNode) tree.getModel().getRoot()).getPath()));
				                } else
				                if (table.getValueAt(row, col).equals("..")) {
				                	//System.out.println("Getting away from "+dir.getAbsolutePath()+" to "+(dir = dir.getParentFile()).getAbsolutePath());
				                	dir = dir.getParentFile();
				                	path.setText(dir.getAbsolutePath());
				                	parentPane.setTitleAt(tabId, dir.getAbsolutePath());
				                	File[] newFiles = dir.listFiles();
				                	
				                	tableModel.setRowCount(0);
				                	tableModel.addRow(new Object[]{"..", "", "", ""});
				                	for (File f: newFiles) {
				                		BasicFileAttributes attr;
										try {
											attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
											String name = f.getName();
											Object rowObj[] = {name, "", null, convertDate(attr.creationTime())};
											//JLabel titleLabel = null;
					            			if (f.isFile()) {
												rowObj[1] = getExt(name); 
												rowObj[2] = convertFileSize(attr.size());
											}
											tableModel.addRow(rowObj);
										} catch (IOException e1) {
											e1.printStackTrace();
										}
				                	}
				                	/*DefaultMutableTreeNode node = findNode(dir.getName());
				                	tree.setSelectionPath(new TreePath(node.getPath()));*/
				                } 
			                } else {
			                	if ((col == 0 && table.getValueAt(row, col+1).equals("")) || table.getColumnCount() == 3) {
			                		//table.clearSelection();
			                		dir = new File((String) table.getValueAt(row, col));
			                		if (table.getColumnCount() == 3) table.setModel(tableModel);
			                		table.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
			                		path.setText(dir.getAbsolutePath());
			                		parentPane.setTitleAt(tabId, dir.getAbsolutePath());
				                	File[] newFiles = dir.listFiles();
				                	
				                	tableModel.setRowCount(0);
				                	tableModel.addRow(new Object[]{"..", "", "", ""});
				                	if (newFiles != null && newFiles.length != 0) { 
					                	for (File f: newFiles) {
					                		BasicFileAttributes attr;
											try {
												attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
												String name = f.getName();
												Object rowObj[] = {name, "", null, convertDate(attr.creationTime())};
												//JLabel titleLabel;
						            			if (f.isFile()) {
													rowObj[1] = getExt(name);
													rowObj[2] = convertFileSize(attr.size());
												}
												tableModel.addRow(rowObj);
											} catch (IOException e1) {
												e1.printStackTrace();
											}
					                	}
					                	/*DefaultMutableTreeNode node = findNode(dir.getName());
					                	tree.setSelectionPath(new TreePath(node.getPath()));*/
				                	} else {
				                		try {
				                			JOptionPane.showMessageDialog(parentFrame, new String(FileSearchManager.prop.getProperty("emptinessApologise").getBytes(FileSearchManager.PROPERTY_ENCODING)));
				                		} catch (HeadlessException | UnsupportedEncodingException e1) {
				                			e1.printStackTrace();
				                		}
				                	}
			                	}
			                }
		                }
		            
	            	/*table.setColumnSelectionAllowed(true);
	                table.setRowSelectionAllowed(true);*/
	                table.repaint();
	                listModel.clearSelection();
	                listModel.addListSelectionListener(this);
	            }
	        });
	        
	        path.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (tabId == parentPane.getSelectedIndex()) {
						try {
							jumpToPath(new File(path.getText()), tableModel);
						} catch (HeadlessException | UnsupportedEncodingException ex) {
							ex.printStackTrace();
						}
					}
				}
	        	
	        });
	        
	        //currentPanel.add(path);
	        
	        //jumpButton.setBounds(860, 10, 75, 25);
	        jumpButton.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent e) {
					if (tabId == parentPane.getSelectedIndex())
						try {
							jumpToPath(new File(path.getText()), tableModel);
						} catch (HeadlessException | UnsupportedEncodingException ex) {
							ex.printStackTrace();
						}
				}
	        	
	        });
	        //currentPanel.add(jumpButton);
	        
	        //searchButton.setBounds(945, 10, 75, 25);
	        searchButton.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (tabId == parentPane.getSelectedIndex()) {
				        String query = path.getText();
				        
				        if (query.equals("") || query.equals("\\")) {
				        	try {
								JOptionPane.showMessageDialog(parentFrame, new String(FileSearchManager.prop.getProperty("emptyPathErr").getBytes(FileSearchManager.PROPERTY_ENCODING)));
							} catch (HeadlessException e) {
								e.printStackTrace();
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
				        }
				        else {
				        	File file = new File(query);
				        	if (file.exists()) {
				        		if (!file.equals(dir)) {
					        		try {
										jumpToPath(file, tableModel);
									} catch (UnsupportedEncodingException e) {
										e.printStackTrace();
									}
				        		}
				        	}
				        	else {		        		
				        		/*JLabel head = new JLabel("Search in progress: ");
				        		JLabel CSearchProgress = new JLabel();
				        		JLabel DSearchProgress = new JLabel();
				        		JPanel searchControls = new JPanel();*/
				        		
				        		//searchControls.setLayout(new FlowLayout(FlowLayout.CENTER));
				        		//searchControls.add(head/*, BorderLayout.LINE_START*/);
				        		//searchControls.add(CSearchProgress/*, BorderLayout.EAST*/);
				        		//searchControls.add(DSearchProgress/*, BorderLayout.CENTER*/);
				        		//searchControls.add(waitSearchButton, BorderLayout.SOUTH);
				        		
				        		fileTab.controlPanel.add(waitSearchButton);
				        		waitSearchButton.setVisible(true);
				        		//ArrayList<SearchThread> searchThreads = new ArrayList<SearchThread>(); 
				        		//SearchThread currentSearchThread = null;
				        		//DefaultTableModel searchResultsModel = new DefaultTableModel(null, new Object[] {"Search resluts"});
				        		//for (File drive: drives) {
				        		try {
				        			if (fileMgr.settings.hasSsdDrives()) {
				        				searchThread = new SearchThread(query, FileSearchManager.getHddDrives(), table, waitSearchButton);
				        				ssdSearchThread = new SearchThread(query, FileSearchManager.getSsdDrives(), table, waitSearchButton);
				        			} else {
				        				searchThread = new SearchThread(query, FileSearchManager.LOCAL_DRIVES, table, waitSearchButton);
				        			}
				        		} catch (UnsupportedEncodingException e1) {
				        			e1.printStackTrace();
				        		}
				        		boolean hasSsdSearch;
				        		if (ssdSearchThread == null)
				        			hasSsdSearch = false;
				        		else
				        			hasSsdSearch = true;
				        		searchThread.start();
				        		if (hasSsdSearch) {
				        			ssdSearchThread.start();
				        		}
				            		//searchThread.execute();
				            	//}
				        		/*DefaultTableModel searchModel = new DefaultTableModel(null, new Object[] {"Results"});
				        		table.setModel(searchModel);
				        		JLabel statusLabel = new JLabel();
				        		SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
				        			boolean anyMatches = false;
				        			
				        			public void search(File dir) {
				        				if (dir.getName().toLowerCase().contains(query.toLowerCase())) {
				        					String match = dir.getAbsolutePath();
				        					searchModel.addRow(new Object[] {match});
				        					System.out.println(match);
				        					searchModel.fireTableDataChanged();
				        					table.revalidate();
				        					table.repaint();
				        					if (!anyMatches) anyMatches = true;
				        				}
				        				if (dir.isDirectory()) {
				        					File driveFiles[] = dir.listFiles();
				        					if (driveFiles != null) {
				        						for (File driveFile: driveFiles) {
				        							String progressTxt = driveFile.getAbsolutePath();
				        							//System.out.println(progressTxt);
				        							//progress.setText(progressTxt);
				        							boolean isDir = driveFile.isDirectory();
				        							if (driveFile.getName().toLowerCase().contains(query.toLowerCase()) && !isDir) {
				        								//synchronized(tableModel) {
				        								SwingUtilities.invokeLater(new Runnable() {
	
				        									@Override
				        									public void run() {
				        										searchModel.addRow(new Object[] {progressTxt});
				        										searchModel.fireTableDataChanged();
				        										table.revalidate();
				        										table.repaint();
				        										System.out.println(progressTxt);
				        									}
				        									
				        								});
				        								//}
				        								if (!anyMatches) anyMatches = true;
				        							}
				        							if (isDir) {
				        								search(driveFile);
				        							}
				        						}
				        					}
				        				}
				        			}
				        			
				        			  /* @Override
				        			   protected Boolean doInBackground() throws Exception {
				        			    // Simulate doing something useful.
				        			    //for (int i = 0; i <= 10; i++) {
				        			     /*Thread.sleep(1000);
				        			     tableModel.addRow(new Object[] {"Running " + i, "", ""});
				        			     tableModel.fireTableDataChanged();
				        					table.revalidate();
				        					table.repaint();*/
				        			    //}
				        				  /* search(new File("C:\\"));
				        				   
				        			    // Here we can return some object of whatever type
				        			    // we specified for the first template parameter.
				        			    // (in this case we're auto-boxing 'true').
				        			    return true;
				        			   }
	
				        			   // Can safely update the GUI from this method.
				        			   protected void done() {
	
				        			    boolean status;
				        			    try {
				        			     // Retrieve the return value of doInBackground.
				        			     status = get();
				        			     statusLabel.setText("Completed with status: " + status);
				        			    } catch (InterruptedException e) {
				        			     // This is thrown if the thread's interrupted.
				        			    } catch (ExecutionException e) {
				        			     // This is thrown if we throw an exception
				        			     // from doInBackground.
				        			    }
				        			   }
				        			   
				        			   
				        			  };*/
				        			  
				        			  
				        			  
				        			  //worker.execute();	
				            	//}
				        		//int threadNum = searchThreads.size();
				            	
				        		/*SearchThread t1 = new SearchThread(query, new File("C:\\"), table, waitSearchButton);
				        		SearchThread t2 = new SearchThread(query, new File("D:\\"), table, waitSearchButton);*/
				        		/*EventQueue.invokeLater(t1);
				        		EventQueue.invokeLater(t2);*/
				        		/*t1.execute();
				        		t2.execute();*/
				        		/*try {
									/*t1.join();
									t2.join();
									if (!t1.anyMatches && !t2.anyMatches) tableModel.addRow(new Object[] {"Nothing was found! Sorry!"});
								} catch (InterruptedException e) {
									e.printStackTrace();
								}*/
				        					        		
				        		continueSearchAction = new ActionListener() {
									@SuppressWarnings("deprecation")
									public void actionPerformed(ActionEvent arg0) {
										try {
											//notifyAll();
											/*for (SearchThread st: searchThreads) {
												st.resume();
											}*/
											//searchThread.resume();
											/*t1.resume();
											t2.resume();*/
											searchThread.resume();
											waitSearchButton.setText(new String(FileSearchManager.prop.getProperty("continueSearch").getBytes(FileSearchManager.PROPERTY_ENCODING)));
											waitSearchButton.removeActionListener(continueSearchAction);
											waitSearchButton.addActionListener(stopSearchAction);
										} catch (UnsupportedEncodingException ex) {
											ex.printStackTrace();
										}
									}
								};
				        		stopSearchAction = new ActionListener() {
	
									@SuppressWarnings("deprecation")
									@Override
									public void actionPerformed(ActionEvent arg0) {
										try {
											/*for (SearchThread st: searchThreads) {
												st.suspend();
											}*/
											/*t1.pause();//wait();
											t2.pause();//wait();*/
											searchThread.suspend();
											waitSearchButton.setText(new String(FileSearchManager.prop.getProperty("stopSearch").getBytes(FileSearchManager.PROPERTY_ENCODING)));
											waitSearchButton.removeActionListener(stopSearchAction);
											waitSearchButton.addActionListener(continueSearchAction);
										} catch (UnsupportedEncodingException ex) {
											ex.printStackTrace();
										}
									}
				        			
				        		};
				        		
				        		waitSearchButton.addActionListener(stopSearchAction);
				        					        		
				        		closeSearchButton.setVisible(true);
				        		closeSearchButton.addActionListener(new ActionListener() {
	
									@SuppressWarnings("deprecation")
									@Override
									public void actionPerformed(ActionEvent arg0) {
										/*for (SearchThread st: searchThreads) {
											st.stop();
										}*/
										/*t1.cancel(true);
										t2.cancel(true);*/
										searchThread.stop();
										table.setModel(tableModel);
						        		try {
											jumpToPath(dir, tableModel);
										} catch (UnsupportedEncodingException e) {
											e.printStackTrace();
										}
						        		waitSearchButton.setVisible(false);
						        		closeSearchButton.setVisible(false);
						        		//searchControls.setVisible(false);
						        		table.revalidate();
						        		table.repaint();
									}
				        			
				        		});
				        		/*while (t1.isAlive() && t2.isAlive()) {}
				        		waitSearchButton.setVisible(false);
				        		if (!t1.anyMatches && !t2.anyMatches) {
				        			tableModel.addRow(new Object[] {"Nothing was found! Sorry!"});
				        			table.revalidate();
				        			table.repaint();
				        		}*/
				        		/*try {
									wait();
									wait();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
				        		waitSearchButton.setVisible(false);*/
				        		/*synchronized(this) {
				        			while (threadNum > 0) {
				        				try {
				        					wait();
				        					threadNum--;
				        					System.out.println(threadNum+" threads left");
				        				} catch (InterruptedException e) {
				        					//e.printStackTrace();
				        					continue;
				        				}
				        			}			        	
				        		}*/
				        	}
				        	
				        }
					}
				}
	        	
	        });
	        //currentPanel.add(searchButton);
	        
	        //JMenuItem createFolderMenuItem = new JMenuItem("Create a new folder"); 
	        //openMenu.add(createFolderMenuItem);
	        //JMenu editMenu = new JMenu("Edit");
	        JMenuItem copyMenuItem = new JMenuItem(new String(FileSearchManager.prop.getProperty("copyPopup").getBytes(FileSearchManager.PROPERTY_ENCODING)));
	        JMenuItem pasteMenuItem = new JMenuItem(new String(FileSearchManager.prop.getProperty("pastePopup").getBytes(FileSearchManager.PROPERTY_ENCODING)));
	        JMenuItem cutMenuItem = new JMenuItem(new String(FileSearchManager.prop.getProperty("cutPopup").getBytes(FileSearchManager.PROPERTY_ENCODING)));
	        JMenuItem renameMenuItem = new JMenuItem(new String(FileSearchManager.prop.getProperty("renamePopup").getBytes(FileSearchManager.PROPERTY_ENCODING)));
	        JMenuItem addFavouriteFromTableMenuItem = new JMenuItem(new String(FileSearchManager.prop.getProperty("addToFavouritesPopup").getBytes(FileSearchManager.PROPERTY_ENCODING)));
	        JMenuItem delMenuItem = new JMenuItem(new String(FileSearchManager.prop.getProperty("deletePopup").getBytes(FileSearchManager.PROPERTY_ENCODING)));
	        JMenuItem propMenuItem = new JMenuItem(new String(FileSearchManager.prop.getProperty("propertiesPopup").getBytes(FileSearchManager.PROPERTY_ENCODING)));
	        JMenu createMenuItem = new JMenu(new String(FileSearchManager.prop.getProperty("createPopup").getBytes(FileSearchManager.PROPERTY_ENCODING)));
	        JMenuItem createNewFolder = new JMenuItem(new String(FileSearchManager.prop.getProperty("newFolderPopup").getBytes(FileSearchManager.PROPERTY_ENCODING)));
	        createMenuItem.add(createNewFolder);
	        copyMenuItem.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (popupRow > -1) {
						File adding;
						if (table.getColumnCount() == 1) {
							adding = new File((String) table.getValueAt(popupRow, 0));
						} else {
							adding = new File(dir.getAbsolutePath()+"\\"+table.getValueAt(popupRow, 0));
						}
						if (adding != null) {
							try {
								if (copyBuffer.contains(adding)) {
									JOptionPane.showMessageDialog(parentFrame, new String(FileSearchManager.prop.getProperty("storedInBuffMsg").getBytes(FileSearchManager.PROPERTY_ENCODING)));
								} else {
									copyBuffer.add(adding);
									JOptionPane.showMessageDialog(parentFrame, new String(FileSearchManager.prop.getProperty("copiedToBuffMsg").getBytes(FileSearchManager.PROPERTY_ENCODING)));
								}
							} catch (HeadlessException | UnsupportedEncodingException ex) {
								ex.printStackTrace();
							}
						}
					} else {
						try {
							JOptionPane.showMessageDialog(parentFrame, new String(FileSearchManager.prop.getProperty("unresolvedErr").getBytes(FileSearchManager.PROPERTY_ENCODING)));
						} catch (HeadlessException | UnsupportedEncodingException ex) {
							ex.printStackTrace();
						}
					}
				}
	        	
	        });
	        pasteMenuItem.addActionListener(new ActionListener() {
	        	
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (table.getColumnCount() == 1) {
						try {
							JOptionPane.showMessageDialog(parentFrame, new String(FileSearchManager.prop.getProperty("invalidOperErr").getBytes(FileSearchManager.PROPERTY_ENCODING)));
						} catch (HeadlessException | UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						return;
					}
					if (!copyOperation) {
						try {
							if (cutBuffer != null) {
								if (cutBuffer.isFile()) {
									Files.move(cutBuffer.toPath(), Paths.get(dir.getAbsolutePath()+"\\"+cutBuffer.getName()), StandardCopyOption.REPLACE_EXISTING);
								} else if (cutBuffer.isDirectory()) {
									File whereTo = new File(dir.getAbsolutePath()+"\\"+cutBuffer.getName());
									whereTo.mkdirs();
									copyMultipleFiles(cutBuffer.toPath(), whereTo.toPath(), copyOperation);
									cutBuffer = null;
								}
							} else {
								JOptionPane.showMessageDialog(parentFrame, new String(FileSearchManager.prop.getProperty("emptyBuffMsg").getBytes(FileSearchManager.PROPERTY_ENCODING)));
							}
						} catch (IOException e) {
							
						}
						//tableModel.fireTableDataChanged();
						try {
							jumpToPath(dir, tableModel);
						} catch (HeadlessException | UnsupportedEncodingException ex) {
							ex.printStackTrace();
						}
					} else {
						int size = copyBuffer.size();
						if (size == 1) {
							File theFile = copyBuffer.get(0);
							if (theFile.isFile())
								try {
									if (copyOperation)
										Files.copy(theFile.toPath(), Paths.get(dir.getAbsolutePath()+"\\"+theFile.getName()), StandardCopyOption.REPLACE_EXISTING);
									else {
										Files.move(theFile.toPath(), Paths.get(dir.getAbsolutePath()+"\\"+theFile.getName()), StandardCopyOption.REPLACE_EXISTING);
										copyBuffer = null;
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							else if (theFile.isDirectory()) {
									File whereTo = new File(dir.getAbsolutePath()+"\\"+theFile.getName());
									whereTo.mkdirs();
									try {
										copyMultipleFiles(theFile.toPath(), whereTo.toPath(), copyOperation);
									} catch (IOException e) {
										e.printStackTrace();
									}
									if (!copyOperation) cutBuffer = null;
							}		
							try {
								jumpToPath(dir, tableModel);
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
						} else {
							if (size > 1) {
								try {
									JComboBox<String> comboBox = new JComboBox<String>(copyBufferToStringArray(copyBuffer));
									int choice = JOptionPane.showConfirmDialog(parentFrame, comboBox);
									if (choice == JOptionPane.YES_OPTION) {
										File theFile = new File((String) comboBox.getSelectedItem());
										//System.out.println(selectedFilePath);
										if (theFile.isFile())
											if (copyOperation)
												Files.copy(theFile.toPath(), Paths.get(dir.getAbsolutePath()+"\\"+theFile.getName()), StandardCopyOption.REPLACE_EXISTING);
											else {
												Files.move(theFile.toPath(), Paths.get(dir.getAbsolutePath()+"\\"+theFile.getName()), StandardCopyOption.REPLACE_EXISTING);
												copyBuffer = null;
											}
										else if (theFile.isDirectory()) {
												File whereTo = new File(dir.getAbsolutePath()+"\\"+theFile.getName());
												whereTo.mkdirs();
												copyMultipleFiles(theFile.toPath(), whereTo.toPath(), copyOperation);
												if (!copyOperation) copyBuffer = null;
										}		
										jumpToPath(dir, tableModel);
									}
								} catch (IOException e) {
									try {
										JOptionPane.showMessageDialog(parentFrame, new String(FileSearchManager.prop.getProperty("invalidOperErr").getBytes(FileSearchManager.PROPERTY_ENCODING)));
									} catch (HeadlessException | UnsupportedEncodingException e1) {
										e1.printStackTrace();
									}
									e.printStackTrace();
								}
						} else {
							try {
								JOptionPane.showMessageDialog(parentFrame, new String(FileSearchManager.prop.getProperty("emptyBuffMsg").getBytes(FileSearchManager.PROPERTY_ENCODING)));
							} catch (HeadlessException | UnsupportedEncodingException e) {
								e.printStackTrace();
							}
						}
					}
					}
					
				}
	        	
	        });
	        cutMenuItem.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (popupRow > -1) {
						if (table.getColumnCount() == 1) {
							cutBuffer = new File((String) table.getValueAt(popupRow, 0));
						} else {
							cutBuffer = new File(dir.getAbsolutePath()+"\\"+table.getValueAt(popupRow, 0));
						}
					}
					if (cutBuffer != null) {
						copyOperation = false;
						try {
							JOptionPane.showMessageDialog(parentFrame, new String(FileSearchManager.prop.getProperty("copiedChooseFolderMsg").getBytes(FileSearchManager.PROPERTY_ENCODING)));
						} catch (HeadlessException | UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}		
				}
	        	
	        });
	        renameMenuItem.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent e) {
					if (popupRow > -1) {
						//File renamingOne = new File((String) table.getValueAt(popupRow, 0));
						/*JOptionPane optionPane = new JOptionPane();
				        optionPane.setMessage("Enter a new name of the file:");
				        optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
				        optionPane.setWantsInput(true);
				        JDialog dialog = optionPane.createDialog("Simple Question");
				        for (Component c : getAllComponents(dialog)) {
					         if (c instanceof JTextField) {
					             ((JComponent) c).setToolTipText((String) table.getValueAt(popupRow, 0));
					         }
				        }
				        dialog.setVisible(true);
				        dialog.dispose();*/
						try {
							String oldName = (String) table.getValueAt(popupRow, 0);
					        String newName = JOptionPane.showInputDialog(new String(FileSearchManager.prop.getProperty("newFileNameSuggestion").getBytes(FileSearchManager.PROPERTY_ENCODING)), oldName);
							if (newName != null) {
								if (!getExt(oldName).equals(getExt(newName))) {
									int answ = JOptionPane.showConfirmDialog(parentFrame, new String(FileSearchManager.prop.getProperty("fileExtChangedMsg").getBytes(FileSearchManager.PROPERTY_ENCODING)));
									switch (answ) {
										case JOptionPane.YES_OPTION: {
											break;
										}
										case JOptionPane.NO_OPTION: {
											actionPerformed(e);
										}
										case JOptionPane.CANCEL_OPTION: {
											return;
										}
									}
								}
								//renamingOne.renameTo(new File(dir.getAbsolutePath()+"\\"+newName));
								try {
									Files.move(new File(dir.getAbsolutePath()+"\\"+oldName).toPath(), new File(dir.getAbsolutePath()+"\\"+newName).toPath(), StandardCopyOption.REPLACE_EXISTING);
									jumpToPath(dir, tableModel);
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						} catch (HeadlessException | UnsupportedEncodingException ex) {
							ex.printStackTrace();
						}
					}
				}
	        	
	        });
	        delMenuItem.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent arg0) {
					File delBuffer = null;
					if (popupRow > -1) {
						if (table.getColumnCount() == 1) {
							delBuffer = new File((String) table.getValueAt(popupRow, 0));
						} else {
							delBuffer = new File(dir.getAbsolutePath()+"\\"+table.getValueAt(popupRow, 0));
						}
					}
					if (delBuffer != null) {
						try {
							int oper = JOptionPane.showConfirmDialog(parentFrame, new String(FileSearchManager.prop.getProperty("areYouSureDelConfirm").getBytes(FileSearchManager.PROPERTY_ENCODING))+" \""+delBuffer.getName()+"\" "+new String(FileSearchManager.prop.getProperty("irretrievablyWord").getBytes(FileSearchManager.PROPERTY_ENCODING)));
							if (oper == JOptionPane.YES_OPTION) {
								if (delBuffer.isFile()) {
									delBuffer.delete();
								} else if (delBuffer.isDirectory()) {
									deleteMulitpleFiles(delBuffer);
									delBuffer.delete();
								}
								jumpToPath(dir, tableModel);
							}
						} catch (HeadlessException | UnsupportedEncodingException ex) {
							ex.printStackTrace();
						}
					}
				}
	        	
	        });
	        propMenuItem.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent arg0) {
					if (popupRow > -1) {
						try {
							int colCnt = table.getColumnCount();
							String name = (String) table.getValueAt(popupRow, 0);
							if (colCnt == 3) {
								String freeSpace = (String) table.getValueAt(popupRow, 1);
								String totalSpace = (String) table.getValueAt(popupRow, 2);
								String[] splitSpace = freeSpace.split("\\s");
								String usedSpace = BigDecimal.valueOf(Double.parseDouble(totalSpace.split("\\s")[0])-Double.parseDouble(splitSpace[0])).setScale(2, BigDecimal.ROUND_HALF_UP).toString()+" "+splitSpace[1];
								JOptionPane.showMessageDialog(parentFrame, "<html>"+new String(FileSearchManager.prop.getProperty("driveName").getBytes(FileSearchManager.PROPERTY_ENCODING))+": "+name+"<br>"+new String(FileSearchManager.prop.getProperty("freeSpace").getBytes(FileSearchManager.PROPERTY_ENCODING))+": "+freeSpace+"<br>"+new String(FileSearchManager.prop.getProperty("usedSpace").getBytes(FileSearchManager.PROPERTY_ENCODING))+": "+usedSpace+"<br>"+new String(FileSearchManager.prop.getProperty("totalSpace").getBytes(FileSearchManager.PROPERTY_ENCODING))+": "+totalSpace+"</html>", "«"+name+"» "+new String(FileSearchManager.prop.getProperty("propertiesConst").getBytes(FileSearchManager.PROPERTY_ENCODING)), JOptionPane.PLAIN_MESSAGE);
							} else if (colCnt == 4) {
								String size = convertFileSize(calcFolderSize(new File(dir.getAbsolutePath()+"\\"+name)));
								String date = (String) table.getValueAt(popupRow, 3);
								JOptionPane.showMessageDialog(parentFrame, "<html>"+new String(((String) FileSearchManager.prop.getProperty("fileName")).getBytes(FileSearchManager.PROPERTY_ENCODING))+": "+name+"<br>"+new String(((String) FileSearchManager.prop.getProperty("sizeConst")).getBytes(FileSearchManager.PROPERTY_ENCODING))+": "+size+"<br>"+new String(((String) FileSearchManager.prop.getProperty("creationDate")).getBytes(FileSearchManager.PROPERTY_ENCODING))+": "+date+"</html>", "«"+name+"» "+new String(((String) FileSearchManager.prop.getProperty("propertiesConst")).getBytes(FileSearchManager.PROPERTY_ENCODING)), JOptionPane.PLAIN_MESSAGE);
							}
						} catch (HeadlessException | UnsupportedEncodingException ex) {
							ex.printStackTrace();
						}
					}
				}
	        	
	        });
	        addFavouriteFromTableMenuItem.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent e) {
					String filePath = dir.getAbsolutePath()+"\\"+table.getValueAt(popupRow, 0);
					File favFile = new File(filePath);
					filePath = favFile.getAbsolutePath();
					boolean alreadyExists = hasJMenuItem(filePath, fileMgr.favouritesMenu);
					
					try {
						if (favFile.isDirectory() && !alreadyExists) {
							JMenu favMenu = new JMenu(favFile.getAbsolutePath());
							favMenu.setFont(fileMgr.font);
							favMenu.addMouseListener(new FavouritesMouseListener(fileMgr, favMenu));
							JMenuItem deleteFavMenuItem = new JMenuItem(new String(FileSearchManager.prop.getProperty("deletePopup").getBytes(FileSearchManager.PROPERTY_ENCODING)));
							deleteFavMenuItem.setFont(fileMgr.font);
							deleteFavMenuItem.addActionListener(new DeleteFavouriteActionListener(favMenu, fileMgr));
							try {
								FileWriter fw = new FileWriter(fileMgr.favouritesFile, true);
								fw.write(favMenu.getText()+'\n');
								fw.flush(); fw.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							favMenu.add(deleteFavMenuItem);
							fileMgr.favouritesMenu.add(favMenu);
						} else if (alreadyExists){
							JOptionPane.showMessageDialog(parentFrame, new String(FileSearchManager.prop.getProperty("alreadyFavMsg").getBytes(FileSearchManager.PROPERTY_ENCODING)));
						} else {
							JOptionPane.showMessageDialog(parentFrame, new String(FileSearchManager.prop.getProperty("onlyDirFavMsg").getBytes(FileSearchManager.PROPERTY_ENCODING)));
						}
					} catch (HeadlessException | UnsupportedEncodingException ex) {
						ex.printStackTrace();
					}
				}
	        	
	        });	
	
	        createNewFolder.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						String folderName = JOptionPane.showInputDialog(new String(FileSearchManager.prop.getProperty("enterFolderNameMsg").getBytes(FileSearchManager.PROPERTY_ENCODING))+": ");
						if (folderName != null) {
							if (folderName.equals("")) {
								JOptionPane.showMessageDialog(parentFrame, new String(FileSearchManager.prop.getProperty("emptyNameErr").getBytes(FileSearchManager.PROPERTY_ENCODING)));
								actionPerformed(arg0);
							} else {
								File newDir = new File(dir.getAbsolutePath()+"\\"+folderName);
								newDir.mkdir();
								jumpToPath(dir, tableModel);
								repaintPath(dir.getAbsolutePath());
							}
						}
					} catch (HeadlessException | UnsupportedEncodingException ex) {
						ex.printStackTrace();
					}
				}
	        	
	        });
	        JMenuItem closeTabMenuItem = new JMenuItem(new String(FileSearchManager.prop.getProperty("closeTabToolTip").getBytes(FileSearchManager.PROPERTY_ENCODING)));
	        closeTabMenuItem.addActionListener(this);
	        
	        popupMenu.add(copyMenuItem);
	        popupMenu.add(pasteMenuItem);
	        popupMenu.add(cutMenuItem);
	        popupMenu.add(renameMenuItem);
	        popupMenu.add(delMenuItem);
	        popupMenu.add(addFavouriteFromTableMenuItem);
	        popupMenu.add(propMenuItem);
	        popupMenu.add(createMenuItem);
	        popupMenu.add(closeTabMenuItem);
	        popupMenu.setFont(fileMgr.font);
	        popupMenu.addPopupMenuListener(new  PopupMenuListener() {
	
				@Override
				public void popupMenuCanceled(PopupMenuEvent arg0) {}
	
				@Override
				public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {}
	
				@Override
				public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
					SwingUtilities.invokeLater(new Runnable() {
	                    @Override
	                    public void run() {
	                        int rowAtPoint = table.rowAtPoint(SwingUtilities.convertPoint(popupMenu, new Point(0, 0), table));
	                        if (rowAtPoint > -1) {
	                            popupRow = rowAtPoint;
	                        }
	                    }
	                });
				}
	        	
	        });
	        table.setComponentPopupMenu(popupMenu);
	        
	        /*JButton closeButton = new JButton(new ImageIcon("d:\\cross.png"));
	        int size = 17;
	        closeButton.setPreferredSize(new Dimension(size, size));
	        closeButton.setToolTipText("Close this file tab");
	        closeButton.setUI(new BasicButtonUI());
	        closeButton.setContentAreaFilled(false);
	        closeButton.setFocusable(false);
	        closeButton.setBorder(BorderFactory.createEtchedBorder());
	        closeButton.setBorderPainted(false);
	        closeButton.addMouseListener(new MouseAdapter() {
	            public void mouseEntered(MouseEvent e) {
	                Component component = e.getComponent();
	                if (component instanceof AbstractButton) {
	                    AbstractButton button = (AbstractButton) component;
	                    button.setBorderPainted(true);
	                }
	            }
	
	            public void mouseExited(MouseEvent e) {
	                Component component = e.getComponent();
	                if (component instanceof AbstractButton) {
	                    AbstractButton button = (AbstractButton) component;
	                    button.setBorderPainted(false);
	                }
	            }
	        });
	        closeButton.setRolloverEnabled(true);
	        closeButton.addActionListener(new ActionListener() {
	
				@SuppressWarnings("deprecation")
				@Override
				public void actionPerformed(ActionEvent arg0) {
					//int i = parentPane.index(currentPanel);
		            //if (i != -1) {
		                parentPane.remove(tabId);
		                int size = threads.size();
		                for (int i = tabId+1; i < size; i++) {
		                	threads.get(i).tabId--;
		                }
		                if (currentTab == threads.get(tabId)) {
		                	currentTab.stop();
		                	if (tabId == 0) {
		                		currentTab = threads.get(1);
		                	} else {
		                		currentTab = threads.get(tabId-1);
		                	}
		                }
		                threads.remove(tabId);
		                fileTab.tabNumber--;
		                //fileMgr.currentTab = threads.get(tabId);
		                
		            //}				
				}
	        	
	        });*/
	        
	        parentPane.addChangeListener(this);
	        
	        tabTitleMenu = new JPopupMenu();
	        tabTitleMenu.setFont(fileMgr.font);
	               
	        JMenuItem duplicateTabMenuItem = new JMenuItem(new String(FileSearchManager.prop.getProperty("duplicateTabToolTip").getBytes(FileSearchManager.PROPERTY_ENCODING)));
	        JMenuItem addFavouriteMenuItem = new JMenuItem(new String(FileSearchManager.prop.getProperty("addToFavToolTip").getBytes(FileSearchManager.PROPERTY_ENCODING)));
	        duplicateTabMenuItem.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent e) {
					if (fileMgr.leftFilePanel != null) {
						try {
							Object[] customAlt = {new String(FileSearchManager.prop.getProperty("leftFilePanelButton").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("rightFilePanelButton").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("cancelButton").getBytes(FileSearchManager.PROPERTY_ENCODING))};
							int answ = JOptionPane.showOptionDialog(parentFrame, new String(FileSearchManager.prop.getProperty("selectFilePanelMsg").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("confirmTitle").getBytes(FileSearchManager.PROPERTY_ENCODING)),
					                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
					                null, customAlt, null);
							switch(answ) {
								case JOptionPane.YES_OPTION: {
										FileTab leftFilePanel = fileMgr.leftFilePanel;
										ArrayList<TabThread> tabThreads = leftFilePanel.threads; 
										TabThread newFileThread = new TabThread(fileMgr, leftFilePanel, ++leftFilePanel.tabNumber, tabThreads, dir);
										tabThreads.add(newFileThread);
										newFileThread.start();
										break;
									} 
								case JOptionPane.NO_OPTION: {
									FileTab rightFilePanel = fileMgr.rightFilePanel;
									ArrayList<TabThread> tabThreads = rightFilePanel.threads; 
									TabThread newFileThread = new TabThread(fileMgr, rightFilePanel, ++rightFilePanel.tabNumber, tabThreads, dir);
									tabThreads.add(newFileThread);
									newFileThread.start();
									break;
								}
								case JOptionPane.CANCEL_OPTION: {
									return;
								}
							}
						} catch (HeadlessException | UnsupportedEncodingException ex) {
							ex.printStackTrace();
						}
					} else {
						try {
							FileTab rightFilePanel = fileMgr.rightFilePanel;
							ArrayList<TabThread> tabThreads = threads; 
							TabThread newFileThread = new TabThread(fileMgr, fileMgr.rightFilePanel, ++rightFilePanel.tabNumber, tabThreads, dir);
							tabThreads.add(newFileThread);
							newFileThread.start();
						} catch (HeadlessException | UnsupportedEncodingException ex) {
							ex.printStackTrace();
						}
					}
				}
	        	
	        });
	        addFavouriteMenuItem.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent e) {
					String filePath = dir.getAbsolutePath();
					boolean alreadyExists = hasJMenuItem(filePath, fileMgr.favouritesMenu);
					if (!alreadyExists) {
						JMenu favMenu = new JMenu(filePath);
						favMenu.setFont(fileMgr.font);
						favMenu.addMouseListener(new FavouritesMouseListener(fileMgr, favMenu));
						try {
							JMenuItem deleteFavMenuItem = new JMenuItem(new String(FileSearchManager.prop.getProperty("deletePopup").getBytes(FileSearchManager.PROPERTY_ENCODING)));
							deleteFavMenuItem.setFont(fileMgr.font);
							deleteFavMenuItem.addActionListener(new DeleteFavouriteActionListener(favMenu, fileMgr));
							try {
								FileWriter fw = new FileWriter(fileMgr.favouritesFile);
								fw.write(favMenu.getText()+'\n');
								fw.flush(); fw.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							favMenu.add(deleteFavMenuItem);
							fileMgr.favouritesMenu.add(favMenu);
						} catch (HeadlessException | UnsupportedEncodingException ex) {
							ex.printStackTrace();
						}
					} else {
						try {
							JOptionPane.showMessageDialog(parentFrame, new String(FileSearchManager.prop.getProperty("alreadyFavMsg").getBytes(FileSearchManager.PROPERTY_ENCODING)));
						} catch (HeadlessException | UnsupportedEncodingException e1) {
							e1.printStackTrace();
						}
					}
				}
	        	
	        });
	
	        tabTitleMenu.add(closeTabMenuItem);
	        tabTitleMenu.add(duplicateTabMenuItem);
	        tabTitleMenu.add(addFavouriteMenuItem);
	 
	        table.addKeyListener(new KeyListener() {
	
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && dir != null) {
						//String path = dir.getAbsolutePath();
						try {
							if (FileSearchManager.isDrive(dir)) {
								jumpToPath(null, tableModel);
							} else {
								jumpToPath(new File(dir.getParent()), tableModel);
							}
						} catch (HeadlessException | UnsupportedEncodingException ex) {
							ex.printStackTrace();
						}
					}
					if (e.getKeyCode() == KeyEvent.VK_N && e.isControlDown()) {
						Component[] cmps = fileMgr.menuBar.getMenu(0).getMenuComponents();
						for (Component cmp: cmps) {
							try {
								if (cmp instanceof JMenuItem && ((JMenuItem) cmp).getText().equals(new String(FileSearchManager.prop.getProperty("createNewTabToolTip").getBytes(FileSearchManager.PROPERTY_ENCODING)))) {
									((JMenuItem) cmp).doClick();
								}
							} catch (HeadlessException | UnsupportedEncodingException ex) {
								ex.printStackTrace();
							}
						}
						//fileMgr.createTabMenuItem.doClick();
					}
					if (e.getKeyCode() == KeyEvent.VK_W && e.isControlDown()) {
						((JMenuItem) tabTitleMenu.getComponent(0)).doClick();
					}
					if (e.getKeyCode() == KeyEvent.VK_D && e.isControlDown()) {
						((JMenuItem) tabTitleMenu.getComponent(1)).doClick();
					}
					if (e.getKeyCode() == KeyEvent.VK_F && e.isControlDown()) {
						((JMenuItem) tabTitleMenu.getComponent(2)).doClick();
					}
				}
	
				@Override
				public void keyReleased(KeyEvent e) {}
	
				@Override
				public void keyTyped(KeyEvent e) {}
	        	
	        });
	        
	        //splitPane.setRightComponent(panel2);
	        //currentPanel.add(splitPane);
	        parentPane.addTab(new String(FileSearchManager.prop.getProperty("newTabConst").getBytes(FileSearchManager.PROPERTY_ENCODING)), currentPanel);
	        //parentPane.setTabComponentAt(tabId, tabTitleMenu);
	        parentPane.addMouseListener(new MouseListener() {
	
				@Override
				public void mouseClicked(MouseEvent arg0) {/*TODO try to move to FileTab*/
					if (SwingUtilities.isRightMouseButton(arg0)) {
						tabTitleMenu.show(parentPane, arg0.getX(), arg0.getY());
					}
				}
	
				@Override
				public void mouseEntered(MouseEvent arg0) {}
	
				@Override
				public void mouseExited(MouseEvent arg0) {}
	
				@Override
				public void mousePressed(MouseEvent arg0) {}
	
				@Override
				public void mouseReleased(MouseEvent arg0) {}
	        	
	        });
	        parentFrame.revalidate();
	        parentFrame.repaint();
	        if (dir != null) {
	        	jumpToPath(dir, tableModel);
	        }
		} catch (HeadlessException | UnsupportedEncodingException ex) {
        	ex.printStackTrace();
        }
	}

	/*@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub
		
	}*/
}

@SuppressWarnings("serial")
class CustomCellRenderer extends DefaultTableCellRenderer {
	/*String name;
	String ext;
	long size;
	String date;*/
	//TabThread thread;
	
	/*public CustomCellRenderer(TabThread thread/*String name, long size, String date) {
		/*this.name = name;
		this.size = size;
		this.date = date;
		//this.thread = thread;
	}*/
	
	public Component getTableCellRendererComponent(
            JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {
		JLabel c = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		/*if (table.getValueAt(row, 0) instanceof JLabel) {
			JLabel name = (JLabel) table.getValueAt(row, 0);
			if (dir != null) {
				File file = new File(dir.getAbsolutePath()+"\\"+name);
				if (file.isDirectory()) {
					c.setToolTipText("<html>File name: "+name.getText()+"<br>Size: "+TabThread.convertFileSize(TabThread.calcFolderSize(file))+"<br>Creation date: "+table.getValueAt(row, 3)+"</html>");
				} else {
					c.setToolTipText("<html>File name: "+name.getText()+"<br>Size: "+table.getValueAt(row, 2)+"<br>Creation date: "+table.getValueAt(row, 3)+"</html>");
				}
			}	
		} else if (table.getValueAt(row, 0) instanceof String) {
			String name = (String) table.getValueAt(row, 0);
			if (dir != null) {
				File file = new File(dir.getAbsolutePath()+"\\"+name);
				if (file.isDirectory()) {
					c.setToolTipText("<html>File name: "+name+"<br>Size: "+TabThread.convertFileSize(TabThread.calcFolderSize(file))+"<br>Creation date: "+table.getValueAt(row, 3)+"</html>");
				} else {
					c.setToolTipText("<html>File name: "+name+"<br>Size: "+table.getValueAt(row, 2)+"<br>Creation date: "+table.getValueAt(row, 3)+"</html>");
				}
			}
		}*/
		if (value.equals("..")) {
			c.setIcon(new ImageIcon(getClass().getResource(FileSearchManager.LEVEL_UP_IMG_URL)));
		} else if (FileSearchManager.isDrive((String) value)) {
			c.setIcon(new ImageIcon(getClass().getResource(FileSearchManager.LOCAL_DRIVE_IMG_URL)));
		} else if (value.equals(TabThread.getExt((String) value))) {
			c.setIcon(new ImageIcon(getClass().getResource(FileSearchManager.FOLDER_IMG_URL)));
			try {
				c.setToolTipText("<html>"+new String(FileSearchManager.prop.getProperty("fileName").getBytes(FileSearchManager.PROPERTY_ENCODING))+": "+value+"<br>"+new String(FileSearchManager.prop.getProperty("sizeConst").getBytes(FileSearchManager.PROPERTY_ENCODING))+": "+new String(FileSearchManager.prop.getProperty("chechPropertiesSuggestion").getBytes(FileSearchManager.PROPERTY_ENCODING))+"<br>"+new String(FileSearchManager.prop.getProperty("creationDate").getBytes(FileSearchManager.PROPERTY_ENCODING))+": "+table.getValueAt(row, 3)+"</html>");
			} catch (HeadlessException | UnsupportedEncodingException ex) {
				ex.printStackTrace();
			}
		} else {
			c.setIcon(new ImageIcon(getClass().getResource(FileSearchManager.FILE_IMG_URL)));
			try {
				c.setToolTipText("<html>"+new String(FileSearchManager.prop.getProperty("fileName").getBytes(FileSearchManager.PROPERTY_ENCODING))+": "+value+"<br>"+new String(FileSearchManager.prop.getProperty("sizeConst").getBytes(FileSearchManager.PROPERTY_ENCODING))+": "+table.getValueAt(row, 2)+"<br>"+new String(FileSearchManager.prop.getProperty("creationDate").getBytes(FileSearchManager.PROPERTY_ENCODING))+": "+table.getValueAt(row, 3)+"</html>");
			} catch (HeadlessException | UnsupportedEncodingException ex) {
				ex.printStackTrace();
			}
		}
		if (table.getColumnCount() == 1) {
			
		}
		return c;
	}
}

class FavouritesMouseListener implements MouseListener {
	FileSearchManager fileMgr;
	JMenu item;
	
	public FavouritesMouseListener(FileSearchManager fileMgr, JMenu theInitialItem) {
		this.fileMgr = fileMgr;
		item = theInitialItem;
	}
	
		@Override
		public void mouseClicked(MouseEvent arg0) {
			if (fileMgr.leftFilePanel != null) {
				try {
					Object[] customAlt = {new String(FileSearchManager.prop.getProperty("leftFilePanelButton").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("rightFilePanelButton").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("cancelButton").getBytes(FileSearchManager.PROPERTY_ENCODING))};
					int answ = JOptionPane.showOptionDialog(fileMgr.f, new String(FileSearchManager.prop.getProperty("selectFilePanelOpenFavMsg").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("confirmTitle").getBytes(FileSearchManager.PROPERTY_ENCODING)),
			                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
			                null, customAlt, null);
					switch(answ) {
						case JOptionPane.YES_OPTION: {
						 	TabThread leftFilePanelCurrentTab = fileMgr.leftFilePanel.currentTab;
						 	leftFilePanelCurrentTab.jumpToPath(new File(item.getText()), leftFilePanelCurrentTab.tableModel);
						 	break;
						}
						case JOptionPane.NO_OPTION: {
							TabThread rightFilePanelCurrentTab = fileMgr.rightFilePanel.currentTab;
							rightFilePanelCurrentTab.jumpToPath(new File(item.getText()), rightFilePanelCurrentTab.tableModel);
							break;
						}
						case JOptionPane.CANCEL_OPTION: {
							return;
						}
					}
				} catch (HeadlessException | UnsupportedEncodingException ex) {
					ex.printStackTrace();
				}
			} else {
				TabThread rightFilePanelCurrentTab = fileMgr.rightFilePanel.currentTab;
				try {
					rightFilePanelCurrentTab.jumpToPath(new File(item.getText()), rightFilePanelCurrentTab.tableModel);
				} catch (HeadlessException | UnsupportedEncodingException ex) {
					ex.printStackTrace();
				}
			}
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {}

		@Override
		public void mouseExited(MouseEvent arg0) {}

		@Override
		public void mousePressed(MouseEvent arg0) {}

		@Override
		public void mouseReleased(MouseEvent arg0) {}
}

class DeleteFavouriteActionListener implements ActionListener {
	JMenu favourite;
	FileSearchManager fileMgr;
	
	public DeleteFavouriteActionListener(JMenu favourite, FileSearchManager fileMgr) {
		this.favourite = favourite;
		this.fileMgr = fileMgr;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		fileMgr.favouritesMenu.remove(favourite);
		try {
			String favRecord = favourite.getText();
			File tmpFavFile = new File(FileSearchManager.FAV_FILE);
			File favFile = fileMgr.favouritesFile;
			FileWriter fw = new FileWriter(tmpFavFile);
			BufferedReader br = new BufferedReader(new FileReader(favFile));
			String buff = null;
			
			while ((buff = br.readLine()) != null) {
				if (buff.equals(favRecord)) continue;
				else fw.write(buff+'\n');
			}
			fw.close();
			br.close();
			Files.move(tmpFavFile.toPath(), favFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			//favFile.delete();
			/*System.out.println(tmpFavFile.renameTo(favFile));
			System.out.println(tmpFavFile.getAbsolutePath());*/
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
}

class SearchThread extends Thread/*SwingWorker<Boolean,Void>*/ {
	String query;
	Drive drives[];
	JTable table;
	JButton button;
	DefaultTableModel tableModel;
	//SearchThread nextThread;
	boolean anyMatches = false;
	volatile boolean done = false;
	
	public SearchThread(String query, Drive[] drives, JTable table, JButton waitSearchButton) throws UnsupportedEncodingException {
		this.drives = drives;
		tableModel = new DefaultTableModel(null, new Object[] {new String(FileSearchManager.prop.getProperty("searchResults").getBytes(FileSearchManager.PROPERTY_ENCODING))});
		this.table = table;
		this.table.setModel(this.tableModel);
		this.query = query;
		this.button = waitSearchButton;
		//this.button = hideThis;
		//this.progress = progress;
	}
	
	/*@Override
	protected Boolean doInBackground() throws Exception {
	public void 
		search(drive);
		if (nextThread != null)
			nextThread.execute();
		return true;
	}
	
	@SuppressWarnings("unused")
	protected void done() {
	    boolean status;
	    try {
	    	status = get();
	    } catch (InterruptedException e) {
	    
	    } catch (ExecutionException e) {
	    
	    }
	}*/
	
	public void search(File dir) {
		if (dir.getName().toLowerCase().contains(query.toLowerCase())) {
			String match = dir.getAbsolutePath();
			tableModel.addRow(new Object[] {match});
			System.out.println(match);
			tableModel.fireTableDataChanged();
			table.revalidate();
			table.repaint();
			if (!anyMatches) anyMatches = true;
		}
		if (dir.isDirectory()) {
			File driveFiles[] = dir.listFiles();
			if (driveFiles != null) {
				for (File driveFile: driveFiles) {
					String progressTxt = driveFile.getAbsolutePath();
					//System.out.println(progressTxt);
					//progress.setText(progressTxt);
					boolean isDir = driveFile.isDirectory();
					if (driveFile.getName().toLowerCase().contains(query.toLowerCase()) && !isDir) {
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								tableModel.addRow(new Object[] {progressTxt});
								tableModel.fireTableDataChanged();
								table.revalidate();
								table.repaint();
								System.out.println(progressTxt);
							}

						});
						if (!anyMatches) anyMatches = true;
					}
					if (isDir) {
						search(driveFile);
					}
				}
			}
		}
	}

	@Override
	public void run() {
		try {
			for (Drive file: drives) {
				tableModel.addRow(new Object[] {file.drive.getAbsolutePath()+" "+new String(FileSearchManager.prop.getProperty("driveResults").getBytes(FileSearchManager.PROPERTY_ENCODING))+":"});
				search(file.drive);
				button.setVisible(false);
			}
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
	}
	
	//@Override
	/*public void run() {
		//table.setModel(tableModel);
		
		//button.setVisible(false);
		//notify();
	}*/
}

class PropertiesHolder {
	private Properties props;
	
	public PropertiesHolder(Properties props) {
		this.props = props;
	}
	
	public String getProperty(String key) {
		return props.getProperty(key);
	}
	
	public Properties getProperties() {
		return props;
	}
}

class FileTab {
	FileSearchManager fileMgr;
	JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
	TabThread currentTab;
	JPanel controlPanel = new JPanel();
	JPanel commonPanel = new JPanel();
	JTextField path = new JTextField();
	JButton jumpButton;
	JButton searchButton;
	JButton closeSearchButton;
	ArrayList<File> copyBuffer = new ArrayList<File>();
    ArrayList<TabThread> threads = new ArrayList<TabThread>();
    int tabNumber = 0;
    
    public FileTab(FileSearchManager fileMgr, ArrayList<File> copyBuffer) {
    	this.fileMgr = fileMgr;
    	this.copyBuffer = copyBuffer;
    }
    
    public void init() throws InterruptedException, UnsupportedEncodingException {
    	jumpButton = new JButton(new String(FileSearchManager.prop.getProperty("jumpButton").getBytes(FileSearchManager.PROPERTY_ENCODING)));
    	searchButton = new JButton(new String(FileSearchManager.prop.getProperty("searchButton").getBytes(FileSearchManager.PROPERTY_ENCODING)));
    	closeSearchButton = new JButton(new String(FileSearchManager.prop.getProperty("backToFileManagingButton").getBytes(FileSearchManager.PROPERTY_ENCODING)));
	    controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		//path.setBounds(0, 0, 610, 25);
		path.setPreferredSize(new Dimension(400, 25));
	    path.setFont(fileMgr.font);
	    controlPanel.add(path);
	    
		controlPanel.add(jumpButton);
		controlPanel.add(searchButton);
		controlPanel.add(closeSearchButton);
		closeSearchButton.setVisible(false);
		
		TabThread fileThread = new TabThread(fileMgr, this, 0, threads, null);
        currentTab = fileThread;
        threads.add(fileThread);
        fileThread.start();
        fileThread.join();
        
        /*tabbedPane.addChangeListener(new ChangeListener() {

        	@Override
        	public void stateChanged(ChangeEvent arg0) {
        		File dir = currentTab.dir;
        		if (dir == null)
        			path.setText("");
        		else
        			path.setText(dir.getAbsolutePath());	
        	}
        	
        });*/
        
        commonPanel.setLayout(new BorderLayout());
        commonPanel.add(this.controlPanel, BorderLayout.NORTH);
        commonPanel.add(this.tabbedPane, BorderLayout.CENTER);
    }
}

class Drive {
	File drive;
	boolean isSsd = false;
	
	public Drive(File file) {
		drive = file;
	}
	
	public void setSsd(boolean isSsd) {
		this.isSsd = isSsd;
	}
}

public class FileSearchManager {
	JFrame f = new JFrame();
	JFrame settingsFrame;
	//JFrame searchFrame = new JFrame("Search Pane");
	JMenuBar menuBar = new JMenuBar();
	JPanel rightPanel = new JPanel();
	FileTree tree;
	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    int index = 1;
    //int rightTabNumber = 1;
    final static String APP_TITLE = "File Manager";
    final static int FILE_LIMIT = 128;
    final static int FRAME_WIDTH = 1080;
    final static int FRAME_HEIGHT = 815;
    final static int DIVIDER_POSITION = 200;
    final static String FAV_FILE = "favourites.mgr";
    final static String FAVICON_URL = "/ico/favicon.png";
    final static String LOCAL_DRIVE_IMG_URL = "/ico/drive.png";
    final static String FOLDER_IMG_URL = "/ico/folder.png";
    final static String FILE_IMG_URL = "/ico/file.png";
    final static String LEVEL_UP_IMG_URL = "/ico/up_arrow.png";
    final static String PROPERTY_ENCODING = "ISO8859-1";
    ArrayList<File> copyBuffer = new ArrayList<File>();
    FileTab rightFilePanel;
    FileTab leftFilePanel;
    JMenu favouritesMenu;
    File favouritesFile = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()+"\\"+FAV_FILE);
    final static Drive[] LOCAL_DRIVES = getDrives()/*initDrives(*//*getReadable()*/;
    JMenuItem createTabMenuItem;
    ApplicationSettings settings = new ApplicationSettings(this);
    Font font;
    int treeRow = -1;
    public static PropertiesHolder prop;
    
    /*public static Drive[] initDrives(File drives[]) {
    	int len;
    	Drive[] localDrives = new Drive[len = drives.length];
    	for (int i = 0; i < len; i++) {
    		localDrives[i] = new Drive(drives[i]);
    	}
    	return localDrives;
    }*/
    
    private static Drive[] getDrives() {
    	File[] drives = File.listRoots();
    	int len = drives.length;
    	Drive[] localDrives = new Drive[len];
    	for (int i = 0; i < len; i++) {
    		localDrives[i] = new Drive(drives[i]);
    	}
    	return localDrives;
    }
    	
    public void addFolder(DefaultMutableTreeNode node, TreePath path) {
		System.out.println(path);
		
		File dir = new File(TabThread.getStringPath(path));
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			
			if (files.length != 0) {
				for (File file: files) {
					if (file.isDirectory()) {
						node.add(new DefaultMutableTreeNode(file.getName()));
					}
					System.out.println(file.getName());
				}
			} else {
				node.add(new DefaultMutableTreeNode("<empty>"));
			}
		}
	}
    
    public static boolean isDrive(File file) {
    	for (Drive drive: LOCAL_DRIVES) {
    		if (file.equals(drive.drive))
    			return true;
    	}
    	return false;
    }
    
    public static boolean isDrive(String file) {
    	for (Drive drive: LOCAL_DRIVES) {
    		if (file.equals(drive.drive.getAbsolutePath()))
    			return true;
    	}
    	return false;
    }
    
    public static Drive[] getSsdDrives() {
    	ArrayList<Drive> ssdDrives = new ArrayList<>();
    	for (Drive drive: LOCAL_DRIVES) {
    		if (drive.isSsd)
    			ssdDrives.add(drive);
    	}
    	return (Drive[]) ssdDrives.toArray();
    }
    
    public static Drive[] getHddDrives() {
    	ArrayList<Drive> hddDrives = new ArrayList<>();
    	for (Drive drive: LOCAL_DRIVES) {
    		if (!drive.isSsd)
    			hddDrives.add(drive);
    	}
    	return (Drive[]) hddDrives.toArray();
    }
    /*private static Object[] getReadable() {
    	ArrayList<File> readable = new ArrayList<File>(); 
    	File[] roots = File.listRoots();
    	for (File root: roots) {
    		if (root.canRead()) {
    			readable.add(root);
    		}
    	}
    	return readable.toArray();
    }*/
    
	public void init() throws InterruptedException, IOException {
		f.setTitle(APP_TITLE);
		f.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        //f.getContentPane().setLayout(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        URL faviconURL = getClass().getResource(FAVICON_URL);
        if (faviconURL != null) {
	        ImageIcon favicon = new ImageIcon(faviconURL);
	        f.setIconImage(favicon.getImage());
        }
        //tabbedPane.setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
        
        settingsFrame = new JFrame(new String(FileSearchManager.prop.getProperty("settingsConst").getBytes(FileSearchManager.PROPERTY_ENCODING)));
        favouritesMenu = new JMenu(new String(FileSearchManager.prop.getProperty("favouritesConst").getBytes(FileSearchManager.PROPERTY_ENCODING)));
        
        splitPane.setDividerLocation(DIVIDER_POSITION);
		splitPane.setDividerSize(5);
        
		f.setContentPane(splitPane);
		font = settings.getFont();
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode (new String(FileSearchManager.prop.getProperty("myPc").getBytes(FileSearchManager.PROPERTY_ENCODING)));
        tree = new FileTree(root);
        
        for (Drive drive: LOCAL_DRIVES) {
        	DefaultMutableTreeNode newDrive = new DefaultMutableTreeNode(drive.drive.getAbsolutePath());
        	root.add(newDrive);
        	tree.addNodeToMap(newDrive);
        	addFolder(newDrive, new TreePath(newDrive.getPath()));
    	}
        /*DefaultMutableTreeNode cdrive = new DefaultMutableTreeNode("C:\\");
        DefaultMutableTreeNode ddrive = new DefaultMutableTreeNode("D:\\");
        root.add(cdrive);
        root.add(ddrive);
        addFolder(cdrive, new TreePath(cdrive.getPath())/*getPath(cdrive.getPath())*//*);
        addFolder(ddrive, new TreePath(ddrive.getPath()));*/
        tree.setFont(font);
        TreeModel model = tree.getModel();
        //tree.addTreeSelectionListener(new FolderTreeListener(tabbedPane));
        tree.addTreeWillExpandListener(new TreeWillExpandListener() {
			
			@Override
			public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
		        TreePath path = event.getPath();
		        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		        
		        int childCnt = model.getChildCount(node);
		        //DefaultMutableTreeNode children[] = new DefaultMutableTreeNode[childCnt];
		        
		        for (int i = 0; i < childCnt; i++) {
		        	DefaultMutableTreeNode children = (DefaultMutableTreeNode) model.getChild(node, i);
		        	if (children.isLeaf()) {
			        	File child = new File(TabThread.getStringPath(path)+"\\"+children.toString()); 
			        	if (child.isDirectory()) {
			        		File[] childDirs = child.listFiles();
			        		if (childDirs != null && childDirs.length != 0) {
				        		for (File childDir: childDirs) {
				        			if (childDir.isDirectory()) {
				        				DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(childDir.getName());
				        				children.add(childNode);
				        				tree.addNodeToMap(childNode);
				        			}
				        		}
			        		}
			        	}
		        	}
		        }
		        
		        //Print the name of the node if toString() was implemented
		        String data = node.getUserObject().toString();
		        System.out.println("WillCollapse: " + data);
			}
			
			@Override
			public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {}
		});
        
        /*JPopupMenu treePopup = new JPopupMenu();
        JMenuItem copyMenuItem = new JMenuItem("Copy");
        copyMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				tree.getSelected
			}
        	
        })
        treePopup.add();
        treePopup.add(pasteMenuItem);
        treePopup.add(cutMenuItem);
        treePopup.add(renameMenuItem);
        treePopup.add(delMenuItem);
        treePopup.add(addFavouriteFromTableMenuItem);
        treePopup.add(propMenuItem);
        treePopup.add(createMenuItem);
        treePopup.add(closeTabMenuItem);*/
        tree.expandRow(0);
		
		rightFilePanel = new FileTab(this, copyBuffer);
		rightFilePanel.init();
		
    	splitPane.setRightComponent(rightFilePanel.commonPanel);
        
        JScrollPane treeScroll = new JScrollPane();
        treeScroll.setBounds(20,10,200,685);
        treeScroll.setPreferredSize(new Dimension(200, 685));
        treeScroll.getViewport().add(tree);
        splitPane.setLeftComponent(treeScroll);
        
        JMenu openMenu = new JMenu(new String(FileSearchManager.prop.getProperty("fileConst").getBytes(FileSearchManager.PROPERTY_ENCODING)));
        menuBar.add(openMenu);
        createTabMenuItem = new JMenuItem(new String(FileSearchManager.prop.getProperty("createNewTab").getBytes(FileSearchManager.PROPERTY_ENCODING))); 
        createTabMenuItem.setFont(font);
        openMenu.add(createTabMenuItem);
        createTabMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (leftFilePanel != null) {
					try {
						Object[] customAlt = {new String(FileSearchManager.prop.getProperty("leftFilePanelButton").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("rightFilePanelButton").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("cancelButton").getBytes(FileSearchManager.PROPERTY_ENCODING))};
						int answ = JOptionPane.showOptionDialog(f, new String(FileSearchManager.prop.getProperty("selectFilePanelCreateTavMsg").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("confirmTitle").getBytes(FileSearchManager.PROPERTY_ENCODING)),
				                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
				                null, customAlt, null);
						switch(answ) {
							case JOptionPane.YES_OPTION: {
									ArrayList<TabThread> tabThreads = leftFilePanel.threads; 
									TabThread newFileThread = new TabThread(FileSearchManager.this, leftFilePanel, ++leftFilePanel.tabNumber, tabThreads, null);
									tabThreads.add(newFileThread);
									newFileThread.start();
									try {
										newFileThread.join();
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									leftFilePanel.currentTab = newFileThread;
									leftFilePanel.tabbedPane.setSelectedIndex(leftFilePanel.tabNumber);
									break;
								} 
							case JOptionPane.NO_OPTION: {
								ArrayList<TabThread> tabThreads = rightFilePanel.threads; 
								TabThread newFileThread = new TabThread(FileSearchManager.this, rightFilePanel, ++rightFilePanel.tabNumber, tabThreads, null);
								tabThreads.add(newFileThread);
								newFileThread.start();
								try {
									newFileThread.join();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								rightFilePanel.currentTab = newFileThread;
								rightFilePanel.tabbedPane.setSelectedIndex(rightFilePanel.tabNumber);
								break;
							}
							case JOptionPane.CANCEL_OPTION: {
								return;
							}
						}
					} catch (UnsupportedEncodingException ex) {
						ex.printStackTrace();
					}
				} else {
					try {
						ArrayList<TabThread> tabThreads = rightFilePanel.threads; 
						TabThread newFileThread = new TabThread(FileSearchManager.this, rightFilePanel, ++rightFilePanel.tabNumber, tabThreads, null);
						tabThreads.add(newFileThread);
						newFileThread.start();
						try {
							newFileThread.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						rightFilePanel.currentTab = newFileThread;
						rightFilePanel.tabbedPane.setSelectedIndex(rightFilePanel.tabNumber);
					} catch (UnsupportedEncodingException ex) {
						ex.printStackTrace();
					}
				}
			}
        	        	
        });
        JMenuItem exitMenuItem = new JMenuItem(new String(FileSearchManager.prop.getProperty("exitConst").getBytes(FileSearchManager.PROPERTY_ENCODING)));
        exitMenuItem.setFont(font);
        exitMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				f.dispatchEvent(new WindowEvent(f, WindowEvent.WINDOW_CLOSING));
			}
        	
        });
        openMenu.add(exitMenuItem);
        openMenu.setFont(font);
        JMenu bufferMenu = new JMenu(new String(FileSearchManager.prop.getProperty("bufferConst").getBytes(FileSearchManager.PROPERTY_ENCODING)));
        bufferMenu.setFont(font);
        menuBar.add(bufferMenu);
        JMenuItem showBufferMenuItem = new JMenuItem(new String(FileSearchManager.prop.getProperty("showBuffer").getBytes(FileSearchManager.PROPERTY_ENCODING)));
        showBufferMenuItem.setFont(font);
        showBufferMenuItem.addActionListener(new ActionListener() {

			@SuppressWarnings("serial")
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					JTable buffer = new JTable() {
			        	public boolean isCellEditable(int row, int column) {                
			                return false;               
			        	}
					};
					buffer.setRowSelectionAllowed(false);
					DefaultTableModel tm = new DefaultTableModel(null, new Object[] {new String(FileSearchManager.prop.getProperty("fileConst").getBytes(FileSearchManager.PROPERTY_ENCODING))});
					buffer.setModel(tm);
					if (!copyBuffer.isEmpty()) {
						for (File file: copyBuffer) {
							tm.addRow(new Object[] {file.getAbsolutePath()});
						}
						JOptionPane.showMessageDialog(f, buffer, new String(FileSearchManager.prop.getProperty("bufferConst").getBytes(FileSearchManager.PROPERTY_ENCODING)), JOptionPane.PLAIN_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(f, new String(FileSearchManager.prop.getProperty("buffIsEmpty").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("bufferConst").getBytes(FileSearchManager.PROPERTY_ENCODING)), JOptionPane.WARNING_MESSAGE);
					}
				} catch (UnsupportedEncodingException ex) {
					ex.printStackTrace();
				}
			}
        	
        });
        JMenuItem cleanBufferMenuItem = new JMenuItem(new String(FileSearchManager.prop.getProperty("clearBuffMenuItem").getBytes(FileSearchManager.PROPERTY_ENCODING)));
        cleanBufferMenuItem.setFont(font);
        cleanBufferMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				copyBuffer.clear();
				if (copyBuffer.isEmpty()) {
					try {
						JOptionPane.showMessageDialog(f, new String(FileSearchManager.prop.getProperty("buffWasClearedMsg").getBytes(FileSearchManager.PROPERTY_ENCODING)));
					} catch (HeadlessException | UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}
        	
        });
        bufferMenu.add(showBufferMenuItem);
        bufferMenu.add(cleanBufferMenuItem);
        JMenu viewMenu = new JMenu(new String(FileSearchManager.prop.getProperty("viewMenuItem").getBytes(FileSearchManager.PROPERTY_ENCODING)));
        viewMenu.setFont(font);
        JMenu styleViewMenu = new JMenu(new String(FileSearchManager.prop.getProperty("styleMenuItem").getBytes(FileSearchManager.PROPERTY_ENCODING)));
        styleViewMenu.setFont(font);
        JMenu orientationViewMenu = new JMenu(new String(FileSearchManager.prop.getProperty("orientationMenuItem").getBytes(FileSearchManager.PROPERTY_ENCODING)));
        orientationViewMenu.setFont(font);
        ButtonGroup radioGroup = new ButtonGroup();
        ButtonGroup styleRadio = new ButtonGroup();
        JRadioButtonMenuItem horizontal = new JRadioButtonMenuItem(new String(FileSearchManager.prop.getProperty("horizontalButton").getBytes(FileSearchManager.PROPERTY_ENCODING)), true);
        horizontal.setFont(font);
        horizontal.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
			}
        	
        });
        JRadioButtonMenuItem vertical = new JRadioButtonMenuItem(new String(FileSearchManager.prop.getProperty("verticalButton").getBytes(FileSearchManager.PROPERTY_ENCODING)));
        vertical.setFont(font);
        vertical.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			}
        	
        });
               
        JRadioButtonMenuItem treeTabStyle = new JRadioButtonMenuItem(new String(FileSearchManager.prop.getProperty("treeTabView").getBytes(FileSearchManager.PROPERTY_ENCODING)), true);
        treeTabStyle.setFont(font);
        treeTabStyle.addActionListener(new ActionListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (leftFilePanel != null) {
					try {
						Object[] customAlt = {new String(FileSearchManager.prop.getProperty("yesConst").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("noConst").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("cancelButton").getBytes(FileSearchManager.PROPERTY_ENCODING))};
						int answ = JOptionPane.showOptionDialog(f, new String(FileSearchManager.prop.getProperty("moveTabToOnlyPanelConfirm").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("confirmTitle").getBytes(FileSearchManager.PROPERTY_ENCODING)),
				                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
				                null, customAlt, null);
						if (answ == JOptionPane.YES_OPTION) {
							ArrayList<TabThread> leftPanelThreads = leftFilePanel.threads;
							ArrayList<TabThread> rightPanelThreads = rightFilePanel.threads; 
							leftFilePanel.currentTab.stop();
							for (TabThread movedThread: leftPanelThreads) {
								TabThread newFileThread = new TabThread(FileSearchManager.this, rightFilePanel, ++rightFilePanel.tabNumber, rightPanelThreads, movedThread.dir);
								newFileThread.start();
								rightPanelThreads.add(newFileThread);
							}
							splitPane.setLeftComponent(treeScroll);
							splitPane.setDividerLocation(DIVIDER_POSITION);
							leftPanelThreads.clear();
							leftPanelThreads = null;
							leftFilePanel = null;
							System.gc();
						} else if (answ == JOptionPane.NO_OPTION) {
							splitPane.setLeftComponent(treeScroll);
							leftFilePanel.threads.clear();
							leftFilePanel = null;
							System.gc();
						} else {
							return;
						}
					} catch (UnsupportedEncodingException ex) {
						ex.printStackTrace();
					}
				} else {
					splitPane.setLeftComponent(treeScroll);
				}
			}
        	
        });
        JRadioButtonMenuItem twoTabsStyle = new JRadioButtonMenuItem(new String(FileSearchManager.prop.getProperty("twoTabsView").getBytes(FileSearchManager.PROPERTY_ENCODING)));
        twoTabsStyle.setFont(font);
        twoTabsStyle.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				leftFilePanel = new FileTab(FileSearchManager.this, copyBuffer);
				try {
					leftFilePanel.init();
					leftFilePanel.commonPanel.setPreferredSize(new Dimension(f.getWidth()/2, f.getHeight()/2));
					splitPane.setLeftComponent(leftFilePanel.commonPanel);
					splitPane.setDividerLocation(f.getWidth()/2);
				} catch (InterruptedException | UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
        	
        });
        radioGroup.add(horizontal);
        radioGroup.add(vertical);
        orientationViewMenu.add(horizontal);
        orientationViewMenu.add(vertical);
        viewMenu.add(orientationViewMenu);
        
        styleRadio.add(treeTabStyle);
        styleRadio.add(twoTabsStyle);
        styleViewMenu.add(treeTabStyle);
        styleViewMenu.add(twoTabsStyle);
        viewMenu.add(styleViewMenu);
        viewMenu.setFont(font);
        
        menuBar.add(viewMenu);
        favouritesMenu.setFont(font);
        menuBar.add(favouritesMenu);
        
        JMenu settingsMenu = new JMenu(new String(FileSearchManager.prop.getProperty("settingsConst").getBytes(FileSearchManager.PROPERTY_ENCODING)));
        settingsMenu.setFont(font);
        settingsMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuCanceled(MenuEvent arg0) {}

			@Override
			public void menuDeselected(MenuEvent arg0) {}

			@Override
			public void menuSelected(MenuEvent arg0) {
				try {
					settings.showSettings();
				} catch (HeadlessException | UnsupportedEncodingException | SecurityException e) {
					e.printStackTrace();
				}
			}
		});
        JMenu aboutMenu = new JMenu(new String(FileSearchManager.prop.getProperty("aboutConst").getBytes(FileSearchManager.PROPERTY_ENCODING)));
        aboutMenu.setFont(font);
        aboutMenu.addMenuListener(new MenuListener() {

			@Override
			public void menuCanceled(MenuEvent arg0) {}

			@Override
			public void menuDeselected(MenuEvent arg0) {}

			@Override
			public void menuSelected(MenuEvent arg0) {
				try {
					if (faviconURL != null) {
						//JOptionPane.showInternalMessageDialog(f, FileSearchManager.APP_TITLE+"\n"+new String(FileSearchManager.prop.getProperty("author").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("aboutConst").getBytes(FileSearchManager.PROPERTY_ENCODING)), JOptionPane.PLAIN_MESSAGE, new ImageIcon(faviconURL));
						JOptionPane.showMessageDialog(f, FileSearchManager.APP_TITLE+"\n"+new String(FileSearchManager.prop.getProperty("author").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("aboutConst").getBytes(FileSearchManager.PROPERTY_ENCODING)), JOptionPane.PLAIN_MESSAGE, new ImageIcon(faviconURL));
					} else {
						JOptionPane.showMessageDialog(f, FileSearchManager.APP_TITLE+"\n"+new String(FileSearchManager.prop.getProperty("author").getBytes(FileSearchManager.PROPERTY_ENCODING)), new String(FileSearchManager.prop.getProperty("aboutConst").getBytes(FileSearchManager.PROPERTY_ENCODING)), JOptionPane.PLAIN_MESSAGE);
					}
				} catch (UnsupportedEncodingException ex) {
					ex.printStackTrace();
				}
			}
		});
        
        menuBar.add(settingsMenu);
        menuBar.add(aboutMenu);
        f.setJMenuBar(menuBar);

        //System.out.println(favouritesFile.getAbsolutePath());
        if (favouritesFile == null || !favouritesFile.exists()) {
        	System.out.println(favouritesFile.createNewFile());
        } else {
        	FileReader fr = new FileReader(favouritesFile);
        	BufferedReader br = new BufferedReader(fr);
        	String buff = null;
        	
        	while ((buff = br.readLine()) != null) {
        		if (new File(buff).exists()) {
	        		JMenu anotherFav = new JMenu(buff);
	        		anotherFav.setFont(font);	        		
	        		anotherFav.addMouseListener(new FavouritesMouseListener(this, anotherFav));
	        		JMenuItem delFavMenuItem = new JMenuItem(new String(FileSearchManager.prop.getProperty("deletePopup").getBytes(FileSearchManager.PROPERTY_ENCODING)));
	        		delFavMenuItem.setFont(font);
	        		delFavMenuItem.addActionListener(new DeleteFavouriteActionListener(anotherFav, this));
	        		anotherFav.add(delFavMenuItem);
	        		favouritesMenu.add(anotherFav);
        		}
        	}
        	br.close();
        	fr.close();
        }
        
        f.addComponentListener(new ComponentAdapter() {
        	@Override
            public void componentResized(ComponentEvent e ) {
        		if (leftFilePanel != null)
        			splitPane.setDividerLocation(f.getWidth()/2);
        		else
        			splitPane.setDividerLocation(DIVIDER_POSITION);
            }
        });
        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
        f.setVisible(true);
	}    
	
	public static void main(String[] args) throws InterruptedException, IOException {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FileSearchManager fm = new FileSearchManager();
					fm.settings.initSettings(new File(ApplicationSettings.SETTINGS_FILE));
					fm.init();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
			}
		});
	}

}