package com.mistesu.frescoloader;

import android.graphics.drawable.Animatable;

import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;

/**
 * Created by Misutesu on 2017/2/23.
 */

public class ControllerListener extends BaseControllerListener<ImageInfo> {

    private OnDownloadListener mOnDownloadListener;

    public ControllerListener(OnDownloadListener onDownloadListener) {
        mOnDownloadListener = onDownloadListener;
    }

    @Override
    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
        super.onFinalImageSet(id, imageInfo, animatable);
        if (mOnDownloadListener != null) {
            mOnDownloadListener.onDownloadEnd(true);
        }
    }

    @Override
    public void onFailure(String id, Throwable throwable) {
        super.onFailure(id, throwable);
        if (mOnDownloadListener != null) {
            mOnDownloadListener.onDownloadEnd(false);
        }
    }
}
