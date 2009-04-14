package rover.impl.sql;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Map;

import jester.IConverter;

import org.apache.commons.lang.SerializationException;

public class OracleSQLStatementConverter extends SQLStatementConverter
{

    public static final SimpleDateFormat ORACLE_TIMESTAMP = new SimpleDateFormat(
            "dd-MMM-yyyy HH:mm:ss");

    public static final String ORACLE_FORMAT = "'DD-MON-YYYY HH24:MI:SS'";

    public OracleSQLStatementConverter()
    {
        super();
        register(new IConverter<Timestamp, String>()
        {
            public String convert(Timestamp from, Map hints)
                    throws SerializationException
            {
                // TODO we may need to specify the timezone...
                return "TO_TIMESTAMP('" + ORACLE_TIMESTAMP.format(from) + "', "
                        + ORACLE_FORMAT + ")";
            };
        });
    }
}
