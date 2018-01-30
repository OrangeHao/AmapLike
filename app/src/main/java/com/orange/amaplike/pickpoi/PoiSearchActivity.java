package com.orange.amaplike.pickpoi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.orange.amaplike.R;
import com.orange.amaplike.utils.RecyclerViewDivider;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PoiSearchActivity extends AppCompatActivity implements PoiSearch.OnPoiSearchListener,PoiListAdapter.ItemListner {

    public static final String FROM_TYPE="type_from";
    public static final int FROM_START=1;
    public static final int FROM_TARGET=2;
    public static final int FROM_HOME=3;
    public static final int FROM_COMPANY=4;

    @BindView(R.id.poiListRv)
    RecyclerView mRecyclerView;
    @BindView(R.id.poi_search_poi_edit)
    EditText mEditText;

    private Context mContext;

    private PoiSearch mPoiSearch;
    private PoiSearch.Query mQuery;

    private PoiListAdapter mAdapter;
    private List<PoiItem> mListData=new ArrayList<PoiItem>();

    private int mFrom=FROM_START;

    public static void start(Context context, Bundle bundle,double laf,double lon,int from) {
        Intent starter = new Intent(context, PoiSearchActivity.class);
        starter.putExtra("data",bundle);
        starter.putExtra("lon",lon);
        starter.putExtra("laf",laf);
        starter.putExtra(FROM_TYPE,from);
        context.startActivity(starter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_search);
        mContext=this;
        getSupportActionBar().hide();
        ButterKnife.bind(this);
        init();
        initView();
    }

    private void init(){
        if (getIntent().hasExtra(FROM_TYPE)){
            mFrom=getIntent().getIntExtra(FROM_TYPE,FROM_START);
        }
    }

    private void initView(){
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId== EditorInfo.IME_ACTION_SEARCH){
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(PoiSearchActivity.this.getCurrentFocus()
                                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    searchOnclick();
                    return true;
                }else if (event.getKeyCode()==KeyEvent.KEYCODE_ENTER && event.getAction()==KeyEvent.ACTION_DOWN){
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(PoiSearchActivity.this.getCurrentFocus()
                                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    searchOnclick();
                    return true;
                }
                return false;
            }
        });


        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager);

        mRecyclerView.addItemDecoration(new RecyclerViewDivider(this, LinearLayoutManager.HORIZONTAL));
    }

    @OnClick(R.id.poi_search_poi_btn)
    public void searchOnclick(){
        String keyword=mEditText.getText().toString().trim();
        if (TextUtils.isEmpty(keyword)){
            Toast.makeText(mContext,"keyword can not be empty",Toast.LENGTH_LONG).show();
            return;
        }
        mQuery=new PoiSearch.Query(keyword,"",getCityCode());
        mQuery.setPageSize(10);
        mQuery.setPageNum(0);

        mPoiSearch=new PoiSearch(mContext,mQuery);
        mPoiSearch.setOnPoiSearchListener(this);
        mPoiSearch.searchPOIAsyn();
    }

    @OnClick({R.id.poi_search_return_btn,R.id.choose_poi_map_layout,R.id.choose_poi_collect_layout,R.id.choose_poi_myloc_layout})
    public void onViewClick(View view){
        switch (view.getId()){
            case R.id.poi_search_return_btn:
                finish();
                break;
            case R.id.choose_poi_map_layout:
                startActivity(new Intent(mContext,SelectPoiFromMapActivity.class).putExtra("from",mFrom));
                break;
            case R.id.choose_poi_collect_layout:
                finish();
                break;
            case R.id.choose_poi_myloc_layout:
                finish();
                EventBus.getDefault().post(new SelectedMyPoiEvent(mFrom));
                break;
        }
    }

    private String getCityCode(){
        if (getIntent().hasExtra("data")){
            Bundle bundle=getIntent().getBundleExtra("data");
            return  bundle.getString("CityCode");
        }else {
            return "";
        }
    }

    private LatLng getLatLng(){
        if (getIntent().hasExtra("laf")){
            return  new LatLng(getIntent().getDoubleExtra("laf",0),getIntent().getDoubleExtra("lon",0));
        }else {
            return null;
        }
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int rCode) {
        if (rCode == 1000) {
            if (poiResult != null && poiResult.getQuery() != null) {// 搜索poi的结果
                if (poiResult.getQuery().equals(mQuery)) {// 是否是同一条
                    // 取得搜索到的poiitems有多少页
                    List<PoiItem> poiItems = poiResult.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    PoiItem item=poiItems.get(0);
                    mListData.clear();
                    mListData.addAll(poiItems);
                    if (mAdapter==null){
                        mAdapter=new PoiListAdapter(mContext,mListData,getLatLng());
                        mAdapter.setItenmListner(this);
                        mRecyclerView.setAdapter(mAdapter);
                    }else {
                        mAdapter.notifyDataSetChanged();
                    }
//                    List<SuggestionCity> suggestionCities = poiResult
//                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
                }
            } else {
                Toast.makeText(mContext,"no data",Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(mContext,"unknow error",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {
    }

    @Override
    public void ItemOnclik(PoiItem item) {
        EventBus.getDefault().post(new PoiItemEvent(item,mFrom));
        finish();
    }
}
