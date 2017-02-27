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
    private String locations="";
    
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
                locations = locations.concat(resultSet.getString("id_group"));
                locations = locations.concat(",");
            }
            if (locations.length()>1) {locations = locations.substring(0, locations.length()-1);}
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
    
    public String getLocationsString(){
        return locations;
    }
}
