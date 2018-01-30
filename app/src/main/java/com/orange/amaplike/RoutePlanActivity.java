package com.orange.amaplike;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.AMapGestureListener;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.Path;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.orange.amaplike.adapter.BusResultListAdapter;
import com.orange.amaplike.adapter.DrivePathAdapter;
import com.orange.amaplike.adapter.RideStepAdapter;
import com.orange.amaplike.adapter.WalkStepAdapter;
import com.orange.amaplike.behavior.NoAnchorBottomSheetBehavior;
import com.orange.amaplike.overlay.AMapServicesUtil;
import com.orange.amaplike.overlay.AMapUtil;
import com.orange.amaplike.overlay.BusRouteOverlay;
import com.orange.amaplike.overlay.DrivingRouteOverlay;
import com.orange.amaplike.overlay.RideRouteOverlay;
import com.orange.amaplike.overlay.WalkRouteOverlay;
import com.orange.amaplike.pickpoi.PoiItemEvent;
import com.orange.amaplike.pickpoi.PoiSearchActivity;
import com.orange.amaplike.pickpoi.SelectedMyPoiEvent;
import com.orange.amaplike.utils.ViewAnimUtils;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * created by czh on 2018/1/4
 * 高德地图路径规划
 */
public class RoutePlanActivity extends AppCompatActivity implements RouteSearch.OnRouteSearchListener ,BusResultListAdapter.BusListItemListner{

    public static final String CITY_CODE="CityCode";
    private final int HISTORY_SIZE=10;

    @BindView(R.id.topLayout)RelativeLayout mTopLayout;
    @BindView(R.id.route_plan_tab_layout) TabLayout mTabLayout;
    @BindView(R.id.route_plan_map) TextureMapView mMapView;
    @BindView(R.id.route_plan_loca_btn) ImageView mImageViewBtn;
    @BindView(R.id.coordinatorlayout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.route_plan_from_edit) TextView mFromText;
    @BindView(R.id.route_plan_to_edit) TextView mTargetText;
    @BindView(R.id.bus_result_recyclerView)RecyclerView mBusResultRview;


    @BindView(R.id.sheet_head_layout)LinearLayout mSheetHeadLayout;
    @BindView(R.id.route_plan_poi_title) TextView mPoiTitleText;
    @BindView(R.id.bottom_sheet) NestedScrollView mNesteScrollView;
    @BindView(R.id.route_plan_poi_desc) TextView mPoiDescText;
    @BindView(R.id.route_plan_poi_detail_layout) LinearLayout mPoiDetailLayout;
    @BindView(R.id.path_detail_recyclerView) RecyclerView mPathDetailRecView;
    @BindView(R.id.path_general_time) TextView mPathDurText;
    @BindView(R.id.path_general_distance) TextView mPathDisText;
    @BindView(R.id.path_detail_traffic_light_text) TextView mTLightsText;
    @BindView(R.id.navi_start_btn_1) TextView mNaviText;
    @BindView(R.id.navi_start_btn) Button mNaviBtn;

    private NoAnchorBottomSheetBehavior mBehavior;
    private BusResultListAdapter mBusResultAdapter;
    private DrivePathAdapter mDrivePathAdapter;
    private RideStepAdapter mRideStepAdapter;
    private WalkStepAdapter mWalkStepAdapter;


    private static final int MSG_MOVE_CAMERA = 0x01;
    private final int TYPE_DRIVE=100;
    private final int TYPE_BUS=101;
    private final int TYPE_WALK=102;
    private final int TYPE_RIDE=103;
    private int mSelectedType =TYPE_DRIVE;

    private int mTopLayoutHeight=200;
    private Context mContext;

    private float mDegree=0f;
    private SensorManager mSensorManager;

    private boolean FirstLocate =true;
    private AMap mAmap;
    private MyLocationStyle mLocationStyle;
    private Poi mEndPoi;
    private Poi mStartPoi;
    private DriveRouteResult mDriveRouteResult;
    private BusRouteResult mBusRouteResult;
    private WalkRouteResult mWalkRouteResult;
    private RideRouteResult mRideRouteResult;

    private Handler mHandlerr=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MOVE_CAMERA:
                    mAmap.moveCamera(CameraUpdateFactory.newLatLngZoom((LatLng) msg.obj, 15));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_plan);
        mContext=this;
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);


        initView();
        initTabLayout();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Handler handler=new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        initMap(savedInstanceState);
                        initSensor();
                        initSheet();
                    }
                });
            }
        }).start();
//        initMap(savedInstanceState);
    }



    private void initView(){
        /**   公交页面   **/
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mBusResultRview.setLayoutManager(linearLayoutManager);


        mTopLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mTopLayoutHeight=mTopLayout.getHeight();
                mTopLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void initSheet(){
        mBehavior = NoAnchorBottomSheetBehavior.from(mNesteScrollView);
        mBehavior.setState(NoAnchorBottomSheetBehavior.STATE_COLLAPSED);
        mBehavior.setBottomSheetCallback(new NoAnchorBottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (slideOffset>0.5){
                    mNaviText.setVisibility(View.GONE);
                }else {
                    mNaviText.setVisibility(View.VISIBLE);
                }
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mPathDetailRecView.setLayoutManager(linearLayoutManager);
    }

    private void initTabLayout(){
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.route_plan_drive));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.route_plan_bus));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.route_plan_walk));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.route_plan_ride));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (getString(R.string.route_plan_drive).equals(tab.getText())){
                    mSelectedType =TYPE_DRIVE;
                    if (mEndPoi==null){
                        return;
                    }
                    Location myLocation=mAmap.getMyLocation();
                    routeSearch(mStartPoi,mEndPoi,TYPE_DRIVE);
                }else if (getString(R.string.route_plan_bus).equals(tab.getText())){
                    mSelectedType =TYPE_BUS;
                    if (mEndPoi==null){
                        return;
                    }
                    routeSearch(mStartPoi,mEndPoi,TYPE_BUS);
                }else if (getString(R.string.route_plan_walk).equals(tab.getText())){
                    mSelectedType =TYPE_WALK;
                    if (mEndPoi==null){
                        return;
                    }
                    Location location=mAmap.getMyLocation();
                    routeSearch(mStartPoi,mEndPoi,TYPE_WALK);
                }else if (getString(R.string.route_plan_ride).equals(tab.getText())){
                    mSelectedType =TYPE_RIDE;
                    if (mEndPoi==null){
                        return;
                    }
                    Location location=mAmap.getMyLocation();
                    routeSearch(mStartPoi,mEndPoi,TYPE_RIDE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void initMap(Bundle savedInstanceState){
        long t1= System.currentTimeMillis();
        mMapView.onCreate(savedInstanceState);
        Log.d("czh",System.currentTimeMillis()-t1+"ms");
        mAmap=mMapView.getMap();

        /**   基本设置   **/
        mAmap.setTrafficEnabled(true);
        mAmap.showIndoorMap(true);
        mAmap.getUiSettings().setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);

        /**   定位模式   **/
        mLocationStyle=new MyLocationStyle();
        mLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        mLocationStyle.interval(2000);
        mAmap.setMyLocationStyle(mLocationStyle);
        mAmap.setMyLocationEnabled(true);
        mAmap.animateCamera(CameraUpdateFactory.zoomTo(15));

        /**   监听   **/
        mAmap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (FirstLocate){
                    FirstLocate =false;
                    LocaBtnOnclick();
                    mStartPoi=new Poi(getString(R.string.poi_search_my_location),
                            new LatLng(location.getLatitude(),location.getLongitude()),"");
                    updateEditUI();
                }
            }
        });
        mAmap.setOnPOIClickListener(new AMap.OnPOIClickListener() {
            @Override
            public void onPOIClick(Poi poi) {
                onPoiClick(poi);
            }
        });
        mAmap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
//                xLog.D("onCameraChange");
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
//                mBtnState=BTN_STATE_NOR;
//                LocateBtnUIChagen();
            }
        });
        mAmap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return false;
            }
        });
        mAmap.setAMapGestureListener(new AMapGestureListener() {
            @Override
            public void onDoubleTap(float v, float v1) {
            }
            @Override
            public void onSingleTap(float v, float v1) {
            }
            @Override
            public void onFling(float v, float v1) {
            }
            @Override
            public void onScroll(float v, float v1) {
            }
            @Override
            public void onLongPress(float v, float v1) {
            }
            @Override
            public void onDown(float v, float v1) {
            }
            @Override
            public void onUp(float v, float v1) {
                mBtnState=BTN_STATE_NOR;
                LocateBtnUIChagen();
            }
            @Override
            public void onMapStable() {
            }
        });
    }

    private SensorEventListener mSensorListner=new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float degree = event.values[0];
            mDegree=degree;
//            xLog.D("degree:"+mDegree);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private void initSensor(){
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListner,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_UI);
    }



    private final int BTN_STATE_NOR=100;
    private final int BTN_STATE_LOCATE=101;
    private final int BTN_STATE_DIRE=102;

    private int mBtnState=BTN_STATE_NOR;
    @OnClick(R.id.route_plan_loca_btn)
    public void LocaBtnOnclick(){
        if (mBtnState==BTN_STATE_NOR){
            mAmap.setMapType(AMap.MAP_TYPE_NORMAL);
            changeMapLevelAndAngle(16,0);
            mBtnState=BTN_STATE_LOCATE;
            LocateBtnUIChagen();
        }else if (mBtnState==BTN_STATE_LOCATE){
            mAmap.setMapType(AMap.MAP_TYPE_NORMAL);
            changeMapLevelAndAngle(18,40);
            mBtnState=BTN_STATE_DIRE;
            LocateBtnUIChagen();
        }else if (mBtnState==BTN_STATE_DIRE){
            mAmap.setMapType(AMap.MAP_TYPE_NORMAL);
            changeMapLevelAndAngle(16,0);
            mBtnState=BTN_STATE_NOR;
            LocateBtnUIChagen();
        }
    }

    private void changeMapLevelAndAngle(final int lv, final int angle){
        CameraUpdate mCameraUpdate= CameraUpdateFactory.newLatLngZoom(
                new LatLng(mAmap.getMyLocation().getLatitude(),mAmap.getMyLocation().getLongitude())
                ,lv);
        mAmap.animateCamera(mCameraUpdate, new AMap.CancelableCallback() {
            @Override
            public void onFinish() {
                mAmap.animateCamera(CameraUpdateFactory.changeTilt(angle), new AMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        if (lv>17){
                            CameraUpdate cameraUpdate= CameraUpdateFactory.changeBearing(mDegree);
                            mAmap.animateCamera(cameraUpdate);
                        }else{
                            CameraUpdate cameraUpdate= CameraUpdateFactory.changeBearing(0);
                            mAmap.animateCamera(cameraUpdate);
                        }

                    }
                    @Override
                    public void onCancel() {}
                });
            }
            @Override
            public void onCancel() {}
        });
    }


    /**
     * 选点返回处理
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void selectPoiEvent(PoiItemEvent event){
        PoiItem item=event.getItem();
        LatLonPoint point=item.getLatLonPoint();
        LatLng latLng=new LatLng(point.getLatitude(),point.getLongitude());
        if (event.getFrom()== PoiSearchActivity.FROM_START){
            mStartPoi=new Poi(item.getTitle(),latLng,item.getAdName());
        }else if (event.getFrom()==PoiSearchActivity.FROM_TARGET){
            mEndPoi=new Poi(item.getTitle(),latLng,item.getAdName());
        }
        updateEditUI();
//        goToPlaceAndMark(item);

        mPoiTitleText.setText(item.getTitle());
        mPoiDescText.setText(item.getAdName()+"    "+item.getSnippet());

        if (mStartPoi==null || mEndPoi==null){
            return;
        }
        routeSearch(mStartPoi,mEndPoi,mSelectedType);
    }



    /**
     * 点击我的位置处理
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void selectedMyPoiEvent(SelectedMyPoiEvent event){
        Location location=mAmap.getMyLocation();
        if (event.getFrom()==PoiSearchActivity.FROM_START){
            mStartPoi=new Poi(getString(R.string.poi_search_my_location),
                    new LatLng(location.getLatitude(),location.getLongitude()),"");
        }else if (event.getFrom()==PoiSearchActivity.FROM_TARGET){
            mEndPoi=new Poi(getString(R.string.poi_search_my_location),
                    new LatLng(location.getLatitude(),location.getLongitude()),"");
        }
        updateEditUI();
    }

    /**
     * 地图poi点击
     * @param poi
     */
    private void onPoiClick(Poi poi){
        goToPlace(poi.getCoordinate());
        mEndPoi=poi;
        mTargetText.setText(poi.getName());
        mPoiTitleText.setText(poi.getName());
        mPoiDescText.setText(poi.getPoiId());
        mAmap.clear();
        mAmap.addMarker(new MarkerOptions().position(poi.getCoordinate()).title(poi.getName()));
    }

    private void goToPlace(LatLng latLng){
        CameraUpdate cameraUpdate= CameraUpdateFactory.newLatLng(latLng);
        mAmap.animateCamera(cameraUpdate);
    }


    private LatLng getLatLngFromLocation(){
        Location location=mAmap.getMyLocation();
        return new LatLng(location.getLatitude(),location.getLongitude());
    }


    private void goToPlaceAndMark(PoiItem item){
        LatLonPoint point=item.getLatLonPoint();
        LatLng latLng=new LatLng(point.getLatitude(),point.getLongitude());
        CameraUpdate cameraUpdate= CameraUpdateFactory.newLatLngZoom(latLng ,16);
        mAmap.clear();
        mAmap.animateCamera(cameraUpdate);
        mAmap.addMarker(new MarkerOptions().position(latLng).title(item.getTitle()));
    }

    private void LocateBtnUIChagen(){
        if (mBtnState==BTN_STATE_NOR){
            mImageViewBtn.setImageResource(R.drawable.icon_c34);
        }else if (mBtnState==BTN_STATE_LOCATE){
            mImageViewBtn.setImageResource(R.drawable.icon_c34_b);
        }else if (mBtnState==BTN_STATE_DIRE){
            mImageViewBtn.setImageResource(R.drawable.icon_c34_a);
        }
    }

    @OnClick({R.id.route_plan_return_btn,R.id.route_plan_start_edit_layout,R.id.route_plan_to_edit_layout,
            R.id.route_plan_exchange_btn,R.id.route_plan_float_btn,R.id.navi_start_btn,R.id.navi_start_btn_1})
    public void onViewclik(View view){
        if (FirstLocate){
            return;
        }
        switch (view.getId()){
            case R.id.route_plan_return_btn:
                finish();
                break;
            case R.id.route_plan_start_edit_layout:
                Location location1=mAmap.getMyLocation();
                PoiSearchActivity.start(mContext,location1.getExtras(),location1.getLatitude(),location1.getLongitude(),PoiSearchActivity.FROM_START);
                break;
            case R.id.route_plan_to_edit_layout:
//                startActivity(new Intent(mContext, BusActivity.class));
                Location location2=mAmap.getMyLocation();
                PoiSearchActivity.start(mContext,location2.getExtras(),location2.getLatitude(),location2.getLongitude(),PoiSearchActivity.FROM_TARGET);
                break;
            case R.id.route_plan_exchange_btn:
                Poi temp=mStartPoi;
                mStartPoi=mEndPoi;
                mEndPoi=temp;
                updateEditUI();
                routeSearch(mStartPoi,mEndPoi, mSelectedType);
                break;
            case R.id.route_plan_float_btn:
                if (mEndPoi==null){
                    return;
                }
                routeSearch(mStartPoi,mEndPoi,TYPE_DRIVE);
                break;
            case R.id.navi_start_btn:
            case R.id.navi_start_btn_1:
                if (mEndPoi==null){
                    return;
                }
                new NaviDialog().showView(mContext,mStartPoi,mEndPoi,mSelectedType);
                break;
        }
    }


    private void updateEditUI(){
        if (mStartPoi==null){
            mFromText.setText("");
//            mFromText.setText(getString(R.string.poi_search_my_location));
        }else {
            mFromText.setText(mStartPoi.getName());
        }
        if (mEndPoi==null){
            mTargetText.setText("");
        }else {
            mTargetText.setText(mEndPoi.getName());
        }
    }

    private void updatePathGeneral(Path path){
        String dur = AMapUtil.getFriendlyTime((int) path.getDuration());
        String dis = AMapUtil.getFriendlyLength((int) path.getDistance());
        mPathDurText.setText(dur);
        mPathDisText.setText(dis);
    }


    private int getSheetHeadHeight(){
        mSheetHeadLayout.measure(0,0);
        Log.d("czh",mSheetHeadLayout.getMeasuredHeight()+"height");
        return mSheetHeadLayout.getMeasuredHeight();
    }

    private int getTopLayoutHeight(){
//        RelativeLayout.LayoutParams lp=(RelativeLayout.LayoutParams) mTopLayout.getLayoutParams();
        Log.d("czh",mTopLayoutHeight+"top height");
        return mTopLayoutHeight;
    }

    private void routeSearch(Poi startPoi, Poi targetPoi, int type){
        if(startPoi==null || targetPoi==null){
            return;
        }
        LatLng start=startPoi.getCoordinate();
        LatLng target=targetPoi.getCoordinate();

        RouteSearch routeSearch=new RouteSearch(this);
        routeSearch.setRouteSearchListener(this);
        RouteSearch.FromAndTo fromAndTo=new RouteSearch.FromAndTo(AMapServicesUtil.convertToLatLonPoint(start),AMapServicesUtil.convertToLatLonPoint(target));
        switch (type){
            case TYPE_DRIVE:
                RouteSearch.DriveRouteQuery dquery=new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DRIVING_MULTI_STRATEGY_FASTEST_SHORTEST,null,null,"");
                routeSearch.calculateDriveRouteAsyn(dquery);
                break;
            case TYPE_BUS:
                RouteSearch.BusRouteQuery bquery=new RouteSearch.BusRouteQuery(fromAndTo, RouteSearch.BUS_DEFAULT,
                        mAmap.getMyLocation().getExtras().getString(CITY_CODE),0);
                routeSearch.calculateBusRouteAsyn(bquery);
                break;
            case TYPE_WALK:
                RouteSearch.WalkRouteQuery wquery=new RouteSearch.WalkRouteQuery(fromAndTo, RouteSearch.WALK_DEFAULT );
                routeSearch.calculateWalkRouteAsyn(wquery);
                break;
            case TYPE_RIDE:
                RouteSearch.RideRouteQuery rquery=new RouteSearch.RideRouteQuery(fromAndTo, RouteSearch.RIDING_DEFAULT );
                routeSearch.calculateRideRouteAsyn(rquery);
                break;
            default:
                break;
        }
    }


    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int errorCode) {
        if (errorCode==1000){
            if (driveRouteResult != null && driveRouteResult.getPaths() != null){
                if (driveRouteResult.getPaths().size() > 0){
                    if (mBusResultRview.getVisibility()==View.VISIBLE){
                        ViewAnimUtils.popupoutWithInterpolator(mBusResultRview, new ViewAnimUtils.AnimEndListener() {
                            @Override
                            public void onAnimEnd() {
                                mBusResultRview.setVisibility(View.GONE);
                            }
                        });
                    }
                    mDriveRouteResult=driveRouteResult;
                    drawDriveRoutes(mDriveRouteResult,mDriveRouteResult.getPaths().get(0));
                    DrivePath path=mDriveRouteResult.getPaths().get(0);

                    mPoiDetailLayout.setVisibility(View.GONE);
                    mDrivePathAdapter=new DrivePathAdapter(mContext,path.getSteps());
                    mPathDetailRecView.setAdapter(mDrivePathAdapter);
                    updatePathGeneral(path);
                    mTLightsText.setVisibility(View.VISIBLE);
                    mTLightsText.setText(getString(R.string.route_plan_path_traffic_lights,path.getTotalTrafficlights()+""));
                    mBehavior.setPeekHeight(getSheetHeadHeight());
                    mNesteScrollView.setVisibility(View.VISIBLE);
                }else if (driveRouteResult != null && driveRouteResult.getPaths() == null) {
                    Toast.makeText(mContext,R.string.no_result,Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(mContext,R.string.no_result,Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(mContext,R.string.poi_search_error,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int errorCode) {
        if (errorCode==1000){
            if (busRouteResult != null && busRouteResult.getPaths() != null){
                if (busRouteResult.getPaths().size() > 0){
                    mNesteScrollView.setVisibility(View.GONE);
                    mBusRouteResult = busRouteResult;
                    mBusResultAdapter=new BusResultListAdapter(mContext,busRouteResult);
                    mBusResultRview.setAdapter(mBusResultAdapter);
                    ViewAnimUtils.popupinWithInterpolator(mBusResultRview, new ViewAnimUtils.AnimEndListener() {
                        @Override
                        public void onAnimEnd() {
                            mBusResultRview.setVisibility(View.VISIBLE);
                        }
                    });
//                    drawBusRoutes(mBusRouteResult,mBusRouteResult.getPaths().get(0));
                }else if (busRouteResult != null && busRouteResult.getPaths() == null) {
                    Toast.makeText(mContext,R.string.no_result,Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(mContext,R.string.no_result,Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(mContext,R.string.poi_search_error,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int errorCode) {
        if (errorCode==1000){
            if (walkRouteResult != null && walkRouteResult.getPaths() != null){
                if (walkRouteResult.getPaths().size() > 0){
                    if (mBusResultRview.getVisibility()==View.VISIBLE){
                        ViewAnimUtils.popupoutWithInterpolator(mBusResultRview, new ViewAnimUtils.AnimEndListener() {
                            @Override
                            public void onAnimEnd() {
                                mBusResultRview.setVisibility(View.GONE);

                            }
                        });
                    }
                    mWalkRouteResult = walkRouteResult;
                    drawWalkRoutes(mWalkRouteResult,mWalkRouteResult.getPaths().get(0));

                    WalkPath path=mWalkRouteResult.getPaths().get(0);
                    mPoiDetailLayout.setVisibility(View.GONE);
                    mWalkStepAdapter=new WalkStepAdapter(mContext,path.getSteps());
                    mPathDetailRecView.setAdapter(mWalkStepAdapter);
                    updatePathGeneral(path);
                    mTLightsText.setVisibility(View.GONE);
                    mBehavior.setPeekHeight(getSheetHeadHeight());
                    mNesteScrollView.setVisibility(View.VISIBLE);
                }else if (walkRouteResult != null && walkRouteResult.getPaths() == null) {
                    Toast.makeText(mContext,R.string.no_result,Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(mContext,R.string.no_result,Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(mContext,R.string.poi_search_error,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int errorCode) {
        if (errorCode==1000){
            if (rideRouteResult != null && rideRouteResult.getPaths() != null){
                if (rideRouteResult.getPaths().size() > 0){
                    if (mBusResultRview.getVisibility()==View.VISIBLE){
                        ViewAnimUtils.popupoutWithInterpolator(mBusResultRview, new ViewAnimUtils.AnimEndListener() {
                            @Override
                            public void onAnimEnd() {
                                mBusResultRview.setVisibility(View.GONE);

                            }
                        });
                    }
                    mRideRouteResult = rideRouteResult;
                    RidePath path=mRideRouteResult.getPaths().get(0);

                    mPoiDetailLayout.setVisibility(View.GONE);
                    mRideStepAdapter=new RideStepAdapter(mContext,path.getSteps());
                    updatePathGeneral(path);
                    mTLightsText.setVisibility(View.GONE);
                    mBehavior.setPeekHeight(getSheetHeadHeight());
                    mNesteScrollView.setVisibility(View.VISIBLE);
                    drawRideRoutes(mRideRouteResult,mRideRouteResult.getPaths().get(0));
                }else if (rideRouteResult != null && rideRouteResult.getPaths() == null) {
                    Toast.makeText(mContext,R.string.no_result,Toast.LENGTH_LONG).show();
                }
            }else {
                Toast.makeText(mContext,R.string.no_result,Toast.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(mContext,R.string.poi_search_error,Toast.LENGTH_LONG).show();
        }
    }



    private void drawDriveRoutes(DriveRouteResult driveRouteResult, DrivePath path){
        mAmap.clear();
        DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(
                mContext, mAmap, path,
                driveRouteResult.getStartPos(),driveRouteResult.getTargetPos(),
                null);
        drivingRouteOverlay.setNodeIconVisibility(true);//设置节点marker是否显示
        drivingRouteOverlay.setIsColorfulline(true);//是否用颜色展示交通拥堵情况，默认true
        drivingRouteOverlay.removeFromMap();
        drivingRouteOverlay.addToMap();
        drivingRouteOverlay.zoomWithPadding(getTopLayoutHeight(),getSheetHeadHeight());
    }

    private void drawBusRoutes(BusRouteResult busRouteResult, BusPath path){
        mAmap.clear();
        BusRouteOverlay busRouteOverlay = new BusRouteOverlay(
                mContext, mAmap,path,busRouteResult.getStartPos(),
                busRouteResult.getTargetPos());
        busRouteOverlay.setNodeIconVisibility(true);//设置节点marker是否显示
        busRouteOverlay.removeFromMap();
        busRouteOverlay.addToMap();
        busRouteOverlay.zoomWithPadding(getTopLayoutHeight(),getSheetHeadHeight());
    }

    private void drawWalkRoutes(WalkRouteResult walkRouteResult, WalkPath path){
        mAmap.clear();
        WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
                mContext, mAmap,path,walkRouteResult.getStartPos(),
                walkRouteResult.getTargetPos());
        walkRouteOverlay.setNodeIconVisibility(true);//设置节点marker是否显示
        walkRouteOverlay.removeFromMap();
        walkRouteOverlay.addToMap();
        walkRouteOverlay.zoomWithPadding(getTopLayoutHeight(),getSheetHeadHeight());
    }


    private void drawRideRoutes(RideRouteResult rideRouteResult, RidePath path){
        mAmap.clear();
        RideRouteOverlay rideRouteOverlay = new RideRouteOverlay(
                mContext, mAmap,path,rideRouteResult.getStartPos(),
                rideRouteResult.getTargetPos());
        rideRouteOverlay.setNodeIconVisibility(true);//设置节点marker是否显示
        rideRouteOverlay.removeFromMap();
        rideRouteOverlay.addToMap();
        rideRouteOverlay.zoomWithPadding(getTopLayoutHeight(),getSheetHeadHeight());
    }


    @Override
    public void onItemClick(BusPath busPath) {
        drawBusRoutes(mBusRouteResult,busPath);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mMapView.onDestroy();
        mSensorManager.unregisterListener(mSensorListner);
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

}
