package DbfReader;

import javax.xml.transform.Result;
import java.sql.*;


public class SQLiteManager implements Runnable{

    private String connectionURL;
    static private Connection conn;
    private String query;
    SQLiteManager(String connectionURL)
    {
        this.connectionURL = "jdbc:sqlite:" + connectionURL;
    }
    SQLiteManager(String connectionURL, String query)
    {
        this.connectionURL = "jdbc:sqlite:" + connectionURL;
        this.query = query;
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

    }

    public void disconnect()
    {
        try
        {
            if(this.conn != null)
                conn.close();
            else
                System.out.println("A conexão já estava fechada!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*public void batchInsert(String sql)
    {
        PreparedStatement prep = con.prepareStatement(sql);
    }*/

    public synchronized void execute(String sql)
    {
        try {
            if(conn != null) {
                Statement stmt = conn.createStatement();
                // create a new table
                stmt.execute(sql);
            }
            else System.out.println("Query: " + sql +" não executada!\n");
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


    @Override
    public void run() {
        this.execute(this.query);
    }
}
