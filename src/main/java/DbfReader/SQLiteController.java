package DbfReader;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Table;
import com.google.common.collect.HashBasedTable;

import javax.servlet.http.HttpServletRequest;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @RequestMapping(value = "/relacionaCol", produces = "application/json")
    @ResponseBody
    public String relacionaColunas(@RequestParam(value = "path") String path,
                                     @RequestParam(value = "main_col") String main_col,
                                     @RequestParam(value = "sec_col") String sec_col) {
        long time_start = System.currentTimeMillis();
        SQLiteManager connection1 = new SQLiteManager(path);//Instanciar nova conexão
        connection1.connect();//Conectar ao banco de dados
        SQLiteManager connection2 = new SQLiteManager(path);//Instanciar nova conexão
        connection2.connect();//Conectar ao banco de dados
        SQLiteManager connection3 = new SQLiteManager(path);//Instanciar nova conexão
        connection3.connect();//Conectar ao banco de dados
        String sql = "CREATE INDEX IF NOT EXISTS " + main_col + "_index ON dbf_import (" + main_col + ");";
        connection1.execute(sql);
        sql = "CREATE INDEX IF NOT EXISTS " + sec_col + "_index ON dbf_import (" + sec_col + ");";
        connection1.execute(sql);
        connection1.commit();


        if(connection1.isConnected())
        {
            String query = "SELECT DISTINCT " + main_col + " FROM dbf_import ORDER BY " + main_col + " ASC;";

            String query2 = "SELECT DISTINCT " + sec_col + " FROM dbf_import ORDER BY " + sec_col + " ASC;";

            ResultSet main = connection1.query(query);

            ResultSet secondary = connection2.query(query2);

            List<String> header_list = new ArrayList<String>();
            List<String> main_list = new ArrayList<String>();
            List<String> sec_list = new ArrayList<String>();
            header_list.add(main_col);
            Table<String, String, String> table = HashBasedTable.create();
            String return_result = "{\"gridData\":[";
            try {
                //ResultSetMetaData main_rsmd = secondary.getMetaData();
                int tamanho_main_col = 0;
                while(main.next())
                {

                    main_list.add(main.getString(1));
                    tamanho_main_col++;

                }


                ResultSetMetaData sec_rsmd = secondary.getMetaData();

                int i = 1;
                while(secondary.next())
                {

                    query2 = "SELECT " + main_col + ", COUNT(rowid) AS \'" + secondary.getString(i) + "\' FROM dbf_import WHERE " + sec_col + "=\'" + secondary.getString(i) + "\' GROUP BY " + main_col + ";";
                    //System.out.println("Passou query3");

                    header_list.add(secondary.getString(i));
                    sec_list.add(secondary.getString(i));

                    ResultSet teste = connection3.query(query2);
                    //System.out.println(i + ": " + teste.getMetaData().getColumnCount());
                    while(teste.next())
                    {

                        table.put(teste.getString(1),secondary.getString(1), teste.getString(2));



                    }



                }
                /*for (Table.Cell<String, String, String> cell: table.cellSet()){
                    System.out.println(cell.getRowKey()+" "+cell.getColumnKey()+" "+cell.getValue());
                }*/

                for(String s : main_list) {
                    System.out.print("\n" + s);
                    return_result += "{ \"" + main_col + "\": \"" + s + "\", ";
                    for (String ss : sec_list) {
                        System.out.print(" " + ss + ": " + ((table.get(s,ss) == null) ? "0" : table.get(s,ss)));
                        return_result += "\"" + ss + "\": \"" + ((table.get(s,ss) == null) ? "0" : table.get(s,ss)) + "\", ";

                    }
                    return_result = return_result.substring(0,return_result.length()-2);
                    return_result +="}, ";
                }
                return_result = return_result.substring(0,return_result.length()-2);
                return_result += "], \"gridColumns\": [";
                for(String s : header_list)
                    return_result += "\"" + s + "\",";
                return_result = return_result.substring(0,return_result.length()-1);
                return_result += "]}";


            } catch (SQLException e) {
                e.printStackTrace();
                connection1.disconnect();
                connection2.disconnect();
                connection3.disconnect();

            }

            System.out.println("Tempo gasto: " + (System.currentTimeMillis()-time_start)/1000);
            connection1.disconnect();
            connection2.disconnect();
            connection3.disconnect();
            return return_result;
        }
        return "ERROR!";
    }

    @RequestMapping(value = "/createDerivatedCol", produces = "application/json")
    @ResponseBody
    public String createDerivatedCol(@RequestParam(value = "path") String path,
                                     @RequestParam(value = "col_name") String col_name,
                                     @RequestParam(value = "start") int start,
                                     @RequestParam(value = "end") int end,
                                     @RequestParam(value = "new_col_name") String new_col_name) {
        SQLiteManager connection = new SQLiteManager(path);//Instanciar nova conexão
        connection.connect();//Conectar ao banco de dados
        long start_time = System.currentTimeMillis();
        if(connection.isConnected())
        {
            String query = "ALTER TABLE dbf_import ADD COLUMN " + new_col_name + " text"+ ";";
            connection.execute(query);//Executa query
            connection.commit();//Atualiza o banco de dados
            query = "UPDATE dbf_import SET " + new_col_name + "=SUBSTR(" + col_name + "," + start + "," + ((end-start)+1) + ");";
            connection.execute(query);
            connection.commit();
            return "Tempo gasto: " + ((System.currentTimeMillis() - start_time)/1000) + " segundos.";
        }


        return "Erro!";
    }

    //Function to retrive sorted columns
    @RequestMapping(value = "/openSQLiteSortedColumn", produces = "application/json")
    @ResponseBody
    public String jsonSortedCol(@RequestParam(value = "path") String path,
                                @RequestParam(value = "start", defaultValue = "0") int startRecord,
                                @RequestParam(value = "amount", defaultValue = "50") int amount,
                                @RequestParam(value = "name", defaultValue = "") String table,
                                @RequestParam(value = "sortBy", defaultValue = "0") int order,
                                @RequestParam(value = "filter", defaultValue = "") String filter,
                                HttpServletRequest request) {


        String currentPath = request.getRequestURL().toString() + "?" + request.getQueryString();
        currentPath = currentPath.replace("/", "\\/").replace("\\","\\/").replace("\\\\","\\").replace("//","/");



        SQLiteManager connection = new SQLiteManager(path);
        connection.connect();
        if (connection.isConnected()) {

            String query = "SELECT Count(*) FROM dbf_import";
            ResultSet rs = connection.query(query);
            String test = "";
            try {
                test = rs.getString(1);
            } catch (SQLException e) {
                e.printStackTrace();
                connection.disconnect();
                return "Error!";
            }


            long startTime = System.currentTimeMillis();
            String sql = "CREATE INDEX IF NOT EXISTS " + table + "_index ON dbf_import (" + table + ");";
            String order_str = (order == 1) ? "ASC" : "DESC";
            System.out.println(order_str + " " + order);
            //if(!table.equals("rowid")) {

                System.out.println(table);
                connection.execute(sql);
                if (connection.commit())
                    System.out.println("Indice criado com sucesso! Tempo gasto:" + (System.currentTimeMillis() - startTime) / 1000);
                sql = "SELECT * FROM dbf_import ORDER BY " + table + " " + order_str + " LIMIT " + amount + " OFFSET " + startRecord + ";";
            if(!filter.equals(""))
            {
                System.out.println("FILTER: " + filter);
                sql = "SELECT * FROM dbf_import WHERE " + filter + " ORDER BY " + table + " " + order_str + " LIMIT " + amount + " OFFSET " + startRecord + ";";
                System.out.println("QUERY: " + sql);
            }

            //}
            //else
            //{
                //sql = "SELECT * FROM dbf_import WHERE rowid > " + startRecord + " ORDER BY " + table + " " + order_str + " LIMIT " + amount + ";";
            //}

            //sql = "SELECT * FROM dbf_import ORDER BY " + table + " " + order_str + " LIMIT " + amount + " OFFSET " + startRecord + ";";
            rs = connection.query(sql);

            if (rs == null){
                System.out.println("A query não retornou nada, provavelmente o indice era invalido!");
                sql = "SELECT * FROM dbf_import ORDER BY rowid " + order_str + " LIMIT " + amount + " OFFSET " + startRecord + ";";
                rs = connection.query(sql);
            }

            int total = Integer.parseInt(test);
            int page = (startRecord/amount) + 1;
            int to_record = (amount*(page));
            if(to_record>total) to_record = total;
            String returnText = "{\"total\": \"" + total + "\", ";
            returnText += "\"per_page\": \"" + amount + "\", " + "\"current_page\": \"" + page + "\", " +
                    "\"last_page\": \"" + ((total/amount)+1) + "\", \"next_page_url\": \"" + "http:\\/\\/localhost:8080\\/api?type=2&page=" +
                    (page+1) + "&path=" + path.replace("\\","\\/") + "&per_page=" + amount + "\", \"from\": " + (amount*(page-1)+1) + ", \"to\": " + to_record + ", ";

            String result = returnText + "\"fields\": [";

            try {
                ResultSetMetaData rsmd = rs.getMetaData();
                int colCount = rsmd.getColumnCount();

                for (int i = 1; i <= colCount; i++) {
                    result += "{ \"name\": \"" + rsmd.getColumnName(i) + "\", ";
                    result += "\"title\": " + "\"" + rsmd.getColumnName(i) + "\", ";
                    result += "\"sortField\": \"" + rsmd.getColumnName(i) + "\", ";
                    result += "\"visible\": \"true\"}, ";


                }
                result = result.substring(0, result.length() - 2);
                result += "], \"data\": [ ";
                while (rs.next()) {
                    result += "{";
                    for (int i = 1; i <= colCount; i++)
                        result += "\"" + rsmd.getColumnName(i) + "\": \"" + rs.getString(i) + "\", ";
                    result = result.substring(0, result.length() - 2);
                    result += "}, ";
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

    @RequestMapping(value = "/getSQLiteRecordAmount")
    @ResponseBody
    public String getSQLiteRecortAmount(@RequestParam(value = "path") String path) {
        SQLiteManager connection = new SQLiteManager(path);
        connection.connect();
        String query = "SELECT Count(*) FROM dbf_import";
        ResultSet rs = connection.query(query);
        String test = "";
        try {
            test = rs.getString(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connection.disconnect();
        return test;
    }

}
