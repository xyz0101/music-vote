package com.zijin.music.musicvote.bean;

import com.zijin.music.musicvote.model.Music;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
@ApiModel("音乐发布实体")
public class UploadMusic extends Music{
    @ApiModelProperty(value = "音乐文件")
    private String musicFile;
    @ApiModelProperty(value = "logo文件")
    private String logoFile;
    @ApiModelProperty(value = "歌词文件")
    private String[] lyricFiles;

    public String getMusicFile() {
        return musicFile;
    }

    public void setMusicFile(String musicFile) {
        this.musicFile = musicFile;
    }

    public String getLogoFile() {
        return logoFile;
    }

    public void setLogoFile(String logoFile) {
        this.logoFile = logoFile;
    }


    public String[] getLyricFiles() {
        return lyricFiles;
    }

    public void setLyricFiles(String[] lyricFiles) {
        this.lyricFiles = lyricFiles;
    }
}
