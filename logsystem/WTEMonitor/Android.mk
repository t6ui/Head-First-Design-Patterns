LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

logserver_dir := \
    ./logserver

LOGSERVER_FILES_SRC := \
    $(logserver_dir)/src/logserver.cpp \
    $(logserver_dir)/src/utils.cpp   \
    $(logserver_dir)/src/monitor.cpp \
    $(logserver_dir)/src/wt_compress.cpp \
    $(logserver_dir)/src/socket.cpp \
    $(logserver_dir)/src/emonitor.cpp

LOCAL_SRC_FILES := $(LOGSERVER_FILES_SRC)

LOCAL_C_INCLUDES := \
    $(LOCAL_PATH)/logserver/include \
    external/zlib

LOCAL_SHARED_LIBRARIES := libcutils \
    libutils \
    libz

LOCAL_MODULE := wtemonitor
LOCAL_MODULE_TAGS := optional
LOCAL_CFLAGS += -Wno-unused-parameter

include $(BUILD_EXECUTABLE)
