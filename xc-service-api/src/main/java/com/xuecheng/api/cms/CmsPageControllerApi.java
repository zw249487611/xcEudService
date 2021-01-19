package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 *
 */
@Api(value = "cms页面管理接口", description = "cms页面管理接口，提供页面的增删改查")
public interface CmsPageControllerApi {
    /**
     * 响应的结果用QueryResponseResult来接收
     * 查询参数为页面，页数据数量，和查询条件类型的实体类参数
     *
     * @param page
     * @param size
     * @param queryPageRequest
     * @return
     */
    //页面查询
    @ApiOperation("分页查询页面列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = true, paramType = "path", dataType = "int"),
            @ApiImplicitParam(name = "size", value = "每页记录数", required = false, paramType = "path", dataType = "int")
    })
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest);

    /**
     * 新增页面
     */
    @ApiOperation("新增页面")
    public CmsPageResult add(CmsPage cmsPage);

    /**
     * 修改页面--首先要根据id进入修改的页面
     *
     * @return
     */
    @ApiOperation("修改页面")
    public CmsPage findById(String id);

    /**
     * 修改页面具体功能。20201227
     */
    @ApiOperation("修改页面")
    public CmsPageResult edit(String id, CmsPage cmsPage);

    /**
     * 删除页面具体功能。20201227
     */
    @ApiOperation("修改页面")
    public ResponseResult delete(String id);

    /**
     * 页面发布
     */
    @ApiOperation("页面发布")
    public ResponseResult post(String pageId);

    @ApiOperation("保存页面")
    public CmsPageResult save(CmsPage cmsPage);

    @ApiOperation("一键发布页面")
    public CmsPostPageResult postPageQuick(CmsPage cmsPage);

}
