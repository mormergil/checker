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

public class Sensor {
 
    private String name="";
    private String description="";
    
    private int ID;
    private int groupID;
    private int locationID;
    
    public Sensor(Connection _conn,int _id) {
        Helper helper = new Helper();
        
        String query = "SELECT name, description, id_group FROM sensors WHERE sensor_id='"+_id+"'";
        ResultSet resultSet = helper.query(_conn, query);
        
        try {
            resultSet.next();
            this.groupID = resultSet.getInt("id_group");
            this.name = resultSet.getString("name");
            this.description = resultSet.getString("description");
            } catch (SQLException e) {
                System.out.println(e);
            }
        
        query = "SELECT id_location FROM chk_locations_groups WHERE id_group='"+this.groupID+"'";
        resultSet = helper.query(_conn, query);
        
        try {
            resultSet.next();
            this.locationID = resultSet.getInt("id_location");
            } catch (SQLException e) {
                System.out.println(e);
            }
    }
    
    public String getName(){
        return name;
    }
    
    public String getDescription(){
        return description;
    }
    
    public int getID(){
        return ID;
    }
    
    public int getGroupID(){
        return groupID;
    }
    
    
}
