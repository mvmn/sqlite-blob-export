package x.mvmn.sqliteblobexporter.db.util;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Properties;
import java.util.Map;


/**
 * @author Mykola Makhin
 *
 */
public class SQLiteUtil {

	public static Connection openSQLiteDBFile(File file) throws SQLException {
		if(file==null) throw new IllegalArgumentException("SQLite DB file cannot be null");
		String dbUrl = "jdbc:sqlite:"+file.getAbsolutePath();
		Driver jdbcDriver = DriverManager.getDriver(dbUrl);
		Connection connection = jdbcDriver.connect(dbUrl, new Properties());
		return connection;
	}

	public static Map<String, Map<String, String>> listTablesFieldsWithTypes(Connection connection) throws SQLException {
		Map<String, Map<String, String>> tablesFieldsWithTypes = new HashMap<String,  Map<String, String>>();
		
		PreparedStatement statementListTables = connection.prepareStatement("select name from SQLITE_MASTER where type='table'");
		PreparedStatement statementDummySelect;
		ResultSet tablesListResultSet = statementListTables.executeQuery();
		while(!tablesListResultSet.isAfterLast()) {
			String tableName = tablesListResultSet.getString(1);

			statementDummySelect = connection.prepareStatement("select * from "+tableName+" limit 1");
			try {
				ResultSet dummySelectResults = statementDummySelect.executeQuery();
				ResultSetMetaData tableMeta = dummySelectResults.getMetaData();
				Map<String, String> columnsData = new HashMap<String, String>();
				for(int i=0; i<tableMeta.getColumnCount(); i++) {
					String columnName = tableMeta.getColumnName(i+1);
					int columnType = tableMeta.getColumnType(i+1);
					columnsData.put(columnName, columnTypeToName(columnType));
				}
				tablesFieldsWithTypes.put(tableName, columnsData);
			} finally {
				try {
					statementDummySelect.close();
				} catch (Exception e) {}
			}
			tablesListResultSet.next();
		}
		
		return tablesFieldsWithTypes;
	}
	
	private static String columnTypeToName(int columnType) {
		String result = String.valueOf(columnType);
		
		switch(columnType) {
			case Types.ARRAY: result="ARRAY"; break;
			case Types.BIGINT: result="BIGINT"; break;
			case Types.BINARY: result="BINARY"; break;
			case Types.BIT: result="BIT"; break;
			case Types.BLOB: result="BLOB"; break;
			case Types.BOOLEAN: result="BOOLEAN"; break;
			case Types.CHAR: result="CHAR"; break;
			case Types.CLOB: result="CLOB"; break;
			case Types.DATALINK: result="DATALINK"; break;
			case Types.DATE: result="DATE"; break;
			case Types.DECIMAL: result="DECIMAL"; break;
			case Types.DISTINCT: result="DISTINCT"; break;
			case Types.DOUBLE: result="DOUBLE"; break;
			case Types.FLOAT: result="FLOAT"; break;
			case Types.INTEGER: result="INTEGER"; break;
			case Types.JAVA_OBJECT: result = "Array"; break;
			case Types.LONGNVARCHAR: result="LONGNVARCHAR"; break;
			case Types.LONGVARBINARY: result="LONGVARBINARY"; break;
			case Types.LONGVARCHAR: result="LONGVARCHAR"; break;
			case Types.NCHAR: result="NCHAR"; break;
			case Types.NCLOB: result="NCLOB"; break;
			case Types.NULL: result="NULL"; break;
			case Types.NUMERIC: result="NUMERIC"; break;
			case Types.NVARCHAR: result="NVARCHAR"; break;
			case Types.OTHER: result="OTHER"; break;
			case Types.REAL: result="REAL"; break;
			case Types.REF: result="REF"; break;
			case Types.ROWID: result="ROWID"; break;
			case Types.SMALLINT: result="SMALLINT"; break;
			case Types.SQLXML: result="SQLXML"; break;
			case Types.STRUCT: result="STRUCT"; break;
			case Types.TIME: result="TIME"; break;
			case Types.TIMESTAMP: result="TIMESTAMP"; break;
			case Types.TINYINT: result="TINYINT"; break;
			case Types.VARBINARY: result="VARBINARY"; break;
			case Types.VARCHAR: result="VARCHAR"; break;
		}
		
		return result;
	}
}
