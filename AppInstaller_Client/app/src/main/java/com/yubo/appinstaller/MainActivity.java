package com.yubo.appinstaller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;

import msg.FileMsg;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    private EditText ipEditText;
    private Button connectBtn;
    private View progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews() {
        ipEditText = (EditText) $(R.id.edit_text);
        connectBtn = (Button) $(R.id.connect_btn);
        connectBtn.setOnClickListener(this);
        progressView = $(R.id.progress_view);
    }

    // 连接服务器
    private void connectServer() {
        String ip = ipEditText.getText().toString();
        if (!TextUtils.isEmpty(ip)) {
            try {
                Socket socket = new Socket(ip, Constants.PORT);
                new Thread(new ReadMsgThread(socket)).start();
                connected();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 读消息线程
    private class ReadMsgThread implements Runnable {

        Socket socket;
        boolean stop = false;

        ReadMsgThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream is = socket.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);
                while (!stop) {
                    Object obj = ois.readObject();
                    if (obj instanceof FileMsg) {
                        saveFile((FileMsg) obj);
                    } else if (obj instanceof String) {
                        if ("start".equals(obj)) {
                            showProgress(true);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showProgress(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void connected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                connectBtn.setEnabled(false);
                connectBtn.setText("已连接服务器");
            }
        });
    }

    private void saveFile(FileMsg fileMsg) {
        FileOutputStream fos = null;
        FileInputStream fis = null;
        try {
            String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            File file = new File(sdPath + File.separator + fileMsg.getFileName());
            fos = new FileOutputStream(file);
            fos.write(fileMsg.getData());
            fos.flush();
            String md5 = getFileMD5(file);
            if (md5.equals(fileMsg.getMd5())) {
                install(file);
            } else {
                toast("文件MD5不匹配！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) fis.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        showProgress(false);
    }

    private String getFileMD5(File file) {
        String result = "";
        try {
            FileInputStream fis = new FileInputStream(file);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = fis.read(buffer, 0, 1024)) != -1) {
                md.update(buffer, 0, length);
            }
            BigInteger bigInt = new BigInteger(1, md.digest());
            result = bigInt.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void install(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);
    }

    private void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private View $(@IdRes int resId) {
        return findViewById(resId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connect_btn:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connectServer();
                    }
                }).start();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteTempFile();
    }

    private void deleteTempFile() {
        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File[] files = new File(sdPath).listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".apk")) {
                    file.delete();
                }
            }
        }
    }
}
