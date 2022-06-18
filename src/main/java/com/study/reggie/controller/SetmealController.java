package com.study.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.reggie.common.R;
import com.study.reggie.dto.SetmealDto;
import com.study.reggie.entity.Category;
import com.study.reggie.entity.Setmeal;
import com.study.reggie.service.CategoryService;
import com.study.reggie.service.SetmealDishService;
import com.study.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("/setmeal")
@RestController
public class SetmealController {
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private CategoryService categoryService;
    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)//清理setmealCache下的所有缓存数据
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("setmealDto:{}",setmealDto);

        return setmealService.saveSetmealAndSetmealDish(setmealDto);
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page,int pageSize,String name){
        //分页构造器对象
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> pageDto = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = Wrappers.lambdaQuery(Setmeal.class);
        //分页查询条件
        queryWrapper.like(StringUtils.isNotBlank(name),Setmeal::getName,name);
        //添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo,queryWrapper);//此时查询出分页结果

        //缺少套餐分类字段，category表中的name字段，通过SetmealDto封装
        //因为泛型不一致，所以先把records排除，把其余的属性赋值到pageDto
        BeanUtils.copyProperties(pageInfo,pageDto,"records");
        //单独处理records属性
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> dtoRecords = pageDto.getRecords();
        dtoRecords=records.stream().map(s->{
            SetmealDto setmealDto = new SetmealDto();
            Category category = categoryService.getById(s.getCategoryId());
            if (category!=null){
                //获取分类名称。并设置给setmealDto
                setmealDto.setCategoryName(category.getName());
            }
            //对象拷贝
            BeanUtils.copyProperties(s,setmealDto);
            return setmealDto;
        }).collect(Collectors.toList());
        pageDto.setRecords(dtoRecords);
        return R.success(pageDto);
    }

    /**
     * 停售、起售、批量停售、批量起售
     * @param ids
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")//@PathVariable Integer status,
    public R<String> status(@RequestParam List<Long> ids,@PathVariable Integer status){

        log.info("ids:{}",ids);
        log.info("status:{}",status);

        return setmealService.status(ids,status);
    }


    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)//清理setmealCache下的所有缓存数据
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        return setmealService.removeSetmealAndDish(ids);
    }

    /**
     * 根据条件查询套餐信息,查询时把返回数据放入缓存中
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal){
        log.info("setmeal:{}",setmeal);
        LambdaQueryWrapper<Setmeal> queryWrapper = Wrappers.lambdaQuery(Setmeal.class);
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        return R.success(setmealList);
    }
}
