package com.szenith.ai.infrastructure.dao;

import com.szenith.ai.domain.blog.model.valobj.MediaReference;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 媒体引用查询 Mapper：只做只读扫描，参数全部绑定，不做字符串拼接 */
public interface MediaReferenceMapper {

    List<MediaReference> selectByResourceId(@Param("resourceId") String resourceId);
}
