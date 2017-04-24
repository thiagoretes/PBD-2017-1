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
                             @RequestParam(value = "start") int startRecord,
                             @RequestParam(value = "amount") int amount)
    {
        SQLiteManager connection = new SQLiteManager(path);
        connection.connect();
        String sql = "SELECT * FROM dbf_import WHERE" +  "rowid > " + startRecord + "AND rowid < " + startRecord+amount + ";";
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


    //Function to show a HTML table representation of the SQLiteDB, provided only for testing
    @RequestMapping(value = "/showSQLiteDB")
    @ResponseBody
    public String selectAll(@RequestParam(value = "sqlitepath") String path)
    {
        SQLiteManager connection = new SQLiteManager(path);
        connection.connect();
        String sql = "SELECT * FROM dbf_import;";
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
}
