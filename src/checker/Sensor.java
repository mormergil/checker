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
public class Sensor {
 
    private String name="";
    private String description="";
    private int ID;
    private int groupID;
    
    public Sensor(int _id) {
        
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
