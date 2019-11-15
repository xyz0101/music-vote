package com.zijin.music.musicvote.repositories;

import com.zijin.music.musicvote.model.Appendix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MusicAppendixRepository extends JpaRepository<Appendix,Integer> {
    Appendix findFirstByMusicIdAndAppendixType(int musicId,String type);
    Appendix findFirstByAppendixCode(String code);
    List<Appendix>  findAllByMusicIdAndAppendixTypeOrderByAppendixIdAsc(int musicId,String type);
    void deleteByMusicIdAndAppendixType(int musicId,String type);
    void deleteByMusicId(int musicId);
    void deleteByAppendixCode( String fileCode);
}
