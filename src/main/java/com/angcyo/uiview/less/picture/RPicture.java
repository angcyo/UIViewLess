package com.angcyo.uiview.less.picture;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.utils.Root;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/12/15
 */
public class RPicture {
    public static void start(@NonNull Activity activity, @Nullable List<LocalMedia> selectionMedia /*已经选中的媒体*/) {
        // 进入相册 以下是例子：不需要的api可以不写
        PictureSelector.create(activity)
                // 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                .openGallery(PictureMimeType.ofAll())
                // 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
                .theme(R.style.picture_default_style)
                // 最大图片选择数量
                .maxSelectNum(9)
                // 最小选择数量
                .minSelectNum(1)
                // 每行显示个数
                .imageSpanCount(4)
                // 多选 or 单选
                .selectionMode(PictureConfig.MULTIPLE)
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
                .enableCrop(true)
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
                .scaleEnabled(true)
                //.videoQuality()// 视频录制质量 0 or 1
                //.videoSecond()//显示多少秒以内的视频or音频也可适用
                //.recordVideoSecond()//录制视频秒数 默认60s
                .forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
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

            }
        }
        return result;
    }
}
