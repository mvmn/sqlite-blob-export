package x.mvmn.sqliteblobexporter.db;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import javax.swing.SwingUtilities;


/**
 * @author Mykola Makhin
 *
 */
public class ExportWorker implements Runnable {

	private static final String FILE_SEPARATOR; 
	static {
		String fileSeparatorStr = System.getProperty("file.separator");
		if(fileSeparatorStr == null || fileSeparatorStr.trim().length()<1) {
			fileSeparatorStr = "/";
		}
		FILE_SEPARATOR = fileSeparatorStr;
	}

	private Connection connection;
	private List<String> fieldsAndTablesNames;
	private File directoryToStoreTo;
	private Runnable startCallback;
	private Runnable progressCallback;
	private Runnable finishCallback;
	
	private volatile Exception error = null;
	private volatile boolean finished = false;
	private volatile int progress = 0;
	private volatile String status = "Initializing";
	
	public ExportWorker(Connection connection, List<String> fieldsAndTablesNames, File directoryToStoreTo, Runnable startCallback, Runnable progressCallback, Runnable finishCallback) {
		super();
		this.connection = connection;
		this.fieldsAndTablesNames = fieldsAndTablesNames;
		this.directoryToStoreTo = directoryToStoreTo;
		this.startCallback = startCallback;
		this.progressCallback = progressCallback;
		this.finishCallback = finishCallback;
	}

	public void run() {
		SwingUtilities.invokeLater(startCallback);
		
		try {
			for(String fieldAndTableNames : fieldsAndTablesNames) {
				status = "Exporting "+fieldAndTableNames;
				progress = 0;
				performExport(fieldAndTableNames);
				SwingUtilities.invokeLater(progressCallback);
			}
		} catch (Exception e) {
			e.printStackTrace();
			error = e;
		} finally {
			SwingUtilities.invokeLater(progressCallback);
			SwingUtilities.invokeLater(finishCallback);
		}
		finished = true;
	}
	
	protected void performExport(String fieldFromTable) throws Exception {
		PreparedStatement statement = connection.prepareStatement("select "+fieldFromTable);
		ResultSet resultSet = statement.executeQuery();
		try {
			while(!resultSet.isAfterLast()) {
				Object obj = resultSet.getObject(1);
				if(obj!=null) {
					FileOutputStream fileOutputStream = new FileOutputStream(directoryToStoreTo.getAbsolutePath()+FILE_SEPARATOR+fieldFromTable.replaceAll(" ", "_")+"_"+resultSet.getRow());
					try {
						if(obj instanceof byte[]) {
							fileOutputStream.write((byte[])obj);
						} else {
							fileOutputStream.write(obj.toString().getBytes());
						}
						fileOutputStream.flush();
					} finally {
						try {
							fileOutputStream.close();
						} catch (Exception e) {}
					}
				}
				
				if(progress%5==0) SwingUtilities.invokeLater(progressCallback);
				progress = resultSet.getRow();
				resultSet.next();
			}
		} finally {
			try {
				statement.close();
			} catch (Exception e) {}
		}
	}
	
	public Exception getError() {
		return error;
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public int getProgress() {
		return progress;
	}
	
	public String getStatus() {
		return status;
	}
}
