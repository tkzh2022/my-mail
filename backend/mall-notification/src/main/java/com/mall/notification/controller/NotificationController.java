package com.mall.notification.controller;

import com.mall.common.result.PageResult;
import com.mall.common.result.R;
import com.mall.notification.entity.Notification;
import com.mall.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public R<PageResult<Notification>> list(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return R.ok(notificationService.listByUserId(userId, page, size));
    }

    @GetMapping("/unread-count")
    public R<Long> unreadCount(@RequestHeader("X-User-Id") Long userId) {
        return R.ok(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public R<Void> markAsRead(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        notificationService.markAsRead(userId, id);
        return R.ok();
    }

    @PutMapping("/read-all")
    public R<Void> markAllAsRead(@RequestHeader("X-User-Id") Long userId) {
        notificationService.markAllAsRead(userId);
        return R.ok();
    }
}
