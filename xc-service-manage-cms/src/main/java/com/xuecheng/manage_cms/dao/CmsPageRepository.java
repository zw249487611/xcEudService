package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CmsPageRepository extends MongoRepository<CmsPage,String> {

    //自定义查询，根据页面名称来查询，这种规则，虽然好，但也要自己先定义一个接口
    CmsPage findByPageName(String pageName);

    //自定义--根据页面名称pageName、站点id、页面wenpath、来查询
    CmsPage findByPageNameAndSiteIdAndPageWebPath(String pageName, String siteId, String pageWebPath);

}
