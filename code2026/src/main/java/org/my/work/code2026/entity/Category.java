package org.my.work.code2026.entity;

public class Category {
    private Integer id;
    private String name;
    private Integer count;

    public Category(Integer id, String name, Integer count) {
        this.id = id;
        this.name = name;
        this.count = count;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Category() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
