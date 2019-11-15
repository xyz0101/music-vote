package com.zijin.music.musicvote.utils;

import java.util.List;

public class ResponseParam {
    private Long total;
    private List<Object> table;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<Object> getTable() {
        return table;
    }

    public void setTable(List<Object> table) {
        this.table = table;
    }
}
