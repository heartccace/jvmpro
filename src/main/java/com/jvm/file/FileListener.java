package com.jvm.file;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;

/**
 * @author heartccace
 * @create 2020-08-25 20:47
 * @Description TODO
 * @Version 1.0
 */

public class FileListener implements FileAlterationListener {
    @Override
    public void onStart(FileAlterationObserver observer) {

        // System.out.println("onStart");
    }

    @Override
    public void onDirectoryCreate(File directory) {
        System.out.println("onDirectoryCreate");
    }

    @Override
    public void onDirectoryChange(File directory) {
        System.out.println("onDirectoryChange");
    }

    @Override
    public void onDirectoryDelete(File directory) {
        System.out.println("onDirectoryDelete");
    }

    @Override
    public void onFileCreate(File file) {
        System.out.println("onFileCreate");
    }

    @Override
    public void onFileChange(File file) {
        System.out.println("onFileChange");
    }

    @Override
    public void onFileDelete(File file) {
        System.out.println("onFileDelete");
    }

    @Override
    public void onStop(FileAlterationObserver observer) {

    }
}
