package hbnu.project.zhiyancommonseata.adapter;

import hbnu.project.zhiyancommonseata.exception.SeataException;
import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MongoDB TCC 事务适配器
 * 将 MongoDB 操作适配到 Seata TCC 模式
 *
 * @author ErgouTree
 */
@Slf4j
@Component
@LocalTCC
public class MongoTccTransactionAdapter {

    private final MongoTemplate mongoTemplate;

    public MongoTccTransactionAdapter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    /**
     * Try 阶段：预提交 MongoDB 数据
     * 将数据标记为"待确认"状态
     *
     * @param collectionName 集合名称
     * @param document 文档数据
     * @param businessKey 业务主键
     * @return 是否成功
     */
    @TwoPhaseBusinessAction(
            name = "mongoInsert",
            commitMethod = "commitInsert",
            rollbackMethod = "rollbackInsert"
    )
    public boolean prepareInsert(
            @BusinessActionContextParameter(paramName = "collectionName") String collectionName,
            @BusinessActionContextParameter(paramName = "document") Map<String, Object> document,
            @BusinessActionContextParameter(paramName = "businessKey") String businessKey
    ){
        try{
            // 添加事务状态标记
            document.put("_txStatus", "PREPARE");
            document.put("_businessKey", businessKey);

            mongoTemplate.insert(document, collectionName);
            log.info("MongoDB Try 阶段成功: collection={}, key={}", collectionName, businessKey);
            return true;
        }catch (SeataException e){
            log.error("MongoDB Try 阶段失败", e);
            return false;
        }
    }


    /**
     * Confirm 阶段：确认提交
     * 将状态从"待确认"改为"已提交"
     */
    public boolean commitInsert(BusinessActionContext context) {
        String collectionName = (String) context.getActionContext("collectionName");
        String documentId = (String) context.getActionContext("documentId");
        String businessKey = (String) context.getActionContext("businessKey");

        try{
            // 更新状态为已提交
            mongoTemplate.updateFirst(
                    org.springframework.data.mongodb.core.query.Query.query(
                            org.springframework.data.mongodb.core.query.Criteria
                                    .where("_businessKey").is(businessKey)
                                    .and("_txStatus").is("PREPARE")
                    ),
                    org.springframework.data.mongodb.core.query.Update.update("_txStatus", "COMMITTED"),
                    collectionName
            );

            log.info("MongoDB Confirm 阶段成功: collection={}, key={}", collectionName, businessKey);
            return true;
        }catch (SeataException e){
            log.error("MongoDB Confirm 阶段失败", e);
            return false;
        }
    }



    /**
     * Cancel 阶段：回滚操作
     * 删除预提交的数据
     */
    public boolean rollbackInsert(BusinessActionContext context) {
        String collectionName = (String) context.getActionContext("collectionName");
        String documentId = (String) context.getActionContext("documentId");
        String businessKey = (String) context.getActionContext("businessKey");

        try{
            // 删除预提交的数据
            assert collectionName != null;
            mongoTemplate.remove(
                    org.springframework.data.mongodb.core.query.Query.query(
                            org.springframework.data.mongodb.core.query.Criteria
                                    .where("_businessKey").is(businessKey)
                                    .and("_txStatus").is("PREPARE")
                    ),
                    collectionName
            );

            log.info("MongoDB Cancel 阶段成功: collection={}, key={}", collectionName, businessKey);
            return true;
        }catch (SeataException e){
            log.error("MongoDB Cancel 阶段失败", e);
            return false;
        }
    }
}
