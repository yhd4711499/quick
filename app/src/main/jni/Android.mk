LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
 
LOCAL_LDLIBS    := -llog
 
LOCAL_MODULE    := getpinyin
LOCAL_SRC_FILES := getpinyin.c

include $(BUILD_SHARED_LIBRARY)