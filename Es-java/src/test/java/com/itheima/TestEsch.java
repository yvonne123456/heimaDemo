package com.itheima;

import com.alibaba.fastjson.JSON;
import com.itheima.pojo.User;
import com.itheima.service.UserService;
import com.itheima.utis.IndexConstants;
import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;


@RunWith(SpringRunner.class)
@SpringBootTest
public class TestEsch {

    @Autowired
    private UserService userService;

    private RestHighLevelClient client;


    /**
     * 1.库和映射的操作
     */
    @Test
    public void testCreateIndex() throws IOException {
        // 1.创建一个用来创建索引库request对象，包含索引库名、setting和mappings
        CreateIndexRequest request = new CreateIndexRequest("users");
        // 2.设置setting和mappings
        request.source(IndexConstants.INDEX_SETTINGS, XContentType.JSON);
        // 3.发出请求
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);

        System.out.println("createIndexResponse = " + createIndexResponse);
    }




    @Test
    public void testAddDocument() throws IOException {
        // 去数据库查询数据
        User user = userService.getById(1L);
        // 1.准备新增文档的请求对象
        IndexRequest request = new IndexRequest("users")
                // 2.指定参数
                .id(user.getId().toString())
                .source(JSON.toJSONString(user), XContentType.JSON);
        // 3.发出请求
        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);

        System.out.println("indexResponse = " + JSON.toJSONString(indexResponse));
    }
    @Test
    public void GetDocument() throws IOException {
        // 1.准备新增文档的请求对象
        GetRequest request = new GetRequest("users", "1");
        // 3.发出请求
        GetResponse response = client.get(request, RequestOptions.DEFAULT);

        String json = response.getSourceAsString();
        User user = JSON.parseObject(json, User.class);

        System.out.println("user = " + user);
    }



    @Before
    public void setUp() throws Exception {
        this.client = new RestHighLevelClient(
                RestClient.builder(
                        HttpHost.create("http://localhost:9200"),
                        HttpHost.create("http://localhost:9201"),
                        HttpHost.create("http://localhost:9202")
                )
        );
    }

    @After
    public void tearDown() throws Exception {
        client.close();
    }

}


