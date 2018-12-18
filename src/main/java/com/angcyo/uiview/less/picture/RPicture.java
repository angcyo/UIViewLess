package com.angcyo.uiview.less.picture;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.utils.RUtils;
import com.angcyo.uiview.less.utils.Root;
import com.luck.picture.lib.PictureSelectionModel;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.luck.picture.lib.config.PictureConfig.MULTIPLE;
import static com.luck.picture.lib.config.PictureConfig.SINGLE;

/**
 * https://github.com/LuckSiege/PictureSelector
 * <p>
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/12/15
 */
public class RPicture {
    public static Builder build(@NonNull Activity activity) {
        return new Builder(activity);
    }

    public static void start(@NonNull Activity activity, @Nullable List<LocalMedia> selectionMedia /*已经选中的媒体*/) {
        build(activity).setSelectionMedia(selectionMedia).circle().doIt();
    }

    /**
     * 打开图片浏览
     */
    public static void previewPicture(@NonNull Activity activity, int position, List<LocalMedia> medias) {
        PictureSelector.create(activity).themeStyle(R.style.picture_default_style).openExternalPreview(position, medias);
    }

    /**
     * 视频
     */
    public static void previewVideo(@NonNull Activity activity, @Nullable String path) {
        PictureSelector.create(activity).externalPictureVideo(path);
    }

    /**
     * 音频
     */
    public static void previewAudio(@NonNull Activity activity, @Nullable String path) {
        PictureSelector.create(activity).externalPictureAudio(path);
    }

    public static void baseEnterAnim(@Nullable Activity activity) {
        if (activity != null) {
            activity.overridePendingTransition(R.anim.base_tran_to_bottom_enter, 0);
        }
    }

    public static void baseExitAnim(@Nullable Activity activity) {
        if (activity != null) {
            activity.overridePendingTransition(0, R.anim.base_tran_to_bottom_exit);
        }
    }

    /**
     * 获取返回值
     */
    public static List<LocalMedia> onActivityResult(int requestCode, int resultCode, Intent data) {
        List<LocalMedia> result = new ArrayList<>();
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片选择结果回调
                    result = PictureSelector.obtainMultipleResult(data);
                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的
                default:
                    break;

            }
        }
        StringBuilder builder = new StringBuilder("图片选择结果返回:");
        for (int i = 0; i < result.size(); i++) {
            LocalMedia media = result.get(i);
            builder.append("\n");
            builder.append(i);
            builder.append("->原始->");
            builder.append(media.getPath());
            builder.append(" ");
            builder.append(RUtils.formatFileSize(new File(media.getPath()).length()));
            builder.append(" ");
            builder.append(media.getSelectorType());
            builder.append(" ");
            builder.append(media.getPictureType());

            if (media.isCut()) {
                builder.append("\n   剪切->");
                builder.append(media.getCutPath());
                builder.append(" ");
                builder.append(RUtils.formatFileSize(new File(media.getCutPath()).length()));
            }

            if (media.isCompressed()) {
                builder.append("\n   压缩->");
                builder.append(media.getCompressPath());
                builder.append(" ");
                builder.append(RUtils.formatFileSize(new File(media.getCompressPath()).length()));
            }
            builder.append("\n");
        }
        L.w(builder.toString());
        return result;
    }

    public static class Builder {
        @NonNull
        Activity activity;

        /**
         * 已经选中的媒体
         */
        @Nullable
        List<LocalMedia> selectionMedia;

        @NonNull
        PictureSelectionModel selectionModel;

        public Builder(@NonNull Activity activity) {
            this.activity = activity;

            // 进入相册 以下是例子：不需要的api可以不写
            selectionModel = PictureSelector.create(activity)
                    // 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                    .openGallery(PictureMimeType.ofImage())
                    // 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
                    .theme(R.style.picture_default_style)
                    // 最大图片选择数量
                    .maxSelectNum(9)
                    // 最小选择数量
                    .minSelectNum(1)
                    // 每行显示个数
                    .imageSpanCount(4)
                    // 多选 or 单选
                    .selectionMode(MULTIPLE)
                    // 是否可预览图片
                    .previewImage(true)
                    // 是否可预览视频
                    .previewVideo(true)
                    // 是否可播放音频
                    .enablePreviewAudio(true)
                    // 是否显示拍照按钮
                    .isCamera(true)
                    // 图片列表点击 缩放效果 默认true
                    .isZoomAnim(true)
                    //.imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                    //.setOutputCameraPath("/CustomPath")// 自定义拍照保存路径
                    // 是否裁剪
                    .enableCrop(false)
                    // 是否压缩
                    .compress(true)
                    //同步true或异步false 压缩 默认同步
                    .synOrAsy(true)
                    //压缩图片保存地址
                    .compressSavePath(Root.getAppExternalFolder("LuBan"))
                    //.sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                    // glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
                    .glideOverride(160, 160)
                    // 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
                    .withAspectRatio(1, 1)
                    // 是否显示uCrop工具栏，默认不显示
                    .hideBottomControls(true)
                    // 是否显示gif图片
                    .isGif(true)
                    // 裁剪框是否可拖拽
                    .freeStyleCropEnabled(false)
                    // 是否圆形裁剪
                    .circleDimmedLayer(false)
                    // 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                    .showCropFrame(true)
                    // 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                    .showCropGrid(true)
                    // 是否开启点击声音
                    .openClickSound(false)
                    // 是否传入已选图片
                    .selectionMedia(selectionMedia)
                    // 是否可拖动裁剪框(固定)
                    .isDragFrame(false)
                    //.videoMaxSecond(15)
                    //.videoMinSecond(10)
                    //.previewEggs(false)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                    //.cropCompressQuality(90)// 裁剪压缩质量 默认100
                    .minimumCompressSize(100)// 小于100kb的图片不压缩
                    //.cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效
                    // 裁剪是否可旋转图片
                    .rotateEnabled(true)
                    // 裁剪是否可放大缩小图片
                    .scaleEnabled(true);
            //.videoQuality()// 视频录制质量 0 or 1
            //.videoSecond()//显示多少秒以内的视频or音频也可适用
            //.recordVideoSecond()//录制视频秒数 默认60s
            //.forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code

        }

        public Builder setSelectionMedia(@Nullable List<LocalMedia> selectionMedia) {
            this.selectionMedia = selectionMedia;
            return this;
        }

        @NonNull
        public PictureSelectionModel getSelectionModel() {
            return selectionModel;
        }

        public Builder ofImage() {
            selectionModel.mimeType(PictureMimeType.ofImage());
            return this;
        }

        public Builder ofAll() {
            selectionModel.mimeType(PictureMimeType.ofAll());
            return this;
        }

        public Builder ofVideo() {
            selectionModel.mimeType(PictureMimeType.ofVideo());
            return this;
        }

        public Builder ofAudio() {
            selectionModel.mimeType(PictureMimeType.ofAudio());
            return this;
        }

        public Builder circle() {
            // 是否圆形裁剪
            selectionModel.circleDimmedLayer(true)
                    // 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                    .showCropFrame(false)
                    // 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                    .showCropGrid(false);
            return this;
        }

        public Builder rect() {
            // 是否圆形裁剪
            selectionModel.circleDimmedLayer(false)
                    // 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                    .showCropFrame(true)
                    // 是否显示裁剪矩形网格 圆形裁剪时建议设为false
                    .showCropGrid(true);
            return this;
        }

        public Builder maxSelectNum(int num) {
            selectionModel.maxSelectNum(num);
            return this;
        }

        public Builder minSelectNum(int num) {
            selectionModel.minSelectNum(num);
            return this;
        }

        /**
         * 单选or多选
         */
        public Builder selectionMode(int mode) {
            selectionModel.selectionMode(mode);
            return this;
        }

        public Builder multipleMode() {
            selectionMode(MULTIPLE);
            return this;
        }

        public Builder singleMode() {
            selectionMode(SINGLE);
            return this;
        }

        public Builder compress(boolean value) {
            selectionModel.compress(value);
            return this;
        }

        public Builder enableCrop(boolean value) {
            selectionModel.enableCrop(value);
            return this;
        }

        public void doIt() {
            selectionModel.forResult();
        }

    }
}
