package x.mvmn.sqliteblobexporter.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;


/**
 * @author Mykola Makhin
 *
 */
public class DBMetaDataTreeModel implements TreeModel {

	private ConcurrentLinkedQueue<TreeModelListener> modelListeners = new ConcurrentLinkedQueue<TreeModelListener>();
	
	//private Map<String, Map<String, String>> dbMetaData;
	private NodeWrapper rootNode;
	
	public static class NodeWrapper implements Comparable<NodeWrapper> {
		
		private String id;
		private String title;
		private String fieldType = null;
		private String fieldTableName = null;
		private List<NodeWrapper> children;
		
		public NodeWrapper(String tableName, String fieldName, String fieldType) {
			id = fieldName;
			title = fieldName+"["+fieldType+"]";
			fieldTableName = tableName;
			this.fieldType = fieldType;
			children = null;
		}

		public NodeWrapper(String tableName, List<NodeWrapper> tableFields) {
			id = tableName;
			title = tableName;
			children = tableFields;			
		}
		
		public NodeWrapper(List<NodeWrapper> tables) {
			id = null;
			title = "Tables";
			children = tables;			
		}
		
		public String toString() {
			return getTitle();
		}
		
		public String getId() {
			return id;
		}
		
		public String getTitle() {
			return title;
		}

		public List<NodeWrapper> getChildren() {
			return children;
		}

		public String getFieldType() {
			return fieldType;
		}

		public String getFieldTableName() {
			return fieldTableName;
		}

		public int compareTo(NodeWrapper o) {
			String title2 = o.getTitle();
			if(title == title2) return 0;
			if(title == null) return -1;
			if(title2 == null) return 1;
			
			return title.compareTo(title2);
		}			
	}
	
	public DBMetaDataTreeModel(final Map<String, Map<String, String>> dbMetaData) {
		//this.dbMetaData = dbMetaData;
		
		List<NodeWrapper> tablesNodes = new ArrayList<NodeWrapper>(dbMetaData.size());
		for(String tableName : dbMetaData.keySet()) {
			Map<String, String> fieldsMeta = dbMetaData.get(tableName);
			List<NodeWrapper> fieldsNodes = new ArrayList<NodeWrapper>(fieldsMeta.size());
			for(Map.Entry<String, String> fieldEntry : fieldsMeta.entrySet()) {
				NodeWrapper node = new NodeWrapper(tableName, fieldEntry.getKey(), fieldEntry.getValue());
				fieldsNodes.add(node);
			}
			Collections.sort(fieldsNodes);
			tablesNodes.add(new NodeWrapper(tableName, fieldsNodes));
		}
		
		Collections.sort(tablesNodes);
		rootNode = new NodeWrapper(tablesNodes);
	}

	public void addTreeModelListener(TreeModelListener tml) {
		modelListeners.add(tml);
	}

	public void removeTreeModelListener(TreeModelListener tml) {
		modelListeners.add(tml);
	}

	public void valueForPathChanged(TreePath arg0, Object arg1) {}
	
	public NodeWrapper getChild(Object nodeObj, int childIndex) {
		NodeWrapper result = null;
		
		if(nodeObj instanceof NodeWrapper) {
			NodeWrapper node = (NodeWrapper)nodeObj;
			if(node.getChildren()!=null && node.getChildren().size()>childIndex) {
				result = node.getChildren().get(childIndex);
			}
		}
		
		return result;
	}

	public int getChildCount(Object nodeObj) {
		int result = 0;
		
		if(nodeObj instanceof NodeWrapper) {
			NodeWrapper node = (NodeWrapper)nodeObj;
			if(node.getChildren()!=null) result = node.getChildren().size();
		}
		
		return result;
	}

	public int getIndexOfChild(Object parentObj, Object childObj) {
		int result = -1;

		if(parentObj instanceof NodeWrapper && childObj instanceof NodeWrapper) {
			NodeWrapper parentNode = (NodeWrapper)parentObj;
			NodeWrapper childNode = (NodeWrapper)childObj;
			if(parentNode.getChildren()!=null) {
				result = Collections.binarySearch(parentNode.getChildren(), childNode);
			}
		}
		
		return result;
	}

	public NodeWrapper getRoot() {
		return rootNode;
	}

	public boolean isLeaf(Object arg0) {
		boolean result = true;
		if((arg0 instanceof NodeWrapper) && ((NodeWrapper)arg0).getChildren()!=null) result = false;
		return result;
	}
}
