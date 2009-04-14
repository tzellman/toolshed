package rover.impl.sql;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import jester.ConverterRegistry;
import jester.IConverter;
import jester.utils.JesterUtils;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaBeanMapDecorator;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.SerializationException;

public class SQLStatementConverter extends ConverterRegistry<Object, String>
{

    public static final String HINT_FIELD_MAP = "fieldMap";

    public static final String HINT_TABLE_NAME = "tableName";

    private static final String HINT_MAP_DEPTH = "mapDepth";

    public static final String HINT_SQL_TYPE = "sqlType";

    public static final String HINT_INSERT = "insert";

    public static final String HINT_UPDATE = "update";

    public static final String HINT_UPDATE_WHERE = "updateWhere";

    public SQLStatementConverter()
    {
        // register(new CollectionConverter(this));
        register(new StringConverter());
        register(new NumberConverter());
        register(new BooleanConverter());
        register(new MapConverter(this));
        register(new IConverter<DynaBean, String>()
        {
            public String convert(DynaBean from, Map hints)
                    throws SerializationException
            {
                return SQLStatementConverter.this.convert(
                        (Map) new DynaBeanMapDecorator(from), hints);
            }
        });
        register(new IConverter<Timestamp, String>()
        {
            public String convert(Timestamp from, Map hints)
                    throws SerializationException
            {
                // might need something different for specific DBs
                return toDBString(from.toString());
            };
        });
    }

    public String toDBString(String val)
    {
        // TODO need to escape for some DBs
        return "'" + val + "'";
    }

    /**
     * Converts a String to a JSON String
     */
    public class StringConverter implements IConverter<String, String>
    {
        public String convert(String from, Map hints)
                throws SerializationException
        {
            return toDBString(from);
        }
    }

    /**
     * Converts a Number to a JSON String
     */
    public class NumberConverter implements IConverter<Number, String>
    {
        public String convert(Number from, Map hints)
                throws SerializationException
        {
            return from.toString();
        }
    }

    /**
     * Converts a Boolean to a JSON String
     */
    public class BooleanConverter implements IConverter<Boolean, String>
    {
        public String convert(Boolean from, Map hints)
                throws SerializationException
        {
            return from.booleanValue() ? "1" : "0";
        }
    }

    /**
     * Converts a Map to a JSON String
     */
    public class MapConverter implements IConverter<Map, String>
    {
        protected SQLStatementConverter registry;

        public MapConverter(SQLStatementConverter registry)
        {
            this.registry = registry;
        }

        public String convert(Map from, Map hints)
                throws SerializationException
        {
            Integer mapDepth = (Integer) hints.get(HINT_MAP_DEPTH);
            if (mapDepth == null || mapDepth < 1)
                mapDepth = 0;
            mapDepth++;

            if (mapDepth > 2)
                throw new SerializationException(
                        "Maps nested > 2 deep are not supported");
            hints.put(HINT_MAP_DEPTH, mapDepth);

            boolean tableSet = hints.containsKey(HINT_TABLE_NAME);
            if (tableSet && !hints.containsKey(HINT_FIELD_MAP))
                hints.put(HINT_FIELD_MAP, new HashMap());

            Object[] keys = from.keySet().toArray();
            for (int i = 0, size = keys.length; i < size; ++i)
            {
                Object key = keys[i];
                // set the table name (mainly, if it's the top-level map)
                if (!tableSet)
                    hints.put(HINT_TABLE_NAME, key);

                String value = registry.convert(from.get(key), String.class,
                        hints);

                // set the field (if it wasn't a table name, but a field name)
                if (tableSet)
                    ((Map) hints.get(HINT_FIELD_MAP)).put(key, value);
            }

            hints.put(HINT_MAP_DEPTH, --mapDepth);

            // if we're at the top, return the entire statement
            if (mapDepth == 0)
            {
                Object sqlType = hints.get(HINT_SQL_TYPE);
                String updateWhere = (String) hints.get(HINT_UPDATE_WHERE);

                if (ObjectUtils.equals(sqlType, HINT_INSERT))
                    updateWhere = null;

                return createStatement((String) hints.get(HINT_TABLE_NAME),
                        (Map) hints.get(HINT_FIELD_MAP), (String) updateWhere);
            }
            // otherwise, nothing...
            return null;
        }
    }

    protected String createStatement(String table, Map<String, String> fields,
            String updateWhere)
    {
        boolean update = updateWhere != null;

        StringBuffer buf = new StringBuffer();
        String[] keys = fields.keySet().toArray(new String[0]);

        if (update)
        {
            buf.append("UPDATE ");
            buf.append(table);
            buf.append(" SET ");
            for (int i = 0; i < keys.length; i++)
            {
                String field = keys[i];
                buf.append(String.format("%s=%s", field, fields.get(field)));
                if (i < keys.length - 1)
                    buf.append(", ");
            }
            buf.append(" WHERE ");
            buf.append(updateWhere);
        }
        else
        {
            buf.append("INSERT INTO ");
            buf.append(table);

            buf.append(" (");
            for (int i = 0; i < keys.length; i++)
            {
                String field = keys[i];
                buf.append(field);
                if (i < keys.length - 1)
                    buf.append(",");
            }
            buf.append(") VALUES (");
            for (int i = 0; i < keys.length; i++)
            {
                String field = keys[i];
                buf.append(fields.get(field));
                if (i < keys.length - 1)
                    buf.append(",");
            }
            buf.append(")");
        }
        buf.append(";");
        return buf.toString();
    }

    @Override
    public Object defaultConvert(Object from, Class<? extends Object> toClass,
            Map hints) throws SerializationException
    {
        if (from == null)
            return "null";
        else if (JesterUtils.isArrayType(from))
        {
            return convert(JesterUtils.objectToCollection(from), String.class,
                    hints);
        }
        // default to String
        // return convert(from.toString(), String.class, hints);
        throw new SerializationException("Unable to convert input object");
    }

    @Override
    public String convert(Object from, Class toClass, Map hints)
            throws SerializationException
    {
        if (hints == null)
            hints = new HashMap();
        return (String) super.convert(from, toClass, hints);
    }

}
