package com.zijin.music.musicvote.anno;



import com.zijin.music.musicvote.bean.ColorEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 设置允许导出
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableExport {
    //设置文件名/标题
    String fileName();
    //设置背景颜色
    ColorEnum cellColor() default ColorEnum.BLANK;
}