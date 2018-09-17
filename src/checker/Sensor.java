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
import java.util.HashMap;

public class Sensor {
 
    private String name="";
    private String description="";
    
    private int ID;
    private int groupID;
    private int locationID;
    private int typeID;
    
    private HashMap[] alertValues; // Набор аварийных значений для датчика. Значений может быть несколько. С такого-то измерения одно значение, через полгода - другое...
    private HashMap[] warningValues; // Набор опасных значений для датчика. Значений может быть несколько. С такого-то измерения одно значение, через полгода - другое...
    
    public void init(Connection _conn,int _id){
        Helper helper = new Helper();
        
        this.ID = _id;
        String query = "SELECT description, id_group, sensor_code FROM sensors WHERE sensor_id='"+_id+"'";
        ResultSet resultSet = helper.query(_conn, query);
        
        try {
            resultSet.next();
            this.groupID = resultSet.getInt("id_group");
            this.typeID = resultSet.getInt("sensor_code");
            //this.name = resultSet.getString("name");
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
        
        this.alertValues = helper.getListFromDB(_conn, "sensor_alerts", new String[]{"value", "dimention"}, "sensor_id='"+this.ID+"'", "dimention ASC");
        this.warningValues = helper.getListFromDB(_conn, "sensor_warnings", new String[]{"value", "dimention"}, "sensor_id='"+this.ID+"'", "dimention ASC");
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
    
    public int getTypeID(){
        return this.typeID;
    }
    
    public int getLocationID(){
        return this.locationID;
    }
    
    public void setName(String _name){
        this.name = _name;
    }
    
    public void setDescription(String _description){
        this.description = _description;
    }
    
    public void setID(int _id){
        this.ID = _id;
    }
    
    public void setGroupID(int _gid){
        this.groupID = _gid;
    }
    
    public void setTypeID(int _tid){
        this.typeID = _tid;
    }
    
    public void setLocationID(int _lid){
        this.locationID = _lid;
    }
}
