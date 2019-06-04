package com.ichifun.git;


import com.alibaba.fastjson.JSON;
import com.ichifun.git.config.GitInfo;
import com.ichifun.git.gitlab.GitLabApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gitlab.api.models.GitlabGroup;
import org.gitlab.api.models.GitlabProject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = GitAutoConfiguration.class)
public class GitLabApiTest {

    @Autowired
    private GitLabApi gitLabApi;

    private GitInfo gitInfo;

    private final String gitUrl = "http://11.1.63.19";
    private final String accessToken = "Yp-AoDfr9dWTyEmEP2W9";

    private final String title = "test";
    private final String tag = "tag";

    @Before
    public void init() {
        gitLabApi.initConnect(new GitInfo(gitUrl, accessToken));
    }

    //    @Test
    public void getGroupsTest() throws IOException {
        List<GitlabGroup> gitlabGroups = gitLabApi.getGroups();
        log.debug(JSON.toJSONString(gitlabGroups));
    }


    //    @Test
    public void getRemoteProjectTest() throws IOException {
        List<GitlabProject> gitLabApiProject = gitLabApi.getProject();
        log.debug(JSON.toJSONString(gitLabApiProject));
    }


    //    @Test
    public void addGroupTest() throws IOException {
        boolean status = false;
        List<GitlabGroup> gitlabGroups = gitLabApi.getGroups();

        for (GitlabGroup gitlabGroup : gitlabGroups) {
            if (gitlabGroup.getName().equalsIgnoreCase("groupOneg")) {
                status = true;
            }
        }
        if (!status) {
            Integer gitlabGroup = gitLabApi.addGroup("groupOneg");

            log.debug(JSON.toJSONString(gitlabGroup));
        } else {
            log.debug(JSON.toJSONString("groupName is exist"));
            return;
        }

    }

    @Test
    public void getProjecCloneTest() throws InterruptedException, GitAPIException, ExecutionException, IOException {
        GitInfo gitDir = gitLabApi.getProjecClone("http://10.1.63.9/groupTwdfddoovsooo/projectone.git", "master");
        log.debug(JSON.toJSONString(gitDir));
    }


    @Test
    public void createProjectAndCloneTest() throws IOException, GitAPIException, ExecutionException, InterruptedException {
        Integer gitlabGroupId = gitLabApi.addGroup("groupTwostsvvgw32Yf");
        GitlabGroup gitlabGroup = gitLabApi.getGroupById(gitlabGroupId);
        GitInfo gitDir = gitLabApi.createProjectAndClone("projectOnegw12Yf", gitlabGroup);
        log.debug(JSON.toJSONString(gitDir));
    }

    @Test
    public void commitAndPushGitRepositoryTest() throws Exception {
        Integer gitlabGroupId = gitLabApi.addGroup("groupTwostsvvgw52Yf");
        GitlabGroup gitlabGroup = gitLabApi.getGroupById(gitlabGroupId);
        GitInfo gitDir = gitLabApi.createProjectAndClone("projectOnegw22Yf", gitlabGroup);
        log.debug(JSON.toJSONString(gitDir));
        FileUtils.write(new File(gitDir.getProjectDir() + "/cxyapi.txt"), "程序换api", "UTF-8", true);
        boolean status = gitLabApi.commitAndPushGitRepository(gitDir.getProjectDir(), title);
        Assert.assertTrue(status);
    }

    @Test
    public void addTagAndPushTest() throws Exception {
        Integer gitlabGroupId = gitLabApi.addGroup("groupTwosdddddstsvvgw6YGsf");
        GitlabGroup gitlabGroup = gitLabApi.getGroupById(gitlabGroupId);
        GitInfo gitDir = gitLabApi.createProjectAndClone("projectOnegw7YGsf", gitlabGroup);
        FileUtils.write(new File(gitDir.getProjectDir() + "/cxyapi.txt"), "程序换api", "UTF-8", true);
        gitLabApi.commitAndPushGitRepository(gitDir.getProjectDir(), title);

        boolean status = gitLabApi.addTagAndPush(gitDir.getProjectDir(), tag);
        Assert.assertTrue(status);
    }


}
