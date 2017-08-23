package lxb.filedownload.dbutils;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.litesuits.orm.db.assit.WhereBuilder;
import com.litesuits.orm.db.model.ColumnsValue;
import com.litesuits.orm.db.model.ConflictAlgorithm;

import java.util.List;

import lxb.filedownload.AppContext;


/**
 * 数据库工具类
 * Created by Administrator on 2017/6/11.
 */

public class DBUtils {

    private static final String DOWNLOAD_DB = "download.db";
    private static DBUtils dbUtils;
    private static LiteOrm liteOrm;

    public static DBUtils getInstance(){

        if(dbUtils == null){
            dbUtils = new DBUtils();
        }
        return dbUtils;
    }

    public LiteOrm getLiteOrm(){

        if (liteOrm == null) {
            liteOrm = LiteOrm.newSingleInstance(AppContext.baseContext, DOWNLOAD_DB);
        }
        //liteOrm.setDebugged(true);
        return liteOrm;

    }


    /**
     * 缓存文件信息到本地数据库中，方便续传
     *
     * @param fileName
     * @param threadId
     * @param begin
     * @param finished
     * @param end
     */
    public synchronized void saveDownloadFile2DB(String fileName,int threadId,long begin,long finished,long end){

        DownLoadInfo downLoadInfo = new DownLoadInfo();
        downLoadInfo.setFileName(fileName);
        downLoadInfo.setThreadId(threadId);
        downLoadInfo.setBegin(begin);
        downLoadInfo.setFinished(finished);
        downLoadInfo.setEnd(end);
        getLiteOrm().save(downLoadInfo);
    }

    /**
     * 更新指定线程下载完成的部分，方便续传
     * @param fileName
     * @param threadId
     * @param finished
     */
    public synchronized void updateDownFileCache(String fileName,int threadId,long finished){

        DownLoadInfo downLoadInfo = new DownLoadInfo();
        downLoadInfo.setFileName(fileName);
        downLoadInfo.setThreadId(threadId);
        downLoadInfo.setFinished(finished);
        ColumnsValue cv = new ColumnsValue(new String[]{"fileName","threadId","finished"},

                new Object[]{fileName,threadId, finished});

        getLiteOrm().update(downLoadInfo,cv, ConflictAlgorithm.Replace);
    }

    /**
     * 删除指定条件数据
     *
     * @param tableName
     * @param colName
     * @param fileName
     */
    public synchronized void deleteDownloadFileCache(Class<?> tableName,String colName,String fileName){
        String tmpCol = colName +"=?";
        getLiteOrm().delete(new WhereBuilder(tableName).where(tmpCol, fileName));
    }

    /**
     * 根据 下载的文件与线程id获取记录
     * @param fileName
     * @param threadId
     * @return
     */
    public synchronized List<DownLoadInfo> queryByFileName(String fileName, int threadId){


        QueryBuilder<DownLoadInfo> qb = new QueryBuilder<DownLoadInfo>(DownLoadInfo.class)
                .whereEquals(DownLoadInfo.COL_fileName, fileName)
                .whereAppendOr()
                .whereEquals(DownLoadInfo.COL_threadId, threadId);
        List<DownLoadInfo>  tmp = liteOrm.query(qb);

        return tmp;

        /*for(int i=0;i<tmp.size();i++){

            String name = tmp.get(i).getFileName();
            long begin = tmp.get(i).getBegin();
            long finished = tmp.get(i).getFinished();
            long end = tmp.get(i).getEnd();
            System.out.println("157----:filename:"+name + "  begin: "+begin + " finished:"+finished +" end:" + end);
        }*/
    }
}
