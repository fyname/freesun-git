package com.ichifun.git;

import com.ichifun.git.config.GitInfoProperties;
import com.ichifun.git.gitlab.GitLabApiV4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author: fyname@163.com
 * @desc:
 * @date: created in 2019-04-26 10:17
 * @modifed by:
 */

@Configuration
@EnableConfigurationProperties(GitInfoProperties.class)
@ComponentScan({"com.ichifun.git"})
public class GitAutoConfiguration {

    @Autowired
    private GitInfoProperties gitInfoProperties;


    @Bean
    public GitLabApiV4 gitLabApiV4() {
        return new GitLabApiV4();
    }
}
