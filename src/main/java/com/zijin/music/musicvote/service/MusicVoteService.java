package com.zijin.music.musicvote.service;

import com.alibaba.fastjson.JSON;
import com.zijin.music.musicvote.bean.MusicQO;
import com.zijin.music.musicvote.bean.MusicResult;
import com.zijin.music.musicvote.bean.UploadMusic;
import com.zijin.music.musicvote.constant.Const;
import com.zijin.music.musicvote.model.Appendix;
import com.zijin.music.musicvote.model.Music;
import com.zijin.music.musicvote.model.Vote;
import com.zijin.music.musicvote.repositories.MusicAppendixRepository;
import com.zijin.music.musicvote.repositories.MusicDetailsRepository;
import com.zijin.music.musicvote.repositories.MusicVoteRepository;
import com.zijin.music.musicvote.utils.PageQueryResult;
import com.zijin.music.musicvote.utils.Util;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class MusicVoteService {
    @Autowired
    private MusicVoteRepository musicVoteRepository;
    @Autowired
    private MusicAppendixRepository musicAppendixRepository;
    @Autowired
    private MusicDetailsRepository musicDetailsRepository;
    @Transactional(rollbackFor = Exception.class)
    public void publishMusic(UploadMusic uploadMusic) {
        String[] lyricFiles = uploadMusic.getLyricFiles();
        String logoFile = uploadMusic.getLogoFile();
        String musicFile = uploadMusic.getMusicFile();
        Music music = uploadMusic.getMusicId()==null?new Music():musicDetailsRepository.findById(uploadMusic.getMusicId()).orElse(new Music());
        Integer musicOrder = uploadMusic.getMusicOrder();
        if(musicOrder!=null){
                int before = music.getMusicId()!=null?music.getMusicOrder():Integer.MAX_VALUE;
                int after = uploadMusic.getMusicOrder();
                if(before>after) {
                    List<Music> musicOrderList = musicDetailsRepository.findAllByMusicOrderBetween(after, before-1);
                    musicOrderList.forEach(item->item.setMusicOrder(item.getMusicOrder()+1));
                    musicDetailsRepository.saveAll(musicOrderList);
                    musicDetailsRepository.flush();
                }else if (before<after){
                    List<Music> musicOrderList = musicDetailsRepository.findAllByMusicOrderBetween(before,after-1);
                    musicOrderList.forEach(item->item.setMusicOrder(item.getMusicOrder()-1));
                    musicDetailsRepository.saveAll(musicOrderList);
                    musicDetailsRepository.flush();
                }
        }else {
            throw new RuntimeException("请输入正确的音乐序号（大于等于1）！");
        }
        boolean lyricChange = !String.valueOf(music.getMusicFileOrder())
                .equals(String.valueOf(uploadMusic.getMusicFileOrder()));
        BeanUtils.copyProperties(uploadMusic,music ,Util.getNullPropertyNames(uploadMusic));

        Music res = musicDetailsRepository.saveAndFlush(music);
        Integer musicId = res.getMusicId();
        Integer musicAppendixId =saveFile(musicId,lyricFiles,logoFile,musicFile,lyricChange);
        if (musicAppendixId>0) {
            res.setMusicAppendixId(musicAppendixId);
            musicDetailsRepository.saveAndFlush(res);
        }
    }

    private Integer saveFile(Integer musicId, String[] lyricFiles, String logoFile, String musicFile, boolean lyricChange) {
        if(lyricFiles!=null&&lyricChange){
            musicAppendixRepository.deleteByMusicIdAndAppendixType(musicId,"lyric");
            for (String lyricFile : lyricFiles) {
                    Appendix appendix = new Appendix(musicId,lyricFile,"lyric");
                    musicAppendixRepository.saveAndFlush(appendix);
            }
        }
        if(!StringUtils.isEmpty(logoFile)){
            musicAppendixRepository.deleteByMusicIdAndAppendixType(musicId,"logo");
                Appendix appendix = new Appendix(musicId,logoFile,"logo");
                musicAppendixRepository.saveAndFlush(appendix);
        }
        if(!StringUtils.isEmpty(musicFile)){
            musicAppendixRepository.deleteByMusicIdAndAppendixType(musicId,"music");
                Appendix appendix = new Appendix(musicId,musicFile,"music");
                Appendix append = musicAppendixRepository.saveAndFlush(appendix);
                return append.getAppendixId();
        }
        return -1;
    }

    public PageQueryResult<MusicResult> listMusics(MusicQO musicQO) {
        Sort sort = getSort(musicQO);
        Pageable pageable = PageRequest.of(musicQO.getPage()>0?musicQO.getPage()-1:0,musicQO.getPageSize(),sort);
        Page<Music> musicPage = null;
        if(musicQO.getMusicName()==null) {
            musicPage = musicDetailsRepository.findAll(pageable);
        }else {
            musicPage = musicDetailsRepository.findByMusicNameContaining(musicQO.getMusicName(),pageable);
        }
        List<MusicResult> musicResults = new ArrayList<>();
        musicPage.get().forEach(item->{
            MusicResult musicResult=  getMusicResult(item);
            musicResults.add(musicResult);
        });
        PageQueryResult<MusicResult> pageQueryResult = new PageQueryResult<>();
        pageQueryResult.setCount(musicPage.getTotalElements());
        pageQueryResult.setResult(musicResults);
        return pageQueryResult;
    }

    public List<Music> exportMusic(MusicQO musicQO) {
        Sort sort = getSort(musicQO);
        List<Music> musics = new ArrayList<>();
        if(musicQO.getMusicName()==null) {
            musics = musicDetailsRepository.findAll(sort);
        }else {
            musics = musicDetailsRepository.findAllByMusicNameContaining(musicQO.getMusicName(),sort);
        }
        return musics;
    }
    private Sort getSort(MusicQO musicQO) {
        if (StringUtils.isEmpty( musicQO.getSortKey())) {
            return Sort.by(Sort.Direction.ASC,"musicOrder");
        }else{
            return Sort.by(musicQO.getDescFlag()? Sort.Direction.DESC: Sort.Direction.ASC,musicQO.getSortKey());
        }
    }

    private MusicResult getMusicResult(Music item) {
        MusicResult musicResult = new MusicResult();
        Appendix logo = musicAppendixRepository.findFirstByMusicIdAndAppendixType(item.getMusicId(),"logo");
        List<Appendix> lyric = new ArrayList<>();
        String musicFileOrder = item.getMusicFileOrder();
        if(musicFileOrder!=null) {
            try {
                String[] orders = JSON.parseObject(musicFileOrder, String[].class);
                for (String order : orders) {
                    Appendix appendix  = musicAppendixRepository.findFirstByAppendixCode(order);
                    lyric.add(appendix);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        if(item.getMusicAppendixId()!=null) {
            try {
                Appendix musicAppendix = musicAppendixRepository.findById(item.getMusicAppendixId()).orElse(null);
                musicResult.setMusicFile(musicAppendix);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        musicResult.setMusic(item);
        musicResult.setLogo(logo);
        musicResult.setLyric(lyric);
        return musicResult;
    }

    public MusicResult getSingleMusic(Integer musicId) {
        Music item = musicDetailsRepository.findById(musicId).orElse(null);
        if (item==null) {
            throw new RuntimeException("歌曲不存在！");
        }
        return getMusicResult(item);
    }

    /**
     * 一个ip一首歌一天只能投一次，每天可以投15首
     * @param musicId
     */
    @Transactional(rollbackFor = Exception.class)
    public void voteSingleMusic(Integer musicId,String clientKey) {
         if(isVoted(musicId,clientKey)){
            throw new RuntimeException("您今天已经投过票了！");
        }else{
            boolean canVote = canVote(clientKey);
            if (!canVote){
                throw new RuntimeException("您每天最多只能对15个作品进行投票！");
            }
            Vote vote1 = new Vote();
            vote1.setIpAddress(clientKey);
            vote1.setMusicId(musicId);
            musicVoteRepository.saveAndFlush(vote1);
            Music music = musicDetailsRepository.findById(musicId).orElse(null);
            if (music!=null) {
                music.setVoteCount((music.getVoteCount()==null?0:music.getVoteCount())+1);
                musicDetailsRepository.saveAndFlush(music);
            }else{
                throw new RuntimeException("该歌曲不存在！");
            }

        }
    }

    public boolean isVoted(Integer musicId, String clientKey) {
        Date start = Util.getStartTime();
        Date end = Util.getEndTime();
        Vote vote = musicVoteRepository.findFirstByMusicIdAndIpAddressAndLastUpdateDateBetween(musicId, clientKey, start, end);
        return vote != null;

    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMusic(Integer[] musicIds) {
        if (musicIds!=null) {
            for (int musicId : musicIds) {
                Music music = musicDetailsRepository.findById(musicId).orElse(null);
                if (music!=null) {
                    Integer musicOrder = music.getMusicOrder();
                    musicDetailsRepository.updateByOrder(musicOrder);
                    musicDetailsRepository.deleteById(musicId);
                    musicAppendixRepository.deleteByMusicId(musicId);
                    musicVoteRepository.deleteByMusicId(musicId);
                }
            }
        }

    }

    public int getMaxOrderByGrade(String grade) {
        Music music = musicDetailsRepository.getFirstByMusicGradeOrderByMusicOrderDesc(grade);
        if (music==null){
            return 1;
        }
        Integer musicOrder = music.getMusicOrder();
        if (musicOrder==null){
            return 1;
        }else{
            return musicOrder+1;
        }
    }

    public void deleteAppendixByFileCode(String fileCode) {
        musicAppendixRepository.deleteByAppendixCode(fileCode);
    }

    public List<Music> listAll() {
        return musicDetailsRepository.findAll();
    }

    public boolean canVote(String key) {
        Date start = Util.getStartTime();
        Date end = Util.getEndTime();
        List<Vote> votes = musicVoteRepository.findAllByLastUpdateDateBetweenAndIpAddress(start, end, key);
        boolean canVote = true;
        if (!CollectionUtils.isEmpty(votes)) {
            Set<Integer> res = new HashSet<>();
            votes.forEach(item -> {
                res.add(item.getMusicId());
            });
            if (res.size()>=15){
                canVote = false;
            }
        }
        return canVote;
    }

    public MusicResult nextMusic(int musicId) {
        return getMusicNextOrPrev(musicId,1);
    }

    public MusicResult prevMusic(int musicId) {
        return getMusicNextOrPrev(musicId,-1);
    }

    private MusicResult getMusicNextOrPrev(int musicId, int nextOrPrev){
        Music music = musicDetailsRepository.findById(musicId).orElse(null);
        if (music!=null) {
            Integer musicOrder = music.getMusicOrder();
            Music byMusicOrder = musicDetailsRepository.getFirstByMusicOrder(musicOrder + nextOrPrev);
            if (byMusicOrder!=null){
                return getMusicResult(byMusicOrder);
            }else {
                throw new RuntimeException("歌曲不存在！");
            }
        }else {
            throw new RuntimeException("歌曲不存在！");
        }
    }
}
