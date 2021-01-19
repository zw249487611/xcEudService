package com.xuecheng.framework.domain.cms.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 定义请求模型，此模型作为查询条件类型
 */
//lombok的Date注解，可以自动产生get、set方法，这样页面简洁了很多
@Data
public class QueryPageRequest {

    //接受页面查询的查询条件
    //站点id
    @ApiModelProperty("站点id")
    private String siteId;
    //页面ID
    private String pageId;
    //页面名称
    private String pageName;
    //别名
    private String pageAliase;
    //模板id
    private String templateId;

    //....

}
