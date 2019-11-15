package com.zijin.music.musicvote.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.io.Files;
import com.zijin.music.musicvote.anno.Idempotent;
import com.zijin.music.musicvote.anno.VoidMethod;
import com.zijin.music.musicvote.bean.MusicQO;
import com.zijin.music.musicvote.bean.MusicResult;
import com.zijin.music.musicvote.bean.UploadMusic;
import com.zijin.music.musicvote.constant.Const;
import com.zijin.music.musicvote.model.Music;
import com.zijin.music.musicvote.service.MusicVoteService;
import com.zijin.music.musicvote.utils.ExcelUtils;
import com.zijin.music.musicvote.utils.PageQueryResult;
import com.zijin.music.musicvote.utils.Response;
import com.zijin.music.musicvote.utils.Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/music")
@Api(tags="音乐投票平台接口")
public class MusicVoteController {
    @Autowired
    private MusicVoteService musicVoteService;
    @PostMapping("/publishMusic")
    @ApiOperation(value = "发布一首音乐",notes = "添加和修改均调用此接口，" +
            "修改的时候需要把musicId传过来，发布成功返回ok，失败返回error+报错信息")
    @Idempotent
    public Response publishMusic(@RequestBody UploadMusic uploadMusic){
        musicVoteService.publishMusic(uploadMusic);
        return Response.ok();
    }

    @PostMapping("/listMusics")
    @ApiOperation(value = "获取音乐列表",notes = "可通过歌曲名称查询，排序，例如：{page:1,pageSize:10,musicName:名称}，成功返回OK以及分页数据")
    public Response listMusics(@RequestBody MusicQO musicQO){
        PageQueryResult<MusicResult> res = musicVoteService.listMusics(musicQO);
        return Util.RespFormat(res);
    }
    @GetMapping("/getSingleMusic")
    @ApiOperation(value = "获取单个音乐",notes = "通过ID获取音乐信息")
    public Response getSingleMusic(String musicId){
        MusicResult musicResult=musicVoteService.getSingleMusic(Integer.parseInt(musicId));
        return Response.ok().data(musicResult);
    }
    @GetMapping("/nextMusic")
    @ApiOperation(value = "获取下一首音乐",notes = "通过ID获取下一首音乐信息")
    public Response nextMusic(String musicId){
        MusicResult musicResult=musicVoteService.nextMusic(Integer.parseInt(musicId));
        return Response.ok().data(musicResult);
    }
    @GetMapping("/prevMusic")
    @ApiOperation(value = "获取上一首音乐",notes = "通过ID获取上一首音乐信息")
    public Response prevMusic(String musicId){
        MusicResult musicResult=musicVoteService.prevMusic(Integer.parseInt(musicId));
        return Response.ok().data(musicResult);
    }
    @GetMapping("/voteSingleMusic")
    @ApiOperation(value = "为单个歌曲投票",notes = "客户端为音乐投票，需要在第一次启动客户端的时候生成一个唯一key，" +
            "然后持久化到客户端，以后投票就用第一次生成的key")
    public Response voteSingleMusic(String musicId,String clientKey){
        musicVoteService.voteSingleMusic(Integer.parseInt(musicId),clientKey);
        return Response.ok();
    }
    @GetMapping("/isVoted")
    @ApiOperation(value = "检查当前歌曲是否已经投票",notes = "已投返回true，否则返回false")
    public Response isVoted(String musicId,String clientKey){
        boolean voted = musicVoteService.isVoted(Integer.parseInt(musicId), clientKey);
        return Response.ok().data(voted);
    }

    @DeleteMapping("/deleteMusic")
    @ApiOperation(value = "删除音乐",notes = "通过ID删除音乐，同时删除投票，附件")
    public Response deleteMusic(String musicId){
        Integer[] idArr = JSON.parseObject(musicId, Integer[].class);
        musicVoteService.deleteMusic(idArr );
        return Response.ok();
    }

    @GetMapping("/getMaxOrderByGrade")
    @ApiOperation(value = "获取最大的顺序",notes = "新增的时候获取默认的顺序")
    public Response getMaxOrderByGrade(){

        int maxOrder = musicVoteService.getMaxOrderByGrade("A");
        return Response.ok().data(maxOrder);
    }

    @DeleteMapping("/deleteFile")
    @ApiOperation(value = "删除文件",notes = "通过文件编码删除文件")
    public Response deleteFile(String fileCode){
        File file = new File(Const.BASE_DIR + fileCode);
        musicVoteService.deleteAppendixByFileCode(fileCode);
        if (file.exists()) {
            file.delete();
        }
        return Response.ok();
    }

    @PostMapping("/upload")
    @ApiOperation(value = "文件上传",notes = "文件上传，其字段为：file,会返回对应的文件编码")
    public Response multifileUpload(HttpServletRequest request) {
        String fileName = UUID.randomUUID().toString();
        MultipartFile multipartFile = ((MultipartHttpServletRequest) request).getFile("file");
        if(multipartFile!=null&&!multipartFile.isEmpty()) {
            System.out.println("文件名====》"+multipartFile.getOriginalFilename());
            try {
                String fileExtension = Files.getFileExtension(multipartFile.getOriginalFilename()==null?"":multipartFile.getOriginalFilename());
                String fname = fileName+"."+fileExtension;
                File file = new File(Const.BASE_DIR + fname);
                if (!file.exists()) {
                    file.createNewFile();
                }
                multipartFile.transferTo(file);
                return Response.ok().data(fname);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        return Response.error("上传文件失败");
    }


    @GetMapping("/download")
    @ApiOperation(value = "下载文件",notes = "根据文件编码下载文件")
    @VoidMethod
    public void downLoad(String fileCode,HttpServletRequest request,HttpServletResponse response){
        String name =Const.BASE_DIR+fileCode;
        getMusic(fileCode,name,request,response);
//        downloadFile(fileCode,name,request,response);
    }

    private void downloadFile(String fileCode, String name, HttpServletRequest request, HttpServletResponse response) {

        InputStream inputStream = null;
        BufferedInputStream buffInputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(name);
            response.setHeader("content-type", "application/octet-stream");
            response.setContentType("application/octet-stream");

            // 设置强制下载不打开
//            response.setContentType("application/force-download");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileCode);
            outputStream = response.getOutputStream();
            buffInputStream = new BufferedInputStream(inputStream);
            byte[] buffer = new byte[4096];
            int num;
            while ((num = buffInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, num);
            }
            outputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (buffInputStream != null) {
                    buffInputStream.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @PostMapping("/exportDetail")
    @ApiOperation(value = "导出",notes = "导出音乐的明细报表")
    @VoidMethod
    public void exportDetail(HttpServletResponse response, @RequestBody MusicQO musicQO){
        List<Music> musics =musicVoteService.exportMusic(musicQO);
        try {
            // 设置强制下载不打开
            response.setContentType("application/force-download");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("投票统计报表.xlsx","utf-8").replaceAll("\\+","%20"));
            ServletOutputStream outputStream = response.getOutputStream();
            ExcelUtils.exportExcel2007(outputStream,musics,Music.class,null,null);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @GetMapping("/test")
    @VoidMethod
    public void test(String key){
        musicVoteService.canVote(key);
    }
    @GetMapping("/getClientKey")
    @ApiOperation("获取一个客户端的唯一key")
    public Response getKey(){
        return Response.ok().data(UUID.randomUUID().toString());
    }

    /**
     * 音频处理
     * @param fileCode
     * @param name
     * @param request
     * @param response
     */
    private void  getMusic(String fileCode, String name, HttpServletRequest request, HttpServletResponse response){
        File music = new File(name);
        if (!music.exists()){
            System.out.println( "不存在 文件名为："+name+"的音频文件！");
        }
        String range=request.getHeader("range");
        //开始下载位置
        long startByte = 0;
        //结束下载位置
        long endByte = music.length() - 1;
        //有range的话
        if (range != null && range.contains("bytes=") && range.contains("-")) {
            range = range.substring(range.lastIndexOf("=") + 1).trim();
            String[] ranges = range.split("-");
            try {
                //判断range的类型
                if (ranges.length == 1) {
                    //类型一：bytes=-2343
                    if (range.startsWith("-")) {
                        endByte = Long.parseLong(ranges[0]);
                    }
                    //类型二：bytes=2343-
                    else if (range.endsWith("-")) {
                        startByte = Long.parseLong(ranges[0]);
                    }
                }
                //类型三：bytes=22-2343
                else if (ranges.length == 2) {
                    startByte = Long.parseLong(ranges[0]);
                    endByte = Long.parseLong(ranges[1]);
                }
            } catch (NumberFormatException e) {
                startByte = 0;
                endByte = music.length() - 1;
            }
        }
        //要下载的长度
        long contentLength = endByte - startByte + 1;
        //文件名
        String fileName = music.getName();
        //文件类型
        String contentType = request.getServletContext().getMimeType(fileName);
        //各种响应头设置
        //参考资料：https://www.ibm.com/developerworks/cn/java/joy-down/index.html
        //坑爹地方一：看代码
        response.setHeader("Accept-Ranges", "bytes");
        //坑爹地方二：http状态码要为206
        response.setStatus(206);
        response.setContentType(contentType);
        response.setHeader("Content-Type", contentType);
        //这里文件名换你想要的，inline表示浏览器直接实用（我方便测试用的）
        //参考资料：http://hw1287789687.iteye.com/blog/2188500
         response.setHeader("Content-Disposition", "inline;filename="+fileCode);
        response.setHeader("Content-Length", String.valueOf(contentLength));
        //坑爹地方三：Content-Range，格式为
        // [要下载的开始位置]-[结束位置]/[文件总大小]
        response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + music.length());
        BufferedOutputStream outputStream = null;
        RandomAccessFile randomAccessFile = null;
        //已传送数据大小
        long transmitted = 0;
        try {
            randomAccessFile = new RandomAccessFile(music, "r");
            outputStream = new BufferedOutputStream(response.getOutputStream());
            byte[] buff = new byte[4096];
            int len = 0;
            randomAccessFile.seek(startByte);
            //坑爹地方四：判断是否到了最后不足4096（buff的length）个byte这个逻辑（(transmitted + len) <= contentLength）要放前面！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
            //不然会会先读取randomAccessFile，造成后面读取位置出错，找了一天才发现问题所在
            while ((transmitted + len) <= contentLength && (len = randomAccessFile.read(buff)) != -1) {
                outputStream.write(buff, 0, len);
                transmitted += len;

            }
            //处理不足buff.length部分
            if (transmitted < contentLength) {
                len = randomAccessFile.read(buff, 0, (int) (contentLength - transmitted));
                outputStream.write(buff, 0, len);
                transmitted += len;
            }
            outputStream.flush();
            response.flushBuffer();
            randomAccessFile.close();
            System.out.println("下载完毕："+fileCode+"  " + startByte + "-" + endByte + "：" + transmitted);
        } catch (ClientAbortException e) {
            System.out.println("用户停止下载：" +fileCode+"  "+ startByte + "-" + endByte + "：" + transmitted);
            //捕获此异常表示拥护停止下载
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}




