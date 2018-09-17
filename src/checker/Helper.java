/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package checker;

import java.sql.*;
import java.util.HashMap;
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
            stmt.close();
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
    
    public HashMap[] getListFromDB(Connection _conn, String _from, String[] _fieldList, String _where, String _order){
        
        HashMap[] result;
        StringBuilder query;
        int recordsCount=0;
        
        // Запросили кол-во нулевых значений для датчика
        query = new StringBuilder("SELECT COUNT("+_fieldList[0]+") AS cnt ");
        query.append(" FROM ").append(_from);
        query.append(" WHERE ").append(_where);
        ResultSet res = this.query(_conn, query.toString());
        
        try {
            if (res.next()){
                recordsCount = res.getInt("cnt");
            }
            res.close();
        } catch (SQLException e) {System.out.println(e);}
        
        if (recordsCount==0) {
            result = new HashMap[1]; // На случай если нет ни одной записи
        } else {
            result = new HashMap[recordsCount];
        }
           
        result[0] = new HashMap();
        for (int i=0; i<_fieldList.length; i++){    // Забиваем первую запись нулевыми значениями. Потом, если что-то будет - перепишем.
            result[0].put(_fieldList[i],0);
        }  

        query.delete(0, query.length());
        query.append("SELECT ");
        for (int i=0; i<_fieldList.length; i++){    // Список полей
            query.append(_fieldList[i]).append(",");
        }
        query.deleteCharAt(query.length()-1); // Удаляем лишнюю запятую
        query.append(" FROM ").append(_from);
        query.append(" WHERE ").append(_where);
        query.append(" ORDER BY ").append(_order);
        
        res = this.query(_conn, query.toString());
        int i=0;
        try {
            while (res.next()){
                if (i>0) {result[i]=new HashMap();}
                for (int j=0; j<_fieldList.length; j++){    // Список полей
                    result[i].replace(_fieldList[j], res.getFloat(_fieldList[j]) );
                }
                i++;
            }
            res.close();
        } catch (SQLException e){System.out.println(e);}
            return result;
        }
}
