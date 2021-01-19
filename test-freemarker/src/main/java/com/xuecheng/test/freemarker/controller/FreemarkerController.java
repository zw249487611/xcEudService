package com.xuecheng.test.freemarker.controller;


import com.xuecheng.test.freemarker.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RequestMapping("/freemarker")
@Controller//不要使用restController，因为要输出html网页，而后者则会输出json数据
public class FreemarkerController {

    @Autowired
    RestTemplate restTemplate;

    @RequestMapping("/course")
    public String course(Map<String, Object> map) {
        System.out.println("哈哈哈哈");
        //使用restTemplate请求轮播图的模型数据
        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31200/course/courseview/4028e58161bd3b380161bd3bcd2f0000", Map.class);
        Map body = forEntity.getBody();

        //设置模型数据
        map.putAll(body);

        return "course";

    }


    @RequestMapping("/banner")
    public String index_banner(Map<String, Object> map) {
//        System.out.println("哈哈哈哈");
        //使用restTemplate请求轮播图的模型数据
        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31001/cms/config/getmodel/5a791725dd573c3574ee333f", Map.class);
        Map body = forEntity.getBody();

        //设置模型数据
        map.putAll(body);

        return "index_banner";

    }


    //测试1
    @RequestMapping("/test1")
    public String test1(Map<String,Object> map) {
        //map就是freemarker模板使用的数据
        map.put("name","传智播客");
        //返回freemarker模板的位置，基于resource/templates路径
        Student stu1 = new Student();
        stu1.setName("小明");
        stu1.setAge(18);
        stu1.setMondy(1000.86f);
        stu1.setBirthday(new Date());
        Student stu2 = new Student();
        stu2.setName("小红");
        stu2.setMondy(200.1f);
        stu2.setAge(19);
        stu2.setBirthday(new Date());
        List<Student> friends = new ArrayList<>();
        friends.add(stu1);
        stu2.setFriends(friends);
        stu2.setBestFriend(stu1);
        List<Student> stus = new ArrayList<>();
        stus.add(stu1);
        stus.add(stu2);
        //向数据模型放数据
        map.put("stus",stus);
        //准备map数据
        HashMap<String,Student> stuMap = new HashMap<>();
        stuMap.put("stu1",stu1);
        stuMap.put("stu2",stu2);
        //向数据模型放数据
        map.put("stu1",stu1);
        //向数据模型放数据
        map.put("stuMap",stuMap);
        //返回模板文件名称

        map.put("point", 102920122);
        return "test1";
    }

}
