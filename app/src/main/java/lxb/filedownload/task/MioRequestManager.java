package lxb.filedownload.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/6/11.
 */
public class MioRequestManager {

    private static MioRequestManager instance;
    private HashMap<String, ArrayList<DownLoadTask>> downtaskMap;

    public static MioRequestManager getInstance() {
        if (instance == null) {
            instance = new MioRequestManager();
        }
        return instance;
    }

    private MioRequestManager() {
        downtaskMap = new HashMap<>();
    }

    // 执行文件下载任务
    public void excuteDownTask(DownLoadTask task) {
        task.startDownload();
        if (!downtaskMap.containsKey(task.getTag())) {
            ArrayList<DownLoadTask> downTasks = new ArrayList<>();
            downtaskMap.put(task.getTag(), downTasks);
        }
        downtaskMap.get(task.getTag()).add(task);
    }

    // 暂停下载任务
    public void resumeDownTask(DownLoadTask task) {
        task.resumeDownload();
    }

    // 取消与tag的Activity相关的所有任务
    public void cancelRequest(String tag) {

        if (tag == null || "".equals(tag.trim())) {
            return;
        }

        // 暂停与该activity关联的所有下载任务
        if (downtaskMap.containsKey(tag)) {
            ArrayList<DownLoadTask> downTasks = downtaskMap.remove(tag);
            for (DownLoadTask downTask : downTasks) {
                if (!downTask.isDownloading() && downTask.getTag().equals(tag)) {
                    downTask.resumeDownload();
                }
            }
        }
    }

    // 取消进程中的所有下载和访问任务
    public void cancleAll() {

        for (Map.Entry<String, ArrayList<DownLoadTask>> entry : downtaskMap.entrySet()) {
            ArrayList<DownLoadTask> downTasks = entry.getValue();
            for (DownLoadTask downTask : downTasks) {
                if (!downTask.isDownloading()) {
                    downTask.resumeDownload();
                }
            }
        }
    }
}
