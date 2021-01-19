package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {

    //将该类dao下的接口注入到此处，因为上面的这个接口已经被spring.mongoDB管理了，自动有了各种方法，这边
    //注入可以直接使用其方法
    @Autowired
    CmsPageRepository cmsPageRepository;

    /**
     * 测试能否查询到mongoDB中的数据
     * 首先根据spring整合mongoDB这个工具类cmsPageRepository，它自带提供
     * 了很多查询方法，供大家选择。返回list,直接使用
     */
    @Test
    public void testFindAll() {
        List<CmsPage> all = cmsPageRepository.findAll();
        System.out.println(all);
    }

    /**
     * 根据分页条件查询测试
     */
    @Test
    public void testFindPage() {
        //定义findALL方法中关于分页参数
        /**
         * org.springframework.data.domain.Pageable;
         * 又是spring 整合，这也太棒了
         */
        int page=0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<CmsPage> all = cmsPageRepository.findAll(pageable);
        System.out.println(all);
    }

    /**
     * 测试修改功能
     */
    @Test
    public void testUpdate() {
        //查询对象
        /**
         * 其中Optional这个返回对象，是jdk1.8的新特性，解决了之前的老是空的问题
         */
        Optional<CmsPage> optional = cmsPageRepository.findById("5abefd525b05aa293098fca6");
        if (optional.isPresent()) {
            CmsPage cmsPage = optional.get();
            //设置修改值
            cmsPage.setPageAliase("ccc");
            //....
            //修改
            CmsPage save = cmsPageRepository.save(cmsPage);

        }
    }

    /**
     * 根据自定义方式查询自定义的列的数据
     */
    @Test
    public void testFindByPageName() {
        CmsPage cmsPage = cmsPageRepository.findByPageName("index2.html");
        System.out.println(cmsPage);
    }

    /**
     * 自定义条件查询测试
     */
    @Test
    public void testFindAllByExample() {
        //分页参数
        int page = 1;//从0开始

        int size = 10;

        Pageable pageable = PageRequest.of(page-1, size);

        //条件值对象
        CmsPage cmsPage = new CmsPage();
        //要查询5a751fab6abb5044e0d19ea1站点的页面
//        cmsPage.setSiteId("5a751fab6abb5044e0d19ea1");
        //设置模板id条件
//        cmsPage.setTemplateId("5aec5dd70e661808240ab7a6");
        //设置页面别名
        cmsPage.setPageAliase("轮播");

        //条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        exampleMatcher= exampleMatcher.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        //自定义Example
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);

        Page<CmsPage> all = cmsPageRepository.findAll(example, pageable);
        List<CmsPage> content = all.getContent();
        System.out.println(content);
    }

}
