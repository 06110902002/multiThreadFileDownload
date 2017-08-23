package lxb.filedownload.util;

import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

/**
 * 文件工具类
 * <p>
 * create by Xiaobing liu in 2017/06/11
 */
public class FileUtils {

    /**
     * 根据url获取待下载文件大小
     *
     * @param fileUrl
     * @param context
     * @return
     */
    public static int getFileLength(String fileUrl, Context context) {
        int length = -1;
        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            // 伪装成浏览器
            connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 2.0.50727)");
            length = connection.getContentLength();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(context, "URL不正确", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return length;
    }

    /**
     * 是否存在sd卡
     *
     * @return
     */
    public static boolean isExistSDCard() {
       /* if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;*/
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取下载的文件名
     *
     * @param url
     * @return
     */
    public static String getFileNameFromUrl(String url) {

        int index = url.lastIndexOf("/") + 1;
        return url.substring(index);
    }

    /**
     * 文件字节转mb换算
     *
     * @param lByte
     * @return
     */
    public static String byte2MB(long lByte) {

        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        if (lByte < 1024) {
            fileSizeString = df.format((double) lByte) + "B";
        } else if (lByte < 1048576) {
            fileSizeString = df.format((double) lByte / 1024) + "KB";
        } else if (lByte < 1073741824) {
            fileSizeString = df.format((double) lByte / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) lByte / 1073741824) + "GB";
        }
        return fileSizeString;

    }
}
