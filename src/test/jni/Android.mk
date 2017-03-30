LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := libdl
LOCAL_SRC_FILES := libdl.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libtee
LOCAL_SRC_FILES := libtee.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE        :=  test_eks
LOCAL_SRC_FILES	    :=  test_eks.c

LOCAL_C_FLAGS       :=  -rdynamic -DANDROID

LOCAL_C_INCLUDES    := /home/andro/Open-TEE/libtee/include

LOCAL_SHARED_LIBRARIES := libdl libtee

ifeq ($(TARGET_ARCH),arm)
LOCAL_LDFLAGS := -Wl,--hash-style=sysv
endif

include $(BUILD_EXECUTABLE)
