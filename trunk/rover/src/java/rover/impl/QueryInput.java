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

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import rover.IFieldInfo;
import rover.IForeignKeyInfo;
import rover.IQueryContext;
import rover.ITableInfo;
import rover.QueryConstants;
import rover.SQLTypeConverter;

/**
 * Internal class used for constructing a query.
 * 
 * @author tzellman
 * 
 */
public class QueryInput
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

    public QueryInput(String tableName, IQueryContext context) throws Exception
    {
        tables = new LinkedList<ITableInfo>();
        tables.push(context.getDatabaseInfo().getTableInfo(tableName));
        this.context = context;

        fkRelationships = new LinkedList<FKRelationship>();
        wheres = new LinkedList<Where>();
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
        distinct = dolly.distinct;
    }

    public void addFK(String column) throws Exception
    {
        // try to get the field from the current table
        ITableInfo currentTable = tables.peekFirst();
        Map<String, IFieldInfo> currentFields = currentTable.getFields();

        IFieldInfo fieldInfo = currentFields.get(column);
        // also, get the FK info
        IForeignKeyInfo fkInfo = fieldInfo == null ? null : fieldInfo
                .getForeignKeyInfo();

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

    public static final class QueryData
    {
        public String table;

        public String query;

        public Object[] values;

        public QueryData[] relatedQueries;

        public Map<String, String> selectAliases;
    }

    protected static final class ProcessData
    {
        protected Set<String> selectStatements;

        protected Set<String> whereStatements;

        protected Set<String> selectedTables;

        protected List<Object> values;

        protected Map<String, String> selectAliases;

        protected Map<String, String> reverseSelectAliases;

        protected int id;

        protected Map<String, List<String>> tableIds;

        public ProcessData()
        {
            selectStatements = new LinkedHashSet<String>();
            whereStatements = new LinkedHashSet<String>();
            selectedTables = new LinkedHashSet<String>();
            values = new LinkedList<Object>();
            selectAliases = new HashMap<String, String>();
            reverseSelectAliases = new HashMap<String, String>();
            id = 0;
            tableIds = new HashMap<String, List<String>>();
        }
    }

    protected void processRelated(ITableInfo table, int depth, String lastId,
            String prefix, ProcessData p) throws Exception
    {
        for (IFieldInfo field : table.getFields().values())
        {
            IForeignKeyInfo fkInfo = field.getForeignKeyInfo();
            if (fkInfo != null)
            {
                String fkTableName = fkInfo.getTable();
                if (!p.tableIds.containsKey(fkTableName))
                    p.tableIds.put(fkTableName, new LinkedList<String>());

                String fkTableId = null;
                List<String> idList = p.tableIds.get(fkTableName);
                if (idList.size() < depth)
                {
                    fkTableId = String.format("o%d", p.id++);
                    p.tableIds.get(fkTableName).add(fkTableId);
                }
                else
                    fkTableId = idList.get(depth - 1);

                p.whereStatements.add(String.format("%s.%s=%s.%s", lastId,
                        field.getName(), fkTableId, fkInfo.getColumn()));

                // now, add all of the FK fields
                ITableInfo fkTable = context.getDatabaseInfo().getTableInfo(
                        fkTableName);
                for (String fieldName : fkTable.getFields().keySet())
                {
                    // alias
                    String selectAs = String.format("%s__%s__%s", prefix,
                            fkTableName, fieldName).toLowerCase();
                    String alias = newAlias(selectAs, p.selectAliases);
                    p.selectStatements.add(String.format("%s.%s AS %s",
                            fkTableId, fieldName, alias));
                    p.reverseSelectAliases.put(selectAs, alias);
                }

                // recursive...
                if (depth < selectRelatedDepth)
                    processRelated(fkTable, depth + 1, fkTableId, String
                            .format("%s__%s", prefix, fkTable.getName())
                            .toLowerCase(), p);
            }
        }

    }

    public QueryData getQuery(int offset, int limit, Set<String> selectFields)
            throws Exception
    {
        ProcessData p = new ProcessData();

        for (Where where : wheres)
        {
            Map<String, Integer> innerTableCount = new HashMap<String, Integer>();
            Deque<FKRelationship> relationships = where.fkRelationships;
            Iterator<FKRelationship> iter = relationships.descendingIterator();
            String fkTableId = null;
            while (iter.hasNext())
            {
                FKRelationship rel = iter.next();
                String tableName = rel.table.getName();
                if (!innerTableCount.containsKey(tableName))
                    innerTableCount.put(tableName, 1);

                // update the fk table count
                String fkTableName = rel.fkTable.getName();
                Integer fkCount = innerTableCount.get(fkTableName);
                fkCount = fkCount == null ? 1 : fkCount + 1;
                innerTableCount.put(fkTableName, fkCount);

                // now, get/create the table Ids for both
                Integer tableCount = innerTableCount.get(tableName);

                if (!p.tableIds.containsKey(tableName))
                    p.tableIds.put(tableName, new LinkedList<String>());
                if (!p.tableIds.containsKey(fkTableName))
                    p.tableIds.put(fkTableName, new LinkedList<String>());

                List<String> tableIdList = p.tableIds.get(tableName);
                List<String> fkTableIdList = p.tableIds.get(fkTableName);

                while (tableCount > tableIdList.size())
                    tableIdList.add(String.format("o%d", p.id++));
                while (fkCount > fkTableIdList.size())
                    fkTableIdList.add(String.format("o%d", p.id++));

                String tableId = tableIdList.get(tableCount - 1);
                fkTableId = fkTableIdList.get(fkCount - 1);

                p.whereStatements.add(String.format("%s.%s=%s.%s", tableId,
                        rel.column, fkTableId, rel.fkColumn));
            }

            String lastTableId = fkTableId;
            if (lastTableId == null)
            {
                String tableName = tables.getLast().getName();
                if (!p.tableIds.containsKey(tableName))
                    p.tableIds.put(tableName,
                            Arrays.asList(new String[] { String.format("o%d",
                                    p.id++) }));
                lastTableId = p.tableIds.get(tableName).get(0);
            }

            SQLOp op = where.op;
            String finalWhere = String.format("%s.%s %s", lastTableId,
                    where.column.getName(), op.getOperator());
            if (!op.isUnary())
                finalWhere = finalWhere + " ?";
            p.whereStatements.add(finalWhere);

            if (!op.isUnary())
            {
                String sqlTypeName = where.column.getSQLType();
                Integer sqlType = QueryConstants.SQL_TYPE_NAMES
                        .get(sqlTypeName);

                // add a hint for the dispatcher
                Map hints = new HashMap();
                hints.put(SQLTypeConverter.HINT_SQL_TYPE, sqlType);

                // turn the String value into an Object for the query
                Object sqlValue = context.getSQLTypeConverter().convert(
                        where.value, hints);
                p.values.add(sqlValue);
            }
        }

        {
            // must at least select one table
            ITableInfo table = tables.getLast();
            String tableName = table.getName();
            List<String> idList = p.tableIds.get(tableName);
            if (idList == null || idList.isEmpty())
            {
                String tableId = String.format("o%d", p.id++);
                p.tableIds.put(tableName, Arrays
                        .asList(new String[] { tableId }));
            }
        }

        // select all from first table by default
        ITableInfo table = tables.getLast();
        String tableName = table.getName();
        String tableId = p.tableIds.get(tableName).get(0);

        if (selectFields != null)
        {
            for (String select : selectFields)
            {
                select = select.trim();
                if (select.matches(".*[ .].*"))
                {
                    // add it as-is - the user better know what they're doing
                    p.selectStatements.add(select);
                }
                else
                {
                    // it's a field they want to add
                    // for now, only support fields in the top table
                    if (table.getFields().containsKey(select.toUpperCase()))
                    {
                        // alias
                        String selectAs = select.toLowerCase();
                        String alias = newAlias(selectAs, p.selectAliases);
                        p.selectStatements.add(String.format("%s.%s AS %s",
                                tableId, select.toUpperCase(), alias));
                        p.reverseSelectAliases.put(selectAs, alias);
                    }
                }
            }
        }

        if (p.selectStatements.isEmpty())
        {
            // select all from first table by default
            for (String field : table.getFields().keySet())
            {
                // alias
                String selectAs = String.format("%s__%s", tableName, field)
                        .toLowerCase();
                String alias = newAlias(selectAs, p.selectAliases);
                p.selectStatements.add(String.format("%s.%s AS %s", tableId,
                        field, alias));
                p.reverseSelectAliases.put(selectAs, alias);
            }
        }

        if (selectRelatedDepth > 0
                && (selectFields == null || selectFields.isEmpty()))
        {
            processRelated(table, 1, tableId, table.getName().toLowerCase(), p);
        }

        List<String> ordering = new LinkedList<String>();
        if (orderBy != null)
        {
            String lTable = tables.getLast().getName().toLowerCase();
            for (String field : orderBy)
            {
                boolean ascending = true;
                if (field.startsWith("-"))
                {
                    ascending = false;
                    field = field.substring(1);
                }

                // the downside is that we currently require you to have
                // selected
                // the field you want to sort on. Maybe in the future we can
                // auto-select it
                String lField = String.format("%s__%s", lTable, field
                        .toLowerCase());
                if (p.reverseSelectAliases.containsKey(lField))
                {
                    String alias = p.reverseSelectAliases.get(lField);
                    ordering.add(String.format("%s %s", alias,
                            ascending ? "ASC" : "DESC"));
                }
                else
                {
                    // maybe warn that it was an invalid field to sort on
                }
            }
        }

        for (String tName : p.tableIds.keySet())
        {
            List<String> idList = p.tableIds.get(tName);
            for (String tId : idList)
                p.selectedTables.add(String.format("%s %s", tName, tId));
        }

        StringBuffer queryBuf = new StringBuffer();
        queryBuf.append("select ");
        if (distinct)
            queryBuf.append("distinct ");
        queryBuf.append(StringUtils.join(p.selectStatements.toArray(), ","));
        queryBuf.append(" from ");
        queryBuf.append(StringUtils.join(p.selectedTables, ", "));

        if (!p.whereStatements.isEmpty())
            queryBuf.append(" where "
                    + StringUtils.join(p.whereStatements, " and "));

        StringBuffer limitBuf = new StringBuffer();
        String databaseTypeName = context.getDatabaseInfo().getDatabaseType();

        if (StringUtils.equalsIgnoreCase(databaseTypeName,
                QueryConstants.DATABASE_ORACLE))
        {
            if (limit > 0 && offset <= 0)
            {
                String limitStr = " rownum <= " + limit;
                if (!p.whereStatements.isEmpty())
                    queryBuf.append(" and " + limitStr);
                else
                    queryBuf.append(" where " + limitStr);
            }
        }
        else
        {
            if (limit > 0)
                limitBuf.append(" limit " + limit);
            if (offset > 0)
                limitBuf.append(" offset " + limit);
        }

        if (!ordering.isEmpty())
            queryBuf.append(" order by " + StringUtils.join(ordering, ", "));

        // other limits go at the end
        if (limitBuf.length() > 0)
        {
            queryBuf.append(limitBuf);
        }

        // finally, if the user supplied an offset - we have to do something
        // sick for Oracle...
        if (StringUtils.equalsIgnoreCase(databaseTypeName,
                QueryConstants.DATABASE_ORACLE)
                && offset > 0)
        {
            // get the current query
            String query = queryBuf.toString();

            queryBuf = new StringBuffer();
            queryBuf.append("select * from (select rownum rnum");

            if (p.selectAliases != null && p.selectAliases.size() > 0)
            {
                queryBuf.append(", ");

                // get all aliases and add them
                queryBuf.append(StringUtils
                        .join(p.selectAliases.keySet(), ", "));
            }

            // add the original query...
            queryBuf.append(" from (" + query + ")");

            if (limit > 0)
                queryBuf.append(" where rownum <= " + (limit + offset));

            queryBuf.append(") where rnum > " + offset);
        }

        QueryData q = new QueryData();
        q.table = tables.getLast().getName();
        q.query = queryBuf.toString();
        q.values = p.values.toArray();
        q.selectAliases = p.selectAliases;
        return q;
    }

    protected String newAlias(String value, Map<String, String> aliasMap)
    {
        String alias = RandomStringGenerator.randomstring();
        while (aliasMap.containsKey(alias))
            alias = RandomStringGenerator.randomstring();
        aliasMap.put(alias, value);
        return alias;
    }

    protected static class RandomStringGenerator
    {
        private static Random random = new Random();

        public static int rand(int lo, int hi)
        {
            int n = hi - lo + 1;
            int i = random.nextInt() % n;
            if (i < 0)
                i = -i;
            return lo + i;
        }

        public static String randomstring(int lo, int hi)
        {
            int n = rand(lo, hi);
            byte b[] = new byte[n];

            b[0] = (byte) rand('a', 'z');
            for (int i = 1; i < n; i++)
            {
                if (i % 2 == 0)
                    b[i] = (byte) rand('a', 'z');
                else
                    b[i] = (byte) rand('0', '9');
            }
            return new String(b, Charset.defaultCharset());
        }

        public static String randomstring()
        {
            return randomstring(5, 30);
        }
    }

}