package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

	private static final String dbName = "taximarshal";
	private static final String dbUrl = "jdbc:mysql://localhost:3306/" + dbName;
	private static final String dbUsername = "root";
	private static final String dbPass = "wiseman";
	private static Connection connection;
	public static Connection getConnection() {
		try {
			 Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager.getConnection(dbUrl, dbUsername, dbPass);
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return connection;
	}
	
	
	public static void  closeConnection() {
		if(connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
