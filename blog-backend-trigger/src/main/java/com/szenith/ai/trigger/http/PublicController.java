package com.szenith.ai.trigger.http;

import com.szenith.ai.api.response.PageResult;
import com.szenith.ai.api.response.Response;
import com.szenith.ai.domain.blog.adapter.port.SupportRepository;
import com.szenith.ai.domain.blog.model.valobj.FeedItem;
import com.szenith.ai.domain.blog.service.ContentQueryService;
import com.szenith.ai.domain.blog.service.InteractionService;
import com.szenith.ai.domain.blog.service.SiteOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 前台公开接口：无需鉴权，全部遵循 API-CONTRACT v1.0。
 * 路径与前端 queries.ts / mock route 一一对应。
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class PublicController {

    private final ContentQueryService query;
    private final InteractionService interaction;
    private final SupportRepository support;
    private final SiteOverviewService overview;

    @GetMapping("/feed")
    public Response<PageResult<FeedItem>> feed(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize) {
        return Response.ok(PageResult.of(query.getFeed(), page, pageSize));
    }

    @GetMapping("/posts")
    public Response<PageResult<?>> posts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String tag) {
        return Response.ok(PageResult.of(query.listPosts(tag), page, pageSize));
    }

    @GetMapping("/posts/{slug}")
    public Response<?> postDetail(@PathVariable String slug) {
        Object post = query.findPostBySlug(slug);
        if (post == null) {
            return Response.fail(404, "文章不存在");
        }
        return Response.ok(post);
    }

    @GetMapping("/moments")
    public Response<PageResult<?>> moments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String mood) {
        return Response.ok(PageResult.of(query.listMoments(mood), page, pageSize));
    }

    @GetMapping("/interviews")
    public Response<PageResult<?>> interviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String kind) {
        return Response.ok(PageResult.of(query.listInterviews(kind), page, pageSize));
    }

    @GetMapping("/links")
    public Response<PageResult<?>> links(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category) {
        return Response.ok(PageResult.of(query.listLinks(category), page, pageSize));
    }

    @GetMapping("/tags")
    public Response<List<?>> tags() {
        return Response.ok(query.getTags());
    }

    @GetMapping("/tags/{slug}/contents")
    public Response<Map<String, List<? extends com.szenith.ai.domain.blog.model.entity.ContentItem>>> tagContents(
            @PathVariable String slug) {
        return Response.ok(query.getTagContents(slug));
    }

    @GetMapping("/search")
    public Response<Map<String, List<? extends com.szenith.ai.domain.blog.model.entity.ContentItem>>> search(
            @RequestParam(defaultValue = "") String q) {
        return Response.ok(query.search(q));
    }

    @GetMapping("/site-config")
    public Response<?> siteConfig() {
        return Response.ok(support.getSiteConfig());
    }

    /** 首页侧栏聚合：公告 + 真实统计 + 常用标签 + 最新评论，一次请求返回 */
    @GetMapping("/site-overview")
    public Response<com.szenith.ai.domain.blog.model.valobj.SiteOverview> siteOverview() {
        return Response.ok(overview.overview());
    }

    @GetMapping("/comments")
    public Response<List<com.szenith.ai.domain.blog.model.entity.Comment>> comments(
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        return Response.ok(interaction.listComments(targetType, targetId));
    }

    @PostMapping("/comments")
    public Response<com.szenith.ai.domain.blog.model.entity.Comment> postComment(
            @RequestBody Map<String, Object> body) {
        return Response.ok(interaction.postComment(body));
    }

    @PostMapping("/likes")
    public Response<Map<String, Object>> toggleLike(@RequestBody Map<String, Object> body) {
        return Response.ok(interaction.toggleLike(body));
    }

    /** 当前访客点赞状态 + 真实计数（GET，参数放在 query） */
    @GetMapping("/likes/state")
    public Response<Map<String, Object>> likeState(
            @RequestParam(required = false) String visitorId,
            @RequestParam String targetType,
            @RequestParam Long targetId) {
        return Response.ok(interaction.likeState(visitorId, targetType, targetId));
    }
}
