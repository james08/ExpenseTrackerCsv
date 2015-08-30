package com.jkukard.expensetrackercsv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcTranHist {

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "org.postgresql.Driver";  
	static final String DB_URL = "jdbc:postgresql://127.0.0.1:5432/postgres";

	//  Database credentials
	static final String USER = "postgres";
	static final String PASS = "postgres";

	public void testConnection() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try{
			Class.forName(JDBC_DRIVER);

			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL,USER,PASS);

			System.out.println("Creating statement...");
			stmt = conn.createStatement();
			String sql;
			sql = "SELECT * from tranhist;";
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				System.out.print(rs.getDate(1));
				System.out.print(rs.getDouble(2));
				System.out.print(rs.getDouble(3));
				System.out.print(rs.getString(4));
				System.out.println();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
