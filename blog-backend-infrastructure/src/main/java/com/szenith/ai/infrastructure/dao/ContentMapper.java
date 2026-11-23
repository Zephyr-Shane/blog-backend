package com.szenith.ai.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 通用内容 Mapper：以 tableName 白名单区分四类内容，避免四套重复接口/SQL。
 * tableName 只能取 blog_post/blog_moment/blog_interview/blog_link（由调用方白名单控制）。
 */
@Mapper
public interface ContentMapper {

    List<Map<String, Object>> listPublished(@Param("table") String table);

    List<Map<String, Object>> listAll(@Param("table") String table);

    Map<String, Object> findById(@Param("table") String table, @Param("id") Long id);

    Map<String, Object> findBySlug(@Param("slug") String slug);

    int insert(@Param("table") String table, @Param("row") Map<String, Object> row);

    /** 部分更新：sets 为白名单占位符子句，vals 承载占位符值 */
    int update(@Param("table") String table, @Param("id") Long id,
               @Param("sets") String sets, @Param("vals") Map<String, Object> vals);

    int delete(@Param("table") String table, @Param("id") Long id);

    long countAll(@Param("table") String table);

    long countByStatus(@Param("table") String table, @Param("status") String status);
}
