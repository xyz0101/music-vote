package com.zijin.music.musicvote.bean;

import com.zijin.music.musicvote.model.Appendix;
import com.zijin.music.musicvote.model.Music;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
@ApiModel("音乐查询的结果对象")
public class MusicResult  {
    @ApiModelProperty(notes = "音乐对象，包含基本信息")
    private Music music;
    @ApiModelProperty(notes = "音乐文件的附件对象，包含文件编码等等")
    private Appendix musicFile;
    @ApiModelProperty(notes = "缩略图的附件对象，包含文件编码等等")
    private Appendix logo;
    @ApiModelProperty(notes = "歌词文件的附件对象，包含文件编码等等")
    private List<Appendix> lyric;

    public Music getMusic() {
        return music;
    }

    public void setMusic(Music music) {
        this.music = music;
    }

    public Appendix getLogo() {
        return logo;
    }

    public void setLogo(Appendix logo) {
        this.logo = logo;
    }

    public List<Appendix> getLyric() {
        return lyric;
    }

    public void setLyric(List<Appendix> lyric) {
        this.lyric = lyric;
    }

    public Appendix getMusicFile() {
        return musicFile;
    }

    public void setMusicFile(Appendix musicFile) {
        this.musicFile = musicFile;
    }
}
