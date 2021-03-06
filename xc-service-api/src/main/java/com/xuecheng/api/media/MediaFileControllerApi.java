package com.xuecheng.api.media;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 专门管理媒资的文件
 */
@Api(value = "媒资文件管理",description = "媒资文件管理接口",tags = "媒体文件管理接口")
public interface MediaFileControllerApi {

    @ApiOperation("查询文件列表")
    public QueryResponseResult<MediaFile> findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest);

}
