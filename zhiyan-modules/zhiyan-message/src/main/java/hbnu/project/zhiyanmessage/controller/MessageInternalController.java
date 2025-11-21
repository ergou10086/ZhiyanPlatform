package hbnu.project.zhiyanmessage.controller;

import hbnu.project.zhiyancommonbasic.domain.R;


import hbnu.project.zhiyanmessage.model.entity.Message;
import hbnu.project.zhiyanmessage.model.enums.MessageScene;
import hbnu.project.zhiyanmessage.model.pojo.SendMessageRequestPOJO;
import hbnu.project.zhiyanmessage.service.InboxMessageService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 消息模块内部接口
 * 供其他微服务通过Feign调用
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequestMapping("/zhiyan/message/internal")
@RequiredArgsConstructor
public class MessageInternalController {

    @Resource
    private final InboxMessageService inboxMessageService;

    /**
     * 发送个人消息(单收件人)
     * 供其他微服务调用
     */
    @PostMapping("/send/personal")
    public R<Void> sendPersonalMessage(@RequestBody SendMessageRequestPOJO request) {
        try{
            // 参数验证
            if (request.getScene() == null || request.getReceiverId() == null || request.getTitle() == null || request.getContent() == null) {
                return R.fail("参数不完整");
            }

            MessageScene scene;
            try{
                scene = MessageScene.valueOf(request.getScene());
            }catch (IllegalArgumentException e){
                log.warn("无效的消息场景: {}", request.getScene());
                return R.fail("无效的消息场景");
            }

            // 调用消息发送
            Message messageBody = inboxMessageService.sendPersonalMessage(
                    scene,
                    request.getSenderId(),
                    request.getReceiverId(),
                    request.getTitle(),
                    request.getContent(),
                    request.getBusinessId(),
                    request.getBusinessType(),
                    request.getExtendData()
            );

            log.info("内部接口发送个人消息成功: scene={}, receiverId={}, messageId={}",
                    request.getScene(), request.getReceiverId(), messageBody.getId());

            return R.ok(null, "消息发送成功");
        }catch (Exception e){
            log.error("内部接口发送个人消息失败: scene={}, receiverId={}",
                    request.getScene(), request.getReceiverId(), e);
            return R.fail("消息发送失败: " + e.getMessage());
        }
    }


    /**
     * 群发消息
     * 供其他微服务调用
     */
    @PostMapping("/send/batch")
    public R<Void> sendBatchMessage(@RequestBody SendMessageRequestPOJO request) {
        try{
            // 参数验证
            if(request.getScene() == null || request.getReceiverIds() == null ||
                    request.getReceiverIds().isEmpty() || request.getTitle() == null ||
                    request.getContent() == null) {
                log.warn("发送批量消息参数不完整: scene={}, receiverCount={}",
                        request.getScene(),
                        request.getReceiverIds() != null ? request.getReceiverIds().size() : 0);
                return R.fail("参数不完整");
            }

            MessageScene scene;
            try{
                scene = MessageScene.valueOf(request.getScene());
            }catch (IllegalArgumentException e){
                log.warn("无效的消息场景: {}", request.getScene());
                return R.fail("无效的消息场景");
            }

            // 调用群发
            Message messageBody = inboxMessageService.sendBatchPersonalMessage(
                    scene,
                    request.getSenderId(),
                    request.getReceiverIds(),
                    request.getTitle(),
                    request.getContent(),
                    request.getBusinessId(),
                    request.getBusinessType(),
                    request.getExtendData()
            );

            log.info("内部接口发送个人消息成功: scene={}, receiverId={}, messageId={}",
                    request.getScene(), request.getReceiverId(), messageBody.getId());

            return R.ok(null, "消息发送成功");
        }catch (Exception e){
            log.error("内部接口发送批量消息失败: scene={}, receiverCount={}",
                    request.getScene(),
                    request.getReceiverIds() != null ? request.getReceiverIds().size() : 0, e);

            return R.fail("消息批量发送失败: " + e.getMessage());
        }
    }


    /**
     * 发送广播消息(全体用户)
     * 供其他微服务调用
     */
    @PostMapping("/send/broadcast")
    public R<Void> sendBroadcastMessage(@RequestBody SendMessageRequestPOJO request) {
        try{
            // 参数验证
            if (request.getScene() == null || request.getTitle() == null || request.getContent() == null) {
                return R.fail("参数不完整");
            }

            MessageScene scene;
            try {
                scene = MessageScene.valueOf(request.getScene());
            } catch (IllegalArgumentException e) {
                log.warn("无效的消息场景: {}", request.getScene());
                return R.fail("无效的消息场景");
            }

            // 调用服务发送广播消息
            Message messageBody = inboxMessageService.sendAllPersonalMessage(
                    scene,
                    request.getSenderId(),
                    request.getTitle(),
                    request.getContent(),
                    request.getExtendData()
            );

            log.info("内部接口发送广播消息成功: scene={}, messageId={}",
                    request.getScene(), messageBody.getId());

            return R.ok(null, "广播消息发送成功");
        }catch (Exception e){
            log.error("内部接口发送广播消息失败: scene={}", request.getScene(), e);
            return R.fail("广播消息发送失败: " + e.getMessage());
        }
    }


    /**
     * 批量发送个人消息（多收件人）
     * 供其他微服务调用
     */
    @PostMapping("/send/batch-personal")
    public R<Void> sendBatchMessageByPersonal(@RequestBody SendMessageRequestPOJO request) {
        try{
            // 参数验证
            if(request.getScene() == null || request.getReceiverIds() == null ||
                    request.getReceiverIds().isEmpty() || request.getTitle() == null ||
                    request.getContent() == null) {
                log.warn("发送批量消息参数不完整: scene={}, receiverCount={}",
                        request.getScene(),
                        request.getReceiverIds() != null ? request.getReceiverIds().size() : 0);
                return R.fail("参数不完整");
            }

            MessageScene scene;
            try{
                scene = MessageScene.valueOf(request.getScene());
            }catch (IllegalArgumentException e){
                log.warn("无效的消息场景: {}", request.getScene());
                return R.fail("无效的消息场景");
            }

            // 调用批量发送（遍历每个接收者发送消息）
            int successCount = 0;
            int failCount = 0;
            List<Long> receiverIds = request.getReceiverIds();

            for (Long receiverId : receiverIds) {
                try {
                    Message messageBody = inboxMessageService.sendPersonalMessage(
                            scene,
                            request.getSenderId(),
                            receiverId,
                            request.getTitle(),
                            request.getContent(),
                            request.getBusinessId(),
                            request.getBusinessType(),
                            request.getExtendData()
                    );
                    successCount++;
                    log.debug("批量消息发送成功: receiverId={}, messageId={}", receiverId, messageBody.getId());
                } catch (Exception e) {
                    failCount++;
                    log.error("批量消息发送失败: receiverId={}", receiverId, e);
                    // 继续发送给其他人，不中断
                }
            }

            log.info("内部接口批量发送消息完成: scene={}, 总数={}, 成功={}, 失败={}",
                    request.getScene(), receiverIds.size(), successCount, failCount);

            if (failCount == 0) {
                return R.ok(null, "批量消息发送成功");
            } else if (successCount > 0) {
                return R.ok(null, String.format("批量消息部分成功: 成功%d个，失败%d个", successCount, failCount));
            } else {
                return R.fail("批量消息发送全部失败");
            }
        }catch (Exception e){
            log.error("内部接口发送批量消息异常: scene={}, receiverCount={}",
                    request.getScene(),
                    request.getReceiverIds() != null ? request.getReceiverIds().size() : 0, e);

            return R.fail("消息批量发送失败: " + e.getMessage());
        }
    }
}
