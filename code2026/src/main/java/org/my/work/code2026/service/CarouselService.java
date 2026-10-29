package org.my.work.code2026.service;

import cn.hutool.core.util.ObjectUtil;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.my.work.code2026.entity.Account;
import org.my.work.code2026.entity.Carousel;
import org.my.work.code2026.exception.CustomException;
import org.my.work.code2026.mapper.CarouselMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理员业务处理
 **/
@Service
public class CarouselService {

    @Resource
    private CarouselMapper carouselMapper;

    /**
     * 新增
     */
    public void add(Carousel carousel) {
        carouselMapper.insert(carousel);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        carouselMapper.deleteById(id);
    }

    /**
     * 修改
     */
    public void updateById(Carousel carousel) {
        carouselMapper.updateById(carousel);
    }

    /**
     * 根据ID查询
     */
    public Carousel selectById(Integer id) {
        return carouselMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Carousel> selectAll(Carousel carousel) {
        return carouselMapper.selectAll(carousel);
    }

    /**
     * 分页查询
     */
    public PageInfo<Carousel> selectPage(Carousel carousel, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Carousel> list = carouselMapper.selectAll(carousel);
        return PageInfo.of(list);
    }


}