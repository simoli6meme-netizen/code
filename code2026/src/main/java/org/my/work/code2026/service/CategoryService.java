package org.my.work.code2026.service;

import cn.hutool.core.util.ObjectUtil;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.my.work.code2026.entity.Account;
import org.my.work.code2026.entity.Category;
import org.my.work.code2026.exception.CustomException;
import org.my.work.code2026.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理员业务处理
 **/
@Service
public class CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    /**
     * 新增
     */
    public void add(Category category) {
        categoryMapper.insert(category);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        categoryMapper.deleteById(id);
    }

    /**
     * 修改
     */
    public void updateById(Category category) {
        categoryMapper.updateById(category);
    }

    /**
     * 根据ID查询
     */
    public Category selectById(Integer id) {
        return categoryMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Category> selectAll(Category category) {
        return categoryMapper.selectAll(category);
    }

    /**
     * 分页查询
     */
    public PageInfo<Category> selectPage(Category category, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Category> list = categoryMapper.selectAll(category);
        return PageInfo.of(list);
    }


}