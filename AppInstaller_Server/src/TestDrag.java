import msg.FileMsg;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by admin on 2017/8/7.
 */
public class TestDrag extends JFrame {

    private Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
    private final int WIDTH = 400;
    private final int HEIGHT = 300;

    private JPanel serverIpPanel;
    private JLabel serverIpLabel;

    private JPanel hintPanel;
    private JLabel hintLabel;

    private JPanel centerPanel;
    private JLabel centerLabel;

    private Socket socket;
    private boolean stop = false;

    TestDrag() {
        setUIFont();

        int centerX = (dimension.width - WIDTH) / 2;
        int centerY = (dimension.height - HEIGHT) / 2;
        setBounds(centerX, centerY, WIDTH, HEIGHT);
        setTitle("AppInstaller");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        serverIpPanel = new JPanel();
        FlowLayout flowLayout = new FlowLayout(FlowLayout.CENTER);
        serverIpPanel.setLayout(flowLayout);
        serverIpLabel = new JLabel("");
        serverIpPanel.add(serverIpLabel);
        add(serverIpPanel, BorderLayout.NORTH);

        hintPanel = new JPanel();
        hintPanel.setLayout(flowLayout);
        hintLabel = new JLabel("暂无客户端连接");
        hintLabel.setForeground(Color.RED);
        hintPanel.add(hintLabel);
        add(hintPanel, BorderLayout.SOUTH);

        centerPanel = new JPanel();
        centerLabel = new JLabel("", JLabel.CENTER);
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(centerLabel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
        centerLabel.setText("拖拽apk文件到这里");
        centerLabel.setBackground(Color.BLUE);
        enableDrag(centerPanel);

        new Thread(() -> startListen()).start();
    }

    private void enableDrag(JPanel panel) {
        new DropTarget(panel, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                if (socket == null) {
                    showHintBox("没有客户端连接！");
                    return;
                }
                try {
                    if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        //如果拖入的文件格式受支持
                        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        //接收拖拽来的数据
                        List<File> list = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        if (list != null && !list.isEmpty()) {
                            if (list.size() > 1) {
                                showHintBox("仅支持一个文件");
                            } else {
                                File file = list.get(0);
                                String fileName = file.getName();
                                if (!fileName.endsWith(".apk")) {
                                    showHintBox("仅支持apk文件");
                                } else {
                                    sendFile(file);
                                }
                            }
                        }
                    } else {
                        dtde.rejectDrop();//否则拒绝拖拽来的数据
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showHintBox(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public void startListen() {
        try {
            ServerSocket serverSocket = new ServerSocket(Global.PORT);
            String serverIp = serverSocket.getInetAddress().getLocalHost().getHostAddress();
            serverIpLabel.setText("服务器IP: " + serverIp + ", 监听端口: " + Global.PORT);
            while (!stop) {
                socket = serverSocket.accept();
                hintLabel.setText("客户端已连接，IP为: " + socket.getInetAddress().getHostAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
            serverIpLabel.setText("服务端监听异常");
        }
    }

    private void sendFile(File file) {
        new SendFileThread(file).start();
    }

    private class SendFileThread extends Thread {
        private File file;

        public SendFileThread(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            super.run();
            if (socket != null && !socket.isClosed() && socket.isConnected()) {
                try {
                    String md5 = getFileMD5(file);
                    System.out.println("正在发送文件：" + file.getAbsolutePath());
                    System.out.println("MD5 = " + md5);
                    OutputStream os = socket.getOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(os);
                    oos.writeObject("start");
                    oos.writeObject(new FileMsg(file.getName(), md5, file2Bytes(file)));
                    System.out.println("发送文件完成！");
                } catch (IOException e) {
                    e.printStackTrace();
                    hintLabel.setText("客户端断开连接");
                }
            } else {
                System.out.println("发送文件失败，客户端已离线");
                hintLabel.setText("客户端断开连接");
            }
        }
    }

    private byte[] file2Bytes(File file) {
        FileInputStream fis = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        byte[] data = null;
        try {
            fis = new FileInputStream(file);
            byteArrayOutputStream = new ByteArrayOutputStream();
            int hasRead = 0;
            byte[] buf = new byte[1024];
            while ((hasRead = fis.read(buf)) > 0) {
                byteArrayOutputStream.write(buf, 0, hasRead);
            }
            byteArrayOutputStream.flush();
            data = byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public static void setUIFont() {
        Font f = new Font("楷体", Font.PLAIN, 16);
        String names[] = {"Label", "CheckBox", "PopupMenu", "MenuItem", "CheckBoxMenuItem",
                "JRadioButtonMenuItem", "ComboBox", "Button", "Tree", "ScrollPane",
                "TabbedPane", "EditorPane", "TitledBorder", "Menu", "TextArea",
                "OptionPane", "MenuBar", "ToolBar", "ToggleButton", "ToolTip",
                "ProgressBar", "TableHeader", "Panel", "List", "ColorChooser",
                "PasswordField", "TextField", "Table", "Label", "Viewport",
                "RadioButtonMenuItem", "RadioButton", "DesktopPane", "InternalFrame"
        };
        for (String item : names) {
            UIManager.put(item + ".font", f);
        }
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

    public static void main(String[] args) {
        TestDrag frame = new TestDrag();
        frame.setVisible(true);
    }

}
