package DbfReader;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by Thiago Retes on 24/04/17.
 * Email: thiagoretes@uft.edu.br
 */

@RestController
public class DBFController {


    @RequestMapping(value = "/getDBFRecordAmount")
    @ResponseBody
    public String getDBFRecordAmount(@RequestParam(value = "path") String dbfPath) {
        DBFManager dbf = new DBFManager(dbfPath);
        dbf.prepareDBF();

        return "" + dbf.getNumberOfRecords();
    }

    @RequestMapping(value = "/openDBF", produces = "application/json")
    @ResponseBody
    public String openDbf(@RequestParam(value = "path", defaultValue = "/teste/") String dbfPath,
                          @RequestParam(value = "page", defaultValue = "0") int page,
                          @RequestParam(value = "amountPerPage", defaultValue = "200") int amountPerPage) {
        System.out.println(dbfPath);
        DBFManager dbf = new DBFManager(dbfPath);
        dbf.prepareDBF();//Carregar o DBF
        String fieldsName[] = dbf.getFieldName();//Armazenar nome dos campos
        //String teste [] = dbf.readNext();
        String[][] row = dbf.seekRecords(page * amountPerPage, amountPerPage);
        String returnText = "[ { \"fields\": [\n";
        for (int i = 0; i < fieldsName.length; i++)
            returnText += "\"" + fieldsName[i] + "\",\n";

        returnText = returnText.substring(0, returnText.length() - 2);
        returnText += "], \"rows\": [ \n";

        amountPerPage = row.length;

        for (int i = 0; i < amountPerPage; i++) {


            returnText += "[";
            for (int j = 0; j < fieldsName.length; j++) {
                if (row[i][j] != null)
                    returnText += "\"" + row[i][j] + "\",\n";
                else returnText += "\"" + "null\",\n";

            }

            returnText = returnText.substring(0, returnText.length() - 2);
            returnText += "],\n";
        }
        returnText = returnText.substring(0, returnText.length() - 2);
        returnText += "]}]";

        return returnText;

    }

    @RequestMapping(value = "/dbfToSqlite")
    public String saveDbfAsSqlite(@RequestParam(value = "dbfpath") String dbfpath, @RequestParam(value = "sqlitepath") String sqlitepath) {
        int count = 0;
        long startTime = System.currentTimeMillis();
        try {
            SQLiteManager connection = new SQLiteManager(sqlitepath);
            connection.connect();

            if (connection.isConnected()) {
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
                //ExecutorService threadExecutor = Executors.newFixedThreadPool(10);
                PreparedStatement statement = connection.createPreStatement(sql);

                while ((rec = dbf.readNext()) != null) {
                    ++count;
                    ++commit_count;
                    for (int k = 0; k < fieldName.length; k++) {
                        statement.setString(k + 1, (rec[k] == null ? "<NULL>" : rec[k]));
                    }

                    statement.addBatch();


                    if (commit_count > 100) {
                        //threadExecutor.execute(new SQLiteManager(sqlitepath, statement));
                        //statement = connection.createPreStatement(sql);
                        statement.executeBatch();

                        commit_count = 0;
                    }

                    if (count % 100000 == 0) System.out.println(count);


                }

                statement.executeBatch();
                connection.commit();
                //threadExecutor.shutdown();
                //System.out.println("Waiting for threads to finish...");
                //while(!threadExecutor.isShutdown()){}

                connection.disconnect();
                System.out.println("Finished job!");
            }
        } catch (SQLException e) {
            System.out.println("Ocorreu um erro na conversão!");
            return "Ocorreu um erro na inserção do SQLite.";
        }


        return "Sucesso! " + count + " linhas foram convertidas! Em " + ((System.currentTimeMillis() - startTime) / 1000) + " segundos.";
    }


}