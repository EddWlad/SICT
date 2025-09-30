package com.goia.sict_backend.service;


import com.goia.sict_backend.entity.Menu;

import java.util.List;

public interface IMenuService {
    List<Menu> getMenusByUsername(String username);
}
