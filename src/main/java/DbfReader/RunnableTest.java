package DbfReader;

//import com.linuxense.javadbf.DBFReader;
import nl.knaw.dans.common.dbflib.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

/**
 * Created by nowayrlz on 23/04/17.
 */


public class RunnableTest implements Runnable {

    private DBFManager dbfManager;
    private String dbfpath;
    private SQLiteManager sqlmanager;
    private int startIndex;
    private int amount;


    public RunnableTest(String dbfpathj, SQLiteManager sqlmanager, int startIndex, int amount) {

            //this.dbfManager = new DBFManager();
            //this.dbfManager.prepareDBF((dbfpathj));

        this.sqlmanager = sqlmanager;
        this.startIndex = startIndex;
        this.amount = amount;

    }


    @Override
    public void run() {
        final Table table = new Table(new File("/home/nowayrlz/Downloads/DOBR2010.dbf"));
        try
        {
            table.open(IfNonExistent.ERROR);

            final Format dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            System.out.println("TABLE PROPERTIES");
            System.out.println("Name          : " + table.getName());
            System.out.println("Last Modified : " + dateFormat.format(table.getLastModifiedDate()));
            System.out.println("--------------");
            System.out.println();
            System.out.println("FIELDS (COLUMNS)");
            String drop_sql = "DROP TABLE IF EXISTS dbf_import;";
            sqlmanager.execute(drop_sql);
            String sql = "CREATE TABLE IF NOT EXISTS dbf_import (\n";

            final List<Field> fields = table.getFields();

            for(final Field field: fields)
            {
                sql+= " " + field.getName() + " character[20],\n";
                System.out.println("  Name       : " + field.getName());
                System.out.println("  Type       : " + field.getType());
                System.out.println("  Length     : " + field.getLength());
                System.out.println("  Dec. Count : " + field.getDecimalCount());
                System.out.println();
            }
            sql = sql.substring(0,sql.length()-2);
            sql += ");";
            sqlmanager.execute(sql);

            System.out.println("--------------");
            System.out.println();
            System.out.println("RECORDS");

            final Iterator<Record> recordIterator = table.recordIterator();
            int count = 0;
            int commitcount = 0;
            sql = "INSERT INTO dbf_import VALUES ";

            while(recordIterator.hasNext())
            {
                final Record record = recordIterator.next();
                //System.out.println(count++);
                count++;
                if(count%1000 == 0) System.out.println(count);
                sql+="(";
                for(final Field field: fields)
                {
                    try
                    {
                        byte[] rawValue = record.getRawValue(field);
                        String fff = new String(rawValue);

                        sql += "\'" + fff + "\',";

                        //System.out.println(field.getName() + " : " + (rawValue == null ? "<NULL>" : new String(rawValue)));
                    }
                    catch(ValueTooLargeException vtle)
                    {
                        // Cannot happen :)
                    }
                }
                sql = sql.substring(0, sql.length() - 1);
                if(commitcount == 100) {
                    commitcount = 0;
                    sql += ");";
                    sqlmanager.execute(sql);
                    //new Thread(new SQLiteManager("/home/nowayrlz/Downloads/teste.sqlite", sql)).start();

                    sql = "INSERT INTO dbf_import VALUES ";
                }
                else
                {
                    sql+="),";
                    commitcount++;
                }


            }

            System.out.println("--------------");
        }
        catch(IOException ioe)
        {
            System.out.println("Trouble reading table or table not found");
            ioe.printStackTrace();
        }
        catch(DbfLibException dbflibException)
        {
            System.out.println("Problem getting raw value");
            dbflibException.printStackTrace();
        }
        finally
        {
            try
            {
                table.close();
            } catch (IOException ex)
            {
                System.out.println("Unable to close the table");
            }
        }




    }
    /*@Override
    public void run() {
        if (this.dbfManager.getNumberOfRecords() < startIndex)
            return;
        String sql = "INSERT INTO dbf_import VALUES ";
        for (int i = 0; i < this.dbfManager.getNumberOfRecords() / 1000; i++) {
            for (int k = 0; k < 1000; k++) {
                sql += "(";
                Object[] row;
                if ((row = dbfManager.readNext()) == null) break;
                for (int j = 0; j < dbfManager.getFieldName().length; j++) {
//                    String temp = row[i][j].toString();
//                    temp.replace("\'", "\'\'");
                    if (row[j] != null)
                        sql += "\'" + row[j].toString().replace("\'", "\'\'") + "\',";
                    else sql += "\'" + "null" + "\',";
                }

                sql = sql.substring(0, sql.length() - 1);
                sql += "),";
            }
            sql = sql.substring(0, sql.length() - 1);
            sql += ";";
            sqlmanager.execute(sql);
            System.out.println("Chanfrou +1k: atual -> " + i);
        }
    }*/
}
