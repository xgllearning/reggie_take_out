package com.study.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.reggie.common.R;
import com.study.reggie.dto.DishDto;
import com.study.reggie.entity.Dish;
import com.study.reggie.service.DishFlavorService;
import com.study.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishService dishService;
    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveDishAndFlavor(dishDto);

        return R.success("新增菜品成功");
    }

}
