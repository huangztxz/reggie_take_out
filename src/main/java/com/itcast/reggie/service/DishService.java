package com.itcast.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itcast.reggie.dto.DishDto;
import com.itcast.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    //新增菜品，同时插入菜品对应的口味数据。dish和dishFlavor
    public void saveWithFlavor(DishDto dishDto);

    //根据id来查询对应的菜品信息和口味
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品信息
    void updateWithFlavor(DishDto dishDto);
}
