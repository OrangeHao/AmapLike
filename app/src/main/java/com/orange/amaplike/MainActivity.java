package com.orange.amaplike;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.goin_text})
    public void onViewclick(View view){
        if (view.getId()==R.id.goin_text){
            startActivity(new Intent(MainActivity.this,RoutePlanActivity.class));
        }
    }
}
