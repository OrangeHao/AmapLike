package com.orange.amaplike.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.orange.amaplike.R;
import com.orange.amaplike.busresult.BusRouteResultActivity;
import com.orange.amaplike.overlay.AMapUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BusResultListAdapter extends RecyclerView.Adapter {

	private Context mContext;
	private List<BusPath> mBusPathList;
	private BusRouteResult mBusRouteResult;

	public BusResultListAdapter(Context context, BusRouteResult busrouteresult) {
		mContext = context;
		mBusRouteResult = busrouteresult;
		mBusPathList = busrouteresult.getPaths();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view;
		view= LayoutInflater.from(mContext).inflate(R.layout.item_bus_result,parent,false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
		ViewHolder viewHolder=(ViewHolder)holder;

		final BusPath item = mBusPathList.get(position);
		viewHolder.title.setText(AMapUtil.getBusPathTitle(item));
		viewHolder.des.setText(AMapUtil.getBusPathDes(item));

		holder.itemView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BusRouteResultActivity.start(mContext,mBusRouteResult,position);
			}
		});
	}


	@Override
	public int getItemCount() {
		return mBusPathList.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder{

		@BindView(R.id.bus_path_title)
		TextView title;
		@BindView(R.id.bus_path_des)
		TextView des;

		public ViewHolder(View view) {
			super(view);
			ButterKnife.bind(this,view);
		}
	}

	private BusListItemListner mListner;
	public interface BusListItemListner {
		public void onItemClick(BusPath busPath);
	}

}
