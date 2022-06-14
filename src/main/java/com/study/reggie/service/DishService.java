package com.study.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.reggie.common.R;
import com.study.reggie.dto.DishDto;
import com.study.reggie.entity.Dish;

public interface DishService extends IService<Dish> {

    void saveDishAndFlavor(DishDto dishDto);

    R<DishDto> getByIdWithFlavor(Long id);

}
