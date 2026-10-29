package org.my.work.code2026.entity;

import java.math.BigDecimal;

public class Recharge {
    /**ID */
    private Integer id;
    /**充值金额 */
    private BigDecimal money;
    /**充值人 */
    private Integer userId;

    private String userName;
    /**支付方式 */
    private String type;
    /**充值时间 */
    private String time;

    public Recharge() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
