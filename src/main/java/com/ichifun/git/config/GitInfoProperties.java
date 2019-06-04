package com.ichifun.git.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author: fyname@163.com
 * @desc: git 连接属性配置
 * @date: created in 2018-06-15 11:37
 * @modifed by:
 */
@Data
@ConfigurationProperties(prefix = GitInfoProperties.PREFIX)
public class GitInfoProperties {

    public static final String PREFIX = "spring.git";

    //本地源码存放路径
    private String rep;

    //支持git服务地址
    private int mode;

}
