package com.study.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.reggie.common.R;
import com.study.reggie.dto.SetmealDto;
import com.study.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    R<String> saveSetmealAndSetmealDish(SetmealDto setmealDto);

    R<String> status(List<Long> ids, Integer status);

    R<String> removeSetmealAndDish(List<Long> ids);
}
