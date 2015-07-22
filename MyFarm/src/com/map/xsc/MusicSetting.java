package com.map.xsc;

import java.io.FileWriter;
import java.io.IOException;

import cn.m15.xys.R;
import android.net.Uri;
import android.os.Bundle;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.app.Activity;
import android.content.Context;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.*;
import android.view.View;
import android.widget.ArrayAdapter;

public class MusicSetting extends Activity {
	public Music data = new Music();
	Button save;
	Button start;
	Spinner spinner1;
	private ArrayAdapter<String> adapter1;
	public MediaPlayer mMediaPlayer = null;
	private boolean flag=false;
	public static int position1 = 0;
	//public static int numOfmusic =5;
	static final String[] music = { "���֮��","��ɫ����", "��������", "����",
			"��ˮ����", "�¹�" };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_musicsetting);
		// ��ʼ��
		mMediaPlayer = new MediaPlayer();
		data.setMusic(music[0],0);
		spinner1=(Spinner)findViewById(R.id.spinner1);
		save = (Button) findViewById(R.id.save);
		start = (Button) findViewById(R.id.startplay);

		// ����ѡ������ArrayAdapter��������
		adapter1 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, music);
		
		// ���������б�ķ��
		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// ��adapter ��ӵ�spinner��
		spinner1.setAdapter(adapter1);
		
		spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				data.setMusic(music[position],position);
				//playMusic("" + music[position]);
				position1 = position;
			}

			public void onNothingSelected(AdapterView<?> arg0) {

			}

		});
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(),
						"��ѡ������֣�" + music[position1], Toast.LENGTH_SHORT)
						.show();
				StartActivity.music=data;
			}
		});
		start.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(flag==false){
					flag=true;
					start.setText("ֹͣ����");
					playMusic(music[position1]);
				}
				else{
					stopMusic();
					flag=false;
					start.setText("��������");
				}
			}
		});

		// ����Ĭ��ֵ
		spinner1.setVisibility(View.VISIBLE);
		//spinner2.setVisibility(View.VISIBLE);
		//spinner3.setVisibility(View.VISIBLE);
	}

	private void playMusic(String path) {
		try {
			if (position1 == 0)
				mMediaPlayer = MediaPlayer.create(MusicSetting.this, R.raw.fallingstar);
			if (position1 == 1)
				mMediaPlayer = MediaPlayer.create(MusicSetting.this, R.raw.qguose);
			if (position1 == 2)
				mMediaPlayer = MediaPlayer.create(MusicSetting.this,
						R.raw.qhuanyin);
			if (position1 == 3)
				mMediaPlayer = MediaPlayer.create(MusicSetting.this,
						R.raw.qlanxi);
			if (position1 == 4)
				mMediaPlayer = MediaPlayer.create(MusicSetting.this,
						R.raw.qyishui);
			if (position1 == 5)
				mMediaPlayer = MediaPlayer.create(MusicSetting.this,
						R.raw.qyueguang);
			
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
			}
			if(mMediaPlayer.isPlaying()){
				mMediaPlayer.stop();
			}
			mMediaPlayer.prepare();
			mMediaPlayer.start();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("error");
		}
	}

	private void stopMusic() {
		try{
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
				mMediaPlayer.release();
			}
			//finish();
		}
		catch(Exception e){
			
		}
		
	}
}