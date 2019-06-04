package com.ichifun.git.gitlab;

import com.ichifun.git.common.Util;
import com.ichifun.git.config.GitInfo;
import com.ichifun.git.config.GitInfoProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author: fyname@163.com
 * @desc: git V3版本实现
 * @date: created in 2019-04-26 10:17
 * @modifed by:
 */
@Slf4j
@EnableConfigurationProperties({GitInfoProperties.class})
public class GitLabApiV4 implements GitLabApi {

    //多线程异步执行
    private static ExecutorService pool = new ThreadPoolExecutor(6, 10, 3L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(1024), new ThreadPoolExecutor.AbortPolicy());

    private final static String GIT = ".git";

    private static int timeout = 15;
    private static CloneCommand.Callback callback = new CloneCommand.Callback() {
        @Override
        public void initializedSubmodules(Collection<String> submodules) {

        }

        @Override
        public void cloningSubmodule(String path) {
            log.error("path...:" + path);
        }

        @Override
        public void checkingOut(AnyObjectId commit, String path) {
            log.error("checkingOut:" + commit.getName() + ",path:" + path);
        }
    };
    private static final ConcurrentMap<String, Lock> CONCURRENT_MAP = new ConcurrentHashMap<>();

    private GitlabAPI gitLabAPIV4;

    @Autowired
    private GitInfoProperties gitInfoProperties;

    private GitInfo gitInfo;

    private String username;
    private String email;

    @Override
    public void initConnect(GitInfo gitInfo) {
        this.gitInfo = gitInfo;
        gitLabAPIV4 = GitlabAPI.connect(gitInfo.getGitUrl(), gitInfo.getAccessToken());
    }

    @Override
    public List<GitlabProject> getProject() throws IOException {

        return gitLabAPIV4.getOwnedProjects();
    }

    @Override
    public Integer addGroup(String groupName) throws IOException {
        GitlabGroup gitlabGroup = null;
        gitlabGroup = gitLabAPIV4.createGroup(groupName);
        return gitlabGroup.getId();
    }

    @Override
    public List<GitlabGroup> getGroups() throws IOException {
        return gitLabAPIV4.getGroups();
    }

    @Override
    public GitlabGroup getGroupById(Integer groupId) throws IOException {
        return gitLabAPIV4.getGroup(groupId);
    }

    @Override
    public GitlabProjectMember addProjectMember(Integer projectId, Integer userId) throws IOException {
        GitlabAccessLevel gitlabAccessLevel = GitlabAccessLevel.fromAccessValue(GitlabAccessLevel.Master.accessValue);
        return gitLabAPIV4.addProjectMember(projectId, userId, gitlabAccessLevel);
    }

    @Override
    public GitInfo createProjectAndClone(String projectName, GitlabGroup group) throws ExecutionException, InterruptedException {
        Callable callable = new Callable() {
            @Override
            public Object call() throws Exception {
                GitInfo res = createProject(projectName, group);

                return res;
            }
        };

        Future future = pool.submit(callable);
        return (GitInfo) future.get();
    }

    @Override
    public GitInfo getProjecClone(String gitProjectUrl, String branchName) throws ExecutionException, InterruptedException {
        Callable callable = new Callable() {
            @Override
            public Object call() throws Exception {
                GitInfo res = getProject(gitProjectUrl, branchName);

                return res;
            }
        };

        Future future = pool.submit(callable);
        return (GitInfo) future.get();

    }


    @Override
    public boolean commitAndPushGitRepository(String gitRoot, String title) throws ExecutionException, InterruptedException {

        Callable callable = new Callable() {
            @Override
            public Object call() throws Exception {
                boolean res = commit(gitRoot, title);
                return res;
            }
        };

        Future future = pool.submit(callable);
        return (boolean) future.get();

    }

    @Override
    public boolean rollBackPreRevision(String gitRoot, String revision) throws ExecutionException, InterruptedException {
        Callable callable = new Callable() {
            @Override
            public Object call() throws Exception {
                boolean res = rollBack(gitRoot, revision);
                return res;
            }
        };

        Future future = pool.submit(callable);
        return (boolean) future.get();
    }

    @Override
    public boolean addTagAndPush(String gitRoot, String tag) throws ExecutionException, InterruptedException {
        Callable callable = new Callable() {
            @Override
            public Object call() throws Exception {
                boolean res = addTag(gitRoot, tag);
                return res;
            }
        };

        Future future = pool.submit(callable);
        return (boolean) future.get();
    }

    @Override
    public List<DiffEntry> getLog(String gitRoot, String revision) throws Exception {
        Git git = Git.open(new File(gitRoot));
        Repository repository = git.getRepository();

        ObjectId objId = repository.resolve(revision);
        Iterable<RevCommit> allCommitsLater = git.log().add(objId).call();
        Iterator<RevCommit> iter = allCommitsLater.iterator();
        RevCommit commit = iter.next();
        TreeWalk tw = new TreeWalk(repository);
        tw.addTree(commit.getTree());

        commit = iter.next();
        if (commit != null)
            tw.addTree(commit.getTree());
        else
            return null;

        tw.setRecursive(true);
        RenameDetector rd = new RenameDetector(repository);
        rd.addAll(DiffEntry.scan(tw));

        return rd.compute();
    }

    @Override
    public String getRevisionInfo(String gitRoot) throws Exception {
        Map<String, Date> map = new HashMap<>();
        Git git = Git.open(new File(gitRoot));
        Iterable<RevCommit> gitlog = git.log().call();
        for (RevCommit revCommit : gitlog) {
            String version = revCommit.getName();//版本号
            revCommit.getAuthorIdent().getName();
            revCommit.getAuthorIdent().getEmailAddress();
            Date time = revCommit.getAuthorIdent().getWhen();//时间
            log.debug("version: {}", version);
            map.put(version, time);
        }
        String ver = Util.mapFind(map, new Date());

        return ver;
    }


    private GitInfo createProject(String projectName, GitlabGroup group) throws IOException {

        //唯一路径
        String gitDir = gitInfoProperties.getRep() + "/" + Util.getMd5(group.getName() + projectName + new Date().getTime());


        List<GitlabProject> gitlabProjects = gitLabAPIV4.getOwnedProjects();
        GitlabProject isProject = gitlabProjects.stream().filter(gitlabProject -> gitlabProject.getName().equalsIgnoreCase(projectName.toLowerCase())).findAny().orElse(null);
        GitlabProject gitlabProject = null;
        GitlabUser gitlabUser = gitLabAPIV4.getUser();
        if (isProject == null) {
            gitlabProject = gitLabAPIV4.createProjectForGroup(projectName, group);
            GitlabAccessLevel gitlabAccessLevel = GitlabAccessLevel.fromAccessValue(GitlabAccessLevel.Master.accessValue);
            gitLabAPIV4.addProjectMember(gitlabProject.getId(), gitlabUser.getId(), gitlabAccessLevel);
        } else {
            gitlabProject = isProject;
        }
        username = gitlabUser.getUsername();
        email = gitlabUser.getEmail();
        String uri = gitlabProject.getHttpUrl();
        log.debug("url:{},username:{},emal{}", uri, username, email);

        CredentialsProvider cp = credentialsProvider(username, gitInfo.getAccessToken());

        Git git = null;
        File repoDir = null;
        Lock cacheLock = getCacheLock(gitDir);
        cacheLock.lock();
        try {
            repoDir = getCacheDir(gitDir);
            git = Git.cloneRepository()
                    .setTimeout(timeout)
                    .setURI(uri)
                    .setDirectory(repoDir)
                    .setCredentialsProvider(cp)
                    .setRemote("origin")
                    .setBranch("master")
                    .setCallback(callback)
                    .call();
            log.info("Cloning from " + uri + " to " + git.getRepository());

        } catch (JGitInternalException e) {
            log.error("JGitInternalException" + e);

        } catch (RefNotFoundException e) {
            log.error("RefNotFoundException" + e);

        } catch (Exception e) {
            log.error("Exception" + e);

        } finally {
            cacheLock.unlock();
            if (git != null) {
                git.close();
            }
        }

        log.debug("Cloning from " + gitlabProject.getWebUrl() + " to " + gitDir);
        GitInfo gitInfo = new GitInfo();
        gitInfo.setProjectDir(gitDir);
        gitInfo.setProjectUrl(gitlabProject.getWebUrl());
        return gitInfo;
    }

    private GitInfo getProject(String gitProjectUrl, String branchName) throws IOException {

        //唯一路径
        String[] ns = gitProjectUrl.split("/");
        String gitDir = gitInfoProperties.getRep() + "/" + Util.getMd5(ns[ns.length - 2] + ns[ns.length - 1] + new Date().getTime());

        FileUtils.forceDelete(new File(gitDir));

        GitlabUser gitlabUser = gitLabAPIV4.getUser();
        username = gitlabUser.getUsername();
        email = gitlabUser.getEmail();

        CredentialsProvider cp = credentialsProvider(username, gitInfo.getAccessToken());

        Git git = null;
        File repoDir = null;
        Ref ref = null;
        Lock cacheLock = getCacheLock(gitDir);
        cacheLock.lock();
        try {
            repoDir = getCacheDir(gitDir);
            git = Git.cloneRepository()
                    .setTimeout(timeout)
                    .setURI(gitProjectUrl)
                    .setDirectory(repoDir)
                    .setCredentialsProvider(cp)
                    .setCallback(callback)
                    .call();
            log.info("Cloning from " + gitProjectUrl + " to " + git.getRepository());

            if (git != null && !"master".equalsIgnoreCase(branchName)) {
                log.warn("branchName:" + branchName);
                ref = git.checkout().
                        setCreateBranch(true).
                        setName(branchName).
                        setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).
                        setStartPoint("origin/" + branchName).
                        call();
                log.warn(ref.getName());
            }

        } catch (JGitInternalException e) {
            log.error("JGitInternalException" + e);

        } catch (RefNotFoundException e) {
            log.error("RefNotFoundException" + e);

        } catch (Exception e) {
            log.error("Exception" + e);

        } finally {
            cacheLock.unlock();
            if (git != null) {
                git.close();
            }
        }

        log.debug("Cloning from " + gitProjectUrl + " to " + gitDir);

        GitInfo gitInfo = new GitInfo();
        gitInfo.setProjectDir(gitDir);
        gitInfo.setProjectUrl(gitProjectUrl);
        return gitInfo;
    }


    private boolean commit(String gitRoot, String title) throws Exception {

        boolean flag = false;
        File rootDir = new File(gitRoot);

        //初始化git仓库
        if (new File(gitRoot + File.separator + GIT).exists() == false) {
            new Exception("this path is not git");
        }
        GitlabUser gitlabUser = gitLabAPIV4.getUser();
        username = gitlabUser.getUsername();
        Lock cacheLock = getCacheLock(gitRoot);
        cacheLock.lock();
        try {
            CredentialsProvider cp = credentialsProvider(username, gitInfo.getAccessToken());
            Git git = Git.open(rootDir);
            //添加文件
            git.add().addFilepattern(".").call();
            //提交
            git.commit().setMessage(title).call();
            //推送到远程
            git.push().setCredentialsProvider(cp).call();
        } catch (Exception e) {
            e.getStackTrace();
        } finally {
            cacheLock.unlock();
        }

        flag = true;

        return flag;
    }


    private boolean rollBack(String gitRoot, String revision) throws IOException, GitAPIException {
        Git git = Git.open(new File(gitRoot));

        Lock cacheLock = getCacheLock(gitRoot);
        cacheLock.lock();
        try {
            Repository repository = git.getRepository();

            RevWalk walk = new RevWalk(repository);
            ObjectId objId = repository.resolve(revision);
            RevCommit revCommit = walk.parseCommit(objId);
            String preVision = revCommit.getParent(0).getName();
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(preVision).call();
            repository.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cacheLock.unlock();
        }

        return true;
    }


    private boolean addTag(String gitRoot, String tag) throws IOException {
        boolean flag = false;
        Lock cacheLock = getCacheLock(gitRoot);
        cacheLock.lock();
        try {
            Git git = Git.open(new File(gitRoot));
            git.commit().setMessage(tag).call();
            git.tag().setName(tag).setForceUpdate(true).call();
            GitlabUser gitlabUser = gitLabAPIV4.getUser();
            username = gitlabUser.getUsername();
            CredentialsProvider cp = credentialsProvider(username, gitInfo.getAccessToken());
            git.push().setPushTags().setForce(true).setCredentialsProvider(cp).call();
            flag = true;
        } catch (Exception e) {
            e.getStackTrace();
        } finally {
            cacheLock.unlock();
        }

        FileUtils.deleteDirectory(new File(gitRoot));
        return flag;
    }

    private CredentialsProvider credentialsProvider(String username, String accessToken) {
        CredentialsProvider cp = null;
        switch (gitInfoProperties.getMode()) {
            case 0:
                //gitlab
                cp = new UsernamePasswordCredentialsProvider(username, accessToken);
                break;

            case 1:
                //gitee
                cp = new UsernamePasswordCredentialsProvider(username, accessToken);
                break;

            case 2:
                //github
                cp = new UsernamePasswordCredentialsProvider("PRIVATE-TOKEN", accessToken);
                break;
            default:
                //默认请求gitlab
                cp = new UsernamePasswordCredentialsProvider(username, accessToken);
        }
        return cp;
    }


    private File getCacheDir(String cacheEntry) {


        File cacheDir = new File(cacheEntry);
        if (!cacheDir.isDirectory()) {
            boolean ok = cacheDir.mkdirs();
            if (!ok) {
                log.error("Failed mkdirs of {0}", cacheDir);
            }
        }
        return cacheDir;
    }

    private Lock getCacheLock(String cacheEntry) {
        Lock cacheLock;
        while (null == (cacheLock = CONCURRENT_MAP.get(cacheEntry))) {
            CONCURRENT_MAP.putIfAbsent(cacheEntry, new ReentrantLock());
        }
        return cacheLock;
    }
}
