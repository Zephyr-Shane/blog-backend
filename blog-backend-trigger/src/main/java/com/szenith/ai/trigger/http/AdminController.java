package com.szenith.ai.trigger.http;

import com.szenith.ai.api.response.Response;
import com.szenith.ai.domain.blog.adapter.port.UploadPort;
import com.szenith.ai.domain.blog.adapter.port.ModerationRulesProvider;
import com.szenith.ai.domain.blog.service.AdminResourceService;
import com.szenith.ai.domain.blog.service.AuthService;
import com.szenith.ai.domain.blog.service.CommentModerationService;
import com.szenith.ai.domain.blog.service.MediaLibraryService;
import com.szenith.ai.domain.blog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 后台管理接口：需 Bearer Token（登录除外，见 AdminAuthInterceptor）。
 * 通用 CRUD 走 /admin/{resource}，由 AdminResourceService 策略分发。
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminResourceService resources;
    private final AuthService auth;
    private final UploadPort uploadPort;
    private final MediaLibraryService mediaLibrary;
    private final NotificationService notifications;
    private final CommentModerationService moderation;
    private final ModerationRulesProvider moderationRules;

    @PostMapping("/auth/login")
    public Response<Map<String, Object>> login(@RequestBody Map<String, Object> body) {
        String email = body.get("email") == null ? null : body.get("email").toString();
        String password = body.get("password") == null ? null : body.get("password").toString();
        return Response.ok(auth.login(email, password));
    }

    @GetMapping("/stats")
    public Response<Map<String, Object>> stats() {
        return Response.ok(resources.stats());
    }

    // ============ 通知中心（精确路径，优先于底部 /{resource} 通配） ============

    @GetMapping("/notifications")
    public Response<?> listNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "all") String status) {
        return Response.ok(notifications.list(page, pageSize, status));
    }

    @GetMapping("/notifications/unread-count")
    public Response<Map<String, Object>> unreadCount() {
        return Response.ok(notifications.unreadCount());
    }

    @PatchMapping("/notifications/{id}/read")
    public Response<?> markRead(@PathVariable String id) {
        return Response.ok(notifications.markRead(id));
    }

    @PatchMapping("/notifications/read-all")
    public Response<Map<String, Object>> markAllRead() {
        return Response.ok(notifications.markAllRead());
    }

    @GetMapping("/site-config")
    public Response<?> getSiteConfig() {
        return Response.ok(resources.getSiteConfig());
    }

    @PutMapping("/site-config")
    public Response<?> updateSiteConfig(@RequestBody Map<String, Object> body) {
        return Response.ok(resources.updateSiteConfig(body));
    }

    /** 评论审核：复数路径与前端一致 */
    @PatchMapping("/comments/{id}/review")
    public Response<?> reviewComment(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String status = body.get("status") == null ? null : body.get("status").toString();
        return Response.ok(resources.reviewComment(id, status));
    }

    /** 手动触发一次评论批量校验（定时任务之外，方便作者立即清理） */
    @PostMapping("/comments/moderate")
    public Response<?> moderateComments() {
        return Response.ok(moderation.scan(moderationRules.rules()));
    }

    /**
     * 图片上传：二进制落库，category 区分上传形式。
     * 响应 url 为可直接访问的地址（指向 GET /media/{id}，从数据库直出）。
     */
    @PostMapping("/upload")
    public Response<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", required = false, defaultValue = "other") String category) {
        try {
            return Response.ok(uploadPort.save(category, file.getOriginalFilename(), file.getContentType(),
                    file.getSize(), file.getBytes()).toMeta());
        } catch (IOException e) {
            return Response.fail(500, "文件读取失败");
        }
    }

    /** 媒体库列表：可按 category 过滤，只返回元数据不含二进制 */
    @GetMapping("/media")
    public Response<?> listMedia(@RequestParam(value = "category", required = false) String category) {
        return Response.ok(mediaLibrary.list(category));
    }

    /** 查看资源被哪些内容引用（删除前提示用） */
    @GetMapping("/media/{id}/references")
    public Response<?> mediaReferences(@PathVariable String id) {
        return Response.ok(mediaLibrary.findReferences(id));
    }

    /** 删除上传资源：先校验引用，被引用则 409 拒绝 */
    @DeleteMapping("/media/{id}")
    public Response<Map<String, Object>> deleteMedia(@PathVariable String id) {
        mediaLibrary.delete(id);
        return Response.ok(Map.of("id", id));
    }

    @GetMapping("/{resource}")
    public Response<?> list(@PathVariable String resource) {
        return Response.ok(resources.list(resource));
    }

    @GetMapping("/{resource}/{id}")
    public Response<?> get(@PathVariable String resource, @PathVariable Long id) {
        return Response.ok(resources.get(resource, id));
    }

    @PostMapping("/{resource}")
    public Response<?> create(@PathVariable String resource, @RequestBody Map<String, Object> body) {
        return Response.ok(resources.create(resource, body));
    }

    /** PUT 采用部分更新语义，避免前端只提交表单字段时清空数据（P1-4） */
    @PutMapping("/{resource}/{id}")
    public Response<?> update(@PathVariable String resource, @PathVariable Long id,
                              @RequestBody Map<String, Object> body) {
        return Response.ok(resources.update(resource, id, body));
    }

    @DeleteMapping("/{resource}/{id}")
    public Response<Map<String, Object>> delete(@PathVariable String resource, @PathVariable Long id) {
        resources.delete(resource, id);
        return Response.ok(Map.of("id", id));
    }
}
