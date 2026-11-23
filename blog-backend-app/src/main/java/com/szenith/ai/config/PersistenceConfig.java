package com.szenith.ai.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Mapper 扫描配置。
 * 上传资源已改为二进制落库，通过 GET /media/{id} 从数据库直出，
 * 不再使用文件系统目录，也不做 /uploads 静态映射。
 * 前端自带的 /images/*.svg 由前端自身提供，后端不代理、不存储。
 */
@Configuration
@MapperScan("com.szenith.ai.infrastructure.dao")
public class PersistenceConfig {
}
