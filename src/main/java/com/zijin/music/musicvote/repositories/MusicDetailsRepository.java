package com.zijin.music.musicvote.repositories;

import com.zijin.music.musicvote.model.Music;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MusicDetailsRepository extends JpaRepository<Music,Integer> {
    Page<Music> findByMusicNameContaining(String name, Pageable pageable);
    List<Music> findAllByMusicNameContaining(String name, Sort sort);
    Music getFirstByMusicGradeOrderByMusicOrderDesc(String grade);
    List<Music> findAllByMusicOrderBetween(int start,int end);
    @Modifying
    @Query("update Music m set m.musicOrder=m.musicOrder-1 where m.musicOrder>?1")
    void updateByOrder(Integer order);
    Music getFirstByMusicOrder(int order);
}
