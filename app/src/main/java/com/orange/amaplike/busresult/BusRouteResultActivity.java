package com.orange.amaplike.busresult;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.BusStep;
import com.orange.amaplike.R;
import com.orange.amaplike.adapter.BusPathAdapter;
import com.orange.amaplike.adapter.BusSegmentListAdapter;
import com.orange.amaplike.behavior.AnchorBottomSheetBehavior;
import com.orange.amaplike.overlay.AMapUtil;
import com.orange.amaplike.overlay.BusRouteOverlay;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BusRouteResultActivity extends AppCompatActivity {

    @BindView(R.id.route_plan_map)
    MapView mMapView;
    @BindView(R.id.bus_paths_bottom_sheet)
    NestedScrollView mNestScrollView;
    @BindView(R.id.bus_paths_viewpage)
    ViewPager mViewPager;
    @BindView(R.id.bus_segment_list)
    RecyclerView mStepsList;
    private AnchorBottomSheetBehavior mBehavior;

    private AMap mAmap;
    private BusRouteOverlay mCurrentOverlay;
    private PathsAdapter mPathAdapter;
    private BusPathAdapter mBusPathAdapter;
    private BusSegmentListAdapter mBusSegmentListAdapter;
    private List<BusStep> mListData=new ArrayList<BusStep>();
    private int mHeight=150;
    private float mOffset=0.0f;
    private int mSheetHeight=120;

    private Context mContext;
    private BusRouteResult mRouteResult;


    public static void start(Context context, BusRouteResult result,int pos) {
        Intent starter = new Intent(context, BusRouteResultActivity.class);
        starter.putExtra("result",result);
        starter.putExtra("position",pos);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_route_result);
        ButterKnife.bind(this);
        mContext=this;

        init();
        initMap(savedInstanceState);
        initSheet();
    }

    private void init(){
        if (getIntent().hasExtra("result")){
            mRouteResult=getIntent().getParcelableExtra("result");
        }else {
            finish();
        }
        DisplayMetrics dm = new DisplayMetrics();
        dm = getResources().getDisplayMetrics();
        mHeight=dm.heightPixels;
        mSheetHeight=getResources().getDimensionPixelSize(R.dimen.sheet_peakHeight);
        Log.d("czh","height:"+mHeight);
        mBehavior=AnchorBottomSheetBehavior.from(mNestScrollView);
        mBehavior.addBottomSheetCallback(new AnchorBottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case AnchorBottomSheetBehavior.STATE_COLLAPSED:
                        Log.d("bottomsheet-", "STATE_COLLAPSED");
                        mCurrentOverlay.zoomToBusSpan(getSheetPadding());
                        break;
                    case AnchorBottomSheetBehavior.STATE_DRAGGING:
                        Log.d("bottomsheet-", "STATE_DRAGGING");
                        break;
                    case AnchorBottomSheetBehavior.STATE_EXPANDED:
                        Log.d("bottomsheet-", "STATE_EXPANDED");
                        break;
                    case AnchorBottomSheetBehavior.STATE_ANCHOR_POINT:
                        Log.d("bottomsheet-", "STATE_ANCHOR_POINT");
                        mCurrentOverlay.zoomToBusSpan(getSheetPadding());
                        break;
                    case AnchorBottomSheetBehavior.STATE_HIDDEN:
                        Log.d("bottomsheet-", "STATE_HIDDEN");
                        break;
                    default:
                        Log.d("bottomsheet-", "STATE_SETTLING");
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//                Log.d("bottomsheet-", "slideOffset:"+slideOffset);
                mOffset=slideOffset;
            }
        });
        mBehavior.setState(AnchorBottomSheetBehavior.STATE_COLLAPSED);

    }

    private void initSheet(){
        int position=getIntent().getIntExtra("position",0);
        List<View> list=new ArrayList<View>();
        for (BusPath path:mRouteResult.getPaths()){
            View view= LayoutInflater.from(mContext).inflate(R.layout.layout_page_item,null);
            TextView title=(TextView)view.findViewById(R.id.bus_path_title);
            TextView content=(TextView)view.findViewById(R.id.path_content);
            title.setText(AMapUtil.getBusPathTitle(path));
            content.setText(AMapUtil.getBusPathDes(path));
            list.add(view);
        }
        mPathAdapter=new PathsAdapter(list);
        mViewPager.setAdapter(mPathAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                drawBusRoutes(mRouteResult,mRouteResult.getPaths().get(position));
//                mListData.clear();
//                mListData.addAll(mRouteResult.getPaths().get(position).getSteps());
                mStepsList.setAdapter(new BusPathAdapter(mContext, mRouteResult.getPaths().get(position).getSteps()));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setCurrentItem(position);
        mListData=mRouteResult.getPaths().get(position).getSteps();


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mStepsList.setLayoutManager(linearLayoutManager);
        mStepsList.setAdapter(new BusPathAdapter(mContext,mListData));
    }

    private void initMap(Bundle savedInstanceState){
        mMapView.onCreate(savedInstanceState);
        mAmap=mMapView.getMap();

        /**   基本设置   **/
        mAmap.setTrafficEnabled(true);
        mAmap.showIndoorMap(true);
        mAmap.getUiSettings().setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);

        /**   定位模式   **/
        MyLocationStyle locationStyle=new MyLocationStyle();
        locationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        mAmap.setMyLocationStyle(locationStyle);
        mAmap.setMyLocationEnabled(true);

        mAmap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                drawBusRoutes(mRouteResult,mRouteResult.getPaths().get(0));
            }
        });

        /**   监听   **/
        mAmap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
            }
        });
    }


    private void drawBusRoutes(BusRouteResult busRouteResult,BusPath path){
        mAmap.clear();
        mCurrentOverlay = new BusRouteOverlay(
                mContext, mAmap,path,busRouteResult.getStartPos(),
                busRouteResult.getTargetPos());
        mCurrentOverlay.setNodeIconVisibility(false);//设置节点marker是否显示
        mCurrentOverlay.removeFromMap();
        mCurrentOverlay.addToMap();
        mCurrentOverlay.zoomToBusSpan(getSheetPadding());
    }

    private int getSheetPadding(){
        return (int)((mHeight-mSheetHeight)*mOffset+mSheetHeight);
    }

    @OnClick({R.id.return_img})
    public void onViewClick(View view){
        if (view.getId()==R.id.return_img){
            finish();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    class PathsAdapter extends PagerAdapter {
        private List<View> mViewList;

        public PathsAdapter(List<View> mViewList) {
            this.mViewList = mViewList;
        }

        @Override
        public int getCount() {
            return mViewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mViewList.get(position));
            return mViewList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViewList.get(position));
        }
    }


}
