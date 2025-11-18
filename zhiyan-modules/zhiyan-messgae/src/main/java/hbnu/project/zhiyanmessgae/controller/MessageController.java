package hbnu.project.zhiyanmessgae.controller;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyanmessgae.model.converter.MessageConverter;
import hbnu.project.zhiyanmessgae.model.dto.MessageListDTO;
import hbnu.project.zhiyanmessgae.model.entity.MessageRecipient;
import hbnu.project.zhiyanmessgae.service.InboxMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 站内消息 Controller
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequestMapping("/zhiyan/message")
@RequiredArgsConstructor
public class MessageController {

    private final InboxMessageService inboxMessageService;


    /**
     * 获取消息列表
     */
    @GetMapping("/list")
    public R<Page<MessageListDTO>> getMessageList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getUserId();
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "triggerTime"));

        Page<MessageRecipient> recipientPage = inboxMessageService.pageInbox(userId, pageable);
        Page<MessageListDTO> dtoPage = recipientPage.map(MessageConverter.INSTANCE::toListDTO);

        return R.ok(dtoPage);
    }

    /**
     * 获取未读消息列表（分页）
     */
    @GetMapping("/unread")
    public R<Page<MessageListDTO>> getUnreadMessageList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getUserId();
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "triggerTime"));

        Page<MessageRecipient> recipientPage = inboxMessageService.pageUnread(userId, pageable);
        Page<MessageListDTO> dtoPage = recipientPage.map(MessageConverter.INSTANCE::toListDTO);

        return R.ok(dtoPage);
    }


    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread/count")
    public R<Long> getUnreadCount() {
        Long userId = SecurityUtils.getUserId();
        long count = inboxMessageService.countUnread(userId);
        return R.ok(count);
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/read/{recipientId}")
    public R<Void> markAsRead(@PathVariable Long recipientId) {
        Long userId = SecurityUtils.getUserId();
        inboxMessageService.markAsRead(userId, recipientId);
        return R.ok();
    }

    /**
     * 全部标记为已读
     */
    @PutMapping("/read/all")
    public R<Void> markAllAsRead() {
        Long userId = SecurityUtils.getUserId();
        inboxMessageService.markAllAsRead(userId);
        return R.ok();
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/{recipientId}")
    public R<Void> deleteMessage(@PathVariable Long recipientId) {
        Long userId = SecurityUtils.getUserId();
        inboxMessageService.deleteMessage(userId, recipientId);
        return R.ok();
    }

    /**
     * 清空所有消息
     */
    @DeleteMapping("/clear")
    public R<Void> clearAll() {
        Long userId = SecurityUtils.getUserId();
        inboxMessageService.clearAll(userId);
        return R.ok();
    }
}
