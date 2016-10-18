package com.haishang.launcher.remote.yobbom.data;

import android.view.KeyEvent;

/**
 * Created by Ren on 2016/7/22.
 */
public class KeyBean {
    //遥控按钮
    final public static int  KEY_POWER = 10;
    final public static int KEY_MUTE = 11;
    final public static int KEY_YOBBOM = 12;
    final public static int KEY_BLUETOUCH = 13;
    final public static int KEY_AUX_ONE = 14;
    final public static int KEY_AUX_TWO = 15;
    final public static int KEY_FIBRE = 16;
    final public static int KEY_COAXAL = 17;
    final public static int KEY_HDMI_ONE = 18;
    final public static int KEY_HDMI_TWO = 19;
    final public static int KEY_THE_HEART = 20;
    final public static int KEY_VOLUME_PLUS = 21;
    final public static int KEY_VOLUME_REDUCE = 22;
    final public static int KEY_BACK = 23;
    final public static int KEY_OK = 24;
    final public static int KEY_UP = 25;
    final public static int KEY_DOWM = 26;
    final public static int KEY_LEFT = 27;
    final public static int KEY_RIGHT = 28;
    final public static int KEY_EFFECT_MUSIC = 29;
    final public static int KEY_EFFECT_KTY = 30;
    final public static int KEY_EFFECT_STEREO = 31;
    final public static int KEY_EFFECT_HIFI = 32;
    //调音台
    final public static int KEY_EFFECT_BASS = 33;
    final public static int KEY_EFFECT_ROCK = 34;
    final public static int KEY_EFFECT_VOICE = 35;
    final public static int KEY_SOUND_LOW = 36;
    final public static int KEY_SOUND_MIDDLE = 37;
    final public static int KEY_SOUND_HIGH = 38;
    final public static int KEY_EFFECT_MODE_ONE = 39;
    final public static int KEY_EFFECT_MODE_TWO = 40;
    final public static int KEY_EFFECT_MODE_THREE = 41;
    final public static int KEY_MIX_PLUS = 42;
    final public static int KEY_MIX_REDUCE = 43;
    final public static int KEY_MIC_PLUS = 44;
    final public static int KEY_MIC_REDUCE = 45;
    //版本设置
    final public static int KEY_RESET = 46;
    final public static int KEY_GET_VERSION = 47;
    final public static int KEY_HDMI_STATE = 48;
    final public static int KEY_HDMI_ON = 49;
    final public static int KEY_HDMI_OFF = 50;
    final public static int KEY_SUSTEND_STATE= 51;
    final public static int KEY_SUSTEND_ON = 52;
    final public static int KEY_SUSTEND_TEN = 53;
    final public static int KEY_SUSTEND_THRTY = 54;
    final public static int KEY_SUSTEND_OFF = 55;
  //长按
    final public static int LONG_KEY_OK = 0x40;
    final public static int LONG_KEY_LEFT = 0x3d;
    final public static int LONG_KEY_RIGHT = 0x3c;
    final public static int LONG_KEY_PLAY = 0x3f;

    int sysKey;
public int  getsyskey(int key) {
	switch (key) {
	case 25:
		sysKey=KeyEvent.KEYCODE_DPAD_UP;
		break;
	case 26:
		sysKey=KeyEvent.KEYCODE_DPAD_DOWN;
		break;
	case 27:
		sysKey=KeyEvent.KEYCODE_DPAD_LEFT;
		break;
	case 28:
		sysKey=KeyEvent.KEYCODE_DPAD_RIGHT;
		break;
	case 24:
		sysKey=KeyEvent.KEYCODE_DPAD_CENTER;
		break;
	case 23:
		sysKey=KeyEvent.KEYCODE_BACK;
		break;
	case 0x3f:
		sysKey=KeyEvent.KEYCODE_MEDIA_PLAY;
		break;
	case 0x40:
		sysKey=KeyEvent.KEYCODE_ENTER;
		break;
	case 0x3d:
		sysKey=KeyEvent.KEYCODE_MEDIA_REWIND;
		break;
	case 0x3c:
		sysKey=KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
		break;
	default:
		sysKey=10086;
		break;
	}
	return sysKey;
}
}