package com.zijin.music.musicvote.bean;

import org.apache.poi.ss.usermodel.IndexedColors;

public enum ColorEnum {
    RED("红色", IndexedColors.RED.index),
    GREEN("绿色", IndexedColors.GREEN.index),
    BLANK("白色", IndexedColors.WHITE.index),
    YELLOW("黄色", IndexedColors.YELLOW.index),
    BLUE("蓝色", IndexedColors.BLUE.index),
    PALE_BLUE("浅蓝色", IndexedColors.PALE_BLUE.index);
    private String name;
    private short index;
    private ColorEnum(String name, short index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public short getIndex() {
        return index;
    }

    public void setIndex(short index) {
        this.index = index;
    }

}
