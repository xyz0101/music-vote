package com.zijin.music.musicvote.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zijin.music.musicvote.anno.EnableExport;
import com.zijin.music.musicvote.anno.EnableExportField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name="music")
@ApiModel("音乐基本信息对象")
@EntityListeners(AuditingEntityListener.class)
@EnableExport(fileName = "音乐投票明细表")
public class Music {
    @Id
    @GeneratedValue
    @ApiModelProperty(notes = "主键")
    private Integer musicId;
    @Column(nullable = false)
    @ApiModelProperty(notes = "音乐名称")
    @EnableExportField(colName = "歌曲名称",colWidth = 300)
    private String musicName;
    @Column(nullable = false,columnDefinition = "varchar(10) default 'A'")
    @ApiModelProperty(notes = "音乐等级，已取消,默认是A")
    private String musicGrade;
    @Column(nullable = false)
    @ApiModelProperty(notes = "音乐顺序，正整数")
    @EnableExportField(colName = "歌曲序号",colWidth = 120)
    private Integer musicOrder;
    @Column
    @ApiModelProperty(notes = "音乐描述")
    @EnableExportField(colName = "歌曲描述",colWidth = 800)
    private String musicNote;
    @Column
    @ApiModelProperty(notes = "音乐作者，已取消")
    private String musicAuthor;
    @Column
    @ApiModelProperty(notes = "音乐关联的MP3附件ID")
    private Integer musicAppendixId;
    @Column
    @ApiModelProperty(notes = "投票数量")
    @EnableExportField(colName = "总票数",colWidth = 100)
    private Integer voteCount;
    @Column
    @ApiModelProperty(notes = "词谱顺序")
    private String musicFileOrder;
    @Column(nullable = false)
    @CreatedDate
    @JsonIgnore
    private Timestamp creationDate;
    @Column(nullable = false)
    @LastModifiedDate
    @JsonIgnore
    private Timestamp lastUpdateDate;
    @Column(nullable = false,columnDefinition = "int default 0")
    @Version
    private  int objectVersionNumber;

    public Integer getMusicId() {
        return musicId;
    }

    public void setMusicId(Integer musicId) {
        this.musicId = musicId;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getMusicGrade() {
        return musicGrade;
    }

    public void setMusicGrade(String musicGrade) {
        this.musicGrade = musicGrade;
    }

    public Integer getMusicOrder() {
        return musicOrder;
    }

    public void setMusicOrder(Integer musicOrder) {
        this.musicOrder = musicOrder;
    }

    public String getMusicNote() {
        return musicNote;
    }

    public void setMusicNote(String musicNote) {
        this.musicNote = musicNote;
    }

    public String getMusicAuthor() {
        return musicAuthor;
    }

    public void setMusicAuthor(String musicAuthor) {
        this.musicAuthor = musicAuthor;
    }

    public Integer getMusicAppendixId() {
        return musicAppendixId;
    }

    public void setMusicAppendixId(Integer musicAppendixId) {
        this.musicAppendixId = musicAppendixId;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public Timestamp getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Timestamp lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Integer getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Integer voteCount) {
        this.voteCount = voteCount;
    }

    public int getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(int objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }


    public String getMusicFileOrder() {
        return musicFileOrder;
    }

    public void setMusicFileOrder(String musicFileOrder) {
        this.musicFileOrder = musicFileOrder;
    }
}
