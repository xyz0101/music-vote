package com.zijin.music.musicvote.repositories;

import com.zijin.music.musicvote.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MusicVoteRepository extends JpaRepository<Vote,Integer> {
    Vote findFirstByMusicIdAndIpAddressAndLastUpdateDateBetween(int musicId, String ip, Date start , Date end);
    void deleteByMusicId(int musicId);
    List<Vote> findAllByLastUpdateDateBetweenAndIpAddress(Date start , Date end,String ip);

}
