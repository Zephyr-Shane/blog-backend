package com.szenith.ai.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SupportMapper {

    List<Map<String, Object>> listTags();
    Map<String, Object> findTagById(@Param("id") Long id);
    Map<String, Object> findTagBySlug(@Param("slug") String slug);
    int insertTag(@Param("row") Map<String, Object> row);
    int updateTag(@Param("id") Long id, @Param("sets") String sets, @Param("vals") Map<String, Object> vals);
    int deleteTag(@Param("id") Long id);
    List<Map<String, Object>> findTagsByTargets(@Param("targetType") String targetType, @Param("ids") List<Long> ids);

    List<Map<String, Object>> listComments();
    List<Map<String, Object>> listApprovedComments(@Param("targetType") String targetType, @Param("targetId") Long targetId);
    Map<String, Object> findCommentById(@Param("id") Long id);
    int insertComment(@Param("row") Map<String, Object> row);
    int updateComment(@Param("id") Long id, @Param("sets") String sets, @Param("vals") Map<String, Object> vals);
    int deleteComment(@Param("id") Long id);
    long countCommentsByStatus(@Param("status") String status);
    long countApprovedComments(@Param("targetType") String targetType, @Param("targetId") Long targetId);

    List<Map<String, Object>> listCommentsByStatusSince(
            @Param("status") String status, @Param("since") String since, @Param("limit") int limit);

    List<Map<String, Object>> listRecentApprovedComments(@Param("limit") int limit);

    Map<String, Object> findLike(@Param("visitorId") String visitorId, @Param("targetType") String targetType, @Param("targetId") Long targetId);
    int insertLike(@Param("row") Map<String, Object> row);
    int deleteLike(@Param("visitorId") String visitorId, @Param("targetType") String targetType, @Param("targetId") Long targetId);
    long countLikesByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);

    Map<String, Object> getSiteConfig();
    int updateSiteConfig(@Param("sets") String sets, @Param("vals") Map<String, Object> vals);

    List<Map<String, Object>> listUsers();
    Map<String, Object> findUserById(@Param("id") Long id);
    Map<String, Object> findUserByEmail(@Param("email") String email);
    int insertUser(@Param("row") Map<String, Object> row);
    int updateUser(@Param("id") Long id, @Param("sets") String sets, @Param("vals") Map<String, Object> vals);
    int deleteUser(@Param("id") Long id);
}
