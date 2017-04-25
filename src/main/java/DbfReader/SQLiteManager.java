package DbfReader;

import java.sql.*;


public class SQLiteManager implements Runnable{

    static private Connection conn;
    private String connectionURL;
    private String query;
    private PreparedStatement preparedStatement;



    SQLiteManager(String connectionURL)
    {
        this.connectionURL = "jdbc:sqlite:" + connectionURL;
    }

    SQLiteManager(String connectionURL, String query)
    {
        this.connectionURL = "jdbc:sqlite:" + connectionURL;
        this.query = query;
    }

    SQLiteManager(String connectionURL, PreparedStatement preparedStatement) {
        this.connectionURL = "jdbc:sqlite:" + connectionURL;
        this.preparedStatement = preparedStatement;
    }

    public boolean isConnected() {
        try {
            return !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean commit()
    {
        try {
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void connect()
    {
        try
        {

            Class.forName("org.sqlite.JDBC");
//            SQLiteConfig config = new SQLiteConfig();
            //           config.setSynchronous(SQLiteConfig.SynchronousMode.OFF);
            //         config.setJournalMode(SQLiteConfig.JournalMode.OFF);

            conn = DriverManager.getConnection(this.connectionURL);//, config.toProperties());
            conn.setAutoCommit(false);
            System.out.println("Connection to database success!");

        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("ZICA!");
            e.printStackTrace();
        }

    }

    /*public void batchInsert(String sql)
    {
        PreparedStatement prep = con.prepareStatement(sql);
    }*/

    public void disconnect()
    {
        try
        {
            if (conn != null)
                conn.close();
            else
                System.out.println("A conexão já estava fechada!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public PreparedStatement createPreStatement(String sql)
    {
        try {
            return conn.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized void execute(PreparedStatement sql)
    {
        try {
            if(conn != null) {
                sql.executeBatch();
            }
            else System.out.println("Query: " + sql +" não executada!\n");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

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
            Statement stmt = conn.createStatement();//preparar query

            return stmt.executeQuery(sql);//executar query
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    @Override
    public void run() {


        this.execute(this.preparedStatement);


    }
}
