package com.itcast.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itcast.reggie.common.CustomException;
import com.itcast.reggie.dto.SetmealDto;
import com.itcast.reggie.entity.Setmeal;
import com.itcast.reggie.entity.SetmealDish;
import com.itcast.reggie.mapper.SetmealMapper;
import com.itcast.reggie.service.SetmealDishService;
import com.itcast.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    SetmealDishService setmealDishService;

    /**
     * 新增套餐，同时保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐信息，操作Setmeal，执行insert操作
        this.save(setmealDto);

        //获取套餐中的菜品
        List<SetmealDish> setmealDishs = setmealDto.getSetmealDishes();
        setmealDishs.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联关系
        setmealDishService.saveBatch(setmealDishs);
    }

    /**
     * 根据id删除套餐及其与之关联的菜品
     * @param ids
     */
    @Transactional
    @Override
    public void removeWithDish(List<Long> ids) {
        //select count(*) from setmeal where id in(1,2,3) and status = 1
        //查询套餐状态，确定是否可删除
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper();
        lqw.in(Setmeal::getId,ids);
        lqw.eq(Setmeal::getStatus,1);

        int count = this.count(lqw);
        if(count > 0){
            //如果不能删除，抛出一个业务异常
            throw new CustomException("套餐正在售卖，不能删除");
        }

        //如果可以删除，先删除套餐表中的数据 ————setmeal
        this.removeByIds(ids);

        //delete from setmeal_dish where setmeal_id in(1,2,3)
        LambdaQueryWrapper<SetmealDish> dishLqw = new LambdaQueryWrapper<>();
        dishLqw.in(SetmealDish::getSetmealId,ids);
        //删除关系表中的数据 ———— setmeal_dish
        setmealDishService.remove(dishLqw);
    }

    @Override
    public SetmealDto getByIdWithDish(Long id) {
        // 根据id查询setmeal表中的基本信息
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        // 对象拷贝。
        BeanUtils.copyProperties(setmeal, setmealDto);
        // 查询关联表setmeal_dish的菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);
        //设置套餐菜品属性
        setmealDto.setSetmealDishes(setmealDishList);
        return setmealDto;

    }
}
