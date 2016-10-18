#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <pthread.h>
#include <assert.h>
#include <fcntl.h>
#include <termios.h>
#include <unistd.h>
#include <sys/stat.h>
#include <jni.h>
#include "android/log.h"
#define LOGE(fmt, args...) __android_log_print(ANDROID_LOG_ERROR, TAG, fmt, ##args)
#define SIGN_GOKE 0x454B4F47

typedef unsigned char U8;
typedef unsigned short U16;
typedef unsigned int U32;

typedef struct {
	U32 signature; /* value is "GOKE" (0x454B4F47)*/
	U32 crc; /*为有效数据的CRC,计算长度不包含crc在内下述内容*/
	U8 msg; /*主从通信msg，详见附2*/
	U8 status; /*消息反馈机制，接受失败GK110x再次发送相同msg给Hi3798，详见附1*/
	U8 reserved[2]; /*预留字段，默认初始值0x00*/
} GK110x_TO_HI3798_MsgInfoT;

typedef struct {
	U32 signature; /* value is "GOKE" (0x454B4F47)*/
	U32 crc; /*为有效数据的CRC,计算长度不包含crc在内下述内容*/
	U8 msg; /*主从通信msg，详见附2*/
	U8 status; /*消息反馈机制，接受失败GK110x再次发送相同msg给Hi3798，详见附1*/
	U8 reserved[2]; /*预留字段，默认初始值0x00*/
	U16 length; /*Length后的数据长度（BYTE），length为有效数据*/
	U8 data[1024]; /*有效数据，超过length的地方默认为0x00*/
} GK110x_TO_HI3798_DATA_S;
typedef struct {
	U32 id;
	U32 version; /*Current verison for this item*/
	U32 offset;
	U32 length;
} UpdateItemDescriptorT;

/*附1:*/
#define MSG_SUCCEED		0x00	/*操作成功*/
#define MSG_ERR_SIG		0x01	/*签名错误*/
#define MSG_ERR_CRC		0x02	/*MSG CRC 校验失败*/
#define MSG_ERR_LENGTH	0x03	/*MSG数据长度错误*/
#define MSG_ERR_FAILED	0x04	/*处理失败*/

/*附2:*/
#define MSG_OPEN_HI3798			0x10	/*打开Hi3798，输出音视频*/
#define MSG_CLOSE_HI3798		0x11	/*关闭Hi3798，关闭输出音视频*/
#define MSG_INIT_FINISH_HI3798	0x12	/* Hi3798初始化完成，由Hi3798发给GK110x*/
#define MSG_STANDBY_HI3798		0x13	/* Hi3798待机*/
#define MSG_SCORE_HI3798		0x14	/* Hi3798评分*/
#define MSG_UPDATE_HI3798		0x15	/*GK1106升级，由Hi3798发给GK110x*/
#define MSG_IR_HI3798			0x16	/*Hi3798需要处理的IR，IR的device ID和Key value分别由GK1106和Hi3798自行初始化，msg传输只负责Key value。该msg需伴随reserved【0】（Key value）的一起发出*/

#define MSG_VERSION_CHECK_HI3798		0x17	/*用户检测新版本号时，Hi3798发送给GK1106，GK1106反馈当前主音频板版本号*/
#define MSG_ENTER_UART_UPDATE_HI3798	0x18	/*GK1106重启后等待Hi3798初始化，循环发送该消息120S直到Hi3798应答*/
#define MSG_UPDATE_DATA_HI3798			0x19	/*Hi3798向GK1106传输的升级文件包*/
#define MSG_UPDATE_DATA_COMPLETE_HI3798	0x1A	/*升级文件传输完毕通知消息，Hi3798向GK1106发送*/
#define MSG_UPDATE_SUCCESS_HI3798		0x1B	/*soundbar升级完毕通知，GK1106向Hi3798发送*/

#define msgStructLength  sizeof(GK110x_TO_HI3798_MsgInfoT)
#define dataStructLength sizeof(GK110x_TO_HI3798_DATA_S)
#define msgCrcOffset 2*sizeof(U32)
#define dataCrcOffset 2*sizeof(U32)
#define msgInfoLength msgStructLength-msgCrcOffset
#define dataInfoLength dataStructLength-dataCrcOffset
static U8 recvInitFinishFlag = 0;
/*static U32 msgStructLength = sizeof(GK110x_TO_HI3798_MsgInfoT), dataStructLength = sizeof(GK110x_TO_HI3798_DATA_S);
 static U32 msgCrcOffset = 2*sizeof(U32), dataCrcOffset =2*sizeof(U32);
 static U32 msgInfoLength = msgStructLength-msgCrcOffset, dataInfoLength =dataStructLength-dataCrcOffset;*/
static int uartFd = -1;
static U8 msgBuffer[1024], dataBuffer[2048];
static U8 update_soundbar_flag = 0;
/* CRC32 implementation acording to CCITT standards */
static unsigned long crcTable[256] = { 0x00000000, 0x04c11db7, 0x09823b6e,
		0x0d4326d9, 0x130476dc, 0x17c56b6b, 0x1a864db2, 0x1e475005, 0x2608edb8,
		0x22c9f00f, 0x2f8ad6d6, 0x2b4bcb61, 0x350c9b64, 0x31cd86d3, 0x3c8ea00a,
		0x384fbdbd, 0x4c11db70, 0x48d0c6c7, 0x4593e01e, 0x4152fda9, 0x5f15adac,
		0x5bd4b01b, 0x569796c2, 0x52568b75, 0x6a1936c8, 0x6ed82b7f, 0x639b0da6,
		0x675a1011, 0x791d4014, 0x7ddc5da3, 0x709f7b7a, 0x745e66cd, 0x9823b6e0,
		0x9ce2ab57, 0x91a18d8e, 0x95609039, 0x8b27c03c, 0x8fe6dd8b, 0x82a5fb52,
		0x8664e6e5, 0xbe2b5b58, 0xbaea46ef, 0xb7a96036, 0xb3687d81, 0xad2f2d84,
		0xa9ee3033, 0xa4ad16ea, 0xa06c0b5d, 0xd4326d90, 0xd0f37027, 0xddb056fe,
		0xd9714b49, 0xc7361b4c, 0xc3f706fb, 0xceb42022, 0xca753d95, 0xf23a8028,
		0xf6fb9d9f, 0xfbb8bb46, 0xff79a6f1, 0xe13ef6f4, 0xe5ffeb43, 0xe8bccd9a,
		0xec7dd02d, 0x34867077, 0x30476dc0, 0x3d044b19, 0x39c556ae, 0x278206ab,
		0x23431b1c, 0x2e003dc5, 0x2ac12072, 0x128e9dcf, 0x164f8078, 0x1b0ca6a1,
		0x1fcdbb16, 0x018aeb13, 0x054bf6a4, 0x0808d07d, 0x0cc9cdca, 0x7897ab07,
		0x7c56b6b0, 0x71159069, 0x75d48dde, 0x6b93dddb, 0x6f52c06c, 0x6211e6b5,
		0x66d0fb02, 0x5e9f46bf, 0x5a5e5b08, 0x571d7dd1, 0x53dc6066, 0x4d9b3063,
		0x495a2dd4, 0x44190b0d, 0x40d816ba, 0xaca5c697, 0xa864db20, 0xa527fdf9,
		0xa1e6e04e, 0xbfa1b04b, 0xbb60adfc, 0xb6238b25, 0xb2e29692, 0x8aad2b2f,
		0x8e6c3698, 0x832f1041, 0x87ee0df6, 0x99a95df3, 0x9d684044, 0x902b669d,
		0x94ea7b2a, 0xe0b41de7, 0xe4750050, 0xe9362689, 0xedf73b3e, 0xf3b06b3b,
		0xf771768c, 0xfa325055, 0xfef34de2, 0xc6bcf05f, 0xc27dede8, 0xcf3ecb31,
		0xcbffd686, 0xd5b88683, 0xd1799b34, 0xdc3abded, 0xd8fba05a, 0x690ce0ee,
		0x6dcdfd59, 0x608edb80, 0x644fc637, 0x7a089632, 0x7ec98b85, 0x738aad5c,
		0x774bb0eb, 0x4f040d56, 0x4bc510e1, 0x46863638, 0x42472b8f, 0x5c007b8a,
		0x58c1663d, 0x558240e4, 0x51435d53, 0x251d3b9e, 0x21dc2629, 0x2c9f00f0,
		0x285e1d47, 0x36194d42, 0x32d850f5, 0x3f9b762c, 0x3b5a6b9b, 0x0315d626,
		0x07d4cb91, 0x0a97ed48, 0x0e56f0ff, 0x1011a0fa, 0x14d0bd4d, 0x19939b94,
		0x1d528623, 0xf12f560e, 0xf5ee4bb9, 0xf8ad6d60, 0xfc6c70d7, 0xe22b20d2,
		0xe6ea3d65, 0xeba91bbc, 0xef68060b, 0xd727bbb6, 0xd3e6a601, 0xdea580d8,
		0xda649d6f, 0xc423cd6a, 0xc0e2d0dd, 0xcda1f604, 0xc960ebb3, 0xbd3e8d7e,
		0xb9ff90c9, 0xb4bcb610, 0xb07daba7, 0xae3afba2, 0xaafbe615, 0xa7b8c0cc,
		0xa379dd7b, 0x9b3660c6, 0x9ff77d71, 0x92b45ba8, 0x9675461f, 0x8832161a,
		0x8cf30bad, 0x81b02d74, 0x857130c3, 0x5d8a9099, 0x594b8d2e, 0x5408abf7,
		0x50c9b640, 0x4e8ee645, 0x4a4ffbf2, 0x470cdd2b, 0x43cdc09c, 0x7b827d21,
		0x7f436096, 0x7200464f, 0x76c15bf8, 0x68860bfd, 0x6c47164a, 0x61043093,
		0x65c52d24, 0x119b4be9, 0x155a565e, 0x18197087, 0x1cd86d30, 0x029f3d35,
		0x065e2082, 0x0b1d065b, 0x0fdc1bec, 0x3793a651, 0x3352bbe6, 0x3e119d3f,
		0x3ad08088, 0x2497d08d, 0x2056cd3a, 0x2d15ebe3, 0x29d4f654, 0xc5a92679,
		0xc1683bce, 0xcc2b1d17, 0xc8ea00a0, 0xd6ad50a5, 0xd26c4d12, 0xdf2f6bcb,
		0xdbee767c, 0xe3a1cbc1, 0xe760d676, 0xea23f0af, 0xeee2ed18, 0xf0a5bd1d,
		0xf464a0aa, 0xf9278673, 0xfde69bc4, 0x89b8fd09, 0x8d79e0be, 0x803ac667,
		0x84fbdbd0, 0x9abc8bd5, 0x9e7d9662, 0x933eb0bb, 0x97ffad0c, 0xafb010b1,
		0xab710d06, 0xa6322bdf, 0xa2f33668, 0xbcb4666d, 0xb8757bda, 0xb5365d03,
		0xb1f740b4 };

U32 crc32_calc_sdram(U32 sdramAddress, U32 sizeInBytes) {
	U32 i, crc, data;

	crc = 0xffffffff;
	for (i = 0; i < sizeInBytes; i += 4) {
		data = *((U32*) sdramAddress);
		sdramAddress += 4;
		crc = (crc << 8) ^ crcTable[((crc >> 24) ^ (U8) (data >> 24)) & 0xff];
		crc = (crc << 8) ^ crcTable[((crc >> 24) ^ (U8) (data >> 16)) & 0xff];
		crc = (crc << 8) ^ crcTable[((crc >> 24) ^ (U8) (data >> 8)) & 0xff];
		crc = (crc << 8) ^ crcTable[((crc >> 24) ^ (U8) data) & 0xff];
	}

	return crc;
}

U32 crc32_calc_word(U32 CrcData, U32 crc) {
	U32 crc2 = crc;

	crc2 = (crc2 << 8) ^ crcTable[((crc2 >> 24) ^ (U8) (CrcData >> 24)) & 0xff];
	crc2 = (crc2 << 8) ^ crcTable[((crc2 >> 24) ^ (U8) (CrcData >> 16)) & 0xff];
	crc2 = (crc2 << 8) ^ crcTable[((crc2 >> 24) ^ (U8) (CrcData >> 8)) & 0xff];
	crc2 = (crc2 << 8) ^ crcTable[((crc2 >> 24) ^ (U8) CrcData) & 0xff];

	return crc2;
}

static int uart_open(const char* mdev) {
	struct termios old;
	struct termios new;

	uartFd = open(mdev, O_RDWR | O_NOCTTY);
	if (uartFd < 0) {
		uartFd = open(mdev, O_RDWR);
		if (uartFd < 0) {
			printf("Open COM failed!\n");
			return -1;
		}
	} else {
		if (tcgetattr(uartFd, &old) < 0) {
			printf("Get COM attr failed\n");
			return -1;
		}
		new = old;
		new.c_lflag = 0;
		new.c_iflag = IGNBRK | IGNPAR;
		new.c_oflag = 0;
		new.c_line = 0;
		new.c_cc[VMIN] = 1;
		new.c_cc[VTIME] = 0;
		new.c_cflag = CS8 | CREAD | CLOCAL | B115200;
		if (tcsetattr(uartFd, TCSAFLUSH, &new) < 0) {
			printf("Set COM attr failed\n");
			return -1;
		}
	}

	printf("Open UART Device successed.\n");

	return 0;
}

static void uart_close() {
	close(uartFd);
	uartFd = -1;
}

static int uart_read(int fd, U8 *buf, int len) {
	fd_set fset;
	struct timeval tv;
	int rval;

	FD_ZERO(&fset);
	FD_SET(fd, &fset);
	tv.tv_sec = 1;
	tv.tv_usec = 0;

	if ((rval = select(fd + 1, &fset, NULL, NULL, &tv)) < 0)
		rval = 0;

	if (rval)
		rval = read(fd, buf, len);

	return rval;
}

static int uart_recv(U8 *msg_buffer, U32 size) {
	int i, j, n;
	static int nbytes = 0;
	static U8 buf[1024];

	if (uartFd < 0) {
		printf("read error");
		return -1;
	}

	while (1) {
		n = uart_read(uartFd, &buf[nbytes], size - nbytes);
		if (n == 0)
			continue;
		else if (n < 0)
			return -2;

		nbytes += n;
		if (nbytes == size) {
			i = 0;
			while ((i < size - 4) && // Get the pack head.
					(buf[i] != (SIGN_GOKE & 0xff)
							|| buf[i + 1] != (SIGN_GOKE >> 8 & 0xff)
							|| buf[i + 2] != (SIGN_GOKE >> 16 & 0xff)
							|| buf[i + 3] != (SIGN_GOKE >> 24 & 0xff))) {
				i++;
			}
			if (i > 0) {
				for (j = 0; j < size - i; j++) {
					buf[j] = buf[j + i];
				}
				nbytes -= i;
				return -3;
			} else {
				nbytes = 0;
			}
			break;
		}
	}

	memcpy(msg_buffer, buf, size);
	memset(buf, 0, size);

	return 0;
}

int uart_send(U8 *msg_buffer, U32 size) {
	int ret;
	ret = write(uartFd, msg_buffer, size);
	if (ret < 0)
		return ret;
	else if (ret != size)
		return -1;

	return 0;
}

int data_send(U8 *data, U16 len) {
	GK110x_TO_HI3798_DATA_S dataInfo = { 0 };

	memset(&dataInfo, 0, dataStructLength);
	dataInfo.signature = SIGN_GOKE;
	dataInfo.msg = MSG_UPDATE_DATA_HI3798;
	dataInfo.length = len;
	memcpy(dataInfo.data, data, len);
	memcpy(dataBuffer, &dataInfo, dataStructLength);
	dataInfo.crc = crc32_calc_sdram((U32) (dataBuffer + dataCrcOffset),
	dataInfoLength);
	memcpy(dataBuffer, &dataInfo, dataStructLength);
	printf("(DATA)(CRC:0x%x):\t", dataInfo.crc);

	return uart_send(dataBuffer, dataStructLength);
}

int msg_send(U8 msg, U8 status, U8 reserved[2]) {
	GK110x_TO_HI3798_MsgInfoT msgInfo = { 0 };

	memset(&msgInfo, 0, msgStructLength);
	msgInfo.signature = SIGN_GOKE;
	msgInfo.msg = msg;
	msgInfo.status = status;
	memcpy(msgInfo.reserved, reserved, 2);
	memcpy(msgBuffer, &msgInfo, msgStructLength);
	msgInfo.crc = crc32_calc_sdram((U32) (msgBuffer + msgCrcOffset),
	msgInfoLength);
	memcpy(msgBuffer, &msgInfo, msgStructLength);
	printf("(CRC:0x%x):\t", msgInfo.crc);

	return uart_send(msgBuffer, msgStructLength);
}

int msg_recv(U8 *msg, U8 *status, U8 reserved[2]) {
	int ret = 0;
	U32 crc;
	GK110x_TO_HI3798_MsgInfoT msgInfo = { 0 };

	memset(&msgBuffer, 0, msgStructLength);
	ret = uart_recv(msgBuffer, msgStructLength);
	if (ret) {
		if (ret != -3)
			printf("recive message failed return %d\n", ret);
		return -1;
	}

	memcpy(&msgInfo, msgBuffer, msgStructLength);
	if (SIGN_GOKE != msgInfo.signature) {
		printf("recive message failed error signature 0x%x\n",
				msgInfo.signature);
		msg_send(msgInfo.msg, MSG_ERR_SIG, msgInfo.reserved);
		return -2;
	}

	crc = crc32_calc_sdram((U32) (msgBuffer + msgCrcOffset), msgInfoLength);
	if (crc != msgInfo.crc) {
		printf("recive message failed error crc(0x%x) recv(0x%x)\n", crc,
				msgInfo.crc);
		msg_send(msgInfo.msg, MSG_ERR_CRC, msgInfo.reserved);
		return -3;
	}

	*msg = msgInfo.msg;
	*status = msgInfo.status;
	memcpy(reserved, msgInfo.reserved, 2);

	return 0;
}
/**
 * 升级代码.
 */

int device_update_check(U8 reserved[2]) {
	int fd = -1, ret = 0;
	UpdateItemDescriptorT updateHeader = { 0 };
	U8 buffer[1024];
	U8 reserved_s[2] = { 0, 0 };

	fd = open("/mnt/sdcard/soundbar_rom.upd", O_RDONLY);
	if (fd < 0) {
		printf("can not open /mnt/sdcard/soundbar_rom.upd\n");
		update_soundbar_flag = 1;
		return -1;
	}

	ret = lseek(fd, 80, SEEK_SET);
	if (ret != 80) {
		printf("can not seek /mnt/sdcard/soundbar_rom.upd\n");
		update_soundbar_flag = 1;
		close(fd);
		return -1;
	}
	ret = read(fd, buffer, 1024);
	close(fd);
	if (ret < sizeof(UpdateItemDescriptorT)) {
		printf("can not read /mnt/sdcard/soundbar_rom.upd as minimal size\n");
		update_soundbar_flag = 1;
		return -1;
	}
	memcpy(&updateHeader, buffer, sizeof(UpdateItemDescriptorT));
	printf("VERSION x%x\n", updateHeader.version);

	if (updateHeader.version <= reserved[0]) {
		printf("do not need update current 0x%x target 0x%x\n", reserved[0],
				updateHeader.version);
		update_soundbar_flag = 2;
		return -1;
	}

	ret = msg_send(MSG_UPDATE_HI3798, MSG_SUCCEED, reserved_s);
	if (ret) {
		update_soundbar_flag = 3;
		printf("send MSG_UPDATE_HI3798(0x15) msg to goke failed(%d)\n", ret);
	} else {
		update_soundbar_flag = 99;
		printf("send MSG_UPDATE_HI3798(0x15) msg to goke\n");
	}

	return 0;
}

int device_update() {
	int fd = -1, len = 0, ret, read_size = 0, size;
	U8 buffer[1024];
	struct stat st;

	fd = open("/mnt/sdcard/soundbar_rom.upd", O_RDONLY);
	if (fd < 0) {
		printf("can not open /mnt/sdcard/soundbar_rom.upd\n");
		return -1;
	}

	stat("/mnt/sdcard/soundbar_rom.upd", &st);
	size = (int) st.st_size;

	while (1) {
		len = read(fd, buffer, 1024);
		if (len == 0) {
			printf("read soundbar update file over.\n");
			break;
		} else if (len < 0) {
			printf("read /mnt/sdcard/soundbar_rom.upd failed\n");
			return -2;
		} else {
			ret = data_send(buffer, len);
			read_size += len;
			if (ret) {
				printf(
						"send MSG_UPDATE_DATA_HI3798(0x19) msg to goke failed(%d)\n",
						ret);
			} else {
				printf(
						"send MSG_UPDATE_DATA_HI3798(0x19) msg to goke(%dpercent)\n",
						(read_size * 100) / size);
			}
		}
	}

	return 0;
}

void check_init_return() {
	int ret = 0;
	U8 reserved[2] = { 0, 0 };

	while (1) {
		sleep(2);
		if (recvInitFinishFlag == 0) {
			ret = msg_send(MSG_INIT_FINISH_HI3798, MSG_SUCCEED, reserved);
			if (ret)
				printf(
						"send MSG_INIT_FINISH_HI3798(0x12) msg to goke failed(%d)\n",
						ret);
			else
				printf("send MSG_INIT_FINISH_HI3798(0x12) msg to goke\n");
		} else {
			break;
		}
	}
}
/*Java调用C函数*/
/**
 * 把一个jstring转换成一个c语言的char* 类型.
 */
char* _JString2CStr(JNIEnv* env, jstring jstr) {
	char* rtn = NULL;
	jclass clsstring = (*env)->FindClass(env, "java/lang/String");
	jstring strencode = (*env)->NewStringUTF(env, "GB2312");
	jmethodID mid = (*env)->GetMethodID(env, clsstring, "getBytes",
			"(Ljava/lang/String;)[B");
	jbyteArray barr = (jbyteArray) (*env)->CallObjectMethod(env, jstr, mid,
			strencode); // String .getByte("GB2312");
	jsize alen = (*env)->GetArrayLength(env, barr);
	jbyte* ba = (*env)->GetByteArrayElements(env, barr, JNI_FALSE);
	if (alen > 0) {
		rtn = (char*) malloc(alen + 1); //"\0"
		memcpy(rtn, ba, alen);
		rtn[alen] = 0;
	}
	(*env)->ReleaseByteArrayElements(env, barr, ba, 0);
	return rtn;
}
/*Java在service中调用********************************************************* */
jbyteArray Java_com_yobbom_service_MainService_msgrecv(JNIEnv* env,
		jobject thiz) {
	jint i;
	U8 cstr_msg_data[2];
	U8 cstr_status_data[2];
	U8 cstr_reserved_data[3];
	unsigned char msgrecv_data[4];
	i = msg_recv(cstr_msg_data, cstr_status_data, cstr_reserved_data);
	if (i < 0)
		return i;
	msgrecv_data[0] = cstr_msg_data[0];
	msgrecv_data[1] = cstr_status_data[0];
	msgrecv_data[2] = cstr_reserved_data[0];
	msgrecv_data[3] = cstr_reserved_data[1];
	jbyteArray array = (*env)->NewByteArray(env, 4);
	(*env)->SetByteArrayRegion(env, array, 0, 4, msgrecv_data);
	return array;
}
jint Java_com_yobbom_service_MainService_msgsend(JNIEnv* env, jobject thiz,
		jbyte msg, jbyte status, jbyteArray reserved_data) {

	jint i;
	unsigned char *cstr_reserved_data = (*env)->GetByteArrayElements(env,
			reserved_data, NULL);
	i = msg_send(msg, status, cstr_reserved_data);
	(*env)->ReleaseByteArrayElements(env, reserved_data, cstr_reserved_data, 0);
	return i;
}

jint Java_com_yobbom_service_MainService_datasend(JNIEnv* env, jobject thiz,
		jbyteArray data_data, jint len) {
//	char* cstr_data_data = _JString2CStr(env, data_data);
	jint i;
	unsigned char *cstr_data_data = (*env)->GetByteArrayElements(env, data_data,
			NULL);
	i = data_send(cstr_data_data, len);
	(*env)->ReleaseByteArrayElements(env, data_data, cstr_data_data, 0);
	return i;

}

//没有返回值
void Java_com_yobbom_service_MainService_sclose(JNIEnv* env, jobject thiz) {
	uart_close();
}

jint Java_com_yobbom_service_MainService_uartopen(JNIEnv* env, jobject thiz,
		jstring mdev_data) {
	char* cstr_mdev_data = _JString2CStr(env, mdev_data);
	/*jint i;
	 unsigned char *cstr_mdev_data= (*env)->GetByteArrayElements(env, mdev_data, NULL);
	 i=
	 (*env)->ReleaseByteArrayElements(env, mdev_data, cstr_mdev_data,0);*/
	return uart_open(cstr_mdev_data);;
}
/*Java在mainActivity中调用********************************************************* */


//初始化完成
void Java_com_yobbom_service_MainService_initreturn(JNIEnv* env, jobject thiz) {
	check_init_return();
}
//传输数据升级完成
jint Java_com_yobbom_service_MainService_deviceupdate(JNIEnv* env,
		jobject thiz) {
	jint i;
	i = device_update();
	return i;
}
//检查更新，通知升级
jint Java_com_yobbom_service_MainService_deviceupdatecheck(JNIEnv* env,
		jobject thiz, jbyteArray reserved_data) {
	jint i;
	unsigned char *cstr_reserved_data = (*env)->GetByteArrayElements(env,
			reserved_data, NULL);
	i = device_update_check(cstr_reserved_data);
	(*env)->ReleaseByteArrayElements(env, reserved_data, cstr_reserved_data, 0);
	return i;
}

