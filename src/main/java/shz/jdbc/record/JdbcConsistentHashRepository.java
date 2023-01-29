package shz.jdbc.record;

import shz.jdbc.entity.SysTableNodeTransferInfo;
import shz.orm.annotation.*;
import shz.orm.enums.ValueStrategy;

@Repository
public interface JdbcConsistentHashRepository {
    @Query("SELECT ds_id " +
            "FROM sys_table_node " +
            "WHERE table_name = :tableName " +
            "AND node = :node"
    )
    Long selectDsId(String tableName, String node);

    @Fields({"id", "finished"})
    SysTableNodeTransferInfo selectForAttempt(String tableName);

    @Update("UPDATE sys_table_node_transfer_info " +
            "SET finished = 1 " +
            "WHERE table_name = :tableName " +
            "AND node = :node"
    )
    int cancelMsg(String tableName, String node);

    @Query("SELECT node " +
            "FROM sys_table_node_transfer_info " +
            "WHERE table_name = :tableName " +
            "AND dilatation = :dilatation " +
            "AND finished = 0"
    )
    String selectFailureNode(String tableName, Boolean dilatation);

    @Update("UPDATE sys_table_node_transfer_info " +
            "SET transfer_info = :transferInfo " +
            "WHERE table_name = :tableName " +
            "AND node = :node"
    )
    int saveTransferInfo(String tableName, String node, @Param(value = "transferInfo", strategy = ValueStrategy.NOT_NULL) String transferInfo);

    @Query("SELECT transfer_info " +
            "FROM sys_table_node_transfer_info " +
            "WHERE table_name = :tableName " +
            "AND node = :node " +
            "AND dilatation = :dilatation"
    )
    String selectTransferInfo(String tableName, String node, Boolean dilatation);

    @Delete("DELETE FROM sys_table_node " +
            "WHERE table_name = :tableName " +
            "AND node = :node")
    int deleteNode(String tableName, String node);
}
