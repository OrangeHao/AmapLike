package com.orange.amaplike.pickpoi;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.orange.amaplike.R;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * created by czh on 2018-01-08
 */

public class PoiListAdapter extends RecyclerView.Adapter{
    private Context mContext;
    private List<PoiItem> mPoiList;
    private Location mLocation;
    private LatLng mLatLng;



    public PoiListAdapter(Context context,List<PoiItem> list,LatLng latLng){
        mContext=context;
        mPoiList=list;
        mLatLng=latLng;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view= LayoutInflater.from(mContext).inflate(R.layout.item_poi_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        ViewHolder viewHolder=(ViewHolder)holder;
        final PoiItem model=mPoiList.get(position);
        viewHolder.itemName.setText(model.getTitle());
        viewHolder.itemDistance.setText(meterTokm(AMapUtils.calculateLineDistance(getLat(model),mLatLng))+"");
        viewHolder.itemAddress.setText(model.getSnippet());

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListner!=null){
                    mListner.ItemOnclik(model);
                }
            }
        });
    }

    public String meterTokm(double dis) {
        String back = "";
        if (dis >= 100) {
            dis = dis / 1000;
            String parten = "#.##";
            DecimalFormat decimal = new DecimalFormat(parten);
            back = decimal.format(dis) + "km";
        } else {
            back = String.format("%dm", dis);
        }
        return back;
    }

    private LatLng getLat(PoiItem item){
        LatLonPoint point=item.getLatLonPoint();
        return new LatLng(point.getLatitude(),point.getLongitude());
    }

    @Override
    public int getItemCount() {
        return mPoiList.size();
    }

    public void setItenmListner(ItemListner listner){
        mListner=listner;
    }

    private ItemListner mListner;
    public interface ItemListner{
        public void ItemOnclik(PoiItem item);
    }


    class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.search_result_name)
        TextView itemName;
        @BindView(R.id.search_result_distance)
        TextView itemDistance;
        @BindView(R.id.search_result_summary)
        TextView itemAddress;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
    }
}
