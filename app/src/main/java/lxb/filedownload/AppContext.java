package lxb.filedownload;

import android.app.Application;

/**
 * Created by Administrator on 2017/6/11.
 */

public class AppContext extends Application {

    public static AppContext baseContext;


    @Override
    public void onCreate() {
        super.onCreate();

        baseContext = this;
    }
}
