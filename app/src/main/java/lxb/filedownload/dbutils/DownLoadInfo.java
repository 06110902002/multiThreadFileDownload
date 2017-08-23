package lxb.filedownload.dbutils;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

/**
 * 下载文件信息实体
 *
 * Created by Administrator on 2017/6/11.
 */

@Table("filedownload")      //数据会存在这个表下
public class DownLoadInfo {

    public static final String COL_fileName= "fileName";
    public static final String COL_threadId = "threadId";

    @PrimaryKey(AssignType.BY_MYSELF)
    @Column("fileName")
    private String fileName;

    @Column("threadId")
    private int threadId;

    @Column("begin")
    private long begin;


    @Column("finished")
    private long finished;


    @Column("end")
    private long end;

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public long getBegin() {
        return begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public long getFinished() {
        return finished;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }

}
