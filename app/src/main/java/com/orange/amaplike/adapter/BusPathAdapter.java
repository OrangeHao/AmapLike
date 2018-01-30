package com.orange.amaplike.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.services.busline.BusStationItem;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.RailwayStationItem;
import com.orange.amaplike.R;
import com.orange.amaplike.busresult.SchemeBusStep;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * created by czh on 2018-01-18
 */

public class BusPathAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<SchemeBusStep> mBusStepList = new ArrayList<SchemeBusStep>();

    public BusPathAdapter(Context context, List<BusStep> list){
        this.mContext = context;
        SchemeBusStep start = new SchemeBusStep(null);
        start.setStart(true);
        mBusStepList.add(start);
        for (BusStep busStep : list) {
            if (busStep.getWalk() != null && busStep.getWalk().getDistance() > 0) {
                SchemeBusStep walk = new SchemeBusStep(busStep);
                walk.setWalk(true);
                mBusStepList.add(walk);
            }
            if (busStep.getBusLine() != null) {
                SchemeBusStep bus = new SchemeBusStep(busStep);
                bus.setBus(true);
                mBusStepList.add(bus);
            }
            if (busStep.getRailway() != null) {
                SchemeBusStep railway = new SchemeBusStep(busStep);
                railway.setRailway(true);
                mBusStepList.add(railway);
            }

            if (busStep.getTaxi() != null) {
                SchemeBusStep taxi = new SchemeBusStep(busStep);
                taxi.setTaxi(true);
                mBusStepList.add(taxi);
            }
        }
        SchemeBusStep end = new SchemeBusStep(null);
        end.setEnd(true);
        mBusStepList.add(end);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view=View.inflate(mContext, R.layout.item_bus_path_detail, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder=(ViewHolder)viewHolder;

        final SchemeBusStep item = mBusStepList.get(position);
        if (position == 0) {
            holder.busDirIcon.setImageResource(R.drawable.dir_start);
            holder.busLineName.setText("出发");
            holder.busDirUp.setVisibility(View.INVISIBLE);
            holder.busDirDown.setVisibility(View.VISIBLE);
            holder.splitLine.setVisibility(View.GONE);
            holder.busStationNum.setVisibility(View.GONE);
            holder.busExpandImage.setVisibility(View.GONE);
        } else if (position == mBusStepList.size() - 1) {
            holder.busDirIcon.setImageResource(R.drawable.dir_end);
            holder.busLineName.setText("到达终点");
            holder.busDirUp.setVisibility(View.VISIBLE);
            holder.busDirDown.setVisibility(View.INVISIBLE);
            holder.busStationNum.setVisibility(View.INVISIBLE);
            holder.busExpandImage.setVisibility(View.INVISIBLE);
        } else {
            if (item.isWalk() && item.getWalk() != null && item.getWalk().getDistance() > 0) {
                holder.busDirIcon.setImageResource(R.drawable.dir13);
                holder.busDirUp.setVisibility(View.VISIBLE);
                holder.busDirDown.setVisibility(View.VISIBLE);
                holder.busLineName.setText("步行"
                        + (int) item.getWalk().getDistance() + "米");
                holder.busStationNum.setVisibility(View.GONE);
                holder.busExpandImage.setVisibility(View.GONE);

            }else if (item.isBus() && item.getBusLines().size() > 0) {
                holder.busDirIcon.setImageResource(R.drawable.dir14);
                holder.busDirUp.setVisibility(View.VISIBLE);
                holder.busDirDown.setVisibility(View.VISIBLE);
                holder.busLineName.setText(item.getBusLines().get(0).getBusLineName());
                holder.busStationNum.setVisibility(View.VISIBLE);
                holder.busStationNum
                        .setText((item.getBusLines().get(0).getPassStationNum() + 1) + "站");
                holder.busExpandImage.setVisibility(View.VISIBLE);
                ArrowClick arrowClick = new ArrowClick(holder, item);
                holder.parent.setTag(position);
                holder.parent.setOnClickListener(arrowClick);
            } else if (item.isRailway() && item.getRailway() != null) {
                holder.busDirIcon.setImageResource(R.drawable.dir16);
                holder.busDirUp.setVisibility(View.VISIBLE);
                holder.busDirDown.setVisibility(View.VISIBLE);
                holder.busLineName.setText(item.getRailway().getName());
                holder.busStationNum.setVisibility(View.VISIBLE);
                holder.busStationNum
                        .setText((item.getRailway().getViastops().size() + 1) + "站");
                holder.busExpandImage.setVisibility(View.VISIBLE);
                ArrowClick arrowClick = new ArrowClick(holder, item);
                holder.parent.setTag(position);
                holder.parent.setOnClickListener(arrowClick);
            } else if (item.isTaxi() && item.getTaxi() != null) {
                holder.busDirIcon.setImageResource(R.drawable.dir14);
                holder.busDirUp.setVisibility(View.VISIBLE);
                holder.busDirDown.setVisibility(View.VISIBLE);
                holder.busLineName.setText("打车到终点");
                holder.busStationNum.setVisibility(View.GONE);
                holder.busExpandImage.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public int getItemCount() {
        return mBusStepList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.bus_item)
        public RelativeLayout parent;
        @BindView(R.id.bus_line_name)
        TextView busLineName;
        @BindView(R.id.bus_dir_icon)
        ImageView busDirIcon;
        @BindView(R.id.bus_station_num)
        TextView busStationNum;
        @BindView(R.id.bus_expand_image)
        ImageView busExpandImage;
        @BindView(R.id.bus_dir_icon_up)
        ImageView busDirUp;
        @BindView(R.id.bus_dir_icon_down)
        ImageView busDirDown;
        @BindView(R.id.bus_seg_split_line)
        ImageView splitLine;
        @BindView(R.id.expand_content)
        LinearLayout expandContent;

        boolean arrowExpend = false;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
    }


    private class ArrowClick implements View.OnClickListener {
        private ViewHolder mHolder;
        private SchemeBusStep mItem;

        public ArrowClick(final ViewHolder holder, final SchemeBusStep item) {
            mHolder = holder;
            mItem = item;
        }

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            int position = Integer.parseInt(String.valueOf(v.getTag()));
            mItem = mBusStepList.get(position);
            if (mItem.isBus()) {
                if (mHolder.arrowExpend == false) {
                    mHolder.arrowExpend = true;
                    mHolder.busExpandImage
                            .setImageResource(R.drawable.up);
                    addBusStation(mItem.getBusLine().getDepartureBusStation());
                    for (BusStationItem station : mItem.getBusLine()
                            .getPassStations()) {
                        addBusStation(station);
                    }
                    addBusStation(mItem.getBusLine().getArrivalBusStation());

                } else {
                    mHolder.arrowExpend = false;
                    mHolder.busExpandImage
                            .setImageResource(R.drawable.down);
                    mHolder.expandContent.removeAllViews();
                }
            } else if (mItem.isRailway()) {
                if (mHolder.arrowExpend == false) {
                    mHolder.arrowExpend = true;
                    mHolder.busExpandImage
                            .setImageResource(R.drawable.up);
                    addRailwayStation(mItem.getRailway().getDeparturestop());
                    for (RailwayStationItem station : mItem.getRailway().getViastops()) {
                        addRailwayStation(station);
                    }
                    addRailwayStation(mItem.getRailway().getArrivalstop());

                } else {
                    mHolder.arrowExpend = false;
                    mHolder.busExpandImage
                            .setImageResource(R.drawable.down);
                    mHolder.expandContent.removeAllViews();
                }
            }


        }

        private void addBusStation(BusStationItem station) {
            LinearLayout ll = (LinearLayout) View.inflate(mContext,
                    R.layout.item_bus_path_detail_ex, null);
            TextView tv = (TextView) ll
                    .findViewById(R.id.bus_line_station_name);
            tv.setText(station.getBusStationName());
            mHolder.expandContent.addView(ll);
        }

        private void addRailwayStation(RailwayStationItem station) {
            LinearLayout ll = (LinearLayout) View.inflate(mContext,
                    R.layout.item_bus_segment_ex, null);
            TextView tv = (TextView) ll
                    .findViewById(R.id.bus_line_station_name);
            tv.setText(station.getName()+ " "+getRailwayTime(station.getTime()));
            mHolder.expandContent.addView(ll);
        }
    }
    public static String getRailwayTime(String time) {
        return time.substring(0, 2) + ":" + time.substring(2, time.length());
    }
}
