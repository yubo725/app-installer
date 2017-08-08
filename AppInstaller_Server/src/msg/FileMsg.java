package msg;

import java.io.Serializable;

/**
 * Created by admin on 2017/8/8.
 */
public class FileMsg implements Serializable {

    private String fileName;
    private String md5;
    private byte[] data;

    public FileMsg(String fileName, String md5, byte[] data) {
        this.fileName = fileName;
        this.md5 = md5;
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
