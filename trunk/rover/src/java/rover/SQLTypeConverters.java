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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jester.IConverter;

import org.apache.commons.lang.SerializationException;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.DateUtils;


/**
 * SQL Transformers
 * 
 * @author tzellman
 */
public final class SQLTypeConverters
{
    public abstract static class SQLTypeConverter<T extends Object> implements
            IConverter<String, T>
    {
        public abstract Integer[] getSupportedTypes();
    }

    public abstract static class SQLDateConverter<T> extends
            SQLTypeConverter<T>
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

    public static class StringConverter extends SQLTypeConverter<String>
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

    public static class IntegerConverter extends SQLTypeConverter<Integer>
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

    public static class FloatConverter extends SQLTypeConverter<Float>
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

    public static class DoubleConverter extends SQLTypeConverter<Double>
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

    private SQLTypeConverters()
    {
    }
}
