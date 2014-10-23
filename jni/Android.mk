LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := seqView
LOCAL_SRC_FILES := seqView.cpp

include $(BUILD_SHARED_LIBRARY)
