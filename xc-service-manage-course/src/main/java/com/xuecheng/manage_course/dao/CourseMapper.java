package com.xuecheng.manage_course.dao;

import com.github.pagehelper.Page;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by Administrator.
 */
@Mapper
public interface CourseMapper {
   //根据课程id查询课程
   CourseBase findCourseBaseById(String id);

   //分页查询
   Page<CourseBase> findCourseList();


   //我的课程查询列表
   Page<CourseInfo> findCourseListPage(CourseListRequest courseListRequest);

}
