LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libdl
LOCAL_SRC_FILES := libdl.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libc
LOCAL_SRC_FILES := libc.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libcrypto_test
LOCAL_SRC_FILES := libcrypto_test.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libInternalApi
LOCAL_SRC_FILES := libInternalApi.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libstorage_test
LOCAL_SRC_FILES := libstorage_test.so
include $(PREBUILT_SHARED_LIBRARY) 

include $(CLEAR_VARS)
LOCAL_MODULE        :=  eks_ta
LOCAL_SRC_FILES	    :=  eks_ta.c

LOCAL_C_INCLUDES := \
	/home/andro/Open-TEE/emulator/include \
	/home/andro/Open-TEE/TAs/include

LOCAL_CFLAGS := -DANDROID -g -O0 -DTA_PLUGIN -DOT_LOGGING

LOCAL_SHARED_LIBRARIES := libc libdl libInternalApi libcrypto_test libstorage_test 
LOCAL_LDLIBS := -llog

ifeq ($(TARGET_ARCH),arm)
LOCAL_LDFLAGS := -Wl,--hash-style=sysv
endif

include $(BUILD_SHARED_LIBRARY)
