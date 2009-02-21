package rover.impl;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import rover.DatabaseInfoCache;
import rover.FieldInfoBean;
import rover.ForeignKeyInfoBean;
import rover.QueryContext;
import rover.SQLTypeConverterRegistry;
import rover.TableInfoBean;

public class QueryInput
{
    protected static class FKRelationship
    {
        public TableInfoBean table;

        public String column;

        public TableInfoBean fkTable;

        public String fkColumn;
    }

    protected static class Where
    {
        public Deque<FKRelationship> fkRelationships;

        public FieldInfoBean column;

        public SQLOp op;

        public String value;
    }

    protected Deque<TableInfoBean> tables;

    protected Deque<FKRelationship> fkRelationships;

    protected List<Where> wheres;

    protected QueryContext context;

    protected DatabaseInfoCache cache;

    protected int selectRelatedDepth;

    protected boolean distinct;

    protected Set<String> selectFields;

    public QueryInput(String tableName, DatabaseInfoCache cache,
            QueryContext context) throws Exception
    {
        tables = new LinkedList<TableInfoBean>();
        tables.push(cache.getTableInfo(tableName, context.getConnection()));
        this.cache = cache;
        this.context = context;

        fkRelationships = new LinkedList<FKRelationship>();
        wheres = new LinkedList<Where>();
        selectRelatedDepth = 0; // no select related, by default
        distinct = false;
    }

    public QueryInput(QueryInput dolly) throws Exception
    {
        this(dolly.tables.getLast().getName(), dolly.cache, dolly.context);
        tables.clear();
        tables.addAll(dolly.tables);
        fkRelationships.addAll(dolly.fkRelationships);
        wheres.addAll(dolly.wheres);
        distinct = dolly.distinct;
    }

    public void addFK(String column) throws Exception
    {
        // try to get the field from the current table
        TableInfoBean currentTable = tables.peekFirst();
        Map<String, FieldInfoBean> currentFields = currentTable.getFields();

        FieldInfoBean fieldInfo = currentFields.get(column);
        // also, get the FK info
        ForeignKeyInfoBean fkInfo = fieldInfo == null ? null : fieldInfo
                .getForeignKeyInfo();

        Connection connection = context.getConnection();

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
            TableInfoBean reverseInfo = cache.getTableInfo(column, connection);
            if (reverseInfo != null)
            {
                Iterator<FieldInfoBean> reverseFieldIter = reverseInfo
                        .getFields().values().iterator();
                while (reverseFieldIter.hasNext())
                {
                    FieldInfoBean f = reverseFieldIter.next();
                    ForeignKeyInfoBean reverseFKInfo = f.getForeignKeyInfo();
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
                        fkInfo.setColumn(f.getName());
                        fkInfo.setTable(f.getTable());

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

        TableInfoBean fkTable = cache.getTableInfo(fkInfo.getTable(),
                connection);
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
        TableInfoBean currentTable = tables.peekFirst();
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
        TableInfoBean firstTable = tables.pollLast();
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

    public static final class QueryData
    {
        public String table;

        public String query;

        public Object[] values;

        public QueryData[] relatedQueries;
    }

    public QueryData getQuery(int offset, int limit, Set<String> selectFields)
            throws Exception
    {
        Set<String> selectStatements = new LinkedHashSet<String>();
        Set<String> whereStatements = new LinkedHashSet<String>();
        Set<String> selectedTables = new LinkedHashSet<String>();
        List<Object> values = new LinkedList<Object>();
        int id = 0;
        Map<String, List<String>> tableIds = new HashMap<String, List<String>>();
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

                if (!tableIds.containsKey(tableName))
                    tableIds.put(tableName, new LinkedList<String>());
                if (!tableIds.containsKey(fkTableName))
                    tableIds.put(fkTableName, new LinkedList<String>());

                List<String> tableIdList = tableIds.get(tableName);
                List<String> fkTableIdList = tableIds.get(fkTableName);

                while (tableCount > tableIdList.size())
                    tableIdList.add(String.format("o%d", id++));
                while (fkCount > fkTableIdList.size())
                    fkTableIdList.add(String.format("o%d", id++));

                String tableId = tableIdList.get(tableCount - 1);
                fkTableId = fkTableIdList.get(fkCount - 1);

                whereStatements.add(String.format("%s.%s=%s.%s", tableId,
                        rel.column, fkTableId, rel.fkColumn));
            }

            String lastTableId = fkTableId;
            if (lastTableId == null)
                lastTableId = tableIds.get(tables.getLast().getName()).get(0);

            SQLOp op = where.op;
            String finalWhere = String.format("%s.%s %s", lastTableId,
                    where.column.getName(), op.getOperator());
            if (!op.isUnary())
                finalWhere = finalWhere + " ?";
            whereStatements.add(finalWhere);

            if (!op.isUnary())
            {
                Integer sqlType = where.column.getSqlType();

                // add a hint for the dispatcher
                Map hints = new HashMap();
                hints.put(SQLTypeConverterRegistry.HINT_SQL_TYPE, sqlType);

                // turn the String value into an Object for the query
                Object sqlValue = context.getRegistry().convert(where.value,
                        hints);
                values.add(sqlValue);
            }
        }

        {
            // must at least select one table
            TableInfoBean table = tables.getLast();
            String tableName = table.getName();
            List<String> idList = tableIds.get(tableName);
            if (idList == null || idList.isEmpty())
            {
                String tableId = String.format("o%d", id++);
                tableIds
                        .put(tableName, Arrays.asList(new String[] { tableId }));
            }
        }

        if (selectFields != null)
        {
            for (String select : selectFields)
            {
                if (select.matches(".*[ .].*"))
                {
                    // add it as-is - the user better know what they're doing
                    selectStatements.add(select);
                }
                else
                {
                    // it's a field they want to add
                    // TODO - not supported yet...
                }
            }
        }

        if (selectStatements.isEmpty())
        {
            // select all from first table by default
            TableInfoBean table = tables.getLast();
            String tableName = table.getName();
            String tableId = tableIds.get(tableName).get(0);
            for (String field : table.getFields().keySet())
            {
                // alias
                selectStatements.add(String.format("%s.%s AS %s__%s", tableId,
                        field, tableName, field));
            }
        }

        if (selectRelatedDepth > 0 && selectFields == null)
        {
            // get more data - this needs to be a recursive func I think...
            TableInfoBean table = tables.getLast();

            int depth = 1;

            // for now, only support 1 deep
            for (FieldInfoBean field : table.getFields().values())
            {
                ForeignKeyInfoBean fkInfo = field.getForeignKeyInfo();
                if (fkInfo != null)
                {
                    String fkTableName = fkInfo.getTable();
                    if (!tableIds.containsKey(fkTableName))
                        tableIds.put(fkTableName, new LinkedList<String>());

                    String fkTableId = null;
                    List<String> idList = tableIds.get(fkTableName);
                    if (idList.size() < depth)
                    {
                        fkTableId = String.format("o%d", id++);
                        tableIds.get(fkTableName).add(fkTableId);
                    }
                    else
                        fkTableId = idList.get(depth - 1);

                    String tableId = tableIds.get(field.getTable()).get(
                            depth - 1);
                    whereStatements.add(String.format("%s.%s=%s.%s", tableId,
                            field.getName(), fkTableId, fkInfo.getColumn()));

                    // now, add all of the FK fields
                    // this should make a recursive call here...
                    Connection connection = context.getConnection();
                    TableInfoBean fkTable = cache.getTableInfo(fkTableName,
                            connection);
                    for (String fieldName : fkTable.getFields().keySet())
                    {
                        // alias
                        selectStatements.add(String.format(
                                "%s.%s AS %s__%s__%s", fkTableId, fieldName,
                                field.getTable(), fkTableName, fieldName));
                    }
                }
            }
        }

        for (String tableName : tableIds.keySet())
        {
            List<String> idList = tableIds.get(tableName);
            for (String tableId : idList)
                selectedTables.add(String.format("%s %s", tableName, tableId));
        }

        StringBuffer queryBuf = new StringBuffer();
        queryBuf.append("select ");
        if (distinct)
            queryBuf.append("distinct ");
        queryBuf.append(StringUtils.join(selectStatements.toArray(), ","));
        queryBuf.append(" from ");
        queryBuf.append(StringUtils.join(selectedTables, ", "));

        if (!whereStatements.isEmpty())
            queryBuf.append(" where "
                    + StringUtils.join(whereStatements, " and "));

        QueryData q = new QueryData();
        q.table = tables.getLast().getName();
        q.query = queryBuf.toString();
        q.values = values.toArray();
        return q;
    }
}
