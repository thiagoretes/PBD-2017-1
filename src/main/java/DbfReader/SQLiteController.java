package DbfReader;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Created by Thiago Retes
 * Email: thiagoretes@uft.edu.br
 */

@RestController
public class SQLiteController {




    @RequestMapping(value = "/querySQLiteDB", produces="application/json")
    @ResponseBody
    public String querySQLiteDB(@RequestParam(value = "path") String path,
                                @RequestParam(value = "query") String query)
    {
        return "not implemented";
    }


    //Function to open make a 'SELECT * FROM table' query to SQLiteDB
    @RequestMapping(value = "/openSQLiteDB", produces="application/json")
    @ResponseBody
    public String openSQLite(@RequestParam(value = "path") String path,
                             @RequestParam(value = "start", defaultValue = "0") int startRecord,
                             @RequestParam(value = "amount", defaultValue = "50") int amount)
    {
        SQLiteManager connection = new SQLiteManager(path);
        connection.connect();
        if (connection.isConnected()) {
            String sql = "SELECT * FROM dbf_import WHERE " + "rowid > " + startRecord + " AND rowid <= " + (startRecord + amount) + ";";
            ResultSet rs = connection.query(sql);

            if (rs == null) System.out.println("deu ruim");

            String result = "{ \"fields\": [\n";

            try {
                ResultSetMetaData rsmd = rs.getMetaData();
                int colCount = rsmd.getColumnCount();

                for (int i = 1; i <= colCount; i++)
                    result += "\"" + rsmd.getColumnName(i) + "\",\n";
                result = result.substring(0, result.length() - 2);
                result += "],\n\"rows\": [\n";
                while (rs.next()) {
                    result += "[";
                    for (int i = 1; i <= colCount; i++)
                        result += "\"" + rs.getString(i) + "\",\n";
                    result = result.substring(0, result.length() - 2);
                    result += "\n],\n";
                }
                result = result.substring(0, result.length() - 2);
                result += "]}";
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
                return result;
            }
        } else return "{\"result\": \"Error\"}";

    }

    //Function to retrive sorted columns
    @RequestMapping(value = "/openSQLiteSortedColumn", produces = "application/json")
    @ResponseBody
    public String jsonSortedCol(@RequestParam(value = "path") String path,
                                @RequestParam(value = "start", defaultValue = "0") int startRecord,
                                @RequestParam(value = "amount", defaultValue = "50") int amount,
                                @RequestParam(value = "name", defaultValue = "") String table,
                                @RequestParam(value = "sortBy", defaultValue = "0") int order) {
        SQLiteManager connection = new SQLiteManager(path);
        connection.connect();
        if (connection.isConnected()) {
            long startTime = System.currentTimeMillis();
            String sql = "CREATE INDEX IF NOT EXISTS " + table + "_index ON dbf_import (" + table + ");";
            connection.execute(sql);
            if (connection.commit())
                System.out.println("Indice criado com sucesso! Tempo gasto:" + (System.currentTimeMillis() - startTime) / 1000);
            String order_str = (order == 1) ? "ASC" : "DESC";
            sql = "SELECT * FROM dbf_import ORDER BY " + table + " " + order_str + " LIMIT " + amount + " OFFSET " + startRecord + ";";
            ResultSet rs = connection.query(sql);

            if (rs == null) System.out.println("deu ruim");

            String result = "{ \"fields\": [\n";

            try {
                ResultSetMetaData rsmd = rs.getMetaData();
                int colCount = rsmd.getColumnCount();

                for (int i = 1; i <= colCount; i++)
                    result += "\"" + rsmd.getColumnName(i) + "\",\n";
                result = result.substring(0, result.length() - 2);
                result += "],\n\"rows\": [\n";
                while (rs.next()) {
                    result += "[";
                    for (int i = 1; i <= colCount; i++)
                        result += "\"" + rs.getString(i) + "\",\n";
                    result = result.substring(0, result.length() - 2);
                    result += "\n],\n";
                }
                result = result.substring(0, result.length() - 2);
                result += "]}";
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
                return result;
            }
        } else return "{\"result\": \"Error\"}";

    }


    //Function to show a HTML table representation of the SQLiteDB, provided only for testing
    @RequestMapping(value = "/showSQLiteDB")
    @ResponseBody
    public String selectAll(@RequestParam(value = "path") String path,
                            @RequestParam(value = "start", defaultValue = "0") int startRecord,
                            @RequestParam(value = "amount", defaultValue = "50") int amount)
    {
        SQLiteManager connection = new SQLiteManager(path);
        connection.connect();
        if (connection.isConnected()) {
            String sql = "SELECT * FROM dbf_import WHERE " + "rowid > " + startRecord + " AND rowid <= " + (startRecord + amount) + ";";
            ResultSet rs = connection.query(sql);


            String result = "<style>\n" +
                    "table, th, td {\n" +
                    "    border: 1px solid black;\n" +
                    "}\n" +
                    "</style><table style=\"width:100%\">\n<tr>";

            try {
                ResultSetMetaData rsmd = rs.getMetaData();
                int colCount = rsmd.getColumnCount();
                //int colCount = 11;
                for (int i = 1; i <= colCount; i++)
                    result += "<th>" + rsmd.getColumnName(i) + "</th>\n";
                result += "</tr>\n";
                while (rs.next()) {
                    result += "<tr>\n";
                    for (int i = 1; i <= colCount; i++)
                        result += "<td>" + rs.getString(i) + "</td>\n";
                    result += "</tr>\n";
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();

                return result;
            }
        } else
            return "Error!";


    }


    //Function to show a HTML table sorted by column of the SQLiteDB, provided only for testing
    @RequestMapping(value = "/showSQLiteSortedColumn")
    @ResponseBody
    public String selectSortedCol(@RequestParam(value = "path") String path,
                                  @RequestParam(value = "start", defaultValue = "0") int startRecord,
                                  @RequestParam(value = "amount", defaultValue = "50") int amount,
                                  @RequestParam(value = "name", defaultValue = "") String table,
                                  @RequestParam(value = "sortBy", defaultValue = "0") int order) {

        SQLiteManager connection = new SQLiteManager(path);
        connection.connect();
        long startTime = System.currentTimeMillis();
        if (connection.isConnected()) {
            String sql = "CREATE INDEX IF NOT EXISTS " + table + "_index ON dbf_import (" + table + ");";
            connection.execute(sql);
            if (connection.commit())
                System.out.println("Indice criado com sucesso! Tempo gasto:" + (System.currentTimeMillis() - startTime) / 1000);
            String order_str = (order == 1) ? "ASC" : "DESC";
            sql = "SELECT * FROM dbf_import ORDER BY " + table + " " + order_str + " LIMIT " + amount + " OFFSET " + startRecord + ";";

            ResultSet rs = connection.query(sql);


            String result = "<style>\n" +
                    "table, th, td {\n" +
                    "    border: 1px solid black;\n" +
                    "}\n" +
                    "</style><table style=\"width:100%\">\n<tr>";

            try {
                ResultSetMetaData rsmd = rs.getMetaData();
                int colCount = rsmd.getColumnCount();

                for (int i = 1; i <= colCount; i++)
                    result += "<th>" + rsmd.getColumnName(i) + "</th>\n";
                result += "</tr>\n";
                while (rs.next()) {
                    result += "<tr>\n";
                    for (int i = 1; i <= colCount; i++)
                        result += "<td>" + rs.getString(i) + "</td>\n";
                    result += "</tr>\n";
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection.commit();
                connection.disconnect();

                return result;
            }
        } else
            return "Error!";


    }
}
