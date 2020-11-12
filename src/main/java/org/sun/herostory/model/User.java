package org.sun.herostory.model;

public class User {

    /**
     * 用户 id
     */
    private Integer userId;

    /**
     * 英雄形象
     */
    private String heroAvatar;

    /**
     * 当前血量
     */
    private int currHp;

    /**
     * 移动状态
     */
    private final MoveState moveState = new MoveState();

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getHeroAvatar() {
        return heroAvatar;
    }

    public void setHeroAvatar(String heroAvatar) {
        this.heroAvatar = heroAvatar;
    }

    public void setCurrHp(int currHp) {
        this.currHp = currHp;
    }

    public int getCurrHp() {
        return currHp;
    }

    public MoveState getMoveState() {
        return this.moveState;
    }

}
