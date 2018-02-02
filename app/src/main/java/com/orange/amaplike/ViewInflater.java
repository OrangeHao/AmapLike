package com.orange.amaplike;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.orange.amaplike.overlay.AMapUtil;

/**
 * created by czh on 2018-01-12
 */

public class ViewInflater {

    private Context mContext;
    private LayoutInflater mInflater;

    public ViewInflater(Context context){
        mContext=context;
        mInflater=LayoutInflater.from(mContext);
    }

    private View getViewWithId(int id){
        return mInflater.inflate(id,null);
    }

    public View getDriveGeneralView(DriveRouteResult driveRouteResult){
        View view=getViewWithId(R.layout.layout_drive_general);
        TextView time=(TextView) view.findViewById(R.id.drive_general_time);
        TextView distance=(TextView) view.findViewById(R.id.drive_general_distance);

        DrivePath path=driveRouteResult.getPaths().get(0);
        String dur = AMapUtil.getFriendlyTime((int) path.getDuration());
        String dis = AMapUtil.getFriendlyLength((int) path.getDistance());
        time.setText(dur);
        distance.setText(dis);
        return view;
    }

    interface OnViewClick{
        void onViewClick(int viewId);
    }
}
