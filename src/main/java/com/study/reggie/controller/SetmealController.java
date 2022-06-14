package com.study.reggie.controller;

import com.study.reggie.common.R;
import com.study.reggie.dto.SetmealDto;
import com.study.reggie.service.SetmealDishService;
import com.study.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/setmeal")
@RestController
public class SetmealController {
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private SetmealService setmealService;
    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @PostMapping
    private R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("setmealDto:{}",setmealDto);

        return setmealService.saveSetmealAndSetmealDish(setmealDto);
    }

}
