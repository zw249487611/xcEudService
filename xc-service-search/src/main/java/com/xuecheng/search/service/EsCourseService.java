package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EsCourseService {

    @Value("${xuecheng.course.index}")
    private String index;

    @Value("${xuecheng.course.type}")
    private String type;

    @Value("${xuecheng.course.source_field}")
    private String source_field;

    @Value("${xuecheng.media.index}")
    private String media_index;

    @Value("${xuecheng.media.type}")
    private String media_type;

    @Value("${xuecheng.media.source_field}")
    private String media_source_field;

    @Autowired
    RestHighLevelClient restHighLevelClient;


    /**
     * 课程搜索
     * @param page
     * @param size
     * @param courseSearchParam
     * @return
     */
    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {

        if (courseSearchParam == null) {
            courseSearchParam = new CourseSearchParam();
        }
        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest(index);
        //设置搜索类型
        searchRequest.types(type);
        //
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //过滤源字段
        String[] sour_field_array = source_field.split(",");
        searchSourceBuilder.fetchSource(sour_field_array, new String[]{});

        //创建布尔查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 搜索条件
        //根据关键字搜索
        if (StringUtils.isNotEmpty(courseSearchParam.getKeyword())) {
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(), "name", "description", "teachplan")
                    .minimumShouldMatch("70%")
                    .field("name", 10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getMt())) {
            //根据一级分类。。。可以用过滤器来实现
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt", courseSearchParam.getMt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getSt())) {
            //根据二级分类。。。可以用过滤器来实现
            boolQueryBuilder.filter(QueryBuilders.termQuery("st", courseSearchParam.getSt()));
        }
        if (StringUtils.isNotEmpty(courseSearchParam.getGrade())) {
            //根据难度分类。。。可以用过滤器来实现
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade", courseSearchParam.getGrade()));
        }

        //设置boolQueryBuilder到searchSourceBuilder中
        searchSourceBuilder.query(boolQueryBuilder);
        //设置分页参数
        if (page <= 0) {
            page=1;
        }
        if (size <= 0) {
            size = 12;
        }
        int from = (page - 1) * size;
        searchSourceBuilder.from(from);//起始记录的下标
        searchSourceBuilder.size(size);//每页显示条数

        //设置高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        //设置哪些高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));

        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);

        QueryResult<CoursePub> queryResult = new QueryResult();
        List<CoursePub> list = new ArrayList<>();

        try {
            //执行搜索
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            //获取响应结果
            SearchHits hits = searchResponse.getHits();
            long totalHits = hits.totalHits;//总的记录数
            queryResult.setTotal(totalHits);
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : searchHits) {
                CoursePub coursePub = new CoursePub();
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();//源文档
                //取出id
                String id = (String) sourceAsMap.get("id");
                coursePub.setId(id);

                //取出name
                String  name = (String) sourceAsMap.get("name");
                    //取出高亮字段name
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                if (highlightFields != null) {
                    HighlightField highlightFieldName = highlightFields.get("name");
                    if (highlightFieldName != null) {
                        Text[] fragments = highlightFieldName.fragments();
                        StringBuffer stringBuffer = new StringBuffer();
                        for (Text text : fragments) {
                            stringBuffer.append(text);
                        }
                        name = stringBuffer.toString();
                    }


                }
                coursePub.setName(name);
                //图片
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);
                //价格
                Double price = null;
                try {
                    if (sourceAsMap.get("price") != null) {
                        price = (Double) sourceAsMap.get("price");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                coursePub.setPrice(price);
                //旧价格
                Double price_old = null;
                try {
                    if (sourceAsMap.get("price") != null) {
                        price_old = (Double) sourceAsMap.get("price_old");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                coursePub.setPrice_old(price_old);
                //将coursePub放入list
                list.add(coursePub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        queryResult.setList(list);
        QueryResponseResult<CoursePub> queryResponseResult = new QueryResponseResult<CoursePub>(CommonCode.SUCCESS, queryResult);
        return queryResponseResult;
    }

    //根据课程id查询课程信息
    public Map<String, CoursePub> getall(String id) {
        //定义一个搜索请求对象,需要指定参数：,这边用的index为索引的名称，
        SearchRequest searchRequest = new SearchRequest(index);
        //再指定type
        searchRequest.types(type);

        //定义一个searchBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            //设置使用termQuery
        searchSourceBuilder.query(QueryBuilders.termQuery("id", id));
            //过滤源字段,这里要取出所有字段，就不过滤啦
//        searchSourceBuilder.fetchSource()
        searchRequest.source(searchSourceBuilder);
        //因为返回的是MAP。所以这边用map来封装
        Map<String, CoursePub> map = new HashMap<>();

        try {
            SearchResponse search = restHighLevelClient.search(searchRequest);
            SearchHits hits = search.getHits();//取出查询结果
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : searchHits) {
                //最终要返回的课程信息对象
                CoursePub coursePub = new CoursePub();
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();//得到源文档的内容
                //获得源文档之后，再一个个拿出每个字段。。需要放入一个对象里，，什么对象呢。?去上面定义
                String  courseId = (String) sourceAsMap.get("id");
                coursePub.setId(courseId);
                String name = (String) sourceAsMap.get("name");
                coursePub.setName(name);
                String grade = (String) sourceAsMap.get("grade");
                coursePub.setGrade(grade);
                String charge = (String) sourceAsMap.get("charge");
                coursePub.setCharge(charge);
                String pic = (String) sourceAsMap.get("pic");
                coursePub.setPic(pic);
                String description = (String) sourceAsMap.get("description");
                coursePub.setDescription(description);
                String teachplan = (String) sourceAsMap.get("teachplan");
                coursePub.setTeachplan(teachplan);

                map.put(courseId, coursePub);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    //根据课程计划多个id查询课程媒资信息
    public QueryResponseResult<TeachplanMediaPub> getmedia(String[] teachplanIds) {
        //定义一个搜索请求对象
        SearchRequest searchRequest = new SearchRequest(media_index);
        //指定type
        searchRequest.types(media_type);
        //定义一个searchBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置使用termsQuery，根据多个id查询
        searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id",teachplanIds));
        //过滤源字段,，
        String[] split = media_source_field.split(",");

        searchSourceBuilder.fetchSource(split, new String[]{});
        searchRequest.source(searchSourceBuilder);

        //进行搜索，使用Es可魂断进行搜索请求ES
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        long total = 0;
        try {
            SearchResponse search = restHighLevelClient.search(searchRequest);
            SearchHits hits = search.getHits();
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit hit : searchHits) {
                TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                //取出课程计划媒资信息
                String courseid = (String) sourceAsMap.get("courseid");
                String media_id = (String) sourceAsMap.get("media_id");
                String media_url = (String) sourceAsMap.get("media_url");
                String teachplan_id = (String) sourceAsMap.get("teachplan_id");
                String media_fileoriginalname = (String) sourceAsMap.get("media_fileoriginalname");
                teachplanMediaPub.setCourseId(courseid);
                teachplanMediaPub.setMediaUrl(media_url);
                teachplanMediaPub.setMediaFileOriginalName(media_fileoriginalname);
                teachplanMediaPub.setMediaId(media_id);
                teachplanMediaPub.setTeachplanId(teachplan_id);
                //将数据加入列表
                teachplanMediaPubList.add(teachplanMediaPub);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //数据集合
        QueryResult<TeachplanMediaPub> queryResult = new QueryResult<>();
        queryResult.setList(teachplanMediaPubList);
        queryResult.setTotal(total);
        QueryResponseResult<TeachplanMediaPub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);

        return queryResponseResult;

    }



}
