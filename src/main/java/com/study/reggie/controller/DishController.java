package com.study.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.reggie.common.R;
import com.study.reggie.dto.DishDto;
import com.study.reggie.entity.Category;
import com.study.reggie.entity.Dish;
import com.study.reggie.service.CategoryService;
import com.study.reggie.service.DishFlavorService;
import com.study.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

        return R.success("修改菜品成功");
    }
}
