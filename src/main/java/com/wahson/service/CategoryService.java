package com.wahson.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wahson.entity.Category;

public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
