    LOCAL_PATH := $(call my-dir)
    include $(CLEAR_VARS)
	#ָ��C�ļ��������ɵ�so��������
    LOCAL_MODULE    := goke
    #ָ��Ҫ�����C�ļ�
    LOCAL_SRC_FILES := goke_ctrl48.c 
    LOCAL_LDLIBS    := -llog
    include $(BUILD_SHARED_LIBRARY)
  
 
