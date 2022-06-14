package com.study.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.reggie.dto.DishDto;
import com.study.reggie.entity.Dish;
import com.study.reggie.entity.DishFlavor;
import com.study.reggie.mapper.DishMapper;
import com.study.reggie.service.DishFlavorService;
import com.study.reggie.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Override
    @Transactional
    public void saveDishAndFlavor(DishDto dishDto) {
        //先保存dish，再保存flavor
        this.save(dishDto);
        //保存完之后，就会产生dishId，获取dishId
        Long dishId = dishDto.getId();//菜品id
        //给flavor每个DishFlavor设置dishId,并收集起来
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors=flavors.stream().map(s->{
            s.setDishId(dishId);
            return s;
        }).collect(Collectors.toList());//收集起来重新作为一个集合，再赋给原对象，否则就是个新对象
        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }
}
