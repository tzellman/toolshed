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
package rover.impl;

import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import rover.IFieldInfo;
import rover.IForeignKeyInfo;
import rover.IQueryContext;
import rover.ITableInfo;

/**
 * Internal class used for constructing a query.
 * 
 * @author tzellman
 * 
 */
public abstract class QueryInput
{
    protected static class FKRelationship
    {
        public ITableInfo table;

        public String column;

        public ITableInfo fkTable;

        public String fkColumn;
    }

    protected static class Where
    {
        public Deque<FKRelationship> fkRelationships;

        public IFieldInfo column;

        public SQLOp op;

        public String value;
    }

    protected Deque<ITableInfo> tables;

    protected Deque<FKRelationship> fkRelationships;

    protected List<Where> wheres;

    protected List<String> orderBy;

    protected IQueryContext context;

    protected int selectRelatedDepth;

    protected boolean distinct;

    protected String tableName;

    public QueryInput(String tableName, IQueryContext context) throws Exception
    {
        this.tableName = tableName;
        tables = new LinkedList<ITableInfo>();
        tables.push(context.getDatabaseInfo().getTableInfo(tableName));
        this.context = context;

        fkRelationships = new LinkedList<FKRelationship>();
        wheres = new LinkedList<Where>();
        orderBy = new LinkedList<String>();
        selectRelatedDepth = 0; // no select related, by default
        distinct = false;
    }

    public QueryInput(QueryInput dolly) throws Exception
    {
        this(dolly.tables.getLast().getName(), dolly.context);
        tables.clear();
        tables.addAll(dolly.tables);
        fkRelationships.addAll(dolly.fkRelationships);
        wheres.addAll(dolly.wheres);
        orderBy.addAll(dolly.orderBy);
        distinct = dolly.distinct;
    }

    public String getTableName()
    {
        return tableName;
    }

    public void addFK(String column) throws Exception
    {
        IForeignKeyInfo fkInfo = null;
        final String[] parts = StringUtils.splitByWholeSeparator(column, "$$",
                3);
        if (parts.length > 1)
        {
            column = parts[0];
            final String table = parts[1];
            final String col = parts.length == 2 ? column : parts[2];
            fkInfo = new IForeignKeyInfo()
            {
                public String getColumn()
                {
                    return col;
                }

                public String getTable()
                {
                    return table;
                }
            };
        }

        // try to get the field from the current table
        ITableInfo currentTable = tables.peekFirst();
        Map<String, IFieldInfo> currentFields = currentTable.getFields();

        IFieldInfo fieldInfo = currentFields.get(column);

        if (fkInfo == null)
        {
            // also, get the FK info
            fkInfo = fieldInfo == null ? null : fieldInfo.getForeignKeyInfo();
        }

        if (fkInfo == null)
        {
            /*
             * This could be the situation where you want to do a reverse lookup
             * to a table that has the current table listed as a foreign key.
             * This is tricky b/c a table can have multiple foreign keys to
             * another table. I would say that in the future we add a reverse
             * lookup map to the query context, that the user can supply.
             * 
             * For now, we'll just support setting that column by the name of
             * the reverse lookup table. We'll use the foreign key, if there is
             * only 1 referring to this table. If there are multiple, we'll
             * throw.
             */

            // see if the "field" was really a table name
            ITableInfo reverseInfo = context.getDatabaseInfo().getTableInfo(
                    column);
            if (reverseInfo != null)
            {
                Iterator<IFieldInfo> reverseFieldIter = reverseInfo.getFields()
                        .values().iterator();
                while (reverseFieldIter.hasNext())
                {
                    IFieldInfo f = reverseFieldIter.next();
                    IForeignKeyInfo reverseFKInfo = f.getForeignKeyInfo();
                    if (reverseFKInfo != null
                            && StringUtils.equalsIgnoreCase(reverseFKInfo
                                    .getTable(), currentTable.getName()))
                    {
                        if (fkInfo != null)
                        {
                            // this means we found two FKs to this
                            // table
                            throw new Exception(
                                    "Unable to differentiate between multiple FK fields");
                        }

                        // reverse lookup!
                        fkInfo = new ForeignKeyInfoBean();
                        ((ForeignKeyInfoBean) fkInfo).setColumn(f.getName());
                        ((ForeignKeyInfoBean) fkInfo).setTable(f.getTable());

                        // set the fieldInfo for THIS field - which
                        // is just the FK of the reverse FK info...
                        // obvious, right?
                        fieldInfo = currentFields
                                .get(reverseFKInfo.getColumn());

                        // if this is null, then the logic for
                        // setting up the TableInfo was incorrect,
                        // for some magical reason...
                        if (fieldInfo == null)
                            throw new Exception(
                                    "Unable to determine the correct FK table/column.");
                    }
                }
            }
        }

        if (fkInfo == null)
            throw new Exception("Invalid FK Column: " + column);

        ITableInfo fkTable = context.getDatabaseInfo().getTableInfo(
                fkInfo.getTable());
        tables.push(fkTable);

        FKRelationship fkRel = new FKRelationship();
        fkRel.table = currentTable;
        fkRel.column = fieldInfo.getName();
        fkRel.fkTable = fkTable;
        fkRel.fkColumn = fkInfo.getColumn();
        fkRelationships.push(fkRel);
    }

    public void addWhere(String column, SQLOp op, String value)
            throws Exception
    {
        // make sure the column is a field in the current table
        ITableInfo currentTable = tables.peekFirst();
        if (!currentTable.getFields().containsKey(column))
            throw new Exception("Invalid Column: " + column);

        Where where = new Where();
        where.column = currentTable.getFields().get(column);
        where.fkRelationships = new LinkedList<FKRelationship>(fkRelationships);
        where.op = op;
        where.value = value;
        wheres.add(where);

        // reset everything else
        reset();
    }

    protected void reset()
    {
        ITableInfo firstTable = tables.pollLast();
        tables.clear();
        tables.push(firstTable);
        fkRelationships.clear();
        // wheres.clear();
        // orderBy.clear();
    }

    /**
     * Returns true if the query is empty
     * 
     * @return
     */
    public boolean isEmpty()
    {
        return fkRelationships.isEmpty() && wheres.isEmpty();
    }

    public void setSelectRelatedDepth(int depth)
    {
        this.selectRelatedDepth = depth;
    }

    public void setDistinct(boolean distinct)
    {
        this.distinct = distinct;
    }

    public void setOrderBy(List<String> orderBy)
    {
        this.orderBy = orderBy;
    }

    public void addOrderBy(String... fields)
    {
        if (this.orderBy == null)
            this.orderBy = new LinkedList<String>();
        this.orderBy.addAll(Arrays.asList(fields));
    }

    public abstract Object getQuery(int offset, int limit,
            Set<String> selectFields) throws Exception;

}
