package com.itheima.pojo;

import lombok.Data;

@Data
public class User {


    private Long id;

    private String gender; // 性别

    private String name;// 姓名

    private Integer age;// 年龄

    private String note;// 备注
}