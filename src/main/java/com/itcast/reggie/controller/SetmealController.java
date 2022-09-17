package com.itcast.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itcast.reggie.common.R;
import com.itcast.reggie.dto.DishDto;
import com.itcast.reggie.dto.SetmealDto;
import com.itcast.reggie.entity.Category;
import com.itcast.reggie.entity.Dish;
import com.itcast.reggie.entity.Setmeal;
import com.itcast.reggie.entity.SetmealDish;
import com.itcast.reggie.service.CategoryService;
import com.itcast.reggie.service.DishService;
import com.itcast.reggie.service.SetmealDishService;
import com.itcast.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishService dishService;
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success("添加菜品成功");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize,String name){
        log.info("开始套餐分页查询....");
        //分页构造对象
        Page<Setmeal> pageInfo = new Page(page,pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper();
        //添加查询条件，根据name模糊查询
        lqw.like(name != null, Setmeal::getName,name);
        //添加排序条件，按照更新时间降序排列
        lqw.orderByAsc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo, lqw);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo, dtoPage);
        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item)->{
            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item, setmealDto);
            //获取套餐分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询对象
            Category category = categoryService.getById(categoryId);
            if(categoryId != null){
                //获取分类名称
                String categoryName = category.getName();
                //把名字赋值给setmealDto对象
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete (@RequestParam List<Long> ids){
        log.info("ids{}",ids);
        setmealService.removeWithDish(ids);
        return R.success("套餐删除成功");
    }

    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable int status,@RequestParam List<Long> ids){
        // 增加日志验证是否接收到前端参数。
        log.info("根据id修改套餐的状态:{},id为：{}", status, ids);
        //修改setmeal表
        UpdateWrapper<Setmeal> updateWrapper = new UpdateWrapper();
        updateWrapper.set("status", status).in("id", ids);
        if (!setmealService.update(updateWrapper)) {
            return R.error("修改状态失败");
        }

        return R.success("修改成功");
    }

    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){
        log.info("根据id查询菜品数据：{}",id);
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    /**
     * 查询套餐菜品
     * @param SetmealId
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<List<DishDto>> dishDto(@PathVariable("id") Long SetmealId){
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,SetmealId);
        //获取套餐里面的所有菜品  这个就是SetmealDish表里面的数据
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        List<DishDto> dishDtos = list.stream().map((setmealDish) -> {
            DishDto dishDto = new DishDto();
            //其实这个BeanUtils的拷贝是浅拷贝，这里要注意一下
            BeanUtils.copyProperties(setmealDish, dishDto);
            //这里是为了把套餐中的菜品的基本信息填充到dto中，比如菜品描述，菜品图片等菜品的基本信息
            Long dishId = setmealDish.getDishId();
            Dish dish = dishService.getById(dishId);
            BeanUtils.copyProperties(dish, dishDto);
            return dishDto;
        }).collect(Collectors.toList());
        return R.success(dishDtos);
    }



    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        //构造查询条件
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper();
        lqw.eq(setmeal.getCategoryId()!=null, Setmeal::getCategoryId,setmeal.getCategoryId());
        //添加条件，查询状态为1（起售状态）
        lqw.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        //添加排序条件
        lqw.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(lqw);
        return R.success(list);
    }




}
