package com.zijin.music.musicvote.constant;

import java.util.concurrent.ConcurrentHashMap;

public class Const {
//    public static final String BASE_DIR = "src/main/resources/appendixs/";
    public static final String BASE_DIR = "/data/music/appendixs/";
    /**
     * 在单机环境下可以使用这个作为幂等校验，但是如果分布式的话就不能使用本地缓存了
     */
    public static final ConcurrentHashMap<String,String> IDEMPOTENT_MAP = new ConcurrentHashMap<>();

}
