package com.itcast.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itcast.reggie.dto.SetmealDto;
import com.itcast.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void saveWithDish(SetmealDto setmealDto);

    void removeWithDish(List<Long> ids);

    SetmealDto getByIdWithDish(Long id);
}
