package com.itheima.utis;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.TableId;
import com.itheima.pojo.PageResult;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 虎哥
 */
public class ElasticUtils<T> {

    private RestHighLevelClient client;
    private String indexName;
    private Class<T> clazz;

    public ElasticUtils(RestHighLevelClient client, String indexName, Class<T> clazz) {
        this.client = client;
        this.indexName = indexName;
        this.clazz = clazz;
    }

    /**
     * 索引库的创建
     *
     * @param source    settings和mappings的json字符串
     */
    public void createIndex(String source) {
        try {
            // 1.创建Request对象
            CreateIndexRequest request = new CreateIndexRequest(indexName);
            // 2.准备参数，settings和mappings
            request.source(source, XContentType.JSON);
            // 3.发出请求
            CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
            // 4.判断
            if (!response.isAcknowledged()) {
                // 创建索引库失败！
                throw new RuntimeException("索引库创建失败！");
            }
        } catch (IOException e) {
            throw new RuntimeException("索引库创建失败！", e);
        }
    }

    /**
     * 删除索引库
     *
     */
    public void deleteIndex() {
        try {
            // 1.创建Request对象
            DeleteIndexRequest request = new DeleteIndexRequest(indexName);
            // 2.发出请求
            client.indices().delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("索引库删除失败！", e);
        }
    }

    /**
     * 文档的新增
     * @param t 文档数据对象
     */
    public void save(T t) {
        try {
            // 1.创建Request对象
            IndexRequest request = new IndexRequest(indexName);
            // 2.准备参数，id和文档数据
            request.id(getId(t));
            request.source(JSON.toJSONString(t), XContentType.JSON);
            // 3.发出请求
            client.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("新增文档失败！", e);
        }
    }

    /**
     * 文档批量新增
     * @param list 数据集合
     */
    public void saveBatch(List<T> list){
        try {
            // 1.创建Request对象
            BulkRequest request = new BulkRequest(indexName);
            // 2.准备参数
            for (T t : list) {
               request.add(new IndexRequest().id(getId(t)).source(JSON.toJSONString(t), XContentType.JSON));
            }
            // 3.发出请求
            client.bulk(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("批量新增失败！", e);
        }
    }

    /**
     * 文档的删除
     * @param id 文档id
     */
    public void deleteById(String id){
        try {
            // 1.请求
            DeleteRequest request = new DeleteRequest(indexName, id);
            // 2.发出请求
            client.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException("删除文档失败！", e);
        }
    }

    /**
     * 文档的根据id查询
     * @param id id
     * @return 文档对象
     */
    public T getById(String id){
        try {
            // 1.请求
            GetRequest request = new GetRequest(indexName, id);
            // 2.发出请求
            GetResponse response = client.get(request, RequestOptions.DEFAULT);
            // 3.解析结果
            String json = response.getSourceAsString();
            // 4.反序列化
            return JSON.parseObject(json, clazz);

        } catch (IOException e) {
            throw new RuntimeException("删除文档失败！", e);
        }
    }

    /**
     * 查询并返回结果，并且带高亮、分页、排序
     * @param sourceBuilder 条件
     * @return 分页结果
     */
    public PageResult<T> search(SearchSourceBuilder sourceBuilder){
        try {
            // 1.准备request
            SearchRequest request = new SearchRequest();
            // 2.准备参数
            request.source(sourceBuilder);
            // 3.发出请求
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);

            // 4.解析结果
            SearchHits searchHits = response.getHits();
            // 4.1.获取总条数
            long total = searchHits.getTotalHits().value;
            // 4.2.获取数据
            SearchHit[] hits = searchHits.getHits();
            if(hits.length == 0){
                return new PageResult<>();
            }
            List<T> list = new ArrayList<>(hits.length);
            // 4.3.循环处理
            for (SearchHit hit : hits) {
                // 获取source
                String json = hit.getSourceAsString();
                // 反序列化
                T t = JSON.parseObject(json, clazz);
                list.add(t);
                // 高亮处理
                handleHighlight(t, hit);
            }

            // 5.封装分页结果
            PageResult<T> result = new PageResult<>();
            result.setTotal(total);
            result.setData(list);
            return result;

        } catch (Exception e) {
            throw new RuntimeException("查询文档失败！", e);
        }
    }

    private void handleHighlight(T t, SearchHit hit) throws InvocationTargetException, IllegalAccessException {
        Map<String, HighlightField> highlightFields = hit.getHighlightFields();
        for (HighlightField highlightField : highlightFields.values()) {
            // 获取高亮字段名称
            String fieldName = highlightField.getName();
            // 获取高亮结果
            String highlightValue = StringUtils.join(highlightField.getFragments());
            // 获取字节码
            BeanUtils.setProperty(t, fieldName, highlightValue);
            /*Class<?> tClass = t.getClass();
            Field field = tClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(t, highlightValue);*/
        }
    }

    private String getId(Object t) {
        try {
            Class<?> tClass = t.getClass();
            Field[] declaredFields = tClass.getDeclaredFields();
            Object id = null;
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                if (declaredField.isAnnotationPresent(TableId.class)) {
                    id = declaredField.get(t);
                }
            }
            if (id == null) {
                throw new RuntimeException("实体类中必须包含@TableId注解！");
            }
            return id.toString();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
