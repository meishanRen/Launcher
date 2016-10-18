    LOCAL_PATH := $(call my-dir)
    include $(CLEAR_VARS)
	#指定C文件编译打包成的so类库的名字
    LOCAL_MODULE    := goke
    #指定要编译的C文件
    LOCAL_SRC_FILES := goke_ctrl48.c 
    LOCAL_LDLIBS    := -llog
    include $(BUILD_SHARED_LIBRARY)
  
 
