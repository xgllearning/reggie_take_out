package com.study.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.reggie.common.CustomException;
import com.study.reggie.common.R;
import com.study.reggie.dto.SetmealDto;
import com.study.reggie.entity.Dish;
import com.study.reggie.entity.Setmeal;
import com.study.reggie.entity.SetmealDish;
import com.study.reggie.mapper.SetmealMapper;
import com.study.reggie.service.SetmealDishService;
import com.study.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Override
    @Transactional
    public R<String> saveSetmealAndSetmealDish(SetmealDto setmealDto) {
        //先往setmeal表中插入基本信息 保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);
        //再往setmeal_dish表中插入数据,需要注意：setmealId为null,应该先获取再插入
        List<SetmealDish> setmealDishesList = setmealDto.getSetmealDishes();
        setmealDishesList.stream().map(s->{
            s.setSetmealId(setmealDto.getId());
            return s;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishesList);
        return R.success("新增套餐成功");
    }

    /**
     * 停售、起售、批量停售、批量起售
     * @param ids
     * @param status
     * @return
     */
    @Override
    public R<String> status(List<Long> ids, Integer status) {
        UpdateWrapper<Setmeal> updateWrapper = new UpdateWrapper<>();
        if (status==0){
            //需要禁用
            for (Long id : ids) {
                updateWrapper.eq("id",id).set("status",status);
                this.update(updateWrapper);
                updateWrapper.clear();
            }
            return R.success("禁用成功");
        }
        //启用
        for (Long id : ids) {
            updateWrapper.eq("id",id).set("status",status);
            this.update(updateWrapper);
            updateWrapper.clear();
        }
        return R.success("启用成功");
    }

    @Override
    @Transactional
    public R<String> removeSetmealAndDish(List<Long> ids) {
        //select count(*) from setmeal where id in (1,2,3) and status = 1
        //删除主表setmeal和从表setmeal_dish
        //删除之前需要判断是不是禁售状态，只有禁售状态才可以删除，不是禁售状态抛出自定义异常
        LambdaQueryWrapper<Setmeal> queryWrapper = Wrappers.lambdaQuery(Setmeal.class);
        queryWrapper.eq(Setmeal::getStatus,1).in(Setmeal::getId,ids);
        int count = this.count(queryWrapper);
        if (count>0){
            //此时说明查询出来的数据有状态不为0的，所以不能删除
            throw new CustomException("套餐不是禁售状态，无法删除");
        }
        //否则说明可以删除，先删从表再删主表
        //删除从表时，需要根据setmeal_id进行删除
        //delete from setmeal_dish where setmeal_id in (1,2,3)
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = Wrappers.lambdaQuery(SetmealDish.class);
        queryWrapper1.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(queryWrapper1);

        //删除主表
        this.removeByIds(ids);

        return R.success("删除套餐成功");
    }
}
