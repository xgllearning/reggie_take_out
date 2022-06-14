package com.study.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.reggie.common.R;
import com.study.reggie.dto.SetmealDto;
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
}
