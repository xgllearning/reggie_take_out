package com.study.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.reggie.common.R;
import com.study.reggie.dto.DishDto;
import com.study.reggie.entity.Dish;
import com.study.reggie.entity.DishFlavor;
import com.study.reggie.mapper.DishMapper;
import com.study.reggie.service.DishFlavorService;
import com.study.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
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
    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    @Transactional
    public R<DishDto> getByIdWithFlavor(Long id) {
        //先根据Id查询dish基本信息
        Dish dish = this.getById(id);
        //再去查询DishFlavor
        LambdaQueryWrapper<DishFlavor> queryWrapper = Wrappers.lambdaQuery(DishFlavor.class);
        queryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> flavorList = dishFlavorService.list(queryWrapper);
        //创建DishDto对象，封装数据
        DishDto dishDto = new DishDto();
        //集合copy,把dish基本数据封装进dishDto
        BeanUtils.copyProperties(dish,dishDto);
        dishDto.setFlavors(flavorList);

        return R.success(dishDto);
    }
    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @Override
    @Transactional
    public void updateDishAndFlavor(DishDto dishDto) {
        //先修改dish表
        this.updateById(dishDto);
        //删除flavor表原先数据，再重新插入
        LambdaQueryWrapper<DishFlavor> queryWrapper = Wrappers.lambdaQuery(DishFlavor.class);
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //重新往flavor表中插入数据，但是需要先获取dish_id,添加当前提交过来的口味数据---dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map(s->{
            s.setDishId(dishDto.getId());
            return s;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);

    }


}
