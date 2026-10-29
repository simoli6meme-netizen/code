package org.my.work.code2026.service;

import cn.hutool.core.util.ObjectUtil;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.my.work.code2026.entity.Account;
import org.my.work.code2026.entity.Cart;
import org.my.work.code2026.exception.CustomException;
import org.my.work.code2026.mapper.CartMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理员业务处理
 **/
@Service
public class CartService {

    @Resource
    private CartMapper cartMapper;

    /**
     * 新增
     */
    public void add(Cart cart) {
        Cart dbCart = cartMapper.selectByGoodsIdAndUserId(cart.getGoodsId(),cart.getUserId());
        if(dbCart != null){ //当前商品用户的购物车已存在，那就添加数量
            dbCart.setNum(dbCart.getNum() + cart.getNum());
            cartMapper.updateById(dbCart); //更新商品数量
        }else{
            cartMapper.insert(cart);
        }
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        cartMapper.deleteById(id);
    }

    /**
     * 修改
     */
    public void updateById(Cart cart) {
        cartMapper.updateById(cart);
    }

    /**
     * 根据ID查询
     */
    public Cart selectById(Integer id) {
        return cartMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Cart> selectAll(Cart cart) {
        return cartMapper.selectAll(cart);
    }

    /**
     * 分页查询
     */
    public PageInfo<Cart> selectPage(Cart cart, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Cart> list = cartMapper.selectAll(cart);
        return PageInfo.of(list);
    }


}