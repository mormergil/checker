/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package checker;

import java.sql.*;
/**
 *
 * @author Draug
 */
public class Helper {
    
    public Helper(){
    }
    
    public Connection createDBConnect(String dbHost, String dbName, String dbUser, String dbPass){
        Connection conn = null;
        
        try {
            String url = "jdbc:mysql://"+dbHost+"/"+dbName;
            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
            conn = DriverManager.getConnection (url, dbUser, dbPass);
        }
            catch (Exception e)
        {
            System.err.println ("Cannot connect to database server");
            e.printStackTrace();
        }
        
        return conn;
    }
    
    
    public void closeDBConnect(Connection conn){
        try {
            if (conn != null) {
                try {
                    conn.close ();
                }
                catch (Exception e) { }
            }
        }
        catch (Exception e) { }
    }

    public ResultSet query(Connection _conn, String _query){
        ResultSet resultSet;
        try {
            Statement stmt = _conn.createStatement();
            resultSet = stmt.executeQuery(_query);
            return resultSet;
          } catch (SQLException e) {
            System.out.println(e);
        }
        return null;
    }
    
}