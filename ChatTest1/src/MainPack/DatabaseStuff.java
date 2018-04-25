package MainPack;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// This class is used to compare and add data with a SQLite database.
// Code doing database interactions is probably not very pretty.
// But it's made so that it works for my use.


public final class DatabaseStuff {
	
	static String dbName = "authentication.db";
	static String dbTable = "people";
	
	public static boolean authenticate(String username, String password) throws SQLException {
		String sql = "SELECT id, username, password FROM "+dbTable;
        Connection conn = null;
        try {
        	conn = connectAndReturn();
            Statement stmt  = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                if(rs.getString("username").equals(username)) {
                	if(rs.getString("password").equals(password)) {
                		return true;
                	}
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }finally {
        	try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
		return false;
	}
	
	public static boolean makeAccount(String username, String password) throws SQLException {
		if(accountExists(username)) {
			System.out.println("Account " + username +" already exists");
			return false;
		}else {
			insert(username, password);
			System.out.println("Account " + username +" created");
			return true;
		}
	}
	
	private static boolean accountExists(String username) throws SQLException {
		String sql = "SELECT id, username, password FROM people";
        Connection conn = null;
        try {
        	conn = connectAndReturn();
            Statement stmt  = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                if(rs.getString("username").equals(username)) {
                	return true;
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }finally {
        	try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
		return false;
	}
	
	
	//Database helper methods:


	private static  Connection connectAndReturn() {
        // SQLite connection string
        String url = "jdbc:sqlite:C://temp/"+dbName;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
	public static void createNewDatabase(String fileName) {
		dbName = fileName;
        String url = "jdbc:sqlite:C:/temp/" + fileName;
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created or already exists.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
	public static void createNewTable(String tablename) {
		dbTable = tablename;
        // SQLite connection string
        String url = "jdbc:sqlite:C://temp/" + dbName;
        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS "+tablename+" (\n"
                + "	id integer PRIMARY KEY,\n"
                + "	username text NOT NULL,\n"
                + "	password text NOT NULL\n"
                + ");";
        try (Connection conn = DriverManager.getConnection(url);
                Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
	
	private static void insert(String username, String password) {
        String sql = "INSERT INTO "+dbTable+"(username,password) VALUES(?,?)";
        Connection conn = null;
        try{
        	conn = DatabaseStuff.connectAndReturn();
            PreparedStatement pstmt = conn.prepareStatement(sql); 
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }finally {
        	try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
	
	public static ResultSet printAll(){
        String sql = "SELECT id, username, password FROM "+dbTable;
        Connection conn = null;
        try {
        	conn = connectAndReturn();
            Statement stmt  = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                System.out.println(rs.getInt("id") +  "\t" + 
                                   rs.getString("username") + "\t" +
                                   rs.getString("password"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }finally {
        	try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
        return null;
    }
    
	
}
