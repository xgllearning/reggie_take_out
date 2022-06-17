package com.study.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.reggie.common.R;
import com.study.reggie.dto.DishDto;
import com.study.reggie.entity.Category;
import com.study.reggie.entity.Dish;
import com.study.reggie.entity.DishFlavor;
import com.study.reggie.service.CategoryService;
import com.study.reggie.service.DishFlavorService;
import com.study.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveDishAndFlavor(dishDto);
        //当新增了菜品之后，需要清理缓存中的原先数据，否则会出现脏数据
        //清理方法：全部清理、局部清理
        String key = "dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);

        return R.success("新增菜品成功");
    }
    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<DishDto>> page(String name, int page,int pageSize){
        //问题：采用Dish封装数据不完整，无法响应菜品分类数据categoryName
        //解决方案,采用DishDto
        //1.0构造分页构造器对象
        Page<Dish> dishPage = new Page<>(page, pageSize);
        //2.0构造DishDto分页构造器
        Page<DishDto> dishDtoPage = new Page<>();
        //1.1条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = Wrappers.lambdaQuery(Dish.class);
        //1.2添加过滤条件
        queryWrapper.like(StringUtils.isNotBlank(name),Dish::getName,name);
        //1.3添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //1.4执行分页查询
        dishService.page(dishPage,queryWrapper);
        //1.5此时的dishPage中的数据，records、total、size、current
        //2.1将dishPage拷贝到dishDtoPage，但是要排除records，因为records不完整(此时泛型为Dish)，缺少categoryName
        BeanUtils.copyProperties(dishPage,dishDtoPage,"records");
        //2.2取出dishPage的records进行单独处理,把每个dish中的值赋给dishDto
        List<Dish> records = dishPage.getRecords();
        List<DishDto> list = records.stream().map(s -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(s, dishDto);
            //2.3获取categoryId，根据Id，查询categoryName(返回category)
            Long categoryId = s.getCategoryId();
            Category category = categoryService.getById(categoryId);
            //2.4需要判断是否查询到category
            if (category != null) {
                //2.5获取categoryName
                String categoryName = category.getName();
                //2.6赋值给dishDto
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }
    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){

        return dishService.getByIdWithFlavor(id);
    }
    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> put(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateDishAndFlavor(dishDto);

        //当修改了菜品之后，需要清理缓存中的原先数据，否则会出现脏数据
        //清理方法：全部清理、局部清理
        String key = "dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);

        return R.success("修改菜品成功");
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

        return dishService.status(ids,status);
    }

    /**
     * 单个删除、批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){

        log.info("ids:{}",ids);


        return dishService.delete(ids);
    }

    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//        log.info("dish:{}",dish);
//        //根据dish中的categoryId进行查询菜品名称
//        LambdaQueryWrapper<Dish> queryWrapper = Wrappers.lambdaQuery(Dish.class);
//        //查询条件：status=1且CategoryId相等
//        queryWrapper.eq(Dish::getStatus,1).eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
//        //添加排序条件
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> list = dishService.list(queryWrapper);
//
//        return R.success(list);
//    }
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        log.info("dish:{}",dish);
        //优化：先从缓冲中查询分类数据，如果有则直接返回，如果没有则进行数据库查询
        List<DishDto> dishDtoList=null;
        //先动态定义一个key：categoryId+status,从redis中获取
        String key = "dish_"+dish.getCategoryId()+"_"+dish.getStatus();
        //如果查询出来不为null,说明缓存中存在,则直接返回
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if (dishDtoList!=null){
            return R.success(dishDtoList);
        }
        //此时说明没有查询出来，则进行数据库操作

        //根据dish中的categoryId进行查询菜品名称
        LambdaQueryWrapper<Dish> queryWrapper = Wrappers.lambdaQuery(Dish.class);
        //查询条件：status=1且CategoryId相等
        queryWrapper.eq(Dish::getStatus,1).eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
            List<Dish> list = dishService.list(queryWrapper);//此时查询出来的是dishList
        //根据dish_id查询dish_flavor表中数据
        dishDtoList = list.stream().map(s->{
            DishDto dishDto = new DishDto();
            //对象拷贝，把原dish数据拷贝到dishDto中
            BeanUtils.copyProperties(s,dishDto);
            Long categoryId = s.getCategoryId();
            //根据categoryId查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category!=null){
                //根据category查询categoryName
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //获取当前菜品的id
            Long dishId = s.getId();
            //根据当前菜品Id,去dish_flavor查询口味信息
            LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = Wrappers.lambdaQuery(DishFlavor.class);
            dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(dishFlavorLambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        //将数据库查询出来的数据dishDtoList存储到缓存中
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }


}
