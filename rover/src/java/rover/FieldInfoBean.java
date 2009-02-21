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

/**
 * Simple bean for holding information regarding a database field/column.
 * 
 * @author tzellman
 */
public class FieldInfoBean
{
    protected String table;

    protected String name;

    protected String sqlTypeName;

    protected Integer sqlType;

    protected ForeignKeyInfoBean foreignKeyInfo;

    public FieldInfoBean()
    {
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getSqlTypeName()
    {
        return sqlTypeName;
    }

    public void setSqlTypeName(String sqlTypeName)
    {
        this.sqlTypeName = sqlTypeName;
    }

    public Integer getSqlType()
    {
        return sqlType;
    }

    public void setSqlType(Integer sqlType)
    {
        this.sqlType = sqlType;
    }

    public ForeignKeyInfoBean getForeignKeyInfo()
    {
        return foreignKeyInfo;
    }

    public void setForeignKeyInfo(ForeignKeyInfoBean foreignKeyInfo)
    {
        this.foreignKeyInfo = foreignKeyInfo;
    }

    public boolean isForeignKey()
    {
        return foreignKeyInfo != null;
    }

    public String getTable()
    {
        return table;
    }

    public void setTable(String table)
    {
        this.table = table;
    }

}
