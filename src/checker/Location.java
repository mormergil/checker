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

public class Location {
    
    private int id;
    private String name="";
    private String description="";
    private String groups="";
    private String sensors="";
    
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
                groups = groups.concat(resultSet.getString("id_group"));
                groups = groups.concat(",");
            }
            if (groups.length()>1) {groups = groups.substring(0, groups.length()-1);}
        } catch (SQLException e) {
                System.out.println(e);
        }
        
        query = "SELECT sensor_id FROM `sensors` WHERE sensors.ID_GROUP in (select id_group from chk_locations_groups where id_location = '"+ _id +"')";
        resultSet = helper.query(_conn, query);
        try {
            while (resultSet.next()){
                sensors = sensors.concat(resultSet.getString("sensor_id"));
                sensors = sensors.concat(",");
            }
            if (sensors.length()>1) {sensors = sensors.substring(0, sensors.length()-1);}
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
        return groups;
    }
    
    public String getSensorsString(){
        return sensors;
    }
    
    public int getSensorCount(){
        int result=0;
        
        result = this.sensors.split(",").length;
        
        return result;
    }
    
}
