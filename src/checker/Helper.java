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
                catch (Exception e) {System.out.println ("Error while closing connection "+e); }
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
    
    public int update(Connection _conn, String _query){
        int res;
        try {
            Statement stmt = _conn.createStatement();
            res = stmt.executeUpdate(_query);
            return res;
          } catch (SQLException e) {
            System.out.println(e);
        }
        return -1;
    }
    
    public String trim(String _str, String t){
        String res="";
        if (_str.endsWith(t)){
            res = _str.substring(0, _str.length()-1);
        }
        return res;
    }
    
    public StringBuilder trim(StringBuilder _str, String t){
        StringBuilder res = new StringBuilder("");
        if (_str.lastIndexOf(t) == _str.length()-1){
                _str.deleteCharAt(_str.length()-1);
                res = _str;
            }
        return res;
    }
}
