package lxb.filedownload.task;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.model.ColumnsValue;
import com.litesuits.orm.db.model.ConflictAlgorithm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lxb.filedownload.dbutils.DBUtils;
import lxb.filedownload.dbutils.DownLoadInfo;
import lxb.filedownload.listener.DownLoadListener;
import lxb.filedownload.util.FileUtils;


/**
 * Created by Administrator on 2017/6/11.
 * 多线程下载任务管理
 */
public class DownLoadTask {

    private static final int DOWNLOADFINISHED = 1;
    private static final int DOWNLOADPROCESS = 2;
    private static final int FILESIZE = 3;

    private DownLoadListener onDownLoadStateListener;

    // 用tag将下载的task与activity绑定
    private String tag;

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }


    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOADFINISHED:

                    onDownLoadStateListener.OnFinished(file);
                    break;
                case DOWNLOADPROCESS:
                    onDownLoadStateListener.OnProcessChange(process);
                    break;

                case FILESIZE:
                    Bundle data = msg.getData();
                    long size = Long.parseLong(data.getString("size").trim());
                    onDownLoadStateListener.OnStart(size);
                    break;
            }
        }
    };


    private int process = 0;
    private int BLOCKCOUNT = 6;     //文件分成BLOCKCOUNT块，启动下载的线程数 filesize/6 向上取整
    private boolean downloading = false;
    private Context context;
    private String fileUrl;
    private File file;
    private URL url;
    private int fileLength = -1;
    private String fileName;

    // 用于断点续传时保存每个线程的上下文
    private List<HashMap<String, Integer>> threadList;

    public DownLoadTask(Context context, String fileUrl, String tag, DownLoadListener onDownLoadStateListener) {
        try {
            fileName = FileUtils.getFileNameFromUrl(fileUrl);
            this.fileUrl = fileUrl;
            this.context = context;
            this.file = new File(Environment.getExternalStorageDirectory(), fileName);
            this.url = new URL(fileUrl);
            this.threadList = new ArrayList<>();
            this.onDownLoadStateListener = onDownLoadStateListener;
            this.tag = tag;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始下载
     */
    public void startDownload() {
        downloading = true;
        if (threadList.size() == 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    fileLength = FileUtils.getFileLength(fileUrl, context);

                    if (fileLength < 0) {
                        onDownLoadStateListener.OnFailed("文件不存在");
                        return;
                    }

                    if (!FileUtils.isExistSDCard()) {
                        onDownLoadStateListener.OnFailed("SD卡不可用");
                        return;
                    }

                    sendMsg2UIThread(FILESIZE, "size", fileLength + "");

                    int blockSize = fileLength / BLOCKCOUNT;         // 第个线程下载的大小，最后一个向上取整

                    /**
                     * 计算每个线程的开始与结束位置，方便续传
                     */
                    for (int i = 0; i < BLOCKCOUNT; i++) {

                        int begin = i * blockSize;
                        int end = (i + 1) * blockSize;
                        if (i == BLOCKCOUNT - 1) {                  // 整除BLOCKCOUNT的误差处理
                            end = fileLength;
                        }

                        /**
                         * 初始化每条线程下载的位置信息
                         */
                       /* HashMap<String, Integer> map = new HashMap<String, Integer>();
                        map.put("begin", begin);
                        map.put("end", end);
                        map.put("finished", 0);
                        threadList.add(map);*/

                        DBUtils.getInstance().saveDownloadFile2DB(fileName,i,begin,0,end);

                        //创建新的线程，下载文件
                        Thread thread = new Thread(new DownloadRunable(i, begin, end));
                        thread.start();
                    }
                }
            }).start();
        } else {
            // 断点续传恢复上下文，恢复下载
            for (int i = 0; i < BLOCKCOUNT; i++) {
               /* HashMap<String, Integer> map = threadList.get(i);
                int begin = map.get("begin");
                int end = map.get("end");
                int finished = map.get("finished");*/

                List<DownLoadInfo> tmp = DBUtils.getInstance().queryByFileName(fileName,i);
                DownLoadInfo dli = tmp.get(0);
                int begin = (int)dli.getBegin();
                int finished = (int)dli.getFinished();
                int end = (int)dli.getEnd();



                Thread thread = new Thread(new DownloadRunable(i, begin + finished, end));
                thread.start();
            }
        }
    }

    /**
     * 暂停下载
     */
    public void resumeDownload() {
        downloading = false;
        onDownLoadStateListener.OnResume(process);
    }


    /**
     * 下载执行逻辑，每个下载线程都执行这样一个逻辑
     * 注意的地方：
     * 1.不同的线程要用不同的randomAccessFile对象，不能直接把主线程的randomAccessFile对象传进来，不然访问时会有冲突，
     * 2.可以用不同的randomAccessFile对象来操作同一个文件
     */
    private class DownloadRunable implements Runnable {

        private int id;                  // 线程id
        private int begin;               // 该block在文件中的起始位置
        private int end;                 // 该block在文件中的结束为止

        public DownloadRunable(int id, int begin, int end) {
            this.id = id;
            this.begin = begin;
            this.end = end;
        }

        @Override
        public void run() {

            InputStream is = null;
            RandomAccessFile randomAccessFile = null;

            try {
                if (begin > end) {
                    return;
                }

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)");
                connection.setRequestProperty("Range", "bytes=" + begin + "-" + end);
                is = connection.getInputStream();

                byte[] buf = new byte[1024 * 1024];
                randomAccessFile = new RandomAccessFile(file, "rw");
                randomAccessFile.seek(begin);
                int len = 0;

                while (((len = is.read(buf)) != -1) && downloading) {

                    randomAccessFile.write(buf, 0, len);
                    updateProgress(len);

                    /**
                     * 保存每条已经下载的部分缓存起来，方便线程断点续传
                     */
                    //threadList.get(id).put("finished", threadList.get(id).get("finished") + len);

                    List<DownLoadInfo> tmp = DBUtils.getInstance().queryByFileName(fileName,id);
                    DownLoadInfo dli = tmp.get(0);
                    long finished = dli.getFinished();
                    DBUtils.getInstance().updateDownFileCache(fileName,id,finished + len);
                }

                if (is != null) {
                    is.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (randomAccessFile != null) {
                        randomAccessFile.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 跟新文件的下载进度total
     *
     * @param add 进度增量
     */
    private synchronized void updateProgress(int add) {
        process += add;
        if (process >= fileLength) {
            Message message;
            message = Message.obtain();
            message.what = DOWNLOADFINISHED;
            mHandler.sendMessage(message);
        } else {
            Message message;
            message = Message.obtain();
            message.what = DOWNLOADPROCESS;
            mHandler.sendMessage(message);
        }
    }


    /**
     * 更新信息到ui线程中去
     *
     * @param msgId
     * @param key
     * @param value
     */
    private void sendMsg2UIThread(int msgId, String key, String value) {
        Message msg = new Message();
        Bundle data = new Bundle();
        data.putString(key, value);
        msg.setData(data);
        msg.what = msgId;
        mHandler.sendMessage(msg);
    }


    public boolean isDownloading() {
        return downloading;
    }




}
