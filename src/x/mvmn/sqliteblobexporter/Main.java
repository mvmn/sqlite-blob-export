package x.mvmn.sqliteblobexporter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import x.mvmn.sqliteblobexporter.db.ExportWorker;
import x.mvmn.sqliteblobexporter.db.util.SQLiteUtil;
import x.mvmn.sqliteblobexporter.gui.MainWindow;
import x.mvmn.sqliteblobexporter.gui.ProgressDialog;
import x.mvmn.sqliteblobexporter.gui.DBMetaDataTreeModel.NodeWrapper;
import x.mvmn.sqliteblobexporter.gui.util.SwingUtil;

/**
 * @author Mykola Makhin
 *
 */
public class Main {
	
	public static void main(String[] args) {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Please put SQLite JDBC driver (sqlitejdbc-v056.jar file) in same folder with this program (or add it to classpath).");
			return;
		}
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileFilter(new FileFilter() {

			@Override
			public boolean accept(File file) {
				if(file==null) return false;
				
				boolean result = false;
				
				String fileName = file.getName();
				int indexOfDot = fileName.indexOf(".");
				if(indexOfDot>-1) {
					String extension = fileName.substring(indexOfDot);
					
					if(".sqlite".equalsIgnoreCase(extension)) result = true;
				}
				return result;
			}

			@Override
			public String getDescription() {
				return "SQLite DB files";
			}
			
		});
		
		int openDialogResult = fileChooser.showOpenDialog(null);
		
		if(JFileChooser.APPROVE_OPTION == openDialogResult) {
			File file = fileChooser.getSelectedFile();
			if(file!=null && file.exists()) {
				new Main().launch(file);
			}			
		}
	}
	
	private Connection connection;
	
	private void launch(File file) {

		
		try {
			connection = SQLiteUtil.openSQLiteDBFile(file);
			Map<String, Map<String, String>> dbMetaData = SQLiteUtil.listTablesFieldsWithTypes(connection);
			final MainWindow mainWindow = new MainWindow(dbMetaData);
			SwingUtil.resizeWindowToBestFit(mainWindow, 20);
			SwingUtil.moveWindowToScreenCenter(mainWindow);
			mainWindow.setVisible(true);
			
			final Connection connectionParam = connection;
			mainWindow.addDisposeListener(new Runnable() {

				public void run() {
					try {
						connectionParam.close();
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			});
			
			mainWindow.addExportButtonListener(new ActionListener() {

				public void actionPerformed(ActionEvent actEvent) {
					JFileChooser jFileChooser = new JFileChooser();
					jFileChooser.setMultiSelectionEnabled(false);
					jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					if(JFileChooser.APPROVE_OPTION == jFileChooser.showOpenDialog(mainWindow)) {
						List<NodeWrapper> nodes = mainWindow.getSelectedFields();
						List<String> fieldsTabelsNames = new ArrayList<String>(nodes.size());
						for(NodeWrapper node : nodes) {
							String fieldFromTable = node.getId()+" from "+node.getFieldTableName();
							fieldsTabelsNames.add(fieldFromTable);
						}
						
						final ProgressDialog progressDialog = new ProgressDialog(mainWindow);
						
						ExportWorker exportWorker = new ExportWorker(connection, fieldsTabelsNames, jFileChooser.getSelectedFile(), 
								new Runnable() {
									public void run() {
										progressDialog.pack();
										SwingUtil.moveWindowToScreenCenter(progressDialog);
										progressDialog.setVisible(true);
									}
								},
								progressDialog.createProgressCallback(),
								progressDialog.createFinishCallback()
							);
						progressDialog.setExportWorker(exportWorker);
						new Thread(exportWorker).start();
					}
				}				
			});
		} catch(Exception e) {
			try {
				if(connection!=null) connection.close();
			} catch(Exception ccex) {}
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Exception "+e.getClass().getName()+" occurred: "+e.getMessage());
		}
	}
	
	//public static 

}
