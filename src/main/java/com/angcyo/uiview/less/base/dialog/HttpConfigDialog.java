package com.angcyo.uiview.less.base.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CompoundButton;
import com.angcyo.http.Http;
import com.angcyo.http.Json;
import com.angcyo.lib.L;
import com.angcyo.uiview.less.R;
import com.angcyo.uiview.less.recycler.RBaseViewHolder;
import com.angcyo.uiview.less.utils.RDialog;
import com.angcyo.uiview.less.utils.T_;
import retrofit2.RetrofitServiceMapping;

import java.util.Map;

/**
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/12/13
 */
public class HttpConfigDialog {
    public static void show(@NonNull Context context, final String baseUrl, @Nullable final OnHttpConfig onHttpConfig) {
        RDialog.build(context)
                .setCanceledOnTouchOutside(false)
                .setContentLayoutId(R.layout.base_http_config_layout)
                .setInitListener(new RDialog.OnInitListener() {
                    @Override
                    public void onInitDialog(@NonNull final Dialog dialog, @NonNull final RBaseViewHolder dialogViewHolder) {
                        dialogViewHolder.exV(R.id.host_edit).setInputText(baseUrl);

                        dialogViewHolder.cb(R.id.map_box, RetrofitServiceMapping.enableMapping, new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                dialogViewHolder.v(R.id.get_list).setEnabled(isChecked);

                                RetrofitServiceMapping.init(isChecked, RetrofitServiceMapping.defaultMap);
                            }
                        });

                        dialogViewHolder.click(R.id.get_list, new View.OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                Http.request("https://www.angcyo.com/api/php/android/c/url_mapping", new Http.OnHttpRequestCallback() {
                                    @Override
                                    public void onRequestCallback(@NonNull final String body) {
                                        L.json(body);
                                        Map mapping = Json.from(body, Map.class);
                                        RetrofitServiceMapping.init(RetrofitServiceMapping.enableMapping, mapping);

                                        v.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                T_.info(body);
                                            }
                                        });
                                    }
                                });
                            }
                        });

                        dialogViewHolder.click(R.id.save_button, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (onHttpConfig != null) {
                                    onHttpConfig.onSaveBaseUrl(dialogViewHolder.exV(R.id.host_edit).string());
                                }

                                dialog.cancel();
                            }
                        });
                    }
                })
                .showAlertDialog();
    }

    public interface OnHttpConfig {
        void onSaveBaseUrl(@NonNull String baseUrl);
    }
}
