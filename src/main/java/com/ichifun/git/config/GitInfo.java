package com.ichifun.git.config;

import lombok.Data;

/**
 * @author: fyname@163.com
 * @desc: git连接信息
 * @date: created in 2018-06-15 11:37
 * @modifed by:
 */
@Data
public class GitInfo {

    //gitlab中项目git地址
    private String gitUrl;

    //个人在gitlab上的token
    private String accessToken;

    //本地项目路径
    private String projectDir;

    //git项目地址
    private String projectUrl;

    public GitInfo() {
    }

    public GitInfo(String gitUrl, String accessToken) {
        this.gitUrl = gitUrl;
        this.accessToken = accessToken;
    }
}
