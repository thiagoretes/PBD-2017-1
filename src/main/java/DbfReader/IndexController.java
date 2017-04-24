package DbfReader;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.util.JSONWrappedObject;
//import com.linuxense.javadbf.DBFReader;
import jdk.nashorn.internal.ir.debug.JSONWriter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import DbfReader.DBFManager;
import DbfReader.SQLiteManager;

import javax.sound.midi.SysexMessage;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by Thiago Retes
 * Email: thiagoretes@uft.edu.br
 */

@RestController
public class IndexController {

    @RequestMapping(value = "/")
    public String index()
    {
        return "This is the DbfReader frontend...";

    }


}