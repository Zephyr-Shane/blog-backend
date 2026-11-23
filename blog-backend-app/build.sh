#!/bin/bash
set -e

# 构建后端镜像并加载到本地，供 push.sh 打标签推送。
# 关键：关闭 provenance / sbom。新版 BuildKit 默认生成 OCI attestation 清单
# （application/vnd.oci.empty.v1+json），阿里云 ACR 个人版不支持，会在 push 时
# 报 "unknown manifest class for application/vnd.oci.empty.v1+json"。两个参数缺一不可。
# 单平台 linux/amd64 与 --load 搭配，避免多架构清单再次触发该问题。
docker buildx build --platform linux/amd64 \
  --provenance=false \
  --sbom=false \
  --load -t shenzhien/zephyr-blog-backend:1.0 -f ./Dockerfile .
