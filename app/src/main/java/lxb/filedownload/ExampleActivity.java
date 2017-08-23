package lxb.filedownload;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.File;

import lxb.filedownload.listener.DownLoadListener;
import lxb.filedownload.task.DownLoadTask;
import lxb.filedownload.task.MioRequestManager;
import lxb.filedownload.util.FileUtils;


/**
 * Created by Administrator on 2017/6/11.
 */
public class ExampleActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "miomin";

    private String url = "http://dlsw.baidu.com/sw-search-sp/soft/ab/27277/konglongkuaida1.0.0.0.1433125592.rar";
    private String url2 = "http://gdown.baidu.com/data/wisegame/6e6f77e3206be0e0/wangzherongyao_19011203.apk";
    private Button btnDown;
    private ProgressBar mTaskBar;
    private long fileSize = 0;

    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 0;

    DownLoadTask multiResumeDownTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTaskBar  =(ProgressBar)findViewById(R.id.pb_progressbar);
        mTaskBar.setProgress(0);

        // 申请Runtime Permission
        requestRuntimePermissions();

        btnDown = (Button) findViewById(R.id.btnDown);

        // 下面的url是需要下载的文件在服务器上的url
        multiResumeDownTask = new DownLoadTask(ExampleActivity.this, url2, toString(),
                        new DownLoadListener() {
                            // 这是监听MultiResumeDownloader下载过程的回调
                            @Override
                            public void OnProcessChange(long process) {
                                //Log.i(TAG, "process:" + process);
                                float pro = (float) process / fileSize * 100;
                                mTaskBar.setProgress((int)pro);
                            }

                            @Override
                            public void OnStart(long fileLength) {
                                fileSize = fileLength;
                            }

                            @Override
                            public void OnResume(long process) {
                                btnDown.setText("暂停下载,下载到：" + process);
                            }

                            @Override
                            public void OnFinished(File file) {
                                btnDown.setText("下载完成");
                                btnDown.setEnabled(false);
                            }

                            @Override
                            public void OnFailed(String error) {
                                Log.i(TAG, "下载失败:" + error);
                            }
                        });

        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!multiResumeDownTask.isDownloading()) {
                    // 开始下载
                    MioRequestManager.getInstance().excuteDownTask(multiResumeDownTask);
                } else {
                    // 暂停下载
                    MioRequestManager.getInstance().resumeDownTask(multiResumeDownTask);
                }
            }
        });

        Button downTest = (Button)findViewById(R.id.btnDown2);
        downTest.setOnClickListener(this);

        Button pause_resume = (Button)findViewById(R.id.btn_pause_resume);
        pause_resume.setOnClickListener(this);


    }


    @Override
    public void onClick(View v) {

        switch(v.getId()){

            case R.id.btnDown2:
                multiResumeDownTask.startDownload();
                break;

            case R.id.btn_pause_resume:

                if (!multiResumeDownTask.isDownloading()) {
                    // 开始下载
                    multiResumeDownTask.startDownload();
                } else {
                    // 暂停下载
                    multiResumeDownTask.resumeDownload();
                }
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 实现request与Activity生命周期绑定
        MioRequestManager.getInstance().cancelRequest(toString());
    }

    // 申请需要的运行时权限
    private void requestRuntimePermissions() {

        // 如果版本低于Android6.0，不需要申请运行时权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    // 对运行时权限做相应处理
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode, grantResults);
    }

    private void doNext(int requestCode, int[] grantResults) {
        if (requestCode == PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
            } else {
                // Permission Denied
                finish();
            }
        }
    }
}
