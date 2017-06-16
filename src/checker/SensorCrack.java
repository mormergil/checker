/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package checker;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author Draug
 */
    
public class SensorCrack extends Sensor{
    Helper helper = new Helper();
    
    private HashMap getCorrect(HashMap[] _history, int _dim){
    	HashMap result = new HashMap();
        result.put("expantion", 0);
        result.put("real_expantion", 0);
        result.put("shift", 0);
        result.put("angle", 0);
        
	if (_history.length > 0){
	    result.replace("expantion", _history[0].get("expantion"));
            result.replace("real_expantion", _history[0].get("real_expantion"));
            result.replace("shift", _history[0].get("shift"));
            result.replace("angle", _history[0].get("angle"));
	    int i=0;
	    while ( (i < _history.length ) && (_dim <= Integer.parseInt(_history[i].get("dimention").toString())) ) {
		result.replace("expantion",_history[i].get("expantion") );
                result.replace("real_expantion",_history[i].get("real_expantion") );
                result.replace("shift",_history[i].get("shift") );
                result.replace("angle",_history[i].get("angle") );
  	        i++;
	    }
	}
        return result;
    }
    
    private HashMap[] getHistory(Connection _conn){
        //HashMap[] result = new HashMap[_count];
        HashMap[] history;

        // Запросили кол-во нулевых значений для датчика
        String query = "SELECT COUNT(dimention) AS cnt "
                       + "FROM cracks_history "
                      + "WHERE sensor_id='"+this.getID()+"'";
        ResultSet res = helper.query(_conn, query);
        int historyCount = 1;
        try {
            if (res.next()){
                historyCount = res.getInt("cnt");
            }
        } catch (SQLException e) {System.out.println(e);}

        history = new HashMap[historyCount];
        history[0] = new HashMap();
        history[0].put("dimention", 1);
        history[0].put("expantion", 0);
        history[0].put("real_expantion", 0);
        history[0].put("shift", 0);
        history[0].put("angle", 0);   

        // Запросили нулевые значения для датчика
        query = "SELECT dimention, expantion, real_expantion, shift, angle "
                + "FROM cracks_history "
               + "WHERE sensor_id='"+this.getID()+"' ORDER BY dimention ASC";
        res = helper.query(_conn, query);
        int i=0;
        try {
            while (res.next()){
                if (i>0) {history[i]=new HashMap();}
                history[i].replace("dimention", res.getInt("dimention") );
                history[i].replace("expantion", res.getFloat("expantion") );
                history[i].replace("real_expantion", res.getFloat("real_expantion") );
                history[i].replace("shift", res.getFloat("shift") );
                history[i].replace("angle", res.getFloat("angle") );
                i++;
            }
        } catch (SQLException e){System.out.println(e);}
        return history;
    }
    
    public void doCompute(Connection _conn, int _sensorsInLocation){
        
        HashMap[] history = this.getHistory(_conn);
                
        int lastDimention=0;
        String query="SELECT MAX(dimention) AS md FROM comp_cracks WHERE sensor_id='"+this.getID()+"'";
        //System.out.println (query);
        ResultSet resultSet = helper.query(_conn, query);
        try {
            if (resultSet.next()){
                lastDimention = resultSet.getInt("md");
                //System.out.println("Last dimention is: "+lastDimention);
            }
        } catch (SQLException e){
            System.out.println (e);
        }
        
        StringBuilder insertQuery = new StringBuilder("INSERT INTO comp_cracks(sensor_id, dimention, comp_expantion, comp_angle, comp_shift, system_status_id) VALUES ");
        
        query = "SELECT * FROM cracks WHERE dimention > '"+lastDimention+"' AND sensor_id='"+this.getID()+"'";
        int newCounter=0;
        //System.out.println(query);
        resultSet = helper.query(_conn, query);
        try {
            while (resultSet.next()){
                // Не проверяем фактическое количество опрошенных датчиков.
                    newCounter++;

                    HashMap zeroData = getCorrect(history, resultSet.getInt("dimention") );
                    insertQuery.append("(");
                    insertQuery.append("'").append(this.getID()).append("',");
                    insertQuery.append("'").append(resultSet.getString("dimention")).append("',");
                    insertQuery.append("'").append(resultSet.getFloat("expantion") + Float.parseFloat(zeroData.get("real_expantion").toString()) - Float.parseFloat(zeroData.get("expantion").toString())).append("',");
                    insertQuery.append("'").append(resultSet.getString("angle")).append("',");
                    insertQuery.append("'").append(resultSet.getString("shift")).append("',");
                    insertQuery.append("'").append(resultSet.getString("system_status_id")).append("'");
                    insertQuery.append("),"); 
//                    System.out.println(newCounter+"  ");
                    if (newCounter > 1000){
                        insertQuery = helper.trim(insertQuery, ",");
                        helper.update(_conn, insertQuery.toString());
                    //System.out.println(insertQuery);
                        newCounter=0;
                        insertQuery = new StringBuilder("INSERT INTO comp_cracks(sensor_id, dimention, comp_expantion, comp_angle, comp_shift, system_status_id) VALUES ");
                    }
            }
        } catch (SQLException e){
            System.out.println (e);
        }
        
        if (insertQuery.lastIndexOf(",") == insertQuery.length()-1){
            insertQuery.deleteCharAt(insertQuery.length()-1);
        }
        //insertQuery += "";
        if (newCounter>0){
            helper.update(_conn, insertQuery.toString());
            //System.out.println(insertQuery);
        }
        System.gc();
    }
}
