package com.orange.amaplike.pickpoi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.orange.amaplike.R;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SearchResultAdapter extends BaseAdapter {

    private List<PoiItem> data;
    private Context context;

    private int selectedPosition = 0;

    public SearchResultAdapter(Context context) {
        this.context = context;
        data = new ArrayList<>();
    }

    public void setData(List<PoiItem> data) {
        this.data = data;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_poi_pick_result, parent, false);

            viewHolder = new ViewHolder(convertView);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.textCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmBtnListner!=null){
                    confirmBtnListner.BtnConfrim(data.get(position));
                }
            }
        });

        viewHolder.bindView(position);

        return convertView;
    }

    private ConfirmBtnListner confirmBtnListner;
    public void setConfirmBtnListner(ConfirmBtnListner listner){
        confirmBtnListner=listner;
    }
    public interface ConfirmBtnListner{
        public void BtnConfrim(PoiItem item);
    }

    class ViewHolder {
        TextView textTitle;
        TextView textSubTitle;
        TextView textCheck;

        public ViewHolder(View view) {
            textTitle = (TextView) view.findViewById(R.id.text_title);
            textSubTitle = (TextView) view.findViewById(R.id.text_title_sub);
            textCheck = (TextView) view.findViewById(R.id.select_confirm_text);
        }

        public void bindView(int position) {
            if (position >= data.size())
                return;

            PoiItem poiItem = data.get(position);

            textTitle.setText(poiItem.getTitle());
            textSubTitle.setText(poiItem.getCityName() + poiItem.getAdName() + poiItem.getSnippet());

            textSubTitle.setVisibility((position == 0 && poiItem.getPoiId().equals("regeo")) ? View.GONE : View.VISIBLE);
        }
    }
}
