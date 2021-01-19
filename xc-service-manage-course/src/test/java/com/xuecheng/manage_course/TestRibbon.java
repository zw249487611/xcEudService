package com.xuecheng.manage_course;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_course.client.CmsPageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRibbon {
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CmsPageClient cmsPageClient;//接口的代理对象，用feign生成代理对象

    @Test
    public void testRibbon(){
        //先确定要获取的服务名称
        String serviceId = "XC-SERVICE-MANAGE-CMS";
        for (int i =0;i<10;i++){
            //ribbon客户端从Eurakaserver中获取服务列表，根据服务名来获取服务的列表
            ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://"+serviceId+"/cms/page/get/5a754adf6abb500ad05688d9", Map.class);
            Map body = forEntity.getBody();
            System.out.println(body+"哈哈");
        }


    }

    //用feign调用方式测试
    @Test
    public void testRibbon2(){
        //发起远程调用
        CmsPage cmsPage = cmsPageClient.findCmsPageById("5a754adf6abb500ad05688d9");
        System.out.println(cmsPage);


    }


}
