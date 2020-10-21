package com.jvm.file;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author heartccace
 * @create 2020-08-25 20:18
 * @Description TODO
 * @Version 1.0
 */
public class FolderMonitor {
    private FileAlterationMonitor monitor;
    private String rootDir;
    private Long interval;
    private static final Long DEFAULT_INTERVAL = 1L;

    public FolderMonitor(String path) {
        this(path,DEFAULT_INTERVAL);
    }

    public FolderMonitor(String path,Long interval) {
        this.rootDir = path;
        this.interval = TimeUnit.SECONDS.toMillis(interval);
    }
    public void init() {
        IOFileFilter directoriesFilter  = FileFilterUtils.and(FileFilterUtils.directoryFileFilter(), HiddenFileFilter.VISIBLE);
        IOFileFilter fileFilter = FileFilterUtils.and(FileFilterUtils.fileFileFilter(), FileFilterUtils.suffixFileFilter(".txt"));
        IOFileFilter filter = FileFilterUtils.or(directoriesFilter, fileFilter);
        FileAlterationObserver observer = new FileAlterationObserver(new File(rootDir), filter);
        //不使用过滤器
        //FileAlterationObserver observer = new FileAlterationObserver(new File(rootDir));
        observer.addListener(new FileListener());
        //创建文件变化监听器
        monitor = new FileAlterationMonitor(interval, observer);

    }

    public void start() {
        try {
            monitor.start();
        } catch (Exception e) {

        }
    }

    public static void main(String[] args) {

        FolderMonitor folderMonitor = new FolderMonitor("G:\\monitor");
        folderMonitor.init();
        folderMonitor.start();
    }

}
