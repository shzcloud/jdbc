package shz.jdbc.record;

import shz.core.io.FileHelp;
import shz.core.io.IOHelp;
import shz.jdbc.JdbcService;
import shz.jdbc.entity.SysTableNode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RecordTableCreator {
    private static final String MERGE_PLACEHOLDER = "@MERGE_PLACEHOLDER@";
    protected final String tableName;
    protected final int tableNum;
    protected final String dsName;
    protected final int node;

    public RecordTableCreator(String tableName, int tableNum, String dsName, int node) {
        this.tableName = tableName;
        this.tableNum = tableNum;
        this.dsName = dsName;
        this.node = node;
    }

    public void create(JdbcService jdbcService) {
        List<String> list = jdbcService.fromIs(sqlIs());
        for (int i = 0; i < tableNum; ++i) {
            String tableNameX = tableName + "_" + node + "_" + i;
            jdbcService.executeBatch(0, list.stream().map(sql -> sql.replaceAll(tableName, tableNameX)).toArray(String[]::new));
        }
    }

    protected InputStream sqlIs() {
        File file = FileHelp.findFile("**/sql/**/" + tableName + ".sql");
        Objects.requireNonNull(file);
        return IOHelp.newBufferedInputStream(file.toPath());
    }

    public void merge(JdbcService jdbcService) {
        List<String> list = jdbcService.fromIs(mergeIs());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tableNum; ++i) {
            if (i > 0) sb.append(',');
            sb.append(tableName).append('_').append(node).append('_').append(i);
        }
        String s = sb.toString();
        jdbcService.executeBatch(0, list.stream().map(sql -> sql.replaceAll(MERGE_PLACEHOLDER, s)).toArray(String[]::new));
    }

    protected InputStream mergeIs() {
        StringWriter sw = new StringWriter();
        IOHelp.read(
                IOHelp.newBufferedReader(sqlIs()),
                IOHelp.newBufferedWriter(sw),
                (line, bw) -> {
                    if (line.contains("ENGINE = MyISAM")) {
                        line = line.replace("ENGINE = MyISAM", "ENGINE = MRG_MYISAM");
                        line = line.replace("AUTO_INCREMENT = 1 ", "");
                        line = line.substring(0, line.length() - 1);
                        line += " INSERT_METHOD = LAST UNION = (" + MERGE_PLACEHOLDER + ");";
                    }
                    try {
                        bw.write(line);
                        bw.newLine();
                    } catch (IOException ignored) {
                    }
                }
        );
        return IOHelp.newBufferedInputStream(new ByteArrayInputStream(sw.toString().getBytes(StandardCharsets.UTF_8)));
    }

    public final void initTableNode(JdbcService jdbcService) {
        List<SysTableNode> entities = new ArrayList<>(tableNum);
        for (int i = 0; i < tableNum; ++i) {
            SysTableNode entity = new SysTableNode();
            entity.setTableName(tableName);
            entity.setNode(node + "_" + i);
            entity.setDsName(dsName);
            entities.add(entity);
        }
        jdbcService.batchInsertOrUpdate(entities, "tableName", "node");
    }
}
