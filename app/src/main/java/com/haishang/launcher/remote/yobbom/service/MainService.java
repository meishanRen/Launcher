package com.haishang.launcher.remote.yobbom.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.haishang.launcher.remote.utils.CacheUtil;
import com.haishang.launcher.remote.utils.Tools;
import com.haishang.launcher.remote.yobbom.data.KeyBean;
import com.haishang.launcher.remote.yobbom.data.MainData;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public class MainService extends Service {
    {
        System.loadLibrary("goke");
    }

    private String _localip;
    private String _sendip;
    private boolean isget;
    private int open;
    private byte[] version = null;
    private boolean isSend = false;
    private final static int MSG_SHOW_TIPS = 2000;
    private final static int MSG_DO_TEST = 3000;
    private boolean isUpdate = false;
    private Byte getVersion = 0;
    private Byte getCloseTime = 0;
    private Byte getYOBBOM = 0;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        super.onCreate();
        isUpdate = CacheUtil.getBoolean(getBaseContext(), "isUpdate", false);
        sclose();
        open = uartopen("/dev/ttyAMA2");
        new DeamonThread().start();
        Log.e("返回打开的串口状态", "" + open);
        if (isUpdate) {
            initreturn();
            deviceupdate();
            CacheUtil.putBoolean(getApplicationContext(), "isUpdate", false);
        }
        startForeground(1, new Notification());// 提高服务优先级 防止清理内存时被杀掉

        Log.v(MainData.TAG, "YOBBOMremotecontrolserver Start");
        if (Build.VERSION.SDK_INT > 9) {
            // 严苛模式
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        // 启动守护线程
        _localip = MainData.getLocalIpAddress();// 获取本机IP
        Log.v(MainData.TAG, "localip:" + _localip);

        _sendip = "255.255.255.255";

        Log.v(MainData.TAG, "sendip:" + _sendip);
        // MainData.sendMessage(_sendip,
        // MainData.sysCopy(MainData.getSendByte(MainData.iptoInt(_localip),
        // 1)));
        // MainData.sendMessage(_sendip, MainData.CMD_SERVER_INIT, null);
        String serverName = MainData.get_profile_string_value(this,
                MainData.PROFILE_SERVER_NAME, MainData.DEFAULT_SERVER_NAME);
        String content = String.format("ip=%s;name=%s;id=%s", _localip,
                serverName, MainData.getDeviceId(this));
        MainData.sendMessage(_sendip, MainData.CMD_SERVER_INIT, content);// 广播消息

        isget = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                getMessage();
            }
        }).start();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        flags = START_STICKY;

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(MainData.TAG, "soniqtvremotecontrolserver Stop");
        isget = false;
        // android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void getMessage() {
        byte[] buf = new byte[64];
        try {
            DatagramSocket getSocket;
            getSocket = new DatagramSocket(MainData.UDP_PORT);
            while (isget) {
                DatagramPacket getPacket = new DatagramPacket(buf, buf.length);
                Log.v(MainData.TAG, "receive...");
                getSocket.receive(getPacket);
                String clientIp = getPacket.getAddress().getHostAddress();
                Log.v(MainData.TAG, "client=" + clientIp);
                byte[] data = getPacket.getData();
                int value = MainData.byteArrayToInt(data, 0);
                Log.v(MainData.TAG, "getMessage:" + String.valueOf(value));
                switch (value) {
                    case MainData.CMD_CLIENT_INIT:// 初始化，收到消息后将所获得的IP地址发送手机遥控器
                    {
                        String serverName = MainData.get_profile_string_value(this,
                                MainData.PROFILE_SERVER_NAME,
                                MainData.DEFAULT_SERVER_NAME);
                        String content = String.format("ip=%s;name=%s;id=%s",
                                _localip, serverName, MainData.getDeviceId(this));
                        MainData.sendMessage(clientIp, MainData.CMD_SERVER_INIT,
                                content);
                    }

                    break;
                    case MainData.CMD_CLIENT_PING: {
                        MainData.sendMessage(clientIp, MainData.CMD_SERVER_PING, "");
                    }
                    break;
                    case MainData.CMD_CLIENT_KEY:// 发送过来的是服务器的按键，则将按键的类型提取出来
                    {

                        // 将按键的数值发送给系统>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                        int length = MainData.byteArrayToInt(data, 4);
                        if (length == 4) {
                            int keyCode = MainData.byteArrayToInt(data, 8);
                            Log.e("发送的键值是：", Integer.toHexString(keyCode));
                            int keys[] = {keyCode, 0};
                            KeyBean keybean = new KeyBean();
                            int keysys = keybean.getsyskey(keyCode);
                            // 发送给安卓系统还是发送给手机
                            if (keysys != 10086) {
                                MainData.sendKeyCodeToSystem(keysys);
                                if (keyCode == KeyBean.KEY_OK) {
                                    int a = msgsend(0x16, 0x00, keys);
                                }
                            } else {

                                if (keyCode == KeyBean.KEY_SUSTEND_STATE) {
                                    int a = msgsend(0x16, 0x00, keys);
                                    Tools.Log("返回的发送串口数值" + a);
                                    byte[] keyback = msgrecv();
                                    Tools.Log("返回的接收串口数值" + keyback[0] + keyback[1]
                                            + keyback[2] + keyback[3]);
                                    Tools.Log("返回的关机时间的值" + keyback[0] + keyback[1]
                                            + keyback[2] + keyback[3]);
                                    getCloseTime = keyback[2];
                                    if (getVersion != 0 && getCloseTime != 0
                                            && getYOBBOM != 0) {
                                        byte[] backKey = {1, getCloseTime, getVersion,
                                                getYOBBOM};
                                        MainData.sendMessage(_sendip, backKey);
                                        Tools.Log("返回给手机的值" + getVersion + getCloseTime
                                                + getYOBBOM);
                                        getVersion = 0;
                                        getCloseTime = 0;
                                        getYOBBOM = 0;
                                    }

                                } else if (keyCode == KeyBean.KEY_GET_VERSION) {
                                    int a = msgsend(0x16, 0x00, keys);
                                    Tools.Log("返回的发送串口数值" + a);
                                    byte[] keyback = msgrecv();
                                    Tools.Log("返回的接收串口数值" + keyback[0] + keyback[1]
                                            + keyback[2] + keyback[3]);
                                    Tools.Log("返回的版本号的值" + keyback[0] + keyback[1]
                                            + keyback[2] + keyback[3]);
                                    getVersion = keyback[2];
                                    getYOBBOM = keyback[3];
                                    if (getVersion != 0 && getCloseTime != 0
                                            && getYOBBOM != 0) {
                                        byte[] backKey = {1, getCloseTime, getVersion,
                                                getYOBBOM};
                                        MainData.sendMessage(clientIp, backKey);
                                        Tools.Log("返回给手机的值" + getVersion + getCloseTime
                                                + getYOBBOM);
                                        getVersion = 0;
                                        getCloseTime = 0;
                                        getYOBBOM = 0;
                                    }
                                } else {
                                    int a = msgsend(0x16, 0x00, keys);
                                    Tools.Log("返回的发送串口数值" + a);
                                    byte[] keyback = msgrecv();
                                    Tools.Log("返回的接收串口数值" + keyback[0] + keyback[1]
                                            + keyback[2] + keyback[3]);

                                }

                            }

                        }
                    }
                    break;

                    case MainData.CMD_CLIENT_SETNAME:// 设置名称
                    {
                        int length = MainData.byteArrayToInt(data, 4);
                        if (length > 0) {
                            byte[] bc = new byte[length];
                            System.arraycopy(data, 8, bc, 0, length);

                            String name = new String(bc, "utf-8");
                            Log.v(MainData.TAG, "CMD_CLIENT_SETNAME:" + name);
                            MainData.save_profile_string_value(this,
                                    MainData.PROFILE_SERVER_NAME, name);
                            String content = String.format("ip=%s;name=%s;id=%s",
                                    _localip, name, MainData.getDeviceId(this));
                            MainData.sendMessage(clientIp,
                                    MainData.CMD_SERVER_INIT, content);
                        }
                        break;
                    }
                }
            }
            getSocket.close();
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String getContentFromClient(DataInputStream dis) {
        String content = "";

        try {
            int length = dis.readInt();
            if (length > 0) {
                // 接收
                byte[] buf = new byte[length];

                int left = length;
                int recved = 0;

                boolean b = true;
                while (left > 0) {
                    int n = dis.read(buf, recved, left);
                    if (n <= 0) {
                        b = false;
                        break;
                    }

                    recved += n;
                    left -= n;
                }

                if (b)
                    content = new String(buf);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return content;
    }

    private void on_device_bind(DataInputStream dis, DataOutputStream dos) {
        try {
            String infoString = getContentFromClient(dis);

            writeLog("bind string: " + infoString);

            String userid = MainData.getTagValue(infoString, "userid");

            MainData.save_profile_string_value(this,
                    MainData.PROFILE_BIND_USERID, userid);

            // dos.writeInt(MainData.CMD_SERVER_BIND);
            String serverName = MainData.get_profile_string_value(this,
                    MainData.PROFILE_SERVER_NAME, MainData.DEFAULT_SERVER_NAME);

            String response = String.format("result=yes;devid=%s;name=%s;",
                    MainData.getDeviceId(this), serverName);
            int len = response.getBytes().length;
            dos.writeInt(len);
            writeLog("len=" + len);
            dos.write(response.getBytes(), 0, response.getBytes().length);
        } catch (Exception e) {

        }
    }

    // 判断其接收的数据发送给串口
    private void on_sekbar_move(DataInputStream dis, DataOutputStream dos) {
        try {
            String offsetString = getContentFromClient(dis);
            writeLog("server: " + offsetString);
            Log.e(MainData.TAG, "CMD_CLIENT_KEY:" + offsetString);
            String KEYdata = "0x"
                    + Integer.toHexString(Integer.valueOf(offsetString));

            datasend(KEYdata, 5);

        } catch (Exception e) {

        }
    }

    // 切换模式
    private void mode_change(DataInputStream dis, DataOutputStream dos) {
        try {
            String offsetString = getContentFromClient(dis);
            writeLog("server: " + offsetString);
            int intkey1 = Integer.valueOf(offsetString) / 10000;
            String strkey1 = "27" + Integer.toHexString(intkey1);
            int intkey2 = Integer.valueOf(offsetString) / 10000;
            String strkey2 = "28" + Integer.toHexString(intkey2);
            int intkey3 = Integer.valueOf(offsetString) / 10000;
            String strkey3 = "29" + Integer.toHexString(intkey3);
            Log.e(MainData.TAG, "CMD_CLIENT_KEY:" + offsetString);

            datasend(strkey1, 5);
            datasend(strkey2, 5);
            datasend(strkey3, 5);

        } catch (Exception e) {

        }
    }

    private void writeLog(String message) {
        Log.v(MainData.TAG, message);
    }

    private void showTips(String text) {
        Message msg = new Message();
        msg.what = MSG_SHOW_TIPS;
        msg.obj = text;
        _handler.sendMessage(msg);

    }

    private void sendMessage(int what, String text) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = text;
        _handler.sendMessage(msg);
    }

    private void on_test(DataInputStream dis, DataOutputStream dos) {
        try {
            // Instrumentation inst=new Instrumentation();
            //
            // inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),
            // MotionEvent.ACTION_DOWN, 240, 400, 0));
            // inst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),
            // MotionEvent.ACTION_UP, 240, 400, 0));

            writeLog("do test....");

            sendMessage(MSG_DO_TEST, null);

            showTips("test...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TCP通讯的接收
    private class DeamonThread extends Thread {
        public void run() {
            try {
                ServerSocket server = new ServerSocket(MainData.TCP_PORT);

                while (true) {
                    DataInputStream dis = null;
                    DataOutputStream dos = null;

                    try {
                        writeLog("waitting client connect...");
                        Socket client = server.accept();

                        writeLog("connected!");
                        dis = new DataInputStream(new BufferedInputStream(
                                client.getInputStream()));
                        dos = new DataOutputStream(new BufferedOutputStream(
                                client.getOutputStream()));

                        int cmd = dis.readInt();

                        writeLog("cmd=" + cmd);

                        int iret = 1;

                        if (cmd == MainData.CMD_CLIENT_BIND) {

                            on_device_bind(dis, dos);
                        } else if (cmd == MainData.CMD_MUSIC_LOW) {

                            on_sekbar_move(dis, dos);
                        } else if (cmd == MainData.CMD_MUSIC_MODILE) {

                            on_sekbar_move(dis, dos);
                        } else if (cmd == MainData.CMD_MUSIC_HIGH) {

                            on_sekbar_move(dis, dos);
                        } else if (cmd == MainData.CMD_MUSIC_MODE) {

                            on_sekbar_move(dis, dos);
                        } else if (cmd == MainData.CMD_MUSIC_THE_HEART) {
                            openApp(dis, dos);
                        } else {

                            dos.writeInt(MainData.CMD_SERVER_INVALID);
                            // dos.write(resp.getBytes(), 0,
                            // resp.getBytes().length);
                        }

                        dos.flush();

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (dis != null)
                                dis.close();

                            if (dos != null)
                                dos.close();
                        } catch (Exception e) {

                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private boolean openApp(DataInputStream dis, DataOutputStream dos) {
        String offsetString = getContentFromClient(dis);
        try {
            if (offsetString != null && offsetString.length() > 0) {
                Intent LaunchIntent = getPackageManager()
                        .getLaunchIntentForPackage(offsetString);
                startActivity(LaunchIntent);
                return true;
            }
        } catch (Exception e) {

        }

        return false;
    }

    Handler _handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_SHOW_TIPS) {
                String text = (String) msg.obj;
                Toast.makeText(MainService.this.getApplicationContext(), text,
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    public native int deviceupdatecheck(int reserved_data[]);

    public native void sclose();

    public native int uartopen(String data);

    public native int datasend(String data, int len);

    public native int msgsend(int data, int status, int reserved_data[]);

    public native byte[] msgrecv();

    public native int deviceupdate();

    public native void initreturn();
}
