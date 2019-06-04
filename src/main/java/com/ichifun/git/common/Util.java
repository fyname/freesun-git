package com.ichifun.git.common;


import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.*;

/**
 * @author: fyname@163.com
 * @desc: 公共方法
 * @date: created in 2019-04-26 10:17
 * @modifed by:
 */
@Slf4j
public class Util {

    private static MessageDigest md5;

    /**
     * @param path
     * @desc 根据路径读取该路径下所有文件列表
     */
    public static List<String> readFolderFile(String path) {

        List<String> fileList = new ArrayList<>();
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files.length <= 0) {
                log.debug("文件夹是空的!");
                return null;
            } else {
                for (File file2 : files) {
                    if (!file2.getName().equalsIgnoreCase(".git")) {
                        if (file2.isDirectory()) {
                            log.debug("文件夹:" + file2.getAbsolutePath());
                            readFolderFile(file2.getAbsolutePath());
                        } else {

                            log.debug("文件:" + file2.getAbsolutePath());
                            fileList.add(file2.getAbsolutePath());
                        }
                    } else {
                        log.debug(".git fie");
                    }

                }
            }
        } else {
            log.debug("文件不存在!");
        }
        return fileList;
    }


    /**
     * 搜索离得最近的日期, 先排序再搜索, 修改下适合进行大量的快速搜索
     *
     * @param list
     * @param d
     * @return
     */
    public static Date find1(List<Date> list, Date d) {
        if (list == null || list.size() <= 0) {
            return null;
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        list = new ArrayList<Date>(list);
        Collections.sort(list);

        int left = 0;
        int right = list.size() - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            int cm = d.compareTo(list.get(mid));
            if (cm < 0) {
                right = mid - 1;
            } else if (cm > 0) {
                left = mid + 1;
            } else {
                return list.get(mid);
            }
        }
        if (left <= 0) {
            return list.get(0);
        }
        if (left >= list.size()) {
            return list.get(list.size() - 1);
        }
        long dleft = d.getTime() - list.get(left - 1).getTime();
        long dright = list.get(left).getTime() - d.getTime();
        return dleft < dright ? list.get(left - 1) : list.get(left);
    }


    /**
     * 搜索离得最近的日期.适合只进行一次的快速搜索.
     *
     * @param map
     * @param d
     * @return
     */
    public static String mapFind(Map<String, Date> map, Date d) {
        if (map == null || map.size() <= 0) {
            return null;
        }
        long gap = Long.MAX_VALUE;
        String r = null;
        long time = d.getTime();
        Iterator<Map.Entry<String, Date>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Date> entry = it.next();
            long tm = Math.abs(time - entry.getValue().getTime());
            if (gap > tm) {
                gap = tm;
                r = entry.getKey();
            }
        }
        return r;
    }


    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getMd5(String string) {
        try {
            byte[] bs = md5.digest(string.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(40);
            for (byte x : bs) {
                if ((x & 0xff) >> 4 == 0) {
                    sb.append("0").append(Integer.toHexString(x & 0xff));
                } else {
                    sb.append(Integer.toHexString(x & 0xff));
                }
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


}
