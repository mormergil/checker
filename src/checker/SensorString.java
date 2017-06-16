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

public class SensorString extends Sensor{
    Helper helper = new Helper();
    
    public void doCompute(Connection _conn, int _sensorsInLocation){
        int lastDimention=0;
        String query="SELECT MAX(dimention) AS md FROM comp_strings WHERE sensor_id='"+this.getID()+"'";
        ResultSet resultSet = helper.query(_conn, query);
        try {
            if (resultSet.next()){
                lastDimention = resultSet.getInt("md");
                //System.out.println(lastDimention);
            }
        } catch (SQLException e){
            System.out.println (e);
        }
        
        query = "SELECT dimention, distance, angle, d_shift, system_status FROM strings WHERE dimention > '"+lastDimention+"' AND sensor_id='"+this.getID()+"'";
       // System.out.println(query);
        resultSet = helper.query(_conn, query);
        StringBuilder insertQuery = new StringBuilder("INSERT INTO comp_strings(sensor_id, dimention, comp_distance, comp_angle, comp_shift, system_status) VALUES ");
        int newCounter=0;
        try {
            while (resultSet.next()){
            //    query = "SELECT count(sensor_id) AS cnt_s FROM strings WHERE dimention="+resultSet.getString("dimention")+"";
            //    ResultSet rs = helper.query(_conn, query);
            //    rs.next();
                // Проверяем фактическое количество опрошенных датчиков.
                // Если оно меньше количества датчиков на объекте, считаем что опрос ещё не завершён
                // и обработку данных оставляем до следующего запуска
            //    if (rs.getInt("cnt_s") == _sensorsInLocation){ 
                    newCounter++;
                    insertQuery.append("(");
                    insertQuery.append("'").append(this.getID()).append("',");
                    insertQuery.append("'").append(resultSet.getString("dimention")).append("',");
                    insertQuery.append("'").append(resultSet.getString("distance")).append("',");
                    insertQuery.append("'").append(resultSet.getString("angle")).append("',");
                    insertQuery.append("'").append(resultSet.getString("d_shift")).append("',");
                    insertQuery.append("'").append(resultSet.getString("system_status")).append("'");
                    insertQuery.append("),");
            //    } else {
            //        System.out.println("No enougth data!");
            //    } 
            }
        } catch (SQLException e){
            System.out.println (e);
        }
        insertQuery = helper.trim(insertQuery, ",");
        //insertQuery += "";
        if (newCounter>0){
            helper.update(_conn, insertQuery.toString());
            //System.out.println(insertQuery);
        } 
        System.gc();
    }
}
