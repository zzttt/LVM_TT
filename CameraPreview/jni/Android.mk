LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := myproc
LOCAL_SRC_FILES := native_proc.c
LOCAL_LDLIBS +=  -llog -ljnigraphics -lz -lm

include $(BUILD_SHARED_LIBRARY)