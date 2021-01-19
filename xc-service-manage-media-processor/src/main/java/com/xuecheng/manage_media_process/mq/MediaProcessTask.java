package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MediaProcessTask {

    @Autowired
    MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.ffmpeg-path}")
    String ffmpeg_path;

    @Value("${xc-service-manage-media.video-location}")
    String serverPath;

    //接受视频处理信息进行视频处理
    @RabbitListener(queues ="${xc-service-manage-media.mq.queue-media-video-processor}",containerFactory = "customContainerFactory")
    public void receiveMediaProcessTask(String msg) {
        //1.解析消息内容得到media
        Map map = JSON.parseObject(msg, Map.class);
        String mediaId = (String) map.get("mediaId");

        //2.拿mediaID从数据库查询文件信息
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()) {
            //没查到，
            return;
        }
        //3.使用工具类将avi文件生成MP4
        MediaFile mediaFile = optional.get();
        //拿到文件的类型
        String fileType = mediaFile.getFileType();
        if (!fileType.equals("avi")) {
            mediaFile.setFileStatus("303004");//无需处理的字典状态
            mediaFileRepository.save(mediaFile);
            return;
        } else {
            mediaFile.setFileStatus("303001");//处理中的字段状态
            mediaFileRepository.save(mediaFile);
        }
        //3.使用工具类将avi文件生成MP4
        //参数有：ffmpeg_path,video_path,mp4_name,mp4folder_path
        String video_path = serverPath + mediaFile.getFilePath() + mediaFile.getFileName();//要处理的视频文件的路径
        String mp4_name = mediaFile.getFileId() + ".mp4";//生成的MP4的文件名称
        String mp4folder_path = serverPath + mediaFile.getFilePath();//生成MP4所在的路径
        //创建工具类对象
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4folder_path);
        //进行处理
        String result = mp4VideoUtil.generateMp4();
        if (result == null || !result.equals("success")) {
            //处理失败
            mediaFile.setFileStatus("303003");
            //定义一个对象，记录失败的原因
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);

            mediaFileRepository.save(mediaFile);
            return;
        }


        //4.将MP4生成m3u8和ts文件
            //参数:ffmpeg_path,video_path,m3u8_name,m3u8folder_path
        //MP4视频文件的路径
        String mp4_video_path = serverPath + mediaFile.getFilePath() + mp4_name;
        //m3u8文件名称
        String m3u8_name = mediaFile.getFileId() + ".m3u8";
        //m3u8文件所在目录
        String m3u8folder_path = serverPath + mediaFile.getFilePath() + "/hls/";
        //工具类，转换
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg_path, mp4_video_path, m3u8_name, m3u8folder_path);
        //生成m3u8和ts文件
        String tsResult = hlsVideoUtil.generateM3u8();
        if (tsResult == null || !tsResult.equals("success")) {
            //处理失败
            mediaFile.setFileStatus("303003");
            //定义一个对象，记录失败的原因
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            //记录失败原因
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);

            mediaFileRepository.save(mediaFile);
            return;
        }
        //处理成功
            //获取ts文件列表
        List<String> ts_list = hlsVideoUtil.get_ts_list();

        mediaFile.setProcessStatus("303002");
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
        //保存fileUrl(此url就是视频播放的相对路径
        String fileUrl = mediaFile.getFilePath() + "hls/" + m3u8_name;
        mediaFile.setFileUrl(fileUrl);
        mediaFileRepository.save(mediaFile);
    }
}
