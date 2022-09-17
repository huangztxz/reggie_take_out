package com.itcast.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itcast.reggie.dto.DishDto;
import com.itcast.reggie.entity.Dish;
import com.itcast.reggie.entity.DishFlavor;
import com.itcast.reggie.mapper.DishMapper;
import com.itcast.reggie.service.DishFlavorService;
import com.itcast.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        Long dishId = dishDto.getId();

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        // flavors.stream().map(item-> {
        //     item.setDishId(dishId);
        //     return item;
        // }).collect(Collectors.toList());
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
        }

        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据id来查询对应的菜品信息和口味
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //1.查询菜品基本信息
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        //2.查询菜品对应的口味信息
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(lqw);

        dishDto.setFlavors(flavors);
        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新Dish表基本信息
        this.updateById(dishDto);

        //清理当前菜品的口味信息 ————dish_flavor表的DELETE操作
        Long dishId = dishDto.getId();
        LambdaQueryWrapper<DishFlavor> lqw = new LambdaQueryWrapper();

        lqw.eq(DishFlavor::getDishId, dishId);
        dishFlavorService.remove(lqw);

        //重新插入当前菜品的口味信息 ————dish_flavor表的INSERT操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item)-> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);

        // for (DishFlavor flavor : flavors) {
        //     flavor.setDishId(dishId);
        // }
    }
}