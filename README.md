# AppInstaller
局域网内用于从PC传apk文件到手机上并自动安装

# 开发原因
不想手机打开USB调试，或者没有数据线可连接手机，又希望打包的apk可以安装到手机上时。（当然通过QQ或者微信也可以发送文件到手机）

# 截图
PC端截图如下：
![image](https://github.com/yubo725/AppInstaller/blob/master/AppInstaller_Server.png)

手机端截图如下：
![image](https://github.com/yubo725/AppInstaller/blob/master/AppInstaller_Client.jpg)

# 使用
将apk安装到手机，
将AppInstaller下载到PC，
双击打开AppInstaller.jar或者使用java -jar AppInstaller.jar打开，
确保PC和手机在同一个局域网内，
打开手机端AppInstaller并点击“连接服务器”，
然后从PC上选择apk文件并拖拽到PC端的AppInstaller的界面上，
程序自动传输选择的apk到手机上，手机端接收成功后自动安装。

