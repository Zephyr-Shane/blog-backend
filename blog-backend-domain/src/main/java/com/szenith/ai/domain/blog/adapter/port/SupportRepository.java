package com.szenith.ai.domain.blog.adapter.port;

import com.szenith.ai.domain.blog.model.valobj.SiteConfig;
import com.szenith.ai.domain.blog.model.entity.User;

import java.util.List;
import java.util.Map;

public interface SupportRepository {

    SiteConfig getSiteConfig();

    SiteConfig updateSiteConfig(Map<String, Object> patch);

    List<User> listUsers();

    User findUserById(Long id);

    User findUserByEmail(String email);

    User saveUser(User user);

    User updateUser(Long id, Map<String, Object> patch);

    boolean deleteUser(Long id);
}
