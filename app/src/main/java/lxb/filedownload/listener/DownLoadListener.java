package lxb.filedownload.listener;

import java.io.File;

/**
 * 下载监听器
 *
 * Created by XiaobingLiu in 2017/06/11.
 */
public interface DownLoadListener {

    /**
     * 下载进度变化的回调
     *
     * @param process
     */
    void OnProcessChange(long process);

    /**
     * 下载开始的回调
     */
    void OnStart(long fileLength);

    /**
     * 暂停下载的回调
     *
     * @param process
     */
    void OnResume(long process);

    /**
     * 下载完成的回调
     */
    void OnFinished(File file);

    /**
     * 下载失败的回调
     */
    void OnFailed(String error);
}
