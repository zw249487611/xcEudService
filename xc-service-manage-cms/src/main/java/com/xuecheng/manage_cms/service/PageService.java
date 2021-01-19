package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRespository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PageService {
    //既然是service就需要首先注入dao层的东西
    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    CmsConfigRespository cmsConfigRespository;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CmsTemplateRepository cmsTemplateRepository;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    CmsSiteRepository cmsSiteRepository;

    //页面查询方法,尽量保持与controller的一致，具有可读性

    /**
     * 页面查询方法
     * @param page 页面，从1开始技术
     * @param size 每页记录数
     * @param queryPageRequest
     * @return
     */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {
        if (queryPageRequest == null) {
            queryPageRequest = new QueryPageRequest();
        }

        //完善自定义条件查询
            //定义条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        //条件值对象
        CmsPage cmsPage = new CmsPage();
        //设置条件值
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())) {
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //设置模板id作为条件
        if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())) {
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        //定义条件对象Example
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);
        //分页参数
        if (page < 0) {
            page = 1;
        }
        page = page - 1;
        if (size < 0) {
            size = 10;
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);

        QueryResult queryResult = new QueryResult();
        queryResult.setList(all.getContent());//数据列表
        queryResult.setTotal(all.getTotalElements());//数据总记录数
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS, queryResult);
        return queryResponseResult;
    }



    /*//新增页面
    public CmsPageResult add(CmsPage cmsPage) {

        //需要先校验页面、站点id、页面webpath的唯一性。

        //根据页面名称、站点id和页面webpasth去cms_page集合，如果查到说明此页面已经存在，如果查询不存在，就继续添加
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (cmsPage1 == null) {
            //调用新增页面
            cmsPage.setPageId(null);
            cmsPageRepository.save(cmsPage);
            return new CmsPageResult((CommonCode.SUCCESS), cmsPage);
        }

        //添加失败
        return new CmsPageResult(CommonCode.FAIL,null);
    }*/

    //新增页面--捕获异常方式
    public CmsPageResult add(CmsPage cmsPage) {

        //需要先校验页面、站点id、页面webpath的唯一性。

        //根据页面名称、站点id和页面webpasth去cms_page集合，如果查到说明此页面已经存在，如果查询不存在，就继续添加
        CmsPage cmsPage1 = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if (cmsPage1 != null) {
            //页面已经存在
            //抛出异常，异常内容就是页面已经存在
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }

        //调用新增页面
        cmsPage.setPageId(null);
        cmsPageRepository.save(cmsPage);
        return new CmsPageResult((CommonCode.SUCCESS), cmsPage);

        //添加失败
//        return new CmsPageResult(CommonCode.FAIL,null);
    }


    //根据id查询页面
    public CmsPage getById(String id) {
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()) {
            CmsPage cmsPage = optional.get();
            return cmsPage;
        }
        return null;
    }

    //修改页面
    public CmsPageResult update(String id, CmsPage cmsPage) {
        //根据id查询数据库中的页面信息
        CmsPage cms = this.getById(id);
        if (cms != null) {
            //准备更新数据
            //设置要修改的的数据
            //更新模板id
            cms.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            cms.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            cms.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            cms.setPageName(cmsPage.getPageName());
            //更新访问路径
            cms.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            cms.setPagePhysicalPath(cmsPage.getPagePhysicalPath());

            //更新dateUrl
            cms.setDataUrl(cmsPage.getDataUrl());

            //提交修改
            cmsPageRepository.save(cms);
            return new CmsPageResult(CommonCode.SUCCESS, cms);
        }
        //修改失败
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    //根据id删除页面
    public ResponseResult delete(String id) {
        //先查询一下
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()) {
            cmsPageRepository.deleteById(id);
            //删除成功
            return new ResponseResult(CommonCode.SUCCESS);
        }
        //删除失败
        return new ResponseResult(CommonCode.FAIL);
    }


    /**
     * 下面是轮播图的FreeMarker的部分
     */
    //根据id查询CmsCOnfig
    public CmsConfig getConfigById(String id) {
        Optional<CmsConfig> optional = cmsConfigRespository.findById(id);
        if (optional.isPresent()) {
            CmsConfig cmsConfig = optional.get();
            return cmsConfig;
        }
        return null;
    }

    //页面静态化方法

    /**
     * 静态化程序获取页面的dateaUrl
     *
     * 静态化程序远程请求dataUrl获取数据模型
     *
     * 静态化程序获取页面的模板信息
     *
     * 执行页面静态化
     *
     * @param pageId
     * @return
     */
    public String getPageHtml(String pageId) {
        //获取数据模型
        Map model = getModelByPageId(pageId);
        if (model == null) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //获取页面的模板信息
        String template = getTemplateByPageId(pageId);
        if (StringUtils.isEmpty(template)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }

        //执行静态化

        String html = generateHtml(template, model);
        return html;
    }

    ////执行静态化的具体方法
    private String generateHtml(String templateContent, Map model) {
        //创建配置对象
        Configuration configuration = new Configuration(Configuration.getVersion());

        //创建模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template",templateContent);

        //向Configuration配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);

        String content = null;
        //获取模板
        try {
            Template template = configuration.getTemplate("template");
            //调用api进行静态化
            content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }

        return content;
    }

    //获取页面的模板信息的具体方法
    private String getTemplateByPageId(String pageId) {
        //取出页面的信息
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage == null) {
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }

        //获取页面的模板id
        String templateId = cmsPage.getTemplateId();
        if (StringUtils.isEmpty(templateId)) {
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);

        }
        //查询模板信息
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById((templateId));
        if (optional.isPresent()) {
            CmsTemplate cmsTemplate = optional.get();
            //从GridFS中获取模板文件内容
            String templateFileId = cmsTemplate.getTemplateFileId();
            //根据文件id查询文件
            //根据文件id查询文件
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));

            //打开一个下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());

            //创建GridFsResource对象，获取流
            GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
            //从流中获取数据
            String content = null;
            try {
                content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");

                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    //获取数据模型的具体方法
    private Map getModelByPageId(String pageId) {
        //取出页面的信息
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage == null) {
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //取出页面的dataUrl
        String dataUrl = cmsPage.getDataUrl();
        if (StringUtils.isEmpty(dataUrl)) {
            //页面的url为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //通过restTemplate请求dataUrl获取数据
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        return body;
    }

    /**
     * 页面发布2021-1-4
     */
    public ResponseResult post(String pageId) {
        //执行页面静态化
        String pageHtml = this.getPageHtml(pageId);
        //将页面静态化文件存储到GridFs中
        CmsPage cmsPage = this.saveHtml(pageId, pageHtml);

        //想MQ发送消息
        this.sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //方法太多，拉出来写，供post方法中使用
    //向MQ发送消息
    private void sendPostPage(String pageId) {
        //得到页面信息
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage == null) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //创建消息对象
        Map<String,String> msg = new HashMap<>();
        msg.put("pageId", pageId);
        //转成json串，
        String jsonString = JSON.toJSONString(msg);
        //发送给mq
            //站点id
        String siteId = cmsPage.getSiteId();
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,siteId,jsonString);

    }

    //方法太多，拉出来写，供post方法中使用
    //保存Html到GridFs
    private CmsPage saveHtml(String pageId, String htmlContent) {

        //先得到页面的信息
        CmsPage cmsPage = this.getById(pageId);
        if (cmsPage == null) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //将htmlContent内容转成输入流
        ObjectId objectId = null;
        try {
            InputStream inputStream = IOUtils.toInputStream(htmlContent, "utf-8");

            //将HTML文件内容保存到GridFs
            objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //将HTML文件的id更新到CmsPage中
        cmsPage.setHtmlFileId(objectId.toHexString());
        cmsPageRepository.save(cmsPage);
        return cmsPage;

    }

    //保存页面，有则更新，没有则添加
    public CmsPageResult save(CmsPage cmsPage) {
        //判断页面是否存在
        CmsPage one = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(), cmsPage.getSiteId(), cmsPage.getPageWebPath());
        if(one!=null){
            //进行更新
            return this.update(one.getPageId(),cmsPage);
        }
        return this.add(cmsPage);

    }

    /**
     * 一键发布页面
     * @param cmsPage
     * @return
     */
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        //1、页面的信息存储到CMS_page集合中
        CmsPageResult save = this.save(cmsPage);
        if (!save.isSuccess()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //得到页面的id
        CmsPage cmsPageSave = save.getCmsPage();
        String pageId = cmsPageSave.getPageId();

        //2、执行页面发布（先静态化、保存GridFs,向MQ发送消息）
        ResponseResult post = this.post(pageId);
        if (!post.isSuccess()) {
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //拼接页面URL=cmsSite.siteDomain+cmsSite.siteWebPath+cmsPage.pageWebPath+cmsPage.pageName
        //取出站点id
        String siteId = cmsPageSave.getSiteId();
        //拉出一个方法，供这边使用
        CmsSite cmsSite = this.findCmsSiteById(siteId);
        String pageUrl = cmsSite.getSiteDomain() +":" + cmsSite.getSitePort()+ cmsSite.getSiteWebPath() + cmsPageSave.getPageWebPath() + cmsPageSave.getPageName();
        return new CmsPostPageResult(CommonCode.SUCCESS, pageUrl);


    }

    //拉出一个方法，供上面使用
    //根据站点id查询站点信息
    public CmsSite findCmsSiteById(String siteId) {
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }
}
