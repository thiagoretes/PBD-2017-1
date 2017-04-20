package DbfReader;

import javax.xml.transform.Result;
import java.sql.*;


public class SQLiteManager {

    private String connectionURL;
    private Connection conn;

    SQLiteManager(String connectionURL)
    {
        this.connectionURL = "jdbc:sqlite:" + connectionURL;
    }

    public void connect()
    {
        try
        {
            Class.forName("org.sqlite.JDBC");

            conn = DriverManager.getConnection(this.connectionURL);
            System.out.println("Connection to database success!");

        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("ZICA!");
            e.printStackTrace();
        }
        finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public void execute(String sql)
    {
        try (Connection conn = DriverManager.getConnection(this.connectionURL);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public ResultSet query(String sql)
    {
        try {
            Connection conn = DriverManager.getConnection(this.connectionURL);
             Statement stmt = conn.createStatement();
            // create a new table
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }


}
