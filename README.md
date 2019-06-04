# freesun-git

#### 介绍
spring  boot git starter 提供git操作快速操作集合，支持 github,gitlab,gitee平台快捷操作。

#### 软件架构
软件架构说明


#### 安装教程

依赖引用:
 <dependency>
    <groupId>com.ichifun</groupId>
    <artifactId>spring-boot-starter-git</artifactId>
    <version>1.0.0</version>
</dependency>

#### 使用说明

1. 创建连接  gitLabApi.initConnect(new GitInfo(gitUrl, accessToken));
2. 提供接口如下：
   
    /**
     * @desc 初始化远端gitlab
     */
    public void initConnect(GitInfo gitInfo);

    /**
     * @return
     * @desc 远端获取所有项目
     */
    public List<GitlabProject> getProject() throws IOException;


    /**
     * @return
     * @desc 远端添加组
     */
    public Integer addGroup(String groupName) throws IOException;


    /**
     * @return
     * @desc 远端获取所有组
     */
    public List<GitlabGroup> getGroups() throws IOException;


    /**
     * @return
     * @desc 根据id远端获取所有组
     */
    public GitlabGroup getGroupById(Integer groupId) throws IOException;

    /**
     * @return
     * @desc 为项目添加用户
     */
    public GitlabProjectMember addProjectMember(Integer projectId, Integer userId) throws IOException;

    /**
     * @return
     * @desc 远端创建项目
     */
    public GitInfo createProjectAndClone(String projectName, GitlabGroup group) throws IOException, GitAPIException, ExecutionException, InterruptedException;


    /**
     * @return
     * @desc pull远端项目
     */
    public GitInfo getProjecClone(String gitProjectUrl, String branchName) throws IOException, GitAPIException, ExecutionException, InterruptedException;


    /**
     * 将文件列表提交到git仓库中并推送到远端仓库
     *
     * @param gitRoot 提交代码并推送，完成后删除代码
     * @return 返回本次推送结果
     * @throws IOException
     */
    public boolean commitAndPushGitRepository(String gitRoot,String title) throws Exception;


    /**
     * 将git仓库内容回滚到指定版本的上一个版本
     *
     * @param gitRoot  仓库目录
     * @param revision 指定的版本号
     * @return true, 回滚成功, 否则flase
     * @throws IOException
     */
    public boolean rollBackPreRevision(String gitRoot, String revision) throws IOException, GitAPIException, ExecutionException, InterruptedException;


    /**
     * 将git仓库内容回滚到指定版本的上一个版本
     *
     * @param gitRoot 创建tag并推送，完成后删除文件
     * @return true, 回滚成功, 否则flase
     * @throws IOException
     */
    public boolean addTagAndPush(String gitRoot,String tag) throws IOException, GitAPIException, ExecutionException, InterruptedException;


    /**
     * 查询本次提交的日志
     *
     * @param gitRoot  git仓库
     * @param revision 版本号
     * @return
     * @throws Exception
     */
    public List<DiffEntry> getLog(String gitRoot, String revision) throws Exception;


    /**
     * 查询当前最后一次提交版本
     *
     * @param gitRoot git仓库
     * @return
     * @throws Exception
     */
    public String getRevisionInfo(String gitRoot) throws Exception;

#### 参与贡献

1. Fork 本仓库
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request