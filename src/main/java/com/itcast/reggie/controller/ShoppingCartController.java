package com.itcast.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itcast.reggie.common.BaseContext;
import com.itcast.reggie.common.R;
import com.itcast.reggie.entity.ShoppingCart;
import com.itcast.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车
 */
@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("当前购物车数据：{}",shoppingCart);
        //获取并设置当前用户id
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper();
        lqw.eq(ShoppingCart::getUserId,currentId);

        //获取购物车中菜品id，查询如果存在则+1，否则默认为0
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();

        if (dishId!=null){
            //添加的是菜品
            lqw.eq(ShoppingCart::getDishId,dishId);
        }else{
            //添加的是套餐
            lqw.eq(ShoppingCart::getSetmealId,setmealId);
        }

        //查询当前菜品或套餐是否在购物车中
        ShoppingCart cartServiceOne = shoppingCartService.getOne(lqw);
        if(cartServiceOne!=null){
            //存在，在原来数量加1
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number+1);
            shoppingCartService.updateById(cartServiceOne);
        }else{
            //如果不存在，添加到购物车，数量默认为1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }
        return R.success(cartServiceOne);

    }

    /**
     * 查询购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车");
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper();
        lqw.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        lqw.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(lqw);
        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        //SQL:delete from shopping_cart where user_id = ?
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }

    /**
     * 购物车减少菜品
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        // log.info("当前购物车数据：{}",shoppingCart);
        // //获取并设置当前用户id
        // Long currentId = BaseContext.getCurrentId();
        // shoppingCart.setUserId(currentId);
        //
        // LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper();
        // lqw.eq(ShoppingCart::getUserId,currentId);
        //
        // //获取购物车中菜品id
        // Long dishId = shoppingCart.getDishId();
        // Long setmealId = shoppingCart.getSetmealId();
        //
        // if (dishId!=null){
        //     //添加的是菜品
        //     lqw.eq(ShoppingCart::getDishId,dishId);
        // }else{
        //     //添加的是套餐
        //     lqw.eq(ShoppingCart::getSetmealId,setmealId);
        // }
        //
        // //查询当前菜品或套餐是否在购物车中
        // ShoppingCart cartServiceOne = shoppingCartService.getOne(lqw);
        // if(cartServiceOne.getNumber()!=1){
        //     //菜品数量不为1，则直接减1
        //     Integer number = cartServiceOne.getNumber();
        //     cartServiceOne.setNumber(number-1);
        //     shoppingCartService.updateById(cartServiceOne);
        // }else{
        //     //如果数量为1，减掉后清除该菜品
        //     Integer number = cartServiceOne.getNumber();
        //     cartServiceOne.setNumber(number-1);
        //     shoppingCartService.updateById(cartServiceOne);
        //     shoppingCartService.remove(lqw);
        //     return R.success(cartServiceOne);
        // }
        // return R.success(cartServiceOne);
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();

        // 代表数量减少的是菜品数量
        if (dishId != null){
            //通过dishId查出购物车对象
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
            //这里必须要加两个条件，否则会出现用户互相修改对方与自己购物车中相同套餐或者是菜品的数量
            queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
            ShoppingCart cart1 = shoppingCartService.getOne(queryWrapper);
            cart1.setNumber(cart1.getNumber()-1);
            Integer LatestNumber = cart1.getNumber();
            if (LatestNumber > 0){
                //对数据进行更新操作
                shoppingCartService.updateById(cart1);
            }else if(LatestNumber == 0){
                //如果购物车的菜品数量减为0，那么就把菜品从购物车删除
                shoppingCartService.removeById(cart1.getId());
            }else if (LatestNumber < 0){
                return R.error("操作异常");
            }
            return R.success(cart1);
        }
        Long setmealId = shoppingCart.getSetmealId();
        if (setmealId != null){
            //代表是套餐数量减少
            queryWrapper.eq(ShoppingCart::getSetmealId,setmealId).eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
            ShoppingCart cart2 = shoppingCartService.getOne(queryWrapper);
            cart2.setNumber(cart2.getNumber()-1);
            Integer LatestNumber = cart2.getNumber();
            if (LatestNumber > 0){
                //对数据进行更新操作
                shoppingCartService.updateById(cart2);
            }else if(LatestNumber == 0){
                //如果购物车的套餐数量减为0，那么就把套餐从购物车删除
                shoppingCartService.removeById(cart2.getId());
            }else if (LatestNumber < 0){
                return R.error("操作异常");
            }
            return R.success(cart2);
        }
        //如果两个大if判断都进不去
        return R.error("操作异常");

    }
}
