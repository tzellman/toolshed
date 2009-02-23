/* =============================================================================
 * This file is part of Rover
 * =============================================================================
 * (C) Copyright 2009, Tom Zellman, tzellman@gmail.com
 *
 * Rover is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package rover;

import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jester.IConverter;

import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;

/**
 * Converts String data to Objects required for a prepared SQL statement, based
 * on the SQL Type.
 * 
 * @author tzellman
 */
public class SQLTypeConverter implements IConverter<String, Object>
{
    public static final String HINT_SQL_TYPE = "SQLType";

    protected Map<Integer, TypeConverter<? extends Object>> transformers;

    public SQLTypeConverter()
    {
        transformers = new HashMap<Integer, TypeConverter<? extends Object>>();

        // load some defaults
        registerTransformer(new StringConverter());
        registerTransformer(new DateConverter());
        registerTransformer(new TimestampConverter());
        registerTransformer(new IntegerConverter());
        registerTransformer(new FloatConverter());
        registerTransformer(new DoubleConverter());
    }

    public void registerTransformer(TypeConverter<? extends Object> transformer)
    {
        for (Integer type : transformer.getSupportedTypes())
            transformers.put(type, transformer);
    }

    public TypeConverter<? extends Object> getConverter(Integer sqlType)
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

        TypeConverter<? extends Object> converter = getConverter(sqlType);
        if (converter != null)
            return converter.convert(from, hints);
        throw new SerializationException("No serializer for type: " + sqlType);
    }

    public abstract static class TypeConverter<T extends Object> implements
            IConverter<String, T>
    {
        public abstract Integer[] getSupportedTypes();
    }

    public abstract static class SQLDateConverter<T> extends TypeConverter<T>
    {
        protected List<String> datePatterns;

        public SQLDateConverter()
        {
            datePatterns = new LinkedList<String>(Arrays
                    .asList(QueryConstants.COMMON_DATE_PATTERNS));
        }

        void addDatePatterns(String... patterns)
        {
            datePatterns.addAll(Arrays.asList(patterns));
        }
    }

    public static class DateConverter extends SQLDateConverter<Date>
    {
        public Date convert(String from, Map hints)
                throws SerializationException
        {
            try
            {
                return new Date(DateUtils.parseDate(from,
                        datePatterns.toArray(new String[0])).getTime());
            }
            catch (ParseException e)
            {
                throw new SerializationException(e);
            }
        }

        @Override
        public Integer[] getSupportedTypes()
        {
            return new Integer[] { Types.DATE };
        }
    }

    public static class TimestampConverter extends SQLDateConverter<Timestamp>
    {

        public Timestamp convert(String from, Map hints)
                throws SerializationException
        {
            try
            {
                return new Timestamp(DateUtils.parseDate(from,
                        datePatterns.toArray(new String[0])).getTime());
            }
            catch (ParseException e)
            {
                throw new SerializationException(e);
            }
        }

        @Override
        public Integer[] getSupportedTypes()
        {
            return new Integer[] { Types.TIME, Types.TIMESTAMP };
        }
    }

    public static class StringConverter extends TypeConverter<String>
    {
        public String convert(String from, Map hints)
                throws SerializationException
        {
            return from;
        }

        @Override
        public Integer[] getSupportedTypes()
        {
            return new Integer[] { Types.VARCHAR, Types.LONGVARCHAR,
                    Types.NVARCHAR, Types.CHAR };
        }
    }

    public static class IntegerConverter extends TypeConverter<Integer>
    {
        public Integer convert(String from, Map hints)
                throws SerializationException
        {
            return NumberUtils.createInteger(from);
        }

        @Override
        public Integer[] getSupportedTypes()
        {
            return new Integer[] { Types.INTEGER };
        }
    }

    public static class FloatConverter extends TypeConverter<Float>
    {
        public Float convert(String from, Map hints)
                throws SerializationException
        {
            return NumberUtils.createFloat(from);
        }

        @Override
        public Integer[] getSupportedTypes()
        {
            return new Integer[] { Types.FLOAT };
        }
    }

    public static class DoubleConverter extends TypeConverter<Double>
    {
        public Double convert(String from, Map hints)
                throws SerializationException
        {
            return NumberUtils.createDouble(from);
        }

        @Override
        public Integer[] getSupportedTypes()
        {
            return new Integer[] { Types.DOUBLE, Types.REAL, Types.DECIMAL };
        }
    }

}
