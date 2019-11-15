package com.zijin.music.musicvote.utils;

import java.util.List;

public class PageQueryResult<T> {
    private Long count;
    private List<T> result;

    public PageQueryResult() {
    }

    public PageQueryResult(Long count, List<T> result) {
        this.count = count;
        this.result = result;
    }

    public Long getCount() {
        return this.count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public List<T> getResult() {
        return this.result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

    public Response httpResponse() {
        Response res = new Response();
        res.setTable(this.result);
        res.setTotal(this.count);
        return res;
    }
}
