package com.study.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.reggie.common.R;
import com.study.reggie.dto.SetmealDto;
import com.study.reggie.entity.Setmeal;

public interface SetmealService extends IService<Setmeal> {

    R<String> saveSetmealAndSetmealDish(SetmealDto setmealDto);
}
