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
    
    private float[][] initCorrect(Connection _conn, int _sID){
        float [][] corrects = new float[0][0];
        
        StringBuilder query = new StringBuilder("SELECT count(sensor_id) AS cnt FROM corrects WHERE sensor_id='").append(_sID).append("'");
        ResultSet res = helper.query(_conn, query.toString());
        try {
            if (res.next()){
                corrects = new float[res.getInt("cnt")][2];
                query.delete(0, query.length());
                query.append("SELECT * FROM corrects WHERE sensor_id='").append(_sID).append("' ORDER BY dimention ASC");
                res = helper.query(_conn, query.toString());
                int i=0;
                while (res.next()){
                    corrects[i][0]=res.getInt("dimention");
                    corrects[i][1]=res.getFloat("volume");
                }
                res.close();
            } 
        } catch (SQLException e){
            System.out.println(e);
        }
        
        return corrects;        
    }
    
    private float getCorrect(float[][] corrects, int _dID){
        float correct=0;
        
        //Правки и корректировки
        for (int i=0; i<corrects.length; i++){
                if (_dID >= corrects[i][0]){
                    correct=corrects[i][1];
                }
            }
        return correct;
    }
    
    private HashMap additionalInit(Connection _conn, int sID){
        // Инициализация сведений, нужных только инклинометрам
        HashMap data = new HashMap<String, Float>();       
        Sensor s = new Sensor();
        s.init(_conn, sID);
        
        // Опорный датчик в группе, опорное измерение
        StringBuilder query = new StringBuilder("SELECT base_sensor, base_dim FROM sensor_groups WHERE ID='").append(s.getGroupID()).append("'");
        ResultSet res = helper.query(_conn, query.toString());
        try {
            if (res.next()){
                data.put("baseSensorID", res.getInt("base_sensor"));
                data.put("baseDimID", res.getInt("base_dim"));
            }
            res.close();
        } catch (SQLException e) {
            System.out.println ("1"+e);
        }
        
        //сырые показания опорного датчика в опорное измерение
        query.delete(0, query.length());
        query.append("SELECT x FROM incline WHERE dimention='").append(data.get("baseDimID")).append("' AND sensor_id='").append(data.get("baseSensorID")).append("'");
        res = helper.query(_conn, query.toString());
        try {
            if (res.next()){
                data.put("baseZeroVal", res.getFloat("x"));
            }
            res.close();
        } catch (SQLException e) {
            System.out.println ("2"+e);
        }
        
        //сырые показания этого датчика в опорное измерение
        query.delete(0, query.length());
        StringBuilder append = query.append("SELECT x FROM incline WHERE dimention='"+data.get("baseDimID")+"' AND sensor_id='"+sID+"'");
        res = helper.query(_conn, query.toString());
        try {
            if (res.next()){
                data.put("ownZeroVal", res.getFloat("x"));
            }
            res.close();
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
        StringBuilder query = new StringBuilder();
        // Запросили значения опорного датчика для интервала
        query.append("SELECT x AS val, dimention ");
        query.append("FROM incline ");
        query.append("WHERE incline.dimention in (").append(_neededDimentions).append(") AND sensor_id='").append(_baseSensorID).append("' ");
        query.append("ORDER BY incline.dimention ASC");
        ResultSet res = helper.query(_conn, query.toString());
        // Формируем двумерный массив из данных
        int i=0;
// по первой - просто нумерация, а по второй - в нулевой ячейке хранятся номера измерений, в первой - значения
        float [][] c = this.initCorrect(_conn, _baseSensorID);
        try{
            
            while (res.next()) {
                data[i] = new HashMap<String, Float>();
                data[i].put("dimention", res.getInt("dimention"));
                data[i].put("value", res.getFloat("val")+this.getCorrect(c, res.getInt("dimention")));
                i++;
            }
            res.close();
        }catch (SQLException e) {
            System.out.println ("4"+e);    
        }

        // запросили текущие значения датчика и его "нулевые" значения, а также номера дименшенов
        query.delete(0,query.length());
        query.append("SELECT x AS val, dimention FROM incline ");
        query.append("WHERE incline.dimention IN (").append(_neededDimentions).append(")  AND sensor_id='").append(_sensorID).append("' ");
        query.append("ORDER BY incline.dimention ASC");

        res = helper.query(_conn, query.toString());
        i=0; // Продолжаем формирование массива
        c = this.initCorrect(_conn, _sensorID);
        try {
            while (res.next()){
                data[i].replace("value", Float.parseFloat(data[i].get("value").toString())-res.getFloat("val") - (_baseZeroVal - _ownZeroVal) - this.getCorrect(c, res.getInt("dimention")) );
                i++;
            }
            res.close();
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
        tmp = this.additionalInit(_conn, additionalSensor);
        this.a_baseDimID = Integer.parseInt(tmp.get("baseDimID").toString());
        this.a_baseSensorID = Integer.parseInt(tmp.get("baseSensorID").toString());
        this.a_baseZeroVal = Float.parseFloat(tmp.get("baseZeroVal").toString());
        this.a_ownZeroVal = Float.parseFloat(tmp.get("ownZeroVal").toString());
                
        StringBuilder query = new StringBuilder("SELECT MAX(dimention) AS md FROM comp_nivelir WHERE sensor_id='"+this.getID()+"'");
        ResultSet res = helper.query(_conn, query.toString());
        try {
            if (res.next()){
                lastDimention = res.getInt("md");
                //System.out.println("LastDimention is " +lastDimention);
            }
            res.close();
        } catch (SQLException e){
            System.out.println ("6"+e);
        }
        
        // Определяем какие дименшены нам нужны - интересны только те где есть достоверные данные по базовому и текущему датчикам
        // Сначала выбираем те, которые в диапазоне и у которых доверенные показания по выбранному датчику
        query.delete(0, query.length());
        query.append("SELECT dimention FROM incline WHERE ((modered=1) OR (modered = '0' AND system_status_id='1')) AND sensor_id = '").append(this.getID()).append("' AND dimention > '").append(lastDimention).append("'");
        res = helper.query(_conn, query.toString());
        try {
            while (res.next()){
                neededDimentions.append(res.getInt("dimention")).append(",");
            }
            res.close();
        } catch (SQLException e){
            System.out.println("7"+e);
        }

        neededDimentions = helper.trim(neededDimentions, ",");
        // Потом из только что выбранных выбираем те, у которых доверенные показания по опорному датчику
        query.delete(0, query.length());
        query.append("SELECT dimention FROM incline WHERE ((modered=1) OR (modered = '0' AND system_status_id='1')) AND sensor_id = '").append(this.baseSensorID).append("' AND dimention in (").append(neededDimentions).append(")");
        
        neededDimentions.delete(0,neededDimentions.length()-1);
        neededDimentions.append("0,");
        res = helper.query(_conn, query.toString());
        try {
            while (res.next()){
                neededDimentions.append(res.getInt("dimention")).append(",");
            }
            res.close();
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
            query.delete(0, query.length()-1);
            query = new StringBuilder("SELECT dimention FROM incline WHERE ((modered=1) OR (modered = '0' AND system_status_id='1')) AND sensor_id = '").append(additionalSensor).append("' AND dimention in (").append(neededDimentions).append(")");
            //System.out.println(query);
            res = helper.query(_conn, query.toString());
            neededDimentions.delete(0,neededDimentions.length());
            neededDimentions.append("0,");
            try {
                while (res.next()){ 
                    neededDimentions.append(res.getInt("dimention")).append(",");
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
                    insertQuery.delete(0, insertQuery.length());
                            
                    insertQuery.append("INSERT INTO comp_nivelir(sensor_id, dimention, comp_x, system_status_id, alarm_id) VALUES ");
                }
            }

            insertQuery = helper.trim(insertQuery, ",");
            if (newCounter>0){
                helper.update(_conn, insertQuery.toString());
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
