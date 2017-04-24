package DbfReader;

import java.io.*;
import java.util.Iterator;
import java.util.List;
//import com.linuxense.javadbf.*;
import nl.knaw.dans.common.dbflib.*;


public class DBFManager
{
    private Iterator<Record> table_iterator;
    private List<Field> fields;
    private final Table table;
    private int numberOfFields;
    private String[] fieldName;
    private Object[][] row;
    private int numberOfRecords;
    private int currentRecord;
    private FileInputStream path;

    public int getNumberOfFields()
    {
        return this.numberOfFields;
    }




    DBFManager(String tablePath)
    {
        this.table = new Table(new File(tablePath));

        this.numberOfFields = 0;
        this.currentRecord = 0;
    }

    /*public void set_startRecord(int start_record)
    {

        if(start_record > numberOfRecords) return;
        if(start_record < currentRecord) {
            //System.out.println("1");
            this.reader.close();
            this.prepareDBF(this.path.toString());
        }
        while(currentRecord < start_record)
        {
            //System.out.println("2");
            if(this.reader.nextRecord() == null)
                return;
            this.currentRecord++;
        }


    }*/


    /*public DBFReader getReader()
    {
        return this.reader;
    }*/
    public String[][] seekRecords(int start_record, int amount) {
        /*int i;
        String[][] data = new String[amount][this.numberOfFields];
        if(start_record > numberOfRecords) return null;
        if(start_record < currentRecord) {
            //System.out.println("1");
            this.reader.close();
            this.prepareDBF(this.path.toString());
        }
        while(currentRecord < start_record)
        {
            //System.out.println("2");
            if(this.reader.nextRecord() == null)
                return null;
            this.currentRecord++;
        }
        i = 0;
        while(amount-- > 0)
        {
            data[i++] = this.reader.nextRecord();
        }
        this.reader.close();
        return data;*/

        if(table_iterator.hasNext())
        {
            String[][] data = new String[amount][this.numberOfFields];
            if(start_record > numberOfRecords) return null;
            if(start_record < currentRecord) {
                try {
                    this.table.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.prepareDBF(this.path.toString());
            }
            while(currentRecord < start_record)
            {
                if(table_iterator.hasNext()) {
                    table_iterator.next();
                    this.currentRecord++;
                }
            }
            int i = 0;

            while(amount > 0 && table_iterator.hasNext())
            {
                int j = 0;
                final Record rec = table_iterator.next();
                for(final Field field : this.fields)
                {
                    try {
                        data[i][j++] = new String(rec.getRawValue(field));
                    } catch (DbfLibException e) {
                        e.printStackTrace();
                    }
                }

                i++;
                amount--;
                this.currentRecord++;

            }
            return data;


        }
        return null;

    }

    public synchronized String[] readNext()
    {
        if(this.table_iterator.hasNext()) {
            int i = 0;
            String data[] = new String[this.numberOfFields];
            Record rec = this.table_iterator.next();
            this.currentRecord++;
            for (final Field field : fields) {
                try {
                    data[i++] = new String(rec.getRawValue(field));
                } catch (DbfLibException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return data;
        }
        else return null;

    }


    public void prepareDBF(String dbfPath)
    {
        this.currentRecord = 0;
        try
        {

            this.table.open(IfNonExistent.ERROR);
            this.fields = table.getFields();
            this.numberOfFields = table.getFields().size();
            System.out.println(this.numberOfFields);
            this.numberOfRecords = this.table.getRecordCount();
            this.fieldName = new String[this.numberOfFields];

            int i = 0;
            for(final Field field : fields)
            {
                this.fieldName[i++] = field.getName();
            }
            this.table_iterator = table.recordIterator();

        }
         catch (CorruptedTableException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*public void loadDBF(String dbfPath)
    {
        try
        {
            this.path = new FileInputStream(dbfPath);
            this.reader = new DBFReader(this.path);
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
    }*/

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

    public void closeTable()
    {
        try {
            this.table.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}