package com.itheima.pojo;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_user")
public class User {
    @TableId
    private Long id;

    private String name;// 姓名

    private Integer age;// 年龄

    private String gender;// 性别

    private String note;// 备注
}