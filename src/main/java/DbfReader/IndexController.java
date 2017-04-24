package DbfReader;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.util.JSONWrappedObject;
//import com.linuxense.javadbf.DBFReader;
import jdk.nashorn.internal.ir.debug.JSONWriter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import DbfReader.DBFManager;
import DbfReader.SQLiteManager;

import javax.sound.midi.SysexMessage;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

@RestController
public class IndexController {

    /*@RequestMapping(value = "/openDBF", produces="application/json")
    @ResponseBody
    public String openDbf(@RequestParam(value="path", defaultValue="/home/") String dbfPath,
                          @RequestParam(value="page", defaultValue = "0") int page,
                          @RequestParam(value="amountPerPage", defaultValue = "200") int amountPerPage){
        System.out.println(dbfPath);
        DBFManager dbf = new DBFManager();
        //dbf.loadDBF("/home/nowayrlz/Downloads/sample.dbf");
        dbf.prepareDBF(dbfPath);
        String fieldsName[] = dbf.getFieldName();
        String returnText = new String();
        Object[][] row = dbf.seekRecords(page*amountPerPage, amountPerPage);
        returnText = "[ { \"fields\": [\n";
        for(int i = 0; i < fieldsName.length; i++)
            returnText = returnText + "\"" + fieldsName[i] + "\",\n";

        returnText = returnText.substring(0,returnText.length()-2);
        returnText += "], \"rows\": [ \n";


        for(int i = 0; i< amountPerPage; i++) {

            //returnText += "<br>";
            returnText += "[";
            for(int j = 0; j < fieldsName.length; j++) {
                if (row[i][j] != null)
                    returnText += "\"" + row[i][j].toString() + "\",\n";
                else returnText += "\"" + "null\",\n";

            }

                returnText = returnText.substring(0, returnText.length() - 2);
                returnText += "],\n";
        }
        returnText = returnText.substring(0,returnText.length()-2);
        returnText += "]}]";

        return returnText;

    }*/
    /*@RequestMapping(value = "/openDBF", produces="application/json")
    @ResponseBody
    //public String openDbf(@PathVariable String dbfPath) {
    public String openDbf(@RequestParam(value="path", defaultValue="/home/") String dbfPath){
        System.out.println(dbfPath);
        DBFManager dbf = new DBFManager();
        //dbf.loadDBF("/home/nowayrlz/Downloads/sample.dbf");
        dbf.loadDBF(dbfPath);
        String fieldsName[] = dbf.getFieldName();
        String returnText = new String();
        Object[][] row = dbf.getRows();
        returnText = "[ { \"fields\": [\n";
        for(int i = 0; i < fieldsName.length; i++)
            returnText = returnText + "\"" + fieldsName[i] + "\",\n";

        returnText = returnText.substring(0,returnText.length()-2);
        returnText += "], \"rows\": [ \n";


        for(int i = 0; i< dbf.getNumberOfRecords(); i++) {
            //returnText += "<br>";
            returnText += "[";
            for(int j = 0; j < fieldsName.length; j++)
                if(row[i][j] != null)
                    returnText += "\"" + row[i][j].toString() + "\",\n";
                else returnText += "\"" + "null\",\n";
            returnText = returnText.substring(0,returnText.length()-2);
            returnText += "],\n";


        }
        returnText = returnText.substring(0,returnText.length()-2);
        returnText += "]}]";
        return returnText;

    }*/


    @RequestMapping(value = "/dbfToSqlite")

    public String saveDbfAsSqlite(@RequestParam(value="dbfpath") String dbfpath, @RequestParam(value="sqlitepath") String sqlitepath)
    {
        int count = 0;


        SQLiteManager connection = new SQLiteManager(sqlitepath);
        connection.connect();
        if(connection != null)
        {
            DBFManager dbf = new DBFManager(dbfpath);
            dbf.prepareDBF(dbfpath);
            String fieldName[] = dbf.getFieldName();
            String drop_sql = "DROP TABLE IF EXISTS dbf_import;";
            connection.execute(drop_sql);
            String sql = "CREATE TABLE IF NOT EXISTS dbf_import (\n";
            //+ "	id integer PRIMARY KEY,\n";

            for(int i = 0; i < fieldName.length; i++)
            {
                sql += " " + fieldName[i] + " text,\n";
            }
            sql = sql.substring(0,sql.length()-2);
            sql += ");";
            //if (1 != 0) return sql;
            connection.execute(sql);

            sql = "INSERT INTO dbf_import VALUES ";
            String base_sql = sql;
            String[] rec;
            int commit_count = 1;

            while((rec = dbf.readNext()) != null)  {
                count++;


                //Thread t1 = new Thread(new RunnableTest(dbfpath, connection, 0, dbf.getNumberOfRecords()/4));

                //t1.start();
                sql+="(";
                for(int k = 0; k < fieldName.length; k++)
                {
                    sql += "\'" + rec[k] + "\',";
                }
                sql = sql.substring(0, sql.length() - 1);
                if(count%1000==0) System.out.println(count);
                if(commit_count<100)
                {
                    sql += "),";
                    commit_count++;
                }
                else
                {
                    sql+=");";
                    new Thread(new SQLiteManager(sqlitepath, sql)).start();

                    sql = base_sql;
                    commit_count = 0;

                }


                /*try {
                    //t1.join();
                    //t2.join();
                    //t3.join();
                    //t4.join();
                    System.out.println("Cabou!");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/



                /*for(int k = 0; k < 1000; k++) {
                    sql += "(";
                    Object[] row;
                    if ((row = reader.nextRecord()) == null) break;
                    System.out.println(k);
                    for (int j = 0; j < fieldName.length; j++) {
//                    String temp = row[i][j].toString();
//                    temp.replace("\'", "\'\'");
                        if (row[j] != null)
                            sql += "\'" + row[j].toString().replace("\'", "\'\'") + "\',";
                        else sql += "\'" + "null" + "\',";
                    }

                    sql = sql.substring(0, sql.length() - 1);
                    sql += "),";

                }
                sql = sql.substring(0,sql.length()-1);
                sql += ";";
                connection.execute(sql);
                System.out.println(i);
                sql = base_sql;
                */
            }
            if(commit_count>0)
            sql = sql.substring(0,sql.length()-1);
            sql += ";";
            connection.execute(sql);
            //if(1==1) return sql;
            connection.disconnect();




        }

        return "Sucesso! " + count + " linhas foram convertidas!";
    }
    /* OLD FUNC
    @RequestMapping(value = "/dbfToSqlite")

    public String saveDbfAsSqlite(@RequestParam(value="dbfpath") String dbfpath, @RequestParam(value="sqlitepath") String sqlitepath)
    {


        SQLiteManager connection = new SQLiteManager(sqlitepath);
        connection.connect();
        if(connection != null)
        {
            DBFManager dbf = new DBFManager();
            dbf.loadDBF(dbfpath);
            String fieldName[] = dbf.getFieldName();
            String drop_sql = "DROP TABLE IF EXISTS dbf_import;";
            connection.execute(drop_sql);
            String sql = "CREATE TABLE IF NOT EXISTS dbf_import (\n";
                    //+ "	id integer PRIMARY KEY,\n";

            for(int i = 0; i < fieldName.length; i++)
            {
                sql += " " + fieldName[i] + " text,\n";
            }
            sql = sql.substring(0,sql.length()-2);
            sql += ");";
            //if (1 != 0) return sql;
            connection.execute(sql);

            sql = "INSERT INTO dbf_import VALUES ";
            Object[][] row = dbf.getRows();
            for(int i = 0; i < dbf.getNumberOfRecords(); i++) {
                sql += "(";
                for (int j = 0; j < fieldName.length; j++) {
//                    String temp = row[i][j].toString();
//                    temp.replace("\'", "\'\'");
                    if(row[i][j] != null)
                        sql += "\'" + row[i][j].toString().replace("\'", "\'\'") + "\',";
                    else sql += "\'" + "null" + "\',";
                }
                sql = sql.substring(0,sql.length()-1);
                sql += "),";

            }
            sql = sql.substring(0,sql.length()-1);
            sql += ";";
            //if(1==1) return sql;
            connection.execute(sql);



        }
        return "Sucesso!";
    }
    */

    @RequestMapping(value = "/showSQLiteDB")
    @ResponseBody
    public String selectAll(@RequestParam(value = "sqlitepath") String path)
    {
        SQLiteManager connection = new SQLiteManager(path);
        connection.connect();
        String sql = "SELECT * FROM dbf_import;";
        ResultSet rs = connection.query(sql);

        //String result = new String();
        String result = "<table style=\"width:100%\">\n<tr>";

        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int colCount = rsmd.getColumnCount();
            //int colCount = 11;
            for(int i = 1; i <= colCount; i++)
                result += "<th>" + rsmd.getColumnName(i) + "</th>\n";
            result += "</tr>\n";
            while(rs.next())
            {
                result+="<tr>\n";
                for(int i = 1; i <= colCount; i++)
                    result += "<td>" + rs.getString(i) + "</td>\n";
                result += "</tr>\n";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "/openSQLiteDB", produces="application/json")
    @ResponseBody
    public String openSQLite(@RequestParam(value = "path") String path)
    {
        SQLiteManager connection = new SQLiteManager(path);
        connection.connect();
        String sql = "SELECT * FROM dbf_import;";
        ResultSet rs = connection.query(sql);

        String result = "[ { \"fields\": [\n";

        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int colCount = rsmd.getColumnCount();

            for(int i = 1; i <= colCount; i++)
                result += "\"" + rsmd.getColumnName(i) + "\",\n";
            result = result.substring(0,result.length()-2);
            result += "],\n\"rows\": [\n";
            while(rs.next())
            {
                result += "[";
                for(int i = 1; i <= colCount; i++)
                    result += "\"" + rs.getString(i) + "\",\n";
                result = result.substring(0,result.length()-2);
                result += "\n],\n";
            }
            result = result.substring(0,result.length()-2);
            result += "]}]";
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "/test")
    @ResponseBody
    public String test() {


        String result = "[{\"id\": \"1\", \"name\": \"Thiago\"}]";
        //StringBuilder result = new StringBuilder("[");


        return result;
    }
}