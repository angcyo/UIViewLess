package com.bigkoo.pickerview;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import com.angcyo.http.Http;
import com.angcyo.uiview.less.RApplication;
import com.bigkoo.pickerview.bean.JsonBean;
import com.google.gson.Gson;
import org.json.JSONArray;
import rx.Observable;
import rx.Observer;
import rx.observables.SyncOnSubscribe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 从 assets 中,初始化 城市信息
 * Email:angcyo@126.com
 *
 * @author angcyo
 * @date 2018/12/18
 */
public class City {

    public static List<JsonBean> options1Items = new ArrayList<>();
    public static List<List> options2Items = new ArrayList<>();
    public static List<List<List>> options3Items = new ArrayList<>();

    /**
     * 返回省市区
     */
    public static String[] getProvinceCityDistrict(int index1, int index2, int index3) {
        String[] result = new String[]{"", "", ""};
        if (index1 >= 0) {
            result[0] = options1Items.get(index1).getName();

            if (index2 >= 0) {
                result[1] = (String) options2Items.get(index1).get(index2);

                if (index3 >= 0) {
                    result[2] = (String) options3Items.get(index1).get(index2).get(index3);
                }
            }
        }
        return result;
    }

    /**
     * 获取省 在列表中的index
     */
    public static int getProvinceIndex(String province) {
        int index = -1;
        if (TextUtils.isEmpty(province)) {
            return index;
        }
        for (int i = 0; i < options1Items.size(); i++) {
            JsonBean bean = options1Items.get(i);
            if (TextUtils.equals(bean.getName(), province)) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 获取市 在列表中的index
     */
    public static int getCityIndex(String province, String city) {
        int provinceIndex = getProvinceIndex(province);
        if (provinceIndex == -1) {
            return -1;
        }

        int index = -1;
        if (TextUtils.isEmpty(city)) {
            return index;
        }
        for (int i = 0; i < options2Items.get(provinceIndex).size(); i++) {
            String bean = (String) options2Items.get(provinceIndex).get(i);
            if (TextUtils.equals(bean, city)) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 获取区 在列表中的index
     */
    public static int getDistrictIndex(String province, String city, String district) {
        int provinceIndex = getProvinceIndex(province);
        int cityIndex = getCityIndex(province, city);
        if (provinceIndex == -1 || cityIndex == -1) {
            return -1;
        }

        int index = -1;
        if (TextUtils.isEmpty(city)) {
            return index;
        }
        for (int i = 0; i < options3Items.get(provinceIndex).get(cityIndex).size(); i++) {
            String bean = (String) options3Items.get(provinceIndex).get(cityIndex).get(i);
            if (TextUtils.equals(bean, district)) {
                index = i;
                break;
            }
        }
        return index;
    }

    private static String getJson(Context context, String fileName) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private static void initJsonData(Context context) {//解析数据

        /**
         * 注意：assets 目录下的Json文件仅供参考，实际使用可自行替换文件
         * 关键逻辑在于循环体
         *
         * */
        String JsonData = getJson(context, "province.json");//获取assets目录下的json文件数据

        ArrayList<JsonBean> jsonBean = parseData(JsonData);//用Gson 转成实体

        /**
         * 添加省份数据
         *
         * 注意：如果是添加的JavaBean实体，则实体类需要实现 IPickerViewData 接口，
         * PickerView会通过getPickerViewText方法获取字符串显示出来。
         */
        options1Items = jsonBean;

        for (int i = 0; i < jsonBean.size(); i++) {//遍历省份
            ArrayList<String> CityList = new ArrayList<>();//该省的城市列表（第二级）
            ArrayList<List> Province_AreaList = new ArrayList<>();//该省的所有地区列表（第三极）

            for (int c = 0; c < jsonBean.get(i).getCityList().size(); c++) {//遍历该省份的所有城市
                String CityName = jsonBean.get(i).getCityList().get(c).getName();
                CityList.add(CityName);//添加城市
                ArrayList<String> City_AreaList = new ArrayList<>();//该城市的所有地区列表

                //如果无地区数据，建议添加空字符串，防止数据为null 导致三个选项长度不匹配造成崩溃
                if (jsonBean.get(i).getCityList().get(c).getArea() == null
                        || jsonBean.get(i).getCityList().get(c).getArea().size() == 0) {
                    City_AreaList.add("");
                } else {
                    City_AreaList.addAll(jsonBean.get(i).getCityList().get(c).getArea());
                }
                Province_AreaList.add(City_AreaList);//添加该省所有地区数据
            }

            /**
             * 添加城市数据
             */
            options2Items.add(CityList);

            /**
             * 添加地区数据
             */
            options3Items.add(Province_AreaList);
        }
    }


    private static ArrayList<JsonBean> parseData(String result) {//Gson 解析
        ArrayList<JsonBean> detail = new ArrayList<>();
        try {
            JSONArray data = new JSONArray(result);
            Gson gson = new Gson();
            for (int i = 0; i < data.length(); i++) {
                JsonBean entity = gson.fromJson(data.optJSONObject(i).toString(), JsonBean.class);
                detail.add(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return detail;
    }

    public static Observable<Boolean> initJson() {
        return Observable.create(new SyncOnSubscribe<Long, Boolean>() {
            @Override
            protected Long generateState() {
                return 1L;
            }

            @Override
            protected Long next(Long state, Observer<? super Boolean> observer) {
                if (!options1Items.isEmpty() && !options2Items.isEmpty()) {
                    observer.onNext(Boolean.TRUE);
                    observer.onCompleted();
                    return 1L;
                }

                options1Items.clear();
                options2Items.clear();
                options3Items.clear();


                initJsonData(RApplication.getApp());

                observer.onNext(Boolean.TRUE);

                observer.onCompleted();

                return 1L;
            }
        }).compose(Http.<Boolean>defaultTransformer());
    }
}
