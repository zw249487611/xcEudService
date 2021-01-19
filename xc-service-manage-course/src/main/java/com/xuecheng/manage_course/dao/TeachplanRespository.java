package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.Teachplan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeachplanRespository extends JpaRepository<Teachplan,String> {

    //根据课程id和parenetId查询teachplan ,select * from tecahplan a where a.courseId='***' and a.partentId = '0'
    public List<Teachplan> findByCourseidAndParentid(String courseId, String parentId);
}
