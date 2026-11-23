package com.szenith.ai.infrastructure.dao;

import com.szenith.ai.domain.blog.model.valobj.UploadedResource;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/** 上传资源 Mapper：二进制走 BLOB 参数绑定，无字符串拼接；id 为应用生成的 UUID */
public interface UploadMapper {

    int insert(@Param("row") Map<String, Object> row);

    UploadedResource selectById(@Param("id") String id);

    List<UploadedResource> selectList(@Param("category") String category);

    int deleteById(@Param("id") String id);
}
