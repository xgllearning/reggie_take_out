package com.study.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.reggie.common.R;
import com.study.reggie.entity.Category;

public interface CategoryService extends IService<Category> {
    /**
     * 自定义根据id删除分类
     * @param id
     * @return
     */
    public R<String> removeById(Long id);
}
