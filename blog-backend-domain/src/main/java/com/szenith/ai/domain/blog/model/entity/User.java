package com.szenith.ai.domain.blog.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String email;
    private String name;
    private String role;
    /** 仅用于校验，不对外输出 */
    @JsonIgnore
    private String passwordHash;
}
