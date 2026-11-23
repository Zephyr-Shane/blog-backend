package com.szenith.ai.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结构（API-CONTRACT v1.0）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private List<T> list;
    private long total;
    private int page;
    private int pageSize;

    public static <T> PageResult<T> of(List<T> all, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(pageSize, 1);
        int from = Math.min((safePage - 1) * safeSize, all.size());
        int to = Math.min(from + safeSize, all.size());
        return PageResult.<T>builder()
                .list(all.subList(from, to))
                .total(all.size())
                .page(safePage)
                .pageSize(safeSize)
                .build();
    }
}
