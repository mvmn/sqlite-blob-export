package x.mvmn.sqliteblobexporter.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import x.mvmn.sqliteblobexporter.gui.DBMetaDataTreeModel.NodeWrapper;


/**
 * @author Mykola Makhin
 *
 */
public class MainWindow extends JFrame {
	
	private static final long serialVersionUID = 4677653305326297023L;

	private final ConcurrentLinkedQueue<Runnable> disposeListeners = new ConcurrentLinkedQueue<Runnable>();
	private JTree dbTree;
	private JButton btnExport;
	
	public MainWindow(final Map<String, Map<String, String>> dbMetaData) {
		super("SQLite BLOB exporter by Mykola Makhin");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.getContentPane().setLayout(new BorderLayout());
		
		btnExport = new JButton("Export");
		this.getContentPane().add(btnExport, BorderLayout.SOUTH);
		
		TreeModel treeModel = new DBMetaDataTreeModel(dbMetaData);
		dbTree = new JTree(treeModel);
		this.getContentPane().add(new JScrollPane(dbTree), BorderLayout.CENTER);
	}

	public void addExportButtonListener(ActionListener actListener) {
		this.btnExport.addActionListener(actListener);
	}

	public void removeExportButtonListener(ActionListener actListener) {
		this.btnExport.removeActionListener(actListener);
	}

	public void addDisposeListener(Runnable disposeListener) {
		this.disposeListeners.add(disposeListener);
	}

	public void removeDisposeListener(Runnable disposeListener) {
		this.disposeListeners.remove(disposeListener);
	}
	
	public List<NodeWrapper> getSelectedFields() {
		List<NodeWrapper> result = Collections.emptyList();
		
		TreePath[] selectionPaths = dbTree.getSelectionPaths();
		if(selectionPaths!=null) {
			result = new ArrayList<NodeWrapper>(selectionPaths.length);
		
			for(TreePath selectionPath : dbTree.getSelectionPaths()) {
				Object nodeObj = selectionPath.getLastPathComponent();
				if(nodeObj instanceof NodeWrapper) {
					NodeWrapper node = (NodeWrapper) nodeObj;
					if(node.getFieldType()!=null) {
						result.add(node);
					}
				}
			}
		}
		
		return result;
	}

	@Override
	public void dispose() {
		for(Runnable disposeListener : disposeListeners) {
			try {
				disposeListener.run();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.dispose();
	}
	
}
