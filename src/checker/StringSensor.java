/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package checker;

/**
 *
 * @author Draug
 */
import java.sql.*;

public class StringSensor extends Sensor{
    Helper helper = new Helper();
    
    public void doCompute(Connection _conn){
        int lastDimention=0;
        String query="SELECT MAX(dimention) AS md FROM comp_strings WHERE sensor_id='"+this.getID()+"'";
        ResultSet resultSet = helper.query(_conn, query);
        try {
            if (resultSet.next()){
                lastDimention = resultSet.getInt("md");
                System.out.println(lastDimention);
            }
        } catch (SQLException e){
            System.out.println (e);
        }
        
        query = "SELECT dimention, distance, angle, d_shift, system_status FROM strings WHERE dimention > '"+lastDimention+"' AND sensor_id='"+this.getID()+"'";
       // System.out.println(query);
        resultSet = helper.query(_conn, query);
        String insertQuery="INSERT INTO comp_strings(sensor_id, dimention, comp_distance, comp_angle, comp_shift, system_status) VALUES ";
        int newCounter=0;
        try {
            while (resultSet.next()){
                newCounter++;
                insertQuery += "(";
                insertQuery += "'"+this.getID()+"',";
                insertQuery += "'"+resultSet.getString("dimention")+"',";
                insertQuery += "'"+resultSet.getString("distance")+"',";
                insertQuery += "'"+resultSet.getString("angle")+"',";
                insertQuery += "'"+resultSet.getString("d_shift")+"',";
                insertQuery += "'"+resultSet.getString("system_status")+"'";
                insertQuery += "),";
            }
        } catch (SQLException e){
            System.out.println (e);
        }
        if (insertQuery.endsWith(",")){
            insertQuery = insertQuery.substring(0, insertQuery.length()-1);
        }
        insertQuery += "";
        if (newCounter>0){
            helper.update(_conn, insertQuery);
        //    System.out.println(insertQuery);
        }   
    }
}
