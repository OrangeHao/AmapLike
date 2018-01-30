package com.orange.amaplike;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.model.Poi;
import com.orange.amaplike.utils.GPSUtil;

import java.util.List;

/**
 * created by czh on 2018-01-25
 */

public class NaviDialog {
    private static Context mContext;

    private AlertDialog.Builder mBuilder;
    private AlertDialog mDialog;
    private View mDialogView;
    private TextView mAmap;
    private TextView mBmap;
    private boolean installAmap=false;
    private boolean installBmap=false;

    private final int TYPE_DRIVE=100;
    private final int TYPE_BUS=101;
    private final int TYPE_WALK=102;
    private final int TYPE_RIDE=103;

    private void initDialog(final Poi start, final Poi end, final int type) {
        mBuilder=new AlertDialog.Builder(mContext);
        mDialogView= LayoutInflater.from(mContext)
                .inflate(R.layout.item_navi_selete_dialog, null);
        mBuilder.setView(mDialogView);
        mAmap=(TextView)mDialogView.findViewById(R.id.amap_text);
        mBmap=(TextView)mDialogView.findViewById(R.id.baidumap_text);

        final PackageManager packageManager = mContext.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String temp=packageInfos.get(i).packageName;
                if (temp.contains("com.autonavi.minimap")){
                    installAmap=true;
                }
                if (temp.contains("com.baidu.BaiduMap")){
                    installBmap=true;
                }
                if (installAmap && installBmap){
                    break;
                }
            }
        }


        if (installAmap){
            mAmap.setText(mContext.getText(R.string.route_plan_navi_amap));
        }else {
            mAmap.setText(mContext.getText(R.string.route_plan_navi_amap_not));
        }
        if (installBmap){
            mBmap.setText(mContext.getString(R.string.route_plan_navi_bmap));
        }else {
            mBmap.setText(mContext.getString(R.string.route_plan_navi_bmap_not));
        }
        LinearLayout amapLyaout=(LinearLayout)mDialogView.findViewById(R.id.amap_layout);
        amapLyaout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAmap(start,end,type);
                mDialog.dismiss();
            }
        });
        LinearLayout bmapLyaout=(LinearLayout)mDialogView.findViewById(R.id.baidumap_layout);
        bmapLyaout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startBaidu(start,end,type);
                mDialog.dismiss();
            }
        });
        mDialog=mBuilder.show();
        mDialog.setCanceledOnTouchOutside(true);
    }

    public void showView(Context context, Poi start, Poi end, int type) {
        mContext=context;
        initDialog(start,end,type);
    }

    /**
     * 检查手机上是否安装了指定的软件
     *
     * @param context
     * @param packageName：应用包名
     * @return
     */
    public static boolean isAvilible(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                if (packageInfos.get(i).packageName.contains(packageName)){
                    return true;
                }
            }
        }
        return false;
    }


    private void startBaidu(Poi start, Poi end, int type){
        try {
            Intent intent=new Intent();
            if (installBmap){
                String temp="";
                double lat0=start.getCoordinate().latitude;
                double lon0=start.getCoordinate().longitude;
                double lat=end.getCoordinate().latitude;
                double lon=end.getCoordinate().longitude;
                double startPois[]= GPSUtil.gcj02_To_Bd09(lat0,lon0);
                double endPois[]=GPSUtil.gcj02_To_Bd09(lat,lon);
                lat0=startPois[0];
                lon0=startPois[1];
                lat=endPois[0];
                lon=endPois[1];
                switch (type){
                    case TYPE_DRIVE:
                        temp="baidumap://map/navi?location="+lat+","+
                              lon;
                        break;
                    case TYPE_RIDE:
                        temp="baidumap://map/bikenavi?origin="+lat0+","+lon0+"&destination="+lat+","+lon;
                        break;
                    case TYPE_WALK:
                        temp="baidumap://map/walknavi?origin="+lat0+","+lon0+"&destination="+lat+","+lon;
                        break;
                }
                Log.d("czh",temp);
                intent.setData(Uri.parse(temp));
                mContext.startActivity(intent);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startAmap(Poi start, Poi end, int type){
        try {
            Intent intent=new Intent();
            if (installAmap){
                String temp="androidamap://navi?sourceApplication="+mContext.getPackageName()
                        +"&poiname="+end.getName()+"&lat="+end.getCoordinate().latitude+
                        "&lon="+end.getCoordinate().longitude
                        +"&dev=0&style=2";
                Log.d("czh",temp);
                intent.setData(Uri.parse(temp));
                mContext.startActivity(intent);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}