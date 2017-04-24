package DbfReader;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Thiago Retes on 24/04/17.
 * Email: thiagoretes@uft.edu.br
 */

@RestController
public class DBFController {


    @RequestMapping(value = "/getDBFRecordAmount")
    @ResponseBody
    public String getDBFRecordAmount(@RequestParam(value = "path") String dbfPath)
    {
        DBFManager dbf = new DBFManager(dbfPath);
        dbf.prepareDBF();

        return "" + dbf.getNumberOfRecords();
    }
    @RequestMapping(value = "/openDBF", produces="application/json")
    @ResponseBody
    public String openDbf(@RequestParam(value="path", defaultValue="/teste/") String dbfPath,
                          @RequestParam(value="page", defaultValue = "0") int page,
                          @RequestParam(value="amountPerPage", defaultValue = "200") int amountPerPage){
        System.out.println(dbfPath);
        DBFManager dbf = new DBFManager(dbfPath);
        dbf.prepareDBF();//Carregar o DBF
        String fieldsName[] = dbf.getFieldName();//Armazenar nome dos campos
        //String teste [] = dbf.readNext();
        String[][] row = dbf.seekRecords(page*amountPerPage, amountPerPage);
        String returnText = "[ { \"fields\": [\n";
        for(int i = 0; i < fieldsName.length; i++)
            returnText += "\"" + fieldsName[i] + "\",\n";

        returnText = returnText.substring(0,returnText.length()-2);
        returnText += "], \"rows\": [ \n";

        amountPerPage = row.length;
        for(int i = 0; i< row.length; i++) {


            returnText += "[";
            for(int j = 0; j < fieldsName.length; j++) {
                if (row[i][j] != null)
                    returnText += "\"" + row[i][j] + "\",\n";
                else returnText += "\"" + "null\",\n";

            }

            returnText = returnText.substring(0, returnText.length() - 2);
            returnText += "],\n";
        }
        returnText = returnText.substring(0,returnText.length()-2);
        returnText += "]}]";

        return returnText;

    }

    @RequestMapping(value = "/dbfToSqlite")
    public String saveDbfAsSqlite(@RequestParam(value="dbfpath") String dbfpath, @RequestParam(value="sqlitepath") String sqlitepath)
    {
        int count = 0;
        long startTime = System.currentTimeMillis();
        long endTime = 0;
        try {


            SQLiteManager connection = new SQLiteManager(sqlitepath);
            connection.connect();
            if (connection != null) {

                DBFManager dbf = new DBFManager(dbfpath);
                dbf.prepareDBF();
                String fieldName[] = dbf.getFieldName();
                String drop_sql = "DROP TABLE IF EXISTS dbf_import;";
                connection.execute(drop_sql);
                String sql = "CREATE TABLE IF NOT EXISTS dbf_import (\n";


                for (int i = 0; i < fieldName.length; i++) {
                    sql += " " + fieldName[i] + " text,\n";
                }
                sql = sql.substring(0, sql.length() - 2);
                sql += ");";

                connection.execute(sql);


                String[] rec;
                int commit_count = 0;
                sql = "INSERT INTO dbf_import VALUES (";
                for (int k = 0; k < fieldName.length; k++) {
                    sql += "?,";

                }
                sql = sql.substring(0, sql.length() - 1);
                sql += ")";


                PreparedStatement statement = connection.createPreStatement(sql);


                while ((rec = dbf.readNext()) != null) {
                    ++count;
                    ++commit_count;



                    for (int k = 0; k < fieldName.length; k++) {



                        statement.setString(k + 1, (rec[k] == null ? "<NULL>" : rec[k]));




                    }



                    statement.addBatch();
                    //statement.clearParameters();

                    if(commit_count>1000) {
                        statement.executeBatch();
                        commit_count = 0;
                    }


                if(count%1000==0) System.out.println(count);



                }






                connection.commit();
                connection.disconnect();


            }
            endTime = System.nanoTime();
        }
        catch (SQLException e)
        {

        }



        return "Sucesso! " + count + " linhas foram convertidas! Em " + (( System.currentTimeMillis() - startTime)/1000) + " segundos.";
    }


    /*@RequestMapping(value = "/dbfToSqlite")
    public String saveDbfAsSqlite(@RequestParam(value="dbfpath") String dbfpath, @RequestParam(value="sqlitepath") String sqlitepath)
    {
        int count = 0;
        long startTime = System.nanoTime();


        SQLiteManager connection = new SQLiteManager(sqlitepath);
        connection.connect();
        if(connection != null)
        {
            ExecutorService executor = Executors.newFixedThreadPool(5);
            DBFManager dbf = new DBFManager(dbfpath);
            dbf.prepareDBF();
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

            connection.execute(sql);

            sql = "INSERT INTO dbf_import VALUES ";
            String base_sql = sql;
            String[] rec;
            int commit_count = 0;

            while((rec = dbf.readNext()) != null)  {
                ++count;
                ++commit_count;



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

                }
                else
                {
                    sql+=");";

                    executor.execute(new SQLiteManager(sqlitepath, sql));

                    sql = base_sql;
                    commit_count = 0;

                }


            }
            if(commit_count>0)
                sql = sql.substring(0,sql.length()-1);
            sql += ";";
            connection.execute(sql);
            executor.shutdown();

            while (!executor.isTerminated()) {
            }

            connection.disconnect();




        }
        long endTime = System.nanoTime();



        return "Sucesso! " + count + " linhas foram convertidas! Em " + ((endTime - startTime)/1000000) + " segundos.";
    }*/
}
