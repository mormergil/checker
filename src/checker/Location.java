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
    }
    
    public String getName(){
        return name;
    }
    
}
