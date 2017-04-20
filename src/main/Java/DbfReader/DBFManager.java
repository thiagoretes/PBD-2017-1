package DbfReader;

import java.io.*;
import com.linuxense.javadbf.*;


public class DBFManager
{
    private DBFReader reader = null;
    private int numberOfFields;
    DBFField[] field;
    private String[] fieldName;
    Object[][] row;
    private int numberOfRecords;

    DBFManager()
    {
        this.numberOfFields = 0;
    }

    public void loadDBF(String dbfPath)
    {
        try
        {
            this.reader = new DBFReader(new FileInputStream(dbfPath));
            this.numberOfFields = this.reader.getFieldCount();
            System.out.println(this.numberOfFields);
            this.field = new DBFField[this.numberOfFields];
            this.fieldName = new String[this.numberOfFields];
            this.numberOfRecords = this.reader.getRecordCount();
            this.row = new Object[this.numberOfRecords][];

            for(int i = 0; i < this.numberOfFields; i++)
            {
                this.field[i] = this.reader.getField(i);
                this.fieldName[i] = this.field[i].getName();




            }

            this.row = new Object[this.reader.getRecordCount()][this.numberOfFields];
            System.out.println(this.numberOfRecords);
            for(int i = 0; i < this.numberOfRecords-2; i++) {
                this.row[i] = this.reader.nextRecord();

            }


        } catch (DBFException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            DBFUtils.close(this.reader);
        }
    }

    public String[] getFieldName()
    {
        return this.fieldName;
    }

    public Object[][] getRows()
    {



        return this.row;
    }

    public int getNumberOfRecords()
    {
        return this.numberOfRecords;
    }

}