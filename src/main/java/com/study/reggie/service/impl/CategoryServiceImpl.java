package com.study.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.reggie.common.CustomException;
import com.study.reggie.common.R;
import com.study.reggie.entity.Category;
import com.study.reggie.entity.Dish;
import com.study.reggie.entity.Setmeal;
import com.study.reggie.mapper.CategoryMapper;
import com.study.reggie.service.CategoryService;
import com.study.reggie.service.DishService;
import com.study.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    /**
     * 根据id删除分类，删除之前需要进行判断
     * @param id
     */
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;
    @Override
    public R<String> removeById(Long id) {
        //根据id进行删除，删除之前需要判断该category是否包含菜品或者套餐
        LambdaQueryWrapper<Dish> dishWrapper = Wrappers.lambdaQuery(Dish.class);
        dishWrapper.eq(Dish::getCategoryId,id);//查询条件
        int count1 = dishService.count(dishWrapper);
        if (count1>0){
            //代表category包含菜品，抛出自定义异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }
        LambdaQueryWrapper<Setmeal> setmealQuery = Wrappers.lambdaQuery(Setmeal.class);
        setmealQuery.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(setmealQuery);
        if (count2>0){
            //代表category包含套餐，抛出自定义异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

        //都不包含，此时可以删除
        super.removeById(id);//调用父类中IService中的方法
        return R.success("分类信息删除成功");
    }
}
