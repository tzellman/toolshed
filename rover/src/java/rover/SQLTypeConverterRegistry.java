package rover;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import jester.IConverter;

import org.apache.commons.lang.SerializationException;

import rover.hood.SQLTypeConverters.CharConverter;
import rover.hood.SQLTypeConverters.DateConverter;
import rover.hood.SQLTypeConverters.DoubleConverter;
import rover.hood.SQLTypeConverters.FloatConverter;
import rover.hood.SQLTypeConverters.IntegerConverter;
import rover.hood.SQLTypeConverters.SQLTypeConverter;
import rover.hood.SQLTypeConverters.StringConverter;
import rover.hood.SQLTypeConverters.TimestampConverter;


/**
 * Converts String data to Objects required for a prepared SQL statement, based
 * on the SQL Type.
 * 
 * @author tzellman
 */
public class SQLTypeConverterRegistry implements IConverter<String, Object>
{
    public static final String HINT_SQL_TYPE = "SQLType";

    protected Map<Integer, SQLTypeConverter<? extends Object>> transformers;

    public SQLTypeConverterRegistry()
    {
        transformers = new HashMap<Integer, SQLTypeConverter<? extends Object>>();

        // load some defaults
        registerTransformer(new StringConverter());
        registerTransformer(new DateConverter());
        registerTransformer(new TimestampConverter());
        registerTransformer(new CharConverter());
        registerTransformer(new IntegerConverter());
        registerTransformer(new FloatConverter());
        registerTransformer(new DoubleConverter());
    }

    public void registerTransformer(
            SQLTypeConverter<? extends Object> transformer)
    {
        for (Integer type : transformer.getSupportedTypes())
            transformers.put(type, transformer);
    }

    public SQLTypeConverter<? extends Object> getConverter(Integer sqlType)
    {
        return transformers.get(sqlType);
    }

    public Object convert(String from, Map hints) throws SerializationException
    {
        if (from == null)
            return null;

        Integer sqlType = Types.VARCHAR; // default
        if (hints != null && hints.containsKey(HINT_SQL_TYPE))
            sqlType = (Integer) hints.get(HINT_SQL_TYPE);

        if (sqlType == null)
        {
            // should we throw here instead?
            sqlType = Types.VARCHAR;
        }

        SQLTypeConverter<? extends Object> converter = getConverter(sqlType);
        if (converter != null)
            return converter.convert(from, hints);
        throw new SerializationException("No serializer for type: " + sqlType);
    }

}
