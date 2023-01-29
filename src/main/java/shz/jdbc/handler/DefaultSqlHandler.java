package shz.jdbc.handler;

import shz.jdbc.model.Table;
import shz.orm.Tnp;
import shz.orm.sql.handler.SqlHandler;

import java.io.InputStream;
import java.util.List;

public interface DefaultSqlHandler extends SqlHandler {
    default String multiSqlSep() {
        return "@SEP@PES@";
    }

    String createTable(Table table);

    String dropTable(Tnp tnp);

    String copyTableStructure(Tnp src, Tnp des);

    String createDatabase(String database);

    String dropDatabase(String database);

    String backupDatabase(String disk, String database);

    String restoreDatabase(String disk, String database);

    List<String> fromIs(InputStream is);
}
