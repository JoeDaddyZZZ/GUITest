package com.clarity.QA.GUITester;
import java.sql.*;

/**
 * Generic database connection object
 * @author jgorski
 *
 */
public class DBSQL {
	public String dbHost = "qatest.clarityssi.local";
	public String dbName = "jobwalker";
	public String dbUser = "qauser";
	public String dbPassword = "ClAr1ty!";
	private Connection con = null;
			
	
	/**
	 * create a direct database connection
	 * @param dbHost
	 * @param dbName
	 * @param dbUser
	 * @param dbPassword
	 */
public DBSQL(String dbHost, String dbName, String dbUser, String dbPassword) {
		super();
		this.dbHost = dbHost;
		this.dbName = dbName;
		this.dbUser = dbUser;
		this.dbPassword = dbPassword;
		con = this.getDBCon();
	}


/**
 * create a direct database connection using default user/password
 * @param dbHost
 * @param dbName
 */
public DBSQL(String dbHost, String dbName) {
	super();
	this.dbHost = dbHost;
	this.dbName = dbName;
	con = this.getDBCon();
}
/**
 * create a direct database connection using default user/password/host/database
 * 
 */
public DBSQL() {
	con = this.getDBCon();
}


/**
 * main test driver
 * returns process_master database from jobwalker
 * @param args
 */
public static void main(String args[]){
		
	DBSQL qatestDB = new DBSQL(
//			"mad-qahome1.clarityssi.local",
			"test.clarityssi.net",
			"jobwalker"
	);
	String query = "select * from process_master where start > CURRENT_DATE order by job_id desc";
	//query = "select * from job";
	ResultSet rs;
	Integer jobid;
	String clientDBName=null;
	try {
		rs = qatestDB.getDBRow(query);
		while (rs.next()) {
			jobid = rs.getInt("job_id");
			clientDBName = rs.getString("db_name");
			System.out.println(jobid+ " " + clientDBName);
		} //end while
		qatestDB.closeCon();
	} catch (SQLException  e) {
		e.printStackTrace();
	}
}

public String getStringValue(String sql) {
	String value = null;
	ResultSet rs;
	try {
		rs = getDBRow(sql);
		while (rs.next()) {
			value = rs.getString(1);
		} //end while
		closeCon();
	} catch (SQLException  e) {
		e.printStackTrace();
	}
	
	return value;
}
	

	/**
	 * close connection
	 */
	public  void closeCon() {
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * generate query results for given sql query
	 * @param query
	 * @return
	 */
	public  ResultSet getDBRow(String query) {
		ResultSet rs = null;
		try {
			Statement stmt = con.createStatement();
			rs = stmt.executeQuery(query);
		} catch (SQLException  e) {
			e.printStackTrace();
		}
		return rs;
	}
	/**
	 * update data 
	 * @param statement
	 * @return
	 */
	public int  putDBInsert(String statement) {
		int result =0;
		try {
			Statement stmt = con.createStatement();
			result = stmt.executeUpdate(statement);
		} catch (SQLException  e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * create db connection for the class
	 * @return
	 */
	public  Connection getDBCon() {
		String dbUrl = "jdbc:mysql://"+dbHost+"/"+ dbName + "?zeroDateTimeBehavior=convertToNull" ;
		String dbClass = "com.mysql.jdbc.Driver";
		//System.out.println(dbUrl);
		Connection con = null;
		try {
			Class.forName(dbClass);
			con = DriverManager.getConnection(dbUrl,dbUser,dbPassword);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return con;
	}

}
