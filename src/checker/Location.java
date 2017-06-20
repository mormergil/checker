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
import java.util.ArrayList;

public class Location {
    
    private int id;
    private String name="";
    private String description="";
    private StringBuilder groups = new StringBuilder("");
    private StringBuilder sensorsString = new StringBuilder("");
    private int sensorCount=0;
    private ArrayList<Integer> sensors = new ArrayList<Integer>();
    
    
    public Location(Connection _conn, int _id){
        Helper helper = new Helper();
        
        String query = "SELECT name, description FROM chk_locations WHERE id='"+_id+"'";
        ResultSet resultSet = helper.query(_conn, query);
        
        if (resultSet != null) {
            this.id = _id;
            try {
                resultSet.next();
                this.name = resultSet.getString("name");
                this.description = resultSet.getString("description");
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
        
        query = "SELECT id_group FROM chk_locations_groups WHERE id_location ='"+ _id +"'";
        resultSet = helper.query(_conn, query);
        try {
            while (resultSet.next()){
                groups.append(resultSet.getInt("id_group")).append(",");
            }
            if (groups.length()>1) {groups.deleteCharAt(groups.length()-1);}
        } catch (SQLException e) {
                System.out.println(e);
        }
        
        query = "SELECT sensor_id FROM `sensors` WHERE sensors.ID_GROUP in (select id_group from chk_locations_groups where id_location = '"+ _id +"')";
        resultSet = helper.query(_conn, query);
        try {
            while (resultSet.next()){
                sensorsString.append(resultSet.getInt("sensor_id")).append(",");
                sensors.add(resultSet.getInt("sensor_id"));
                sensorCount++;
            }
            if (sensorsString.length()>1) {sensorsString.deleteCharAt(sensorsString.length()-1);}
        } catch (SQLException e) {
                System.out.println(e);
        }
    }
    
    public String getName(){
        return name;
    }
    
    public String getDesription(){
        return description;
    }
    
    public String getGroupsString(){
        return groups.toString();
    }
    
    public String getSensorsString(){
        return sensorsString.toString();
    }
    
    public int getSensorCount(){
        return sensorCount;
    }
    
    public ArrayList getSensors(){
        return sensors;
    }
    
}
