package rover;

import java.util.Map;

public interface ITableInfo
{
    String getName();

    Map<String, IFieldInfo> getFields();
}
