package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import jdk.management.resource.ResourceRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class MediaUploadService {

    @Autowired
    MediaFileRepository mediaFileRepository;

    @Value("${xc-service-manage-media.upload-location}")
    String upload_location;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("${xc-service-manage-media.mq.routingkey‐media‐video}")
    String routingkey_media_video;

    //得到文件所属目录
    private String getFileFolderPath(String fileMd5) {
        return upload_location + fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" +fileMd5 +"/";
    }

    //得到文件的路径
    private String getFilePath(String fileMd5, String fileExt) {
        return upload_location + fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" +fileMd5 +"/" +fileMd5 +"." +fileExt;
    }

    //得到块文件所属目录
    private String getChunKFileFolderPath(String fileMd5) {
        return upload_location + fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" +fileMd5 +"/chunk/" ;
    }
    /**
     * 根据文件md5得到文件路径
     * 规则：
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符
     * 三级目录：md5
     * 文件名：md5+文件扩展名
     * @param fileMd5 文件md5值
     * @param fileExt 文件扩展名
     * @return 文件路径
     */
    //文件上传前的注册，检查文件是否存在
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //1.检查文件再磁盘上是否存在
            //1.1先拿到文件所属目录的路径
        String fileFolderPath = this.getFileFolderPath(fileMd5);

        //1.2文件的路径
        String filePath = this.getFilePath(fileMd5, fileExt);

        File file = new File(filePath);
        //文件是否存在
        boolean exists = file.exists();

        //2.检查文件的信息，再mongoDb，是否存在
        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);
        if (exists && optional.isPresent()) { //表示文件已存在的，
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }

        //文件不存在时，做一些准备工作，检查文件所在目录是否存在，如果不存在，则创建
        File fileFolder = new File(fileFolderPath);
        if (!fileFolder.exists()) {
            fileFolder.mkdirs();
        }
        return new ResponseResult(CommonCode.SUCCESS);

    }

    ////检查块文件

    /**
     *
     * @param fileMd5
     * @param chunk 块的下标
     * @param chunkSize 快的大小
     * @return
     */
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        //检查分块文件是否存在
        //得到分块文件的所属目录
        String chunKFileFolderPath = this.getChunKFileFolderPath(fileMd5);
        //块文件
        File chunkFile = new File(chunKFileFolderPath + chunk);
        if (chunkFile.exists()) {
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK, true);
        } else {
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK, false);
        }
    }

    //上传分块
    public ResponseResult uploadchunk(MultipartFile file, String fileMd5, Integer chunk) {
        //先去检查分块目录，如果不存在，则自动创建
        String chunKFileFolderPath = this.getChunKFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunKFileFolderPath);
        //得到分块路径
        String chunkFilePath = chunKFileFolderPath + chunk;
        //如果不存在，则自动创建
        if (!chunkFileFolder.exists()) {
            chunkFileFolder.mkdirs();
        }
        //得到上传文件的输入流
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = file.getInputStream();
            outputStream = new FileOutputStream(new File(chunkFilePath));
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //文件合并
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //1.合并所有的分块
            //1.1，得到分块文件的所属目录
        String chunKFileFolderPath = this.getChunKFileFolderPath(fileMd5);
        File chunkFileFolder = new File(chunKFileFolderPath);//拿到分块目录下的所有文件
        File[] files = chunkFileFolder.listFiles();//分块文件列表
        List<File> fileList = Arrays.asList(files);
        //1.2.得到合并后的文件路径
        String filePath = this.getFilePath(fileMd5, fileExt);
        File mergeFile = new File(filePath);

            //1.3.执行合并。。拉出一个方法，这边调用
        mergeFile = this.mergeFile(fileList, mergeFile);
        if (mergeFile == null) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);

        }

        //2.校验文件的md5值是否和前端传入的MD5是否一致
            //校验文件，拉出一个方法，供这边调用
        boolean checkFileMd5 = this.checkFileMd5(mergeFile, fileMd5);
        if (!checkFileMd5) {
            //说明校验文件失败了
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        //3。将文件写入mongoDB
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileOriginalName(fileName);
        mediaFile.setFileName(fileMd5 + "." +fileExt);
        //保存文件的相对路径
        String filePath1 = fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" +fileMd5 +"/";
        mediaFile.setFilePath(filePath1);
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        mediaFile.setFileStatus("301002");
        mediaFileRepository.save(mediaFile);
        //向MQ发送视频处理信息
        this.sendProcessVideoMsg(mediaFile.getFileId());
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 发送视频处理信息,发送到MQ
     *
     * @param mediaId 媒资文件的id
     * @param
     * @return
     */
    public ResponseResult sendProcessVideoMsg(String mediaId) {
        //先判断mediaId是否正确及存在
            //查询数据库
        Optional<MediaFile> optional = mediaFileRepository.findById(mediaId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //向MQ发送视频处理信息
            //先构造消息的格式，
        Map<String, String> map = new HashMap<>();
        map.put("mediaId", mediaId);
        String jsonString = JSON.toJSONString(map);
            //向MQ发送视频处理信息
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK,routingkey_media_video,jsonString);

        } catch (AmqpException e) {
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //校验文件，拉出一个方法，供上边调用
    private boolean checkFileMd5(File mergeFile, String md5) {
        //创建一个文件的输入流
        try {
            FileInputStream fileInputStream = new FileInputStream(mergeFile);

            //得到文件的MD5
           String md5Hex =  DigestUtils.md5Hex(fileInputStream);
            //和传入的MD5进行比较
            if (md5.equalsIgnoreCase(md5Hex)) {
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //拉出一个方法，合并文件，供上面使用
    private File mergeFile(List<File> chunkFileList, File mergeFile) {
        try {
        //判断合并的文件是否存在，存在就干掉,否则，创建新文件
            if (mergeFile.exists()) {
                mergeFile.delete();
            } else {
                //创建一个新文件
                mergeFile.createNewFile();
            }
            //对块文件进行排序
            Collections.sort(chunkFileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName())) {
                        return 1;
                    }
                    return -1;
                }
            });
            //创建一个写对象
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");
            //缓冲区
            byte[] b = new byte[1024];
            for (File chunkFile : chunkFileList) {
                RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r");
                int len = -1;
                while ((len = raf_read.read(b)) != -1) {
                    raf_write.write(b, 0, len);
                }
                raf_read.close();

            }
            raf_write.close();
            return mergeFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
