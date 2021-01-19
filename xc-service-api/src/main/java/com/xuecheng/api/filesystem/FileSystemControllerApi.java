package com.xuecheng.api.filesystem;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件系统，用于存储文件
 *  将文件信息存入到mongoDB,并将具体的文件、图片等存入centos7.7的fastDFS系统中
 */
@Api(value = "文件管理接口",description = "文件管理接口，提供页面增删改查")
public interface FileSystemControllerApi {

    //上传文件

    /**
     *
     * @param multipartFile
     * @param filetag  业务标签
     * @param businesskey  业务key
     * @param metadata 文件元信息,json格式的，
     * @return
     */
    @ApiOperation("上传文件接口")
    public UploadFileResult upload(MultipartFile multipartFile,
                                   String filetag,
                                   String businesskey,
                                   String metadata);
}
