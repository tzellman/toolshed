package rover;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 * Bean that holds database table metadata information.
 * 
 * @author tzellman
 */
public class TableInfoBean
{

    protected String name;

    protected Map<String, FieldInfoBean> fields;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Map<String, FieldInfoBean> getFields()
    {
        return fields;
    }

    public void setFields(Map<String, FieldInfoBean> fields)
    {
        this.fields = fields;
    }

    /**
     * Returns a new TableInfo object
     * 
     * @param tableName
     * @return
     * @throws Exception
     */
    public static TableInfoBean getTableInfo(String tableName,
            Connection connection) throws Exception
    {
        TableInfoBean tableInfo = new TableInfoBean();
        tableInfo.setName(tableName.toUpperCase());
        Map<String, FieldInfoBean> infos = new TreeMap<String, FieldInfoBean>();

        DatabaseMetaData metaData = connection.getMetaData();

        ResultSet rsCols = metaData.getColumns(null, null, tableName, null);

        // map foreign key columns
        Map<String, ForeignKeyInfoBean> fkInfoMap = new HashMap<String, ForeignKeyInfoBean>();
        ResultSet rsFKs = metaData.getImportedKeys(null, null, tableName
                .toUpperCase());
        while (rsFKs.next())
        {
            String fkColumnName = rsFKs
                    .getString(QueryConstants.FKCOLUMN_NAME_FIELD);
            String pkTableName = rsFKs
                    .getString(QueryConstants.PKTABLE_NAME_FIELD);
            String pkColumnName = rsFKs
                    .getString(QueryConstants.PKCOLUMN_NAME_FIELD);

            ForeignKeyInfoBean fkInfo = new ForeignKeyInfoBean();
            fkInfo.setTable(pkTableName);
            fkInfo.setColumn(pkColumnName);

            fkInfoMap.put(fkColumnName, fkInfo);
        }
        rsFKs.close();

        // get column info
        Statement stmt = connection.createStatement();
        for (int j = 0; rsCols.next(); ++j)
        {
            String columnName = rsCols
                    .getString(QueryConstants.COLUMN_NAME_FIELD);
            String columnType = rsCols
                    .getString(QueryConstants.COLUMN_TYPE_FIELD);

            FieldInfoBean info = new FieldInfoBean();
            info.setTable(tableName.toUpperCase());
            info.setName(columnName);
            info.setSqlTypeName(columnType);
            info.setSqlType(QueryConstants.SQL_TYPE_NAMES.get(columnType));

            // if it's a FK column
            if (fkInfoMap.containsKey(columnName))
                info.setForeignKeyInfo(fkInfoMap.get(columnName));
            infos.put(columnName.toUpperCase(), info);
        }
        stmt.close();
        rsCols.close();

        tableInfo.setFields(infos);
        return tableInfo;
    }
}
