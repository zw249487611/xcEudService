package com.xuecheng.search;

import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class testSearch {
    @Autowired
    RestHighLevelClient client;

    @Autowired
    RestClient restClient;

    //搜索全部记录
    @Test
    public void testSearchAll() throws Exception {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            //搜索方式
            //matchAllQuery搜索全部
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            //设置搜索源字段过滤
                //第一个参数结果集包括哪些字段，第二个参数结果集不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"}, new String[]{});


        //向搜素请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        //执行搜素,向ES发起HTTP请求
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
            //日期格式化对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            //文档的主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //由于前面设置了源文档字段的过滤，这里description是娶不到的
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期
            Date timestamp = dateFormat.parse((String)sourceAsMap.get("timestamp"));

            System.out.println("name:"+name);
            System.out.println("description:"+description);
            System.out.println("studymodel:"+studymodel);
            System.out.println("price:"+price);
            System.out.println("timestamp:"+timestamp);
        }
    }

    //分页查询
    @Test
    public void testSearchPage() throws Exception {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            //设置分页参数
            //页码
//        int page = 1;
//            //每页显示条数
//        int size = 2;
//        //计算出来记录的起始下标
//        int from = (page - 1) / size;
//        searchSourceBuilder.from(from);//起始的记录下标，从0开始
//        searchSourceBuilder.size(size);//每页显示的记录数
        //搜索方式
        //matchAllQuery搜索全部
//        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.query(QueryBuilders.termQuery("name","spring"));//为关键字请确查询，如果value很多值的话，就按很多值精确查询
        //设置搜索源字段过滤
        //第一个参数结果集包括哪些字段，第二个参数结果集不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"}, new String[]{});
        //向搜素请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        //执行搜素,向ES发起HTTP请求
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
        //日期格式化对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            //文档的主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //由于前面设置了源文档字段的过滤，这里description是娶不到的
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期
            Date timestamp = dateFormat.parse((String)sourceAsMap.get("timestamp"));

            System.out.println("name:"+name);
            System.out.println("description:"+description);
            System.out.println("studymodel:"+studymodel);
            System.out.println("price:"+price);
            System.out.println("timestamp:"+timestamp);
        }
        System.out.println("总记录数"+totalHits);
    }


    //term查询，搜索的时候不分词
    @Test
    public void testTermQuery() throws Exception {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置分页参数
        //页码
        int page = 1;
        //每页显示条数
        int size = 2;
        //计算出来记录的起始下标
        int from = (page - 1) / size;
        searchSourceBuilder.from(from);//起始的记录下标，从0开始
        searchSourceBuilder.size(size);//每页显示的记录数
        //搜索方式
        //matchAllQuery搜索全部,
            //根据name查询
//        searchSourceBuilder.query(QueryBuilders.termQuery("name","spring"));
            //根据id查询
        // 定义id
        String[] ids = new String[]{"2","3"};
        searchSourceBuilder.query(QueryBuilders.termsQuery("_id",ids));
        //设置搜索源字段过滤
        //第一个参数结果集包括哪些字段，第二个参数结果集不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"}, new String[]{});
        //向搜素请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        //执行搜素,向ES发起HTTP请求
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
        //日期格式化对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            //文档的主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //由于前面设置了源文档字段的过滤，这里description是娶不到的
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期
            Date timestamp = dateFormat.parse((String)sourceAsMap.get("timestamp"));

            System.out.println("name:"+name);
            System.out.println("description:"+description);
            System.out.println("studymodel:"+studymodel);
            System.out.println("price:"+price);
            System.out.println("timestamp:"+timestamp);
        }
        System.out.println("总记录数"+totalHits);
    }

    //macth查询,搜索的时候分词
    @Test
    public void testMatchQuery() throws Exception {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        //设置分页参数
//        //页码
//        int page = 1;
//        //每页显示条数
//        int size = 2;
//        //计算出来记录的起始下标
//        int from = (page - 1) / size;
//        searchSourceBuilder.from(from);//起始的记录下标，从0开始
//        searchSourceBuilder.size(size);//每页显示的记录数
        //搜索方式
        //matchAllQuery搜索全部,
        //根据name查询
//        searchSourceBuilder.query(QueryBuilders.termQuery("name","spring"));
        //根据id查询
        // 定义id
        String[] ids = new String[]{"2","3"};
        searchSourceBuilder.query(QueryBuilders.matchQuery("description","spring开发框架")
                                            .minimumShouldMatch("80%"));
        //设置搜索源字段过滤
        //第一个参数结果集包括哪些字段，第二个参数结果集不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"}, new String[]{});
        //向搜素请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        //执行搜素,向ES发起HTTP请求
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
        //日期格式化对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            //文档的主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //由于前面设置了源文档字段的过滤，这里description是娶不到的
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期
            Date timestamp = dateFormat.parse((String)sourceAsMap.get("timestamp"));

            System.out.println("name:"+name);
            System.out.println("description:"+description);
            System.out.println("studymodel:"+studymodel);
            System.out.println("price:"+price);
            System.out.println("timestamp:"+timestamp);
        }
        System.out.println("总记录数"+totalHits);
    }

    //Multi_macth查询,搜索的时候分词
    @Test
    public void testMultiMatchQuery() throws Exception {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        //设置分页参数
//        //页码
//        int page = 1;
//        //每页显示条数
//        int size = 2;
//        //计算出来记录的起始下标
//        int from = (page - 1) / size;
//        searchSourceBuilder.from(from);//起始的记录下标，从0开始
//        searchSourceBuilder.size(size);//每页显示的记录数
        //搜索方式
        //matchAllQuery搜索全部,
        //根据name查询
//        searchSourceBuilder.query(QueryBuilders.termQuery("name","spring"));
        //根据id查询
        // 定义id
        String[] ids = new String[]{"2","3"};
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery("spring开发框架","name","description")
                                .minimumShouldMatch("50%")
                                .field("name",10));
        //定义一个boolQuery


        //设置搜索源字段过滤
        //第一个参数结果集包括哪些字段，第二个参数结果集不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"}, new String[]{});
        //向搜素请求对象中设置搜索源
        searchRequest.source(searchSourceBuilder);

        //执行搜素,向ES发起HTTP请求
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
        //日期格式化对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            //文档的主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //由于前面设置了源文档字段的过滤，这里description是娶不到的
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期
            Date timestamp = dateFormat.parse((String)sourceAsMap.get("timestamp"));

            System.out.println("name:"+name);
            System.out.println("description:"+description);
            System.out.println("studymodel:"+studymodel);
            System.out.println("price:"+price);
            System.out.println("timestamp:"+timestamp);
        }
        System.out.println("总记录数"+totalHits);
    }

    //布尔查询查询,搜索的时候分词
    @Test
    public void testBoolQuery() throws Exception {
        //创建搜索请求对象
        SearchRequest searchRequest= new SearchRequest("xc_course");
        searchRequest.types("doc");
//创建搜索源配置对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.fetchSource(new String[]{"name","pic","studymodel"},new String[]{});
            //multiQuery
        String keyword = "spring开发";
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring框架",
                "name", "description")
                .minimumShouldMatch("50%");
        multiMatchQueryBuilder.field("name",10);
             //TermQuery
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("studymodel", "201001");

        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(multiMatchQueryBuilder);
        boolQueryBuilder.must(termQueryBuilder);
            //设置布尔查询对象
        searchSourceBuilder.query(boolQueryBuilder);
        //设置搜索源字段过滤
        //第一个参数结果集包括哪些字段，第二个参数结果集不包括哪些字段
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"}, new String[]{});

        searchRequest.source(searchSourceBuilder);//设置搜索源配置
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
        //日期格式化对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            //文档的主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //由于前面设置了源文档字段的过滤，这里description是娶不到的
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期
            Date timestamp = dateFormat.parse((String)sourceAsMap.get("timestamp"));

            System.out.println("name:"+name);
            System.out.println("description:"+description);
            System.out.println("studymodel:"+studymodel);
            System.out.println("price:"+price);
            System.out.println("timestamp:"+timestamp);
        }
        System.out.println("总记录数"+totalHits);
    }


    //过虑器
    @Test
    public void testFielte() throws Exception {
        //创建搜索请求对象
        SearchRequest searchRequest= new SearchRequest("xc_course");
        searchRequest.types("doc");
//创建搜索源配置对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        searchSourceBuilder.fetchSource(new String[]{"name","pic","studymodel"},new String[]{});
        //multiQuery
//        String keyword = "spring开发";
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring框架",
                "name", "description")
                .minimumShouldMatch("50%")
                .field("name",10);

        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(multiMatchQueryBuilder);
            //定义一个过滤器
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel","201001"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(60).lte(100));

        //设置布尔查询对象
        searchSourceBuilder.query(boolQueryBuilder);
        //设置搜索源字段过滤
        //第一个参数结果集包括哪些字段，第二个参数结果集不包括哪些字段
//        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"}, new String[]{});

        searchRequest.source(searchSourceBuilder);//设置搜索源配置
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
        //日期格式化对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            //文档的主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //由于前面设置了源文档字段的过滤，这里description是娶不到的
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期
            Date timestamp = dateFormat.parse((String)sourceAsMap.get("timestamp"));

            System.out.println("name:"+name);
            System.out.println("description:"+description);
            System.out.println("studymodel:"+studymodel);
            System.out.println("price:"+price);
            System.out.println("timestamp:"+timestamp);
        }
        System.out.println("总记录数"+totalHits);
    }


    //排序
    @Test
    public void testSort() throws Exception {
        //创建搜索请求对象
        SearchRequest searchRequest= new SearchRequest("xc_course");
        searchRequest.types("doc");
//创建搜索源配置对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        searchSourceBuilder.fetchSource(new String[]{"name","pic","studymodel"},new String[]{});


        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //定义一个过滤器
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));


        //设置布尔查询对象
        searchSourceBuilder.query(boolQueryBuilder);

                //添加排序
        searchSourceBuilder.sort("studymodel", SortOrder.DESC);
        searchSourceBuilder.sort("price", SortOrder.ASC);
        //设置搜索源字段过滤
        //第一个参数结果集包括哪些字段，第二个参数结果集不包括哪些字段
//        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"}, new String[]{});

        searchRequest.source(searchSourceBuilder);//设置搜索源配置
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
        //日期格式化对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            //文档的主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            //由于前面设置了源文档字段的过滤，这里description是娶不到的
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期
            Date timestamp = dateFormat.parse((String)sourceAsMap.get("timestamp"));

            System.out.println("name:"+name);
            System.out.println("description:"+description);
            System.out.println("studymodel:"+studymodel);
            System.out.println("price:"+price);
            System.out.println("timestamp:"+timestamp);
        }
        System.out.println("总记录数"+totalHits);
    }


    //高亮显示
    @Test
    public void testHightLight() throws Exception {
        //创建搜索请求对象
        SearchRequest searchRequest= new SearchRequest("xc_course");
        searchRequest.types("doc");
//创建搜索源配置对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        searchSourceBuilder.fetchSource(new String[]{"name","pic","studymodel"},new String[]{});
        //multiQuery
//        String keyword = "spring开发";
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("开发框架",
                "name", "description")
                .minimumShouldMatch("50%")
                .field("name",10);

        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(multiMatchQueryBuilder);
        //定义一个过滤器
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0).lte(100));

        //设置布尔查询对象
        searchSourceBuilder.query(boolQueryBuilder);
        //设置搜索源字段过滤
        //第一个参数结果集包括哪些字段，第二个参数结果集不包括哪些字段
//        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price","timestamp"}, new String[]{});

        //设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<red>");
        highlightBuilder.postTags("</red>");
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        searchSourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);//设置搜索源配置
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        //匹配到的总记录数
        long totalHits = hits.getTotalHits();
        //得到匹配度高的文档
        SearchHit[] searchHits = hits.getHits();
        //日期格式化对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (SearchHit hit : searchHits) {
            //文档的主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //源文档的name字段
            String name = (String) sourceAsMap.get("name");
            //取出name高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (highlightFields != null) {
                HighlightField nameHighLightField = highlightFields.get("name");
                if (nameHighLightField != null) {
                    Text[] fragments = nameHighLightField.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (Text fragment : fragments) {
                        stringBuffer.append(fragment);
                    }
                    name = stringBuffer.toString();
                }
            }
            //由于前面设置了源文档字段的过滤，这里description是娶不到的
            String description = (String) sourceAsMap.get("description");
            //学习模式
            String studymodel = (String) sourceAsMap.get("studymodel");
            //价格
            Double price = (Double) sourceAsMap.get("price");
            //日期
            Date timestamp = dateFormat.parse((String)sourceAsMap.get("timestamp"));

            System.out.println("name:"+name);
            System.out.println("description:"+description);
            System.out.println("studymodel:"+studymodel);
            System.out.println("price:"+price);
            System.out.println("timestamp:"+timestamp);
        }
        System.out.println("总记录数"+totalHits);
    }
}
