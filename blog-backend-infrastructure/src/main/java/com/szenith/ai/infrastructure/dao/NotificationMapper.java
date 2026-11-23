package com.szenith.ai.infrastructure.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface NotificationMapper {
    int insert(@Param("row") Map<String, Object> row);
    List<Map<String, Object>> list(@Param("offset") int offset, @Param("size") int size, @Param("status") String status);
    long countAll(@Param("status") String status);
    long countUnread();
    Map<String, Object> findById(@Param("id") String id);
    int markRead(@Param("id") String id);
    int markAllRead();
}
