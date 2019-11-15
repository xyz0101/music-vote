package com.zijin.music.musicvote.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;
@Entity
@Table(name = "vote")
@ApiModel("投票记录")
@EntityListeners(AuditingEntityListener.class)
public class Vote {

    @Id
    @GeneratedValue
    @ApiModelProperty(notes = "主键")
    private Integer voteId;
    @Column(nullable = false)
    @ApiModelProperty(notes = "投票记录对应的音乐")
    private Integer musicId;
    @Column(nullable = false)
    @ApiModelProperty(notes = "原先是ip，现在改为客户端唯一标识")
    private String ipAddress;
    @CreatedDate
    @Column(nullable = false)
    private Date creationDate;
    @LastModifiedDate
    @Column(nullable = false)
    private Date lastUpdateDate;

    public Integer getMusicId() {
        return musicId;
    }

    public void setMusicId(Integer musicId) {
        this.musicId = musicId;
    }

    public Integer getVoteId() {
        return voteId;
    }

    public void setVoteId(Integer voteId) {
        this.voteId = voteId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }
}
