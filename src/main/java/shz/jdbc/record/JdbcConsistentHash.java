package shz.jdbc.record;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import shz.core.*;
import shz.core.msg.ClientFailureMsg;
import shz.core.msg.ServerFailureMsg;
import shz.core.serializable.SerializableLongPredicate;
import shz.core.serializable.Serializer;
import shz.jdbc.JdbcService;
import shz.jdbc.entity.SysTableNode;
import shz.jdbc.entity.SysTableNodeTransferInfo;
import shz.orm.Tnp;
import shz.orm.annotation.Transactional;
import shz.orm.record.OrmConsistentHash;
import shz.orm.record.OrmConsistentHashRecordEntity;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JdbcConsistentHash<S extends JdbcConsistentHash<S, T>, T extends JdbcConsistentHashRecordEntity<T, S>> extends OrmConsistentHash<JdbcService, T> implements CommandLineRunner {
    @Autowired
    protected JdbcService jdbcService;
    @Autowired
    protected JdbcConsistentHashRepository repository;

    public JdbcConsistentHash(int num) {
        super(num);
    }

    public JdbcConsistentHash() {
    }

    protected Tnp tnp;

    @Override
    public void run(String... args) {
        tnp = jdbcService.nonNullClassInfo(cls).tnp;
        List<SysTableNode> tableNodes = jdbcService.selectListByColumn(SysTableNode.class, "tableName", tnp.tableName);
        if (!tableNodes.isEmpty())
            setNodes(ToSet.explicitCollect(tableNodes.stream().map(SysTableNode::getNode), tableNodes.size()));
    }

    private static final Map<String, Map<String, String>> TABLE_NODE_DS_NAME_CACHE = new ConcurrentHashMap<>();

    @Override
    public final JdbcService service(String node) {
        if (NullHelp.isBlank(node)) return null;
        String dsName = TABLE_NODE_DS_NAME_CACHE.computeIfAbsent(tnp.tableName, k -> new ConcurrentHashMap<>(128)).computeIfAbsent(node, k -> {
            String name = repository.selectDsName(tnp.tableName, node);
            return name == null ? "" : name;
        });
        return jdbcService.getService(dsName);
    }

    public final void removeServiceByNode(String node) {
        if (NullHelp.nonBlank(node)) {
            Map<String, String> map = TABLE_NODE_DS_NAME_CACHE.get(tnp.tableName);
            if (NullHelp.nonEmpty(map)) map.remove(node);
        }
    }

    public final void clearServiceByNode() {
        TABLE_NODE_DS_NAME_CACHE.remove(tnp.tableName);
    }

    @Override
    protected final Set<String> getFailureNodesForDelete() {
        String node = repository.selectFailureNode(tnp.tableName, Boolean.FALSE);
        return node == null ? null : Collections.singleton(node);
    }

    @Override
    protected final void saveDeleteMap(String node, Map<String, SerializableLongPredicate> map) {
        if (NullHelp.isEmpty(map)) return;
        String transferInfo = Serializer.toString(map);
        if (transferInfo == null) return;
        int row = repository.saveTransferInfo(tnp.tableName, node, transferInfo);
        ServerFailureMsg.requireNon(jdbcService.fail(row), "保存删除表:%s,节点:%s转移信息失败", tnp.tableName, node);
    }

    @Override
    protected final Map<String, SerializableLongPredicate> getDeleteMap(String node) {
        if (NullHelp.isBlank(node)) return null;
        String transferInfo = repository.selectTransferInfo(tnp.tableName, node, Boolean.FALSE);
        return NullHelp.isBlank(transferInfo) ? null : Serializer.fromString(transferInfo);
    }

    @Override
    protected final void cancelDeleteMap(String node) {
        if (NullHelp.isBlank(node)) return;
        int row = repository.saveTransferInfo(tnp.tableName, node, "");
        ServerFailureMsg.requireNon(jdbcService.fail(row), "取消删除表:%s,节点:%s转移信息失败", tnp.tableName, node);
    }

    @Override
    protected final void attemptDeleteMsg(String node) {
        if (NullHelp.isBlank(node)) return;
        SysTableNodeTransferInfo transferInfo = repository.selectForAttempt(tnp.tableName);
        boolean insert = transferInfo == null;
        if (insert) {
            transferInfo = new SysTableNodeTransferInfo();
            transferInfo.setTableName(tnp.tableName);
        } else ClientFailureMsg.requireNon(!transferInfo.getFinished(), "表:%s存在未完成的转移节点", tnp.tableName);
        transferInfo.setNode(node);
        transferInfo.setDilatation(Boolean.FALSE);
        transferInfo.setFinished(Boolean.FALSE);

        int row;
        if (insert) row = jdbcService.insert(transferInfo);
        else row = jdbcService.updateById(transferInfo);

        ServerFailureMsg.requireNon(jdbcService.fail(row), "尝试删除表:%s,节点:%s失败", tnp.tableName, node);
    }

    @Override
    protected final void cancelDeleteMsg(String node) {
        if (NullHelp.isBlank(node)) return;
        int row = repository.cancelMsg(tnp.tableName, node);
        ServerFailureMsg.requireNon(jdbcService.fail(row), "取消删除表:%s,节点:%s消息失败", tnp.tableName, node);
        repository.deleteNode(tnp.tableName, node);
        removeServiceByNode(node);
    }

    @Override
    protected final Set<String> getFailureNodesForAdd() {
        String node = repository.selectFailureNode(tnp.tableName, Boolean.TRUE);
        return node == null ? null : Collections.singleton(node);
    }

    @Override
    protected final void saveAddMap(String node, Map<String, SerializableLongPredicate> map) {
        if (NullHelp.isEmpty(map)) return;
        String transferInfo = Serializer.toString(map);
        if (transferInfo == null) return;
        int row = repository.saveTransferInfo(tnp.tableName, node, transferInfo);
        ServerFailureMsg.requireNon(jdbcService.fail(row), "保存新增表:%s,节点:%s转移信息失败", tnp.tableName, node);
    }

    @Override
    protected final Map<String, SerializableLongPredicate> getAddMap(String node) {
        if (NullHelp.isBlank(node)) return null;
        String transferInfo = repository.selectTransferInfo(tnp.tableName, node, Boolean.TRUE);
        return NullHelp.isBlank(transferInfo) ? null : Serializer.fromString(transferInfo);
    }

    @Override
    protected final void cancelAddMap(String node) {
        if (NullHelp.isBlank(node)) return;
        int row = repository.saveTransferInfo(tnp.tableName, node, "");
        ServerFailureMsg.requireNon(jdbcService.fail(row), "取消新增表:%s,节点:%s转移信息失败", tnp.tableName, node);
    }

    @Override
    @Transactional
    protected final void attemptAddMsg(String node) {
        if (NullHelp.isBlank(node)) return;
        SysTableNodeTransferInfo transferInfo = repository.selectForAttempt(tnp.tableName);
        boolean insert = transferInfo == null;
        if (insert) {
            transferInfo = new SysTableNodeTransferInfo();
            transferInfo.setTableName(tnp.tableName);
        } else ClientFailureMsg.requireNon(!transferInfo.getFinished(), "表:%s存在未完成的转移节点", tnp.tableName);
        transferInfo.setNode(node);
        transferInfo.setDilatation(Boolean.TRUE);
        transferInfo.setFinished(Boolean.FALSE);

        int row;
        if (insert) row = jdbcService.insert(transferInfo);
        else row = jdbcService.updateById(transferInfo);

        ServerFailureMsg.requireNon(jdbcService.fail(row), "尝试新增表:%s,节点:%s失败", tnp.tableName, node);

        createTable(node);
    }

    protected void createTable(String node) {
        JdbcService service = service(node);
        if (service == null) {
            removeServiceByNode(node);
            throw PRException.of(ClientFailureMsg.fail("表:%s,节点:%s不存在数据源", tnp.tableName, node));
        }

        InputStream is = createTableInputStream();
        if (is == null) return;

        List<String> sqlList = service.fromIs(is);

        //简单替换,若有冲突请自行替换规则
        String newTableName = OrmConsistentHashRecordEntity.joint(tnp.tableName, node);
        service.executeBatch(0, sqlList.stream().map(sql -> sql.replaceAll(tnp.tableName, newTableName)).toArray(String[]::new));
    }

    protected InputStream createTableInputStream() {
        //如果手动创建表可忽略此方法
        return null;
    }

    @Override
    protected final void cancelAddMsg(String node) {
        if (NullHelp.isBlank(node)) return;
        int row = repository.cancelMsg(tnp.tableName, node);
        ServerFailureMsg.requireNon(jdbcService.fail(row), "取消添加表:%s,节点:%s消息失败", tnp.tableName, node);
    }
}
