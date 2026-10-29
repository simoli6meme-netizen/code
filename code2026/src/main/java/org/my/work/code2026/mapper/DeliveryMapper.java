package org.my.work.code2026.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.my.work.code2026.entity.Delivery;

import java.util.List;

@Mapper
public interface DeliveryMapper {
    Delivery selectByUsername(String username);

    void insert(Delivery delivery);

    List<Delivery> selectAll(String name);

    Delivery selectById(Integer id);

    void deleteById(Integer id);

    void updateById(Delivery delivery);
}
