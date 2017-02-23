package com.mistesu.frescoloader;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.facebook.imagepipeline.request.Postprocessor;

import java.io.File;

/**
 * Created by Misutesu on 2017/2/22.
 */

public class FrescoLoader {

    private static Context mContext;

    public static final int CENTER_CROP = 1;
    public static final int FIT_CENTER = 2;
    public static final int FIT_XY = 3;
    public static final int CENTER_INSIDE = 4;

    public static final int CIRCLE = -1;

    private FrescoLoader() {
    }

    public static void init(@NonNull Context context) {
        mContext = context;
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(context)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(context, config);
//        Fresco.initialize(context);
    }

    @NonNull
    public static Builder load(@NonNull Uri uri) {
        return new Builder(uri);
    }

    public static Builder load(@NonNull File file) {
        return new Builder("file://" + file.getAbsolutePath());
    }

    public static Builder load(@NonNull String url) {
        return new Builder(url);
    }

    public static Builder load(int resId) {
        return new Builder("res:// /" + resId);
    }

    public static class Builder {

        private int width = 0;
        private int height = 0;
        private int durationTime = 300;
        private int scaleType = CENTER_CROP;
        private int failureImgResId = -1;
        private int placeImgResId = -1;
        private int circleRound = 0;
        private int borderWidth = 0;
        private int borderColor = 0;
        private boolean clickRetryEnable = false;
        private Postprocessor postprocessor;
        private OnDownloadListener mOnDownloadListener;

        private Uri mUri;

        private Builder(Uri uri) {
            mUri = uri;
        }

        private Builder(String url) {
            if (!TextUtils.isEmpty(url)) {
                mUri = getUri(url);
            }
        }

        public Builder resize(int width, int height) {
            if (width > 0 && height > 0) {
                this.width = width;
                this.height = height;
            }
            return this;
        }

        public Builder setDurationTime(int time) {
            if (time > 0)
                this.durationTime = time;
            return this;
        }

        public Builder setScaleType(int scaleType) {
            this.scaleType = scaleType;
            return this;
        }

        public Builder setFailureImage(int res) {
            failureImgResId = res;
            return this;
        }

        public Builder setPlaceImage(int res) {
            placeImgResId = res;
            return this;
        }

        public Builder setCircleRound(int round) {
            circleRound = round;
            return this;
        }

        public Builder setCircle() {
            circleRound = -1;
            return this;
        }

        public Builder setBorder(int width, int color) {
            if (width > 0) {
                borderWidth = width;
                borderColor = color;
            }
            return this;
        }

        public Builder setRetryEnable(boolean enable) {
            clickRetryEnable = enable;
            return this;
        }

        public Builder setPostprocessor(@NonNull Postprocessor postprocessor) {
            this.postprocessor = postprocessor;
            return this;
        }

        public Builder setOnDownloadListener(OnDownloadListener onDownloadListener) {
            mOnDownloadListener = onDownloadListener;
            return this;
        }

        public Builder clearImgCache() {
            FrescoLoader.clearImgCache(mUri);
            return this;
        }

        public void into(@NonNull SimpleDraweeView simpleDraweeView) {
            if (mUri != null) {
                GenericDraweeHierarchy hierarchy = simpleDraweeView.getHierarchy();
                hierarchy.setFadeDuration(durationTime);
                ScalingUtils.ScaleType scale = getScaleType(scaleType);
                if (failureImgResId != -1) {
                    hierarchy.setFailureImage(ContextCompat.getDrawable(mContext, failureImgResId), scale);
                }
                if (placeImgResId != -1) {
                    hierarchy.setPlaceholderImage(ContextCompat.getDrawable(mContext, placeImgResId), scale);
                }
                hierarchy.setActualImageScaleType(scale);

                RoundingParams roundingParams = null;
                if (circleRound == -1) {
                    roundingParams = new RoundingParams();
                    roundingParams.setRoundAsCircle(true);
                } else if (circleRound != 0) {
                    roundingParams = RoundingParams.fromCornersRadius(circleRound);
                }
                if (borderWidth > 0) {
                    if (roundingParams == null) {
                        roundingParams = new RoundingParams();
                    }
                    roundingParams.setBorder(borderColor, borderWidth);
                }

                if (roundingParams != null) {
                    hierarchy.setRoundingParams(roundingParams);
                }

                simpleDraweeView.setHierarchy(hierarchy);

                ImageRequestBuilder requestBuilder = ImageRequestBuilder.newBuilderWithSource(mUri);
                if (postprocessor != null) {
                    requestBuilder.setPostprocessor(postprocessor);
                }

                if (width > 0 && height > 0) {
                    requestBuilder.setResizeOptions(new ResizeOptions(width, height));
                }

                simpleDraweeView.setController(Fresco.newDraweeControllerBuilder()
                        .setImageRequest(requestBuilder.build())
                        .setControllerListener(new ControllerListener(mOnDownloadListener))
                        .setTapToRetryEnabled(clickRetryEnable)
                        .setOldController(simpleDraweeView.getController())
                        .build());
            }
        }
    }

    public static void clearImgChche(@NonNull File file) {
        if (file.exists())
            clearImgCache(getUri(file));
    }

    public static void clearImgChche(@NonNull String url) {
        clearImgCache(getUri(url));
    }

    private static void clearImgCache(@NonNull Uri uri) {
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        imagePipeline.evictFromMemoryCache(uri);
        imagePipeline.evictFromDiskCache(uri);
        imagePipeline.evictFromCache(uri);
    }

    public static Uri getUri(int resId) {
        return getUri("res:// /" + resId);
    }

    public static Uri getUri(@NonNull File file) {
        return getUri("file://" + file.getAbsolutePath());
    }

    public static Uri getUri(@NonNull String uri) {
        return Uri.parse(uri);
    }

    private static ScalingUtils.ScaleType getScaleType(int scaleType) {
        switch (scaleType) {
            case 2:
                return ScalingUtils.ScaleType.FIT_CENTER;
            case 3:
                return ScalingUtils.ScaleType.FIT_XY;
            case 4:
                return ScalingUtils.ScaleType.CENTER_INSIDE;
            default:
                return ScalingUtils.ScaleType.CENTER_CROP;
        }
    }
}