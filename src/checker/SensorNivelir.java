/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package checker;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

/**
 *
 * @author Draug
 */
    
public class SensorNivelir extends Sensor{
    Helper helper = new Helper();
    
    private int baseSensorID;
    private int baseDimID;
    private float baseZeroVal;
    private float ownZeroVal;
    
    private int a_baseSensorID;
    private int a_baseDimID;
    private float a_baseZeroVal;
    private float a_ownZeroVal;
    
    private HashMap additionalInit(Connection _conn, int sID){
        // Инициализация сведений, нужных только инклинометрам
        HashMap data = new HashMap();       
        Sensor s = new Sensor();
        s.init(_conn, sID);
        
        // Опорный датчик в группе, опорное измерение
        String query = "SELECT base_sensor, base_dim FROM sensor_groups WHERE ID='"+s.getGroupID()+"'";
        ResultSet res = helper.query(_conn, query);
        try {
            if (res.next()){
                data.put("baseSensorID", res.getInt("base_sensor"));
                data.put("baseDimID", res.getInt("base_dim"));
            }
        } catch (SQLException e) {
            System.out.println ("1"+e);
        }
        
        //сырые показания опорного датчика в опорное измерение
        query = "SELECT x FROM incline WHERE dimention='"+data.get("baseDimID")+"' AND sensor_id='"+data.get("baseSensorID")+"'";
        res = helper.query(_conn, query);
        try {
            if (res.next()){
                data.put("baseZeroVal", res.getFloat("x"));
            }
        } catch (SQLException e) {
            System.out.println ("2"+e);
        }
        
        //сырые показания этого датчика в опорное измерение
        query = "SELECT x FROM incline WHERE dimention='"+data.get("baseDimID")+"' AND sensor_id='"+sID+"'";
        res = helper.query(_conn, query);
        try {
            if (res.next()){
                data.put("ownZeroVal", res.getFloat("x"));
            }
        } catch (SQLException e) {
            System.out.println ("3"+e);
        }
        
        return data;
    }
        
    private HashMap[] getSensorData(Connection _conn, int _sensorID, int _baseSensorID, float _baseZeroVal, float _ownZeroVal, String _neededDimentions){
    //Функция занимается тем что выбирает из заданного интервала все значения
    //заданного датчика, значения базового датчика для группы, их нулевые значения
    // и на основании всего этого формирует двумерный массив "время - значение", который
    // потои и отображается на графике
        int count = _neededDimentions.split(",").length;
        HashMap [] data = new HashMap[count]; 

        // Запросили значения опорного датчика для интервала
        String query = "SELECT x AS val, dimention " +
                         "FROM incline "+
                        "WHERE incline.dimention in ("+_neededDimentions+") "+
                               "AND sensor_id='"+_baseSensorID+"' "+
                        "ORDER BY incline.dimention ASC";
        ResultSet res = helper.query(_conn, query);
        // Формируем двумерный массив из данных
        int i=0;
// по первой - просто нумерация, а по второй - в нулевой ячейке хранятся номера измерений, в первой - значения
        try{
            while (res.next()) {
                data[i] = new HashMap();
                data[i].put("dimention", res.getInt("dimention"));
                data[i].put("value", res.getFloat("val"));
///Эот важно!        
//                $data['y'][$i] += $sensClass->getCorrect($row['dimention']);
                i++;
        }    
        }catch (SQLException e) {
            System.out.println ("4"+e);    
        }

        // запросили текущие значения датчика и его "нулевые" значения, а также номера дименшенов
        query = "SELECT x AS val, dimention " +
                  "FROM incline " +
                 "WHERE incline.dimention IN ("+_neededDimentions+") " +
                      " AND sensor_id='"+_sensorID+"' " +
                "ORDER BY incline.dimention ASC";

        res = helper.query(_conn, query);
        i=0; // Продолжаем формирование массива
        try {
            while (res.next()){
                data[i].replace("value", Float.parseFloat(data[i].get("value").toString())-res.getFloat("val") - (_baseZeroVal - _ownZeroVal) );
///
// Наверное, это тоже важно
///
//        $data['y'][$i] -= $sensClass->getCorrect($row['dimention']);
                i++;
            }
        } catch (SQLException e) {
            System.out.println("5"+e);
        }    
        return data;
    }
    
    
    public void doCompute(Connection _conn, int _sensorsInLocation){
        
        int lastDimention=0;
        StringBuilder neededDimentions = new StringBuilder("0,");
    // Если выбран блок С тогда при построении каждого графика мы должны вычитать показания 18-го датчика.
    // Иначе говоря, сначала мы рассчитываем показания каждого датчика как рассчитывали раньше, а потом
    // из уже рассчитанных показаний вычитаем точно так же расчитанные показания 18-го датчика.
        int [] workaroundSensors = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};
        int additionalSensor=18;
        
        //Сразу возьмём все нужные сведения
        HashMap tmp = this.additionalInit(_conn, this.getID());
        this.baseDimID = Integer.parseInt(tmp.get("baseDimID").toString());
        this.baseSensorID = Integer.parseInt(tmp.get("baseSensorID").toString());
        this.baseZeroVal = Float.parseFloat(tmp.get("baseZeroVal").toString());
        this.ownZeroVal = Float.parseFloat(tmp.get("ownZeroVal").toString());
        
        //И, на всякий случай, для того самого костыльного
        tmp = this.additionalInit(_conn, this.getID());
        this.a_baseDimID = Integer.parseInt(tmp.get("baseDimID").toString());
        this.a_baseSensorID = Integer.parseInt(tmp.get("baseSensorID").toString());
        this.a_baseZeroVal = Float.parseFloat(tmp.get("baseZeroVal").toString());
        this.a_ownZeroVal = Float.parseFloat(tmp.get("ownZeroVal").toString());
                
        String query="SELECT MAX(dimention) AS md FROM comp_nivelir WHERE sensor_id='"+this.getID()+"'";
        ResultSet res = helper.query(_conn, query);
        try {
            if (res.next()){
                lastDimention = res.getInt("md");
                //System.out.println("LastDimention is " +lastDimention);
            }
        } catch (SQLException e){
            System.out.println ("6"+e);
        }
        
        // Определяем какие дименшены нам нужны - интересны только те где есть достоверные данные по базовому и текущему датчикам
        // Сначала выбираем те, которые в диапазоне и у которых доверенные показания по выбранному датчику
        query="SELECT dimention " +
                "FROM incline " +
               "WHERE ((modered=1) OR (modered = '0' AND system_status_id='1')) " +
                      "AND sensor_id = '"+this.getID()+"' " +
                      "AND dimention > '"+lastDimention+"'";
        res = helper.query(_conn, query);
        try {
            while (res.next()){
                neededDimentions.append(res.getString("dimention")).append(",");
            }
        } catch (SQLException e){
            System.out.println("7"+e);
        }

        neededDimentions = helper.trim(neededDimentions, ",");
        // Потом из только что выбранных выбираем те, у которых доверенные показания по опорному датчику
        query="SELECT dimention " +
                "FROM incline " +
               "WHERE ((modered=1) OR (modered = '0' AND system_status_id='1')) " +
                 "AND sensor_id = '"+this.baseSensorID+"' " +
                 "AND dimention in ("+neededDimentions+")";
    
        neededDimentions = new StringBuilder("0,");
        res = helper.query(_conn, query);
        try {
            while (res.next()){
                neededDimentions.append(res.getString("dimention")).append(",");
            }
        } catch (SQLException e){
            System.out.println("8"+e);
        }
        neededDimentions = helper.trim(neededDimentions, ",");
    // А сейчас всё тот же первый костыль! Если датчик из прилагаемого списка -
    // тогда нужно проверить наличие показаний и для вычитаемого датчика
        
        boolean kostil=false;
        for (int k=0; k<workaroundSensors.length; k++){
            if (this.getID() == workaroundSensors[k]) {kostil=true;}
        }   
        if (kostil){
            query="SELECT dimention " +
                    "FROM incline " +
                   "WHERE ((modered=1) OR (modered = '0' " +
                     "AND system_status_id='1')) " +
                     "AND sensor_id = '"+additionalSensor+"' AND dimention in ("+neededDimentions+")";
            //System.out.println(query);
            res = helper.query(_conn, query);
            neededDimentions = new StringBuilder("0,");
            try {
                while (res.next()){ 
                    neededDimentions.append(res.getString("dimention")).append(",");
                }
            } catch (SQLException e){
                System.out.println("9"+e);
            }
            neededDimentions = helper.trim(neededDimentions, ",");
        }
        
        if (!neededDimentions.equals("0")) {
            HashMap[] data=getSensorData(_conn, this.getID(), this.baseSensorID, this.baseZeroVal, this.ownZeroVal, neededDimentions.toString());

            // И снова первый костыль! Если датчик из списка (где-то в начале объявлен массив),
            // То тогда из него всё-таки нужно вычитать показания 18 датчика, а для этого их надо получить
            if (kostil){
                HashMap[] tmp_data=getSensorData(_conn, additionalSensor, this.a_baseSensorID, this.a_baseZeroVal, this.a_ownZeroVal, neededDimentions.toString());

                for (int i=0; i<tmp_data.length-1; i++) {
                    data[i].replace("value", Float.parseFloat(data[i].get("value").toString()) - Float.parseFloat(tmp_data[i].get("value").toString()) );
                }
            }
            // вроде как костыль закончился

            StringBuilder insertQuery = new StringBuilder ("INSERT INTO comp_nivelir(sensor_id, dimention, comp_x, system_status_id, alarm_id) VALUES ");

            int newCounter=0;
            for (int i=0; i<data.length-1; i++) {
                //System.out.println("Sensor: "+this.getID()+" dimention: "+data[i].get("dimention")+" (i="+i+")" );
               // query = "SELECT count(sensor_id) AS cnt_s FROM incline WHERE dimention="+data[i].get("dimention").toString()+"";
                //System.out.println();
              //  ResultSet rs = helper.query(_conn, query);
              //  try {
              //      rs.next();
             //       if (rs.getInt("cnt_s") == _sensorsInLocation){
                        newCounter++;
                        insertQuery.append("('").append(this.getID()).append("', ");
                        insertQuery.append("'").append(data[i].get("dimention")).append("', ");
                        insertQuery.append("'").append(data[i].get("value")).append("', ");
                        insertQuery.append("'1', '1' ),");

                        if (newCounter > 1000){
                            insertQuery = helper.trim(insertQuery, ",");
                            helper.update(_conn, insertQuery.toString());
                    //System.out.println(insertQuery);
                            newCounter=0;
                            insertQuery = new StringBuilder("INSERT INTO comp_nivelir(sensor_id, dimention, comp_x, system_status_id, alarm_id) VALUES ");
                        }

              //      } else {
              //          System.out.println(" No enougth data");
              //      }    
              //  } catch (SQLException e) {
              //      System.out.println("10"+e);
              //  }       
            }

            insertQuery = helper.trim(insertQuery, ",");
            //insertQuery += "";
            if (newCounter>0){
                helper.update(_conn, insertQuery.toString());
                //System.out.println(insertQuery);
            }
            tmp.clear();
            for (int i=0; i<data.length-1; i++){
                data[i].clear();
            }
            data = null;
            System.gc();
        }    
    }
}
