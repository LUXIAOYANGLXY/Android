package com.map.xsc;

import cn.m15.xys.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class StartActivity extends Activity {
	Context mContext = null;
	private Class targetClass=null;
	public static Music music;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mContext = this;
		music = new Music();
		music.init();
		/** ������Ϸ���� - ����˵�ͼ���� **/
		Button botton0 = (Button) findViewById(R.id.button0);
		botton0.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				targetClass =SurfaceViewAcitvity.class;
				if(targetClass!=null){
					Intent intent = new Intent(StartActivity.this, targetClass);
					startActivity(intent);
				}
			}
		});
		Button botton1 = (Button) findViewById(R.id.button1);
		botton1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				targetClass =MusicSetting.class;
				if(targetClass!=null){
					Intent intent=new Intent(StartActivity.this,targetClass);
					startActivity(intent);
				}
			}
		});
		
		Button botton2 = (Button) findViewById(R.id.button2);
		botton2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				System.exit(0);
			}
		});
		

	}
	@Override
	protected void onResume() {
		super.onResume();
		// ��activityִ��onResumeʱִ��mMapView. onResume ()��ʵ�ֵ�ͼ�������ڹ���
	}

	@Override
	protected void onPause() {
		super.onPause();
		// ��activityִ��onPauseʱִ��mMapView. onPause ()��ʵ�ֵ�ͼ�������ڹ���
	}
	protected void onStop(){
		super.onStop();
	}
	
	
}
