package com.itheima.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author 虎哥
 */
@Data
public class PageResult<T> {
    private Long total;
    private List<T> data;
}
