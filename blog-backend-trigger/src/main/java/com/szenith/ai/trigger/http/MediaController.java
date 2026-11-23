package com.szenith.ai.trigger.http;

import com.szenith.ai.domain.blog.adapter.port.UploadPort;
import com.szenith.ai.domain.blog.model.valobj.UploadedResource;
import com.szenith.ai.types.enums.ResponseCode;
import com.szenith.ai.types.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 上传资源直出接口（公开）。
 * 二进制来自数据库 blog_upload.data，不依赖文件系统，
 * 因此多实例部署、容器重启都不会丢资源。
 */
@RestController
@RequiredArgsConstructor
public class MediaController {

    private final UploadPort uploadPort;

    @GetMapping("/media/{id}")
    public ResponseEntity<byte[]> media(@PathVariable String id) {
        UploadedResource r = uploadPort.findById(id)
                .orElseThrow(() -> new AppException(ResponseCode.NOT_FOUND, "资源不存在"));
        MediaType type;
        try {
            type = MediaType.parseMediaType(r.getMimeType());
        } catch (Exception e) {
            type = MediaType.APPLICATION_OCTET_STREAM;
        }
        return ResponseEntity.ok()
                .contentType(type)
                .contentLength(r.getData().length)
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(r.getData());
    }
}
