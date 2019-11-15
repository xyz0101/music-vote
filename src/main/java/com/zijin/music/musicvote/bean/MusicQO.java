package com.zijin.music.musicvote.bean;

import com.zijin.music.musicvote.model.Music;
import io.swagger.annotations.ApiModel;

@ApiModel("音乐列表查询")
public class MusicQO extends Music {
    private int page;
    private int pageSize;
    private String sortKey;
    private boolean descFlag;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortKey() {
        return sortKey;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }


    public boolean getDescFlag() {
        return descFlag;
    }

    public void setDescFlag(boolean descFlag) {
        this.descFlag = descFlag;
    }
}
