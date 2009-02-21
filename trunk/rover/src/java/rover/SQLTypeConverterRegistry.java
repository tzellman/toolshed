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

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import jester.IConverter;

import org.apache.commons.lang.SerializationException;

import rover.SQLTypeConverters.DateConverter;
import rover.SQLTypeConverters.DoubleConverter;
import rover.SQLTypeConverters.FloatConverter;
import rover.SQLTypeConverters.IntegerConverter;
import rover.SQLTypeConverters.SQLTypeConverter;
import rover.SQLTypeConverters.StringConverter;
import rover.SQLTypeConverters.TimestampConverter;

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
