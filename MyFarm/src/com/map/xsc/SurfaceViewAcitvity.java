package com.map.xsc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import cn.m15.xys.R;

import com.google.android.maps.MapView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.PorterDuff.Mode;
import android.hardware.*;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnLongClickListener; 

public class SurfaceViewAcitvity extends Activity {

	AnimView mAnimView = null;
	double TOUCH_TOLERANCE = 10.0;
	private static final DisplayMetrics SCREEN = new DisplayMetrics();
	private MediaPlayer mMediaPlayer = new MediaPlayer();
	private Thread threads = new Thread(new TRun());
	private Random rand = new Random(); 
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ȫ����ʾ����
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// ��ȡ��Ļ���
		getWindowManager().getDefaultDisplay().getMetrics(SCREEN);
		// Display display = getWindowManager().getDefaultDisplay();
		// System.out.println(SCREEN.widthPixels+" "+ SCREEN.heightPixels);
		// ��ʾ�Զ������ϷView
		mAnimView = new AnimView(this, SCREEN.widthPixels, SCREEN.heightPixels);
		setContentView(mAnimView);
		initMusic();
		threads.start();
	}

	@SuppressLint("NewApi")
	public class AnimView extends SurfaceView implements Callback, Runnable,
			SensorEventListener {
		/** �����ƶ����� **/
		public final static int ANIM_DOWN = 0;
		/** �����ƶ����� **/
		public final static int ANIM_LEFT = 1;
		/** �����ƶ����� **/
		public final static int ANIM_RIGHT = 2;
		/** �����ƶ����� **/
		public final static int ANIM_UP = 3;
		/** ������������ **/
		public final static int ANIM_COUNT = 4;

		Animation mHeroAnim[][] = new Animation[2][ANIM_COUNT];
		private int roleId = 0; //��Ů���ǵ�id
		Paint mPaint = null;

		/** ����������� **/
		private boolean mAllkeyDown = false;
		/** ������ **/
		private boolean mIskeyDown = false;
		/** ������ **/
		private boolean mIskeyLeft = false;
		/** ������ **/
		private boolean mIskeyRight = false;
		/** ������ **/
		private boolean mIskeyUp = false;

		// ��ǰ���ƶ���״̬ID
		int mAnimationState = 0;

		// tile��Ŀ��
		public final static int TILE_WIDTH = 32;
		public final static int TILE_HEIGHT = 32;

		// �����Ŀ�ߵ�����
		public final static int BUFFER_WIDTH_COUNT = 10;
		public final static int BUFFER_HEIGHT_COUNT = 15;

		// �����Ŀ��,��������ͼ�Ĵ�С
		public final static int SCENCE_WIDTH = 960;
		public final static int SCENCE_HEIGHT = 640;

		// ����ƫ���� δ�������߽��ͼ��ع���
		public final static int SCENCE_OFFSET = 3;
		public final static int SCENCE_OFFSET_WIDTH = 100;

		// ������Ŀ�ߵ�����
		public final static int TILE_WIDTH_COUNT = SCENCE_WIDTH / TILE_WIDTH;// 30
		public final static int TILE_HEIGHT_COUNT = SCENCE_HEIGHT / TILE_HEIGHT;// 20

		// ����Ԫ��Ϊ0��ʲô������
		public final static int TILE_NULL = 0;
		// ��һ����ϷView��ͼ����
		public int[][] mMapView = new int[TILE_HEIGHT_COUNT][TILE_WIDTH_COUNT]; // ��������Ū���ˡ�����

		/*
		 * �ڶ�����Ϸʵ��actor����,����������ͼ�� �������ͼ���������ĵ�ͼ���������زĻ�������
		 * �����ֵ���Ǹ��ݸ�tile����map�ز����ǵڼ������Ȱ��У��ٰ������еģ�����map�ز���ÿ��tile���Ӧ��idΪ�� 1 2 3 4 5
		 * 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24
		 */
		public int[][] mMapAcotor = new int[TILE_HEIGHT_COUNT][TILE_WIDTH_COUNT];
		// public int[][] mCollision = new
		// int[TILE_HEIGHT_COUNT][TILE_WIDTH_COUNT];
		/*
		 * ��������Ϸ��ײ����������ͻ�������⣬�տ�ʼͦ�õģ�������ȥ�󣬾Ͳ�����������������Ϊ����
		 * ԭ������ʱ������ײ�ˣ������ܼ����ߡ������������ڵ�ͼ�е�λ�û��ǵ���ԭ����ײǰ�ģ�ֵһ��û�䣬���˾��ǻ������ߡ�����
		 * ����Ϊ��Ȼ������������ײǰ�ڵ�ͼ�С���Ļ�е�λ�ã�����û�б��ݵ�ͼ��ײǰ��λ�ã�����
		 * ��Ϊ���˹�����Ļ���ȵ�����֮����Сʱ���Ͳ������ڶ��ˣ���ʵ�����ǵ�ͼ�����϶���
		 * ������Ȼ��ײ���˵�λ�ñ�Ϊ��ײǰ��λ�ã����ǵ�ͼû�лָ���ԭ���������������ƶ������Կ������ͺ�����������������ߣ�
		 * 
		 * ����֮����Ȼż������һ�������ǲ�������������Դ�ǽ�Ļ����ˣ�
		 * 
		 * Ϊ�˽���������������ԭ�ȴ������drawRimString���Լ�Log.v�����ֵ��Կ�ֵ����������
		 */
		public int[][] mCollision = {
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
						-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
						-1 },
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
						-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
						-1 },
				{ -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, -1, -1, -1, -1,
						-1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, -1 },
				{ -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, -1 },
				{ -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, -1 },
				{ -1, -1, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1 },
				{ -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, -1 },
				{ -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, -1 },
				{ -1, -1, 0, 0, 0, 0, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1 },
				{ -1, -1, 0, -1, -1, 0, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1 },
				{ -1, -1, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1 },
				{ -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, -1 },
				{ -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1 },
				{ -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1 },
				{ -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1 },
				{ -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, -1 },
				{ -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1 },
				{ -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
						0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1 },
				{ -1, -1, -1, -1, -1, -1, -1, 0, -1, -1, -1, -1, -1, -1, -1,
						-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
						-1 },
				{ -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
						-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
						-1 } };

		// ��Ϸ��ͼ��Դ
		Bitmap mBitmap = null;
		int totOfProds=6;
		//ũ��ƷͼƬ��Դ
		Bitmap product[]=new Bitmap[totOfProds];
		int numOfProds[]=new int[totOfProds];
		int landx1=8,landx2=27,landy1=5,landy2=15; //���ص�������Χ
		//Bitmap toolIcon[]=new Bitmap[2];
		//int toolXY[][]=new int[2][2]; //�洢x\y�����Ӧ������Ļ�ϵ�����
		// ��Դ�ļ�
		Resources mResources = null;

		// ��������tile�������
		int mWidthTileCount = 0; // map�زĺ���tile�������
		int mHeightTileCount = 0; // map�ز�����tile�������

		// ��������
		int mBitMapWidth = 0;
		int mBitMapHeight = 0;

		// Ӣ���ڵ�ͼ�е�������Ӣ�۽ŵ�����Ϊԭ��
		int mHeroPosX = 0;
		int mHeroPosY = 0;

		// ����Ӣ�۷�����ײ��ǰ�������
		int mBackHeroPosX = 0;
		int mBackHeroPosY = 0;

		// ����Ӣ�۷�����ײ��ǰ����Ļ��ʾ�����
		int mBackHeroScreenX = 0;
		int mBackHeroScreenY = 0;

		// Ӣ���ڵ�ͼ�л�������
		int mHeroImageX = 0;
		int mHeroImageY = 0;

		// Ӣ�������߷�Χ�л�������
		int mHeroScreenX = 0;
		int mHeroScreenY = 0;

		// Ӣ���ڵ�ͼ��λ�����е�����
		int mHeroIndexX = 0;
		int mHeroIndexY = 0;

		// ��Ļ��߲ųߴ�
		int mScreenWidth = 0;
		int mScreenHeight = 0;

		// �����������ݵ�index
		int mBufferIndexX = 0;
		int mBufferIndexY = 0;

		// ��ͼ������
		int mMapPosX = 0;
		int mMapPosY = 0;
		// ���ݵ�ͼ������
		int mBackmMapPosX = 0;
		int mBackmMapPosY = 0;

		/** ����ͼƬ��Դ��ʵ��Ӣ�۽ŵװ������ƫ�� **/
		public final static int OFF_HERO_X = 16;
		public final static int OFF_HERO_Y = 35;

		/** �������߲��� **/
		public final static int HERO_STEP = 4;

		/** ��ʵ��㷢����ײ **/
		private boolean isAcotrCollision = false;
		/** ��߽�㷢����ײ **/
		private boolean isBorderCollision = false;
		/** �Ƿ��ո�**/
		private boolean isHarvest = false;
		/** ��Ϸ���߳� **/
		private Thread mThread = null;
		/** �߳�ѭ����־ **/
		private boolean mIsRunning = false;
		// ��ʾһ��surface�ĳ���ӿڣ�ʹ����Կ���surface�Ĵ�С�͸�ʽ�� �Լ���surface�ϱ༭���أ�
		// �ͼ���surace�ĸı䡣����ӿ�ͨ��ͨ��SurfaceView��ʵ�֡�

		// �й�SurfaceHolder:
		// http://blog.csdn.net/pathuang68/article/details/7351317
		private SurfaceHolder mSurfaceHolder = null;
		private Canvas mCanvas = null;

		/** SensorManager������ **/
		private SensorManager mSensorMgr = null;
		Sensor mSensor = null;
		Sensor mSensor2 = null;
		/** ������ӦX�� Y�� Z�������ֵ **/
		private float mGX = 0;
		private float mGY = 0;
		private float mGZ = 0;
		private boolean isNight=false;
		// private float mGZ = 0;
		// private float lastmGX=0;
		/**
		 * ���췽��
		 * 
		 * @param context
		 */
		public AnimView(Context context, int screenWidth, int screenHeight) {
			super(context);
			mPaint = new Paint();
			mScreenWidth = screenWidth;
			mScreenHeight = screenHeight;
			initAnimation(context);
			initMap(context);
			initHero();

			/** ��ȡmSurfaceHolder **/
			mSurfaceHolder = getHolder();
			mSurfaceHolder.addCallback(this); // ΪSurfaceHolder���һ��SurfaceHolder.Callback�ص��ӿڡ�
			setFocusable(true);
			/** �õ�SensorManager���� **/
			mSensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
			mSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			mSensor2 = mSensorMgr.getDefaultSensor(Sensor.TYPE_LIGHT);
			// ע��listener�������������Ǽ��ľ�ȷ��
			// SENSOR_DELAY_FASTEST ������ ��Ϊ̫����û��Ҫʹ��
			// SENSOR_DELAY_GAME ��Ϸ������ʹ��
			// SENSOR_DELAY_NORMAL �����ٶ�
			// SENSOR_DELAY_UI �������ٶ�
			mSensorMgr.registerListener(this, mSensor,
					SensorManager.SENSOR_DELAY_GAME);
			mSensorMgr.registerListener(this, mSensor2,
					SensorManager.SENSOR_DELAY_GAME);
			/*
			for(int i=0;i<totOfProds;i++){
				for(int j=0;j<2;j++){
					toolXY[i][0]=i*4+1;
					toolXY[i][1]=mScreenHeight/TILE_HEIGHT-4;
				}
			}
			*/
		}

		private void initHero() {
			mHeroImageX = 100;
			mHeroImageY = 100;
			/** ����ͼƬ��ʾ���������Ӣ�۽ŵ׵����� **/
			/** X��+ͼƬ��ȵ�һ�� Y���ͼƬ�ĸ߶� **/
			mHeroPosX = mHeroImageX + OFF_HERO_X;
			mHeroPosY = mHeroImageY + OFF_HERO_Y;
			mHeroIndexX = mHeroPosX / TILE_WIDTH;
			mHeroIndexY = mHeroPosY / TILE_HEIGHT;
			mHeroScreenX = mHeroPosX;
			mHeroScreenY = mHeroPosY;
			// �����Ҹ�����ȥ��
			mBackHeroPosX = mHeroPosX;
			mBackHeroPosY = mHeroPosY;
			mBackHeroScreenX = mHeroScreenX;
			mBackHeroScreenY = mHeroScreenY;
		}

		private void initMap(Context context) {
			mBitmap = ReadBitMap(context, R.drawable.field);
			mBitMapWidth = mBitmap.getWidth();
			mBitMapHeight = mBitmap.getHeight();
			mWidthTileCount = mBitMapWidth / TILE_WIDTH; // 8=256/32
			mHeightTileCount = mBitMapHeight / TILE_HEIGHT; // 3=96/32
			// ��ʼ����Ϸ���������
			for (int i = 0; i < TILE_HEIGHT_COUNT; i++) {
				for (int j = 0; j < TILE_WIDTH_COUNT; j++) {
					mMapView[i][j] = 1;
					mMapAcotor[i][j]=-1;
				}
			}
			product[0]=ReadBitMap(context, R.drawable.prod0);
			product[1]=ReadBitMap(context, R.drawable.prod1);
			product[2]=ReadBitMap(context, R.drawable.prod2);
			product[3]=ReadBitMap(context, R.drawable.prod3);
			product[4]=ReadBitMap(context, R.drawable.prod4);
			product[5]=ReadBitMap(context, R.drawable.prod5);
			//toolIcon[0]=ReadBitMap(context, R.drawable.prod0);
		}

		private void initAnimation(Context context) {
			// ���������ѭ����������֮������Ҫ�Ѷ�����ID����ȥ
			//������
			mHeroAnim[0][ANIM_DOWN] = new Animation(context, new int[] {
					R.drawable.hero_down_a1, R.drawable.hero_down_b1,
					R.drawable.hero_down_c1, R.drawable.hero_down_d1 }, true);
			mHeroAnim[0][ANIM_LEFT] = new Animation(context, new int[] {
					R.drawable.hero_left_a1, R.drawable.hero_left_b1,
					R.drawable.hero_left_c1, R.drawable.hero_left_d1 }, true);
			mHeroAnim[0][ANIM_RIGHT] = new Animation(context, new int[] {
					R.drawable.hero_right_a1, R.drawable.hero_right_b1,
					R.drawable.hero_right_c1, R.drawable.hero_right_d1 }, true);
			mHeroAnim[0][ANIM_UP] = new Animation(context, new int[] {
					R.drawable.hero_up_a1, R.drawable.hero_up_b1,
					R.drawable.hero_up_c1, R.drawable.hero_up_d1 }, true);
			//Ů����
			mHeroAnim[1][ANIM_DOWN] = new Animation(context, new int[] {
					R.drawable.hero_down_a2, R.drawable.hero_down_b2,
					R.drawable.hero_down_c2, R.drawable.hero_down_d2 }, true);
			mHeroAnim[1][ANIM_LEFT] = new Animation(context, new int[] {
					R.drawable.hero_left_a2, R.drawable.hero_left_b2,
					R.drawable.hero_left_c2, R.drawable.hero_left_d2 }, true);
			mHeroAnim[1][ANIM_RIGHT] = new Animation(context, new int[] {
					R.drawable.hero_right_a2, R.drawable.hero_right_b2,
					R.drawable.hero_right_c2, R.drawable.hero_right_d2 }, true);
			mHeroAnim[1][ANIM_UP] = new Animation(context, new int[] {
					R.drawable.hero_up_a2, R.drawable.hero_up_b2,
					R.drawable.hero_up_c2, R.drawable.hero_up_d2 }, true);
		}

		protected void Draw() {
			if (isShrink) {
				// �������㶨������(http://blog.csdn.net/yanzi1225627/article/details/8236309)
				// ���ǲ���ľͱ�ɺ�ɫ�ˡ�������
				Paint p = new Paint();
				// p.setColor(Color.WHITE); //û���ö����
				p.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
				mCanvas.drawPaint(p);
				p.setXfermode(new PorterDuffXfermode(Mode.SRC));

			}
			if(!isNight)
				mCanvas.drawColor(Color.WHITE); // ���죬�����������û�������Ϊ��ɫ
			else
				mCanvas.drawColor(Color.GRAY); // ҹ�����߰������û�������Ϊ��ɫ
			// ���Ż���(��ͼƬ���ĵ�������ţ�XY�����ű�����ͬ)��Ӧ������Ϊ���Ľ�������
			mCanvas.scale(rate, rate, mHeroScreenX, mHeroScreenY);
			/** ���Ƶ�ͼ **/
			DrawMap(mCanvas, mPaint, mBitmap);
			//DrawTools(mCanvas,mPaint,toolIcon[0],toolXY[0][0],toolXY[0][1]);
			/** ���ƶ��� **/
			RenderAnimation(mCanvas);
			/** ���¶��� **/
			UpdateHero();
			// String
			// str=mHeroIndexX+","+mHeroIndexY+" "+mCollision[mHeroIndexY][mHeroIndexX];
			// drawRimString(mCanvas, str, Color.BLACK, 20,mScreenHeight >> 1);
			/*
			if (isBorderCollision) {
				DrawCollision(mCanvas, "��ײ");
			}

			if (isAcotrCollision) {
				DrawCollision(mCanvas, "��ײ"); 
			}
			if(isHarvest){
				DrawCollision(mCanvas, "�ո�");
				isHarvest=false;
			}
			*/
		}

		private void DrawCollision(Canvas canvas, String str) {
			// String
			// str2="\n Map width:"+mBitMapWidth+" "+"height:"+mBitMapHeight;
			// String
			// str3="\n Screen width:"+mScreenWidth+" "+"height:"+mScreenHeight;
			if(!isNight)
			drawRimString(canvas, str, Color.WHITE, mHeroImageX,
					mHeroImageY);
			else
				drawRimString(canvas, str, Color.BLACK, mHeroImageX,
						mHeroImageY);
		}
		
		private void UpdateHero() {
			// һ��ע�;����ͼ��Ϊ480����Ϊ800. �ֻ���Ļ��288*480(�����ܾ���Ӧ����320*480)
			if (mAllkeyDown) {
				/** ���ݰ���������ʾ���� **/
				/** ����ײ������Ѱ�ҿ��Լ��Ƿ����ͼ����㷢����ײ **/
				// ����������
				int h = mScreenHeight / 3;
				int w = mScreenWidth / 3;
				if (mIskeyDown) {
					mAnimationState = ANIM_DOWN;
					mHeroPosY += HERO_STEP; // Ӣ���ڵ�ͼ�е�����
					// >=320/10,(480-160)/32=20
					if (mHeroScreenY >= h * 2) {
						/*
						 * mHeroIndexX = mHeroPosX / TILE_WIDTH; 
						 * mHeroIndexY =mHeroPosY / TILE_HEIGHT;
						 * �Դ�����һЩ���ͣ���Ȼ����ֵ����Դ��ԭ�������еġ�
						 * ��ͼ�����ƶ��и���Χ����ͼ�Ͻ缴��Ļ�Ͻ�~��ͼ�½缴��Ļ�½� ��ͼ���϶�Ϊ��Ļ���϶�ʱ��320/32=10
						 * ��ͼ���¶�Ϊ��Ļ���¶�ʱ��(800-160)/32=20
						 * ����<=20�����ͼ���������һС�����ظ����϶����ߵ��������ֶ���� �ĳ�17�󣬾�û���ˡ�
						 * �������е��ֻ����ظ߶ȱȽϴ󣨴���800�������Ի�����������ͼ�ظ��ƶ������
						 */
						if (mHeroIndexY >= h * 2 / 32
								&& mHeroIndexY <= (SCENCE_HEIGHT - h) / 32) {
							// mMapPosY:��ͼ�����꣬�����ﲻ������ͼ�����ƶ���Ҳ���ǵ�ͼ��Y�����С
							mMapPosY -= HERO_STEP;
						} else {
							mHeroScreenY += HERO_STEP;
						}
					} else {
						// �˾�����Ļ�������ƶ�
						mHeroScreenY += HERO_STEP;
					}
				}
				// ����������
				else if (mIskeyLeft) {
					mAnimationState = ANIM_LEFT;
					mHeroPosX -= HERO_STEP;
					if (mHeroScreenX <= w) {
						/*
						 * >=96/32=3������ͼ�����ǡ��Ϊ��Ļ�����
						 * ��ͼ���Ҷ�Ϊ��Ļ���Ҷ�ʱ��(480-192)/32=9��ԭ�Ȳ�֪Ϊ����7
						 */
						if (mHeroIndexX >= w / 32
								&& mHeroIndexX <= (SCENCE_WIDTH - w * 2) / 32) {
							// �˲�������ͼ�����ƶ�
							mMapPosX += HERO_STEP;
						} else {
							// ������Ļ�������ƶ���Ӧ���ǵ�ͼ�����ƶ�������
							mHeroScreenX -= HERO_STEP;
						}
					} else {
						// ������Ļ�������ƶ�
						mHeroScreenX -= HERO_STEP;
					}
				}
				// ���������ƶ�
				else if (mIskeyRight) {
					mAnimationState = ANIM_RIGHT;
					mHeroPosX += HERO_STEP;
					if (mHeroScreenX >= w * 2) {
						/*
						 * 192/32=6��Ӧ�þ��ǵ�ͼ�����ǡ��Ϊ��Ļ����˵�ʱ��
						 * ��ͼ���Ҷ�ǡ��Ϊ��Ļ���Ҷ�ʱ��Ϊ:(480-96)/32=12
						 * �������ĳ�12��11�������������ƶ�������ͼ�������ƶ�ʱ����
						 * ���һС�ξ��루2��1�����ӣ��������Ծǰ����������һ���Ӿ͵������Ҷ�
						 */
						if (mHeroIndexX >= w * 2 / 32
								&& mHeroIndexX <= (SCENCE_WIDTH - w) / 32) {
							mMapPosX -= HERO_STEP;
						} else {
							mHeroScreenX += HERO_STEP;
						}
					} else {
						mHeroScreenX += HERO_STEP;
					}
				}
				// ���������ƶ�
				else if (mIskeyUp) {
					mAnimationState = ANIM_UP;
					mHeroPosY -= HERO_STEP;
					/*
					 * ��ͼ���϶�Ϊ��Ļ���϶�ʱ��160/32=5 ��ͼ���¶�Ϊ��Ļ���¶�ʱ��(800-320)/32=15
					 */
					if (mHeroScreenY <= h) {
						if (mHeroIndexY >= h / 32
								&& mHeroIndexY <= (SCENCE_HEIGHT - h * 2) / 32) {
							mMapPosY += HERO_STEP;
						} else {
							mHeroScreenY -= HERO_STEP;
						}
					} else {
						mHeroScreenY -= HERO_STEP;
					}
				}
//Log.v("xy","mapX:"+mMapPosX+" mapY:"+mMapPosY);
				/** ���Ӣ���ƶ����ڵ�ͼ��λ�����е����� **/
				mHeroIndexX = mHeroPosX / TILE_WIDTH;
				mHeroIndexY = mHeroPosY / TILE_HEIGHT;

				// /** ��������Ƿ���� **/
				isBorderCollision = false;
				if (mHeroPosX <= 0) {
					mHeroPosX = 0;
					mHeroScreenX = 0;
					isBorderCollision = true;
				} else if (mHeroPosX >= TILE_WIDTH_COUNT * TILE_WIDTH) {
					mHeroPosX = TILE_WIDTH_COUNT * TILE_WIDTH; // 15*32
					mHeroScreenX = mScreenWidth;
					isBorderCollision = true;
				}
				if (mHeroPosY <= 0) {
					mHeroPosY = 0;
					mHeroScreenY = 0;
					isBorderCollision = true;
				} else if (mHeroPosY >= TILE_HEIGHT_COUNT * TILE_HEIGHT) {
					mHeroPosY = TILE_HEIGHT_COUNT * TILE_HEIGHT;
					mHeroScreenY = mScreenHeight;
					isBorderCollision = true;
				}

				// ��ֹ��ͼԽ��
				if (mMapPosX >= 0) {
					mMapPosX = 0;
					// ����Ϊʲô����320��������Ļ�Ŀ�����Ӧ����96*3=288
				} else if (mMapPosX <= -(SCENCE_WIDTH - mScreenWidth)) {
					mMapPosX = -(SCENCE_WIDTH - mScreenWidth);
				}
				if (mMapPosY >= 0) {
					mMapPosY = 0;
				} else if (mMapPosY <= -(SCENCE_HEIGHT - mScreenHeight)) {
					mMapPosY = -(SCENCE_HEIGHT - mScreenHeight);
				}

				/** Խ���� **/
				int width = mCollision[0].length - 1;
				int height = mCollision.length - 1;

				if (mHeroIndexX <= 0) {
					mHeroIndexX = 0;
				} else if (mHeroIndexX >= width) {
					mHeroIndexX = width;
				}
				if (mHeroIndexY <= 0) {
					mHeroIndexY = 0;
				} else if (mHeroIndexY >= height) {
					mHeroIndexY = height;
				}
				// ��ײ���
				if (mCollision[mHeroIndexY][mHeroIndexX] == -1) {
					// drawRimString(mCanvas, "-1", Color.BLACK,
					// mScreenWidth>>1,(mScreenHeight >> 1)+50);
					// String str1=mHeroPosX+" "+mHeroPosY;
					// drawRimString(mCanvas, str1, Color.BLACK,
					// mScreenWidth>>1,(mScreenHeight >> 1)+150);
					// String str2=mBackHeroPosX+" "+mBackHeroPosY;
					// drawRimString(mCanvas, str2, Color.BLACK,
					// mScreenWidth>>1,(mScreenHeight >> 1)+200);
					// Log.v("hero", "mHeroPosY:" +
					// mHeroPosY+" "+"mBackHeroPosY:" +
					// mBackHeroPosY+" mHeroScreenY:"+mHeroScreenY);
					mHeroPosX = mBackHeroPosX;
					mHeroPosY = mBackHeroPosY;
					mHeroScreenY = mBackHeroScreenY;
					mHeroScreenX = mBackHeroScreenX;
					//�����ӵ�
					mMapPosX = mBackmMapPosX;
					mMapPosY = mBackmMapPosY;
					isAcotrCollision = true;
				} else {

					mBackHeroPosX = mHeroPosX;
					mBackHeroPosY = mHeroPosY;
					mBackHeroScreenX = mHeroScreenX;
					mBackHeroScreenY = mHeroScreenY;
					//�����ӵ�
					mBackmMapPosX = mMapPosX;
					mBackmMapPosY = mMapPosY;
					isAcotrCollision = false;
					// Log.v("hero", "mHeroPosY:" +
					// mHeroPosY+" "+"mBackHeroPosY:" +
					// mBackHeroPosY+" mHeroScreenY:"+mHeroScreenY);
				}
				//Log.v("hero", "mHeroIndexX " + mHeroIndexX + " "
						//+ "mHeroIndexY " + mHeroIndexY);
				/*
				 * Ӣ���ڵ�ͼ�л������ꣿ��������ʵ����Ļ�аɣ����� int mHeroImageX = 0; int mHeroImageY
				 * = 0; Ӣ�������߷�Χ�л������� int mHeroScreenX = 0; int mHeroScreenY = 0;
				 */
				mHeroImageX = mHeroScreenX - OFF_HERO_X;
				mHeroImageY = mHeroScreenY - OFF_HERO_Y;
			}
		}

		private void RenderAnimation(Canvas canvas) {
			if (mAllkeyDown) {
				/** ����������� **/
				/** �������Ƕ��� **/
				mHeroAnim[roleId][mAnimationState].DrawAnimation(canvas, mPaint,
						mHeroImageX, mHeroImageY);
			} else {
				/** ����̧�������ֹͣ���� **/
				mHeroAnim[roleId][mAnimationState].DrawFrame(canvas, mPaint,
						mHeroImageX, mHeroImageY, 0);
			}
		}

		/**
		 * ���ð���״̬trueΪ���� falseΪ̧��
		 * 
		 * @param keyCode
		 * @param state
		 */
		public void setKeyState(int keyCode, boolean state) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_DOWN:
				mIskeyDown = state;
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				mIskeyUp = state;
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				mIskeyLeft = state;
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				mIskeyRight = state;
				break;
			}
			mAllkeyDown = state;
		}

		/**
		 * �������а���״̬ false
		 * 
		 * @param keyCode
		 * @param state
		 */
		public void setKeyStateFalse() {
			mIskeyDown = false;
			mIskeyUp = false;
			mIskeyLeft = false;
			mIskeyRight = false;
			mAllkeyDown = false;
		}

		/**
		 * �����mbitmap��res�е�map�ز�
		 * 
		 * @param canvas
		 * @param paint
		 * @param bitmap
		 */
		private void DrawMap(Canvas canvas, Paint paint, Bitmap bitmap) {
			/*
			 * Ϊʲô����������������һ������󣬵�ͼ�����ˣ��������x���껹�����ӣ�
			 * ԭ����UpdateHero���������ͼ�Ƿ�Խ�磬֮ǰ�õ�����ֵ����������û�Ĺ�����������
			 */
			int i, j;
			for (i = 0; i < TILE_HEIGHT_COUNT; i++) {
				for (j = 0; j < TILE_WIDTH_COUNT; j++) {
					DrawMapTile(mMapAcotor[i][j], canvas, paint, bitmap, mMapPosX
							+ (j * TILE_WIDTH), mMapPosY + (i * TILE_HEIGHT),
							i, j,1);
				}
			}
		}
		
		/**
		 * ����ID����һ��tile��
		 * 
		 * @param id
		 * @param canvas
		 * @param paint
		 * @param bitmap
		 */
		private void DrawMapTile(int id, Canvas canvas, Paint paint,
				Bitmap bitmap, int x, int y, int row, int col,int cnt) {
			// ���������е�ID����ڵ�ͼ��Դ�е�XY ����
			// ��Ϊ�༭��Ĭ��0 ���Ե�һ��tile��ID����1����0�� �������� id--
			/*
			 * map�زĺ���tile������� mWidthTileCount map�ز�����tile������� mHeightTileCount
			 */
			// id--;
			// int rows = id / mWidthTileCount; //������map�ز����ǵڼ���
			// int bitmapX = (id - (rows * mWidthTileCount)) * TILE_WIDTH;
			// int bitmapY = rows * TILE_HEIGHT;
			int bitmapX = col * TILE_WIDTH;
			int bitmapY = row * TILE_HEIGHT;
			if(id>=0 && col>=landx1 && col<=landx2 && row>=landy1 && row<=landy2)
				DrawClipImage(canvas, paint, product[id], x, y, 0, 0,
						TILE_WIDTH*cnt, TILE_HEIGHT*cnt);
			else
				DrawClipImage(canvas, paint, bitmap, x, y, bitmapX, bitmapY,
					TILE_WIDTH*cnt, TILE_HEIGHT*cnt);
		}

		/**
		 * ����ͼƬ�е�һ����ͼƬ
		 * 
		 * @param canvas
		 * @param paint
		 * @param bitmap
		 * @param x
		 *            ����Ļ��Ҫ����tile������Ͻ�x�������ǻ�������ͼ�����Ը�x��y���ܻᳬ�������ֻ���Ļ
		 * @param y
		 *            ����Ļ��Ҫ����tile������Ͻ�y
		 * @param src_x
		 *            ��map�ز���Ҫ������Ļ�ϵ�tile������Ͻ�x����
		 * @param src_y
		 *            ��map�ز���Ҫ������Ļ�ϵ�tile������Ͻ�y����
		 * @param src_width
		 * @param src_Height
		 */
		private void DrawClipImage(Canvas canvas, Paint paint, Bitmap bitmap,
				int x, int y, int src_x, int src_y, int src_xp, int src_yp) {
			canvas.save();
			// ͨ��ָ�������޸ĵ�ǰ����
			canvas.clipRect(x, y, x + src_xp, y + src_yp);
			// �ø�����paint����ָ��λͼ��top/left��ֵ��Ϊλͼ�����ģ�λͼ����ʹ�õ�ǰ�ľ�����Ρ� ???
			canvas.drawBitmap(bitmap, x - src_x, y - src_y, paint);
			canvas.restore();
		}

		/**
		 * �����и�ͼƬ ����һ�����ɱ��λͼ����λͼ����Դͼָ�����Ӽ��� x ��λͼ��һ��������Դλͼ��X���� y ��λͼ��һ��������Դλͼ��y����
		 * w ��λͼÿһ�е����ظ��� h ��λͼ������
		 * 
		 * @param bitmap
		 * @param x
		 * @param y
		 * @param w
		 * @param h
		 * @return
		 */
		public Bitmap BitmapClipBitmap(Bitmap bitmap, int x, int y, int w, int h) {
			return Bitmap.createBitmap(bitmap, x, y, w, h);
		}

		/**
		 * ��ȡ������Դ��ͼƬ
		 * 
		 * @param context
		 * @param resId
		 * @return
		 */
		public Bitmap ReadBitMap(Context context, int resId) {
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inPreferredConfig = Bitmap.Config.RGB_565;
			opt.inPurgeable = true;
			opt.inInputShareable = true;
			// ��ȡ��ԴͼƬ
			InputStream is = context.getResources().openRawResource(resId);
			return BitmapFactory.decodeStream(is, null, opt);
		}

		/**
		 * ���ƻ�����Ӱ������
		 * 
		 * @param canvas
		 * @param str
		 * @param color
		 * @param x
		 * @param y
		 */
		public final void drawRimString(Canvas canvas, String str, int color,
				int x, int y) {
			int backColor = mPaint.getColor();
			mPaint.setColor(~color);
			canvas.drawText(str, x + 1, y, mPaint);
			canvas.drawText(str, x, y + 1, mPaint);
			canvas.drawText(str, x - 1, y, mPaint);
			canvas.drawText(str, x, y - 1, mPaint);
			mPaint.setColor(color);
			canvas.drawText(str, x, y, mPaint);
			mPaint.setColor(backColor);
		}

		@Override
		public void run() {
			while (mIsRunning) {
				// ����������̰߳�ȫ��
				synchronized (mSurfaceHolder) {
					// ��ȡһ��Canvas���󣬲�����֮�����õ���Canvas������ʵ����Surface��һ����Ա��
					mCanvas = mSurfaceHolder.lockCanvas();
					Draw();
					/** ���ƽ����������ʾ����Ļ�� **/
					// ���޸�Surface�е�������ɺ��ͷ�ͬ���������ύ�ı䣬Ȼ���µ����ݽ���չʾ��ͬʱSurface��������ݻᱻ��ʧ��
					mSurfaceHolder.unlockCanvasAndPost(mCanvas);
				}
			}
		}

		/**
		 * SurfaceHolder.Callback�����������ӿڷ���
		 * ��surface�����κνṹ�Եı仯ʱ����ʽ���ߴ�С�����÷����ͻᱻ�������á�
		 */
		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
				int arg3) {
			// surfaceView�Ĵ�С�����ı��ʱ��

		}

		/**
		 * ��surface���󴴽��󣬸÷����ͻᱻ�������á�
		 */
		@Override
		public void surfaceCreated(SurfaceHolder arg0) {
			/** ������Ϸ���߳� **/
			mIsRunning = true;
			mThread = new Thread(this);
			mThread.start();
		}

		/**
		 * ��surface�����ڽ�Ҫ����ǰ���÷����ᱻ�������á�
		 */
		@Override
		public void surfaceDestroyed(SurfaceHolder arg0) {
			// surfaceView���ٵ�ʱ��
			mIsRunning = false;
		}

		/**
		 * ������Ӧ��
		 */
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) { 
				
				mGX = event.values[0];
				mGY = event.values[1];
				mGZ = event.values[2];
//String str = "X:"+mGX+" "+"Y:"+mGY+" "+"Z:"+mGZ;
//Log.v("xyz:",str);
				// mGZ = event.values[SensorManager.DATA_Z];
				setKeyStateFalse();
				if (Math.abs(mGX) >= Math.abs(mGY) && Math.abs(mGX) >= 1.0) {
					if (mGX >= 0) { 
						// ������
						setKeyState(KeyEvent.KEYCODE_DPAD_LEFT, true);
					} else {
						// ������
						setKeyState(KeyEvent.KEYCODE_DPAD_RIGHT, true);
					}
				} else if (Math.abs(mGX) < Math.abs(mGY) && Math.abs(mGY) >= 1.0) {
					// ����֮�������ó�һ������6.0��һ��С��3.0������Ϊ�����ֻ���ʱ���ֻ������Ͼ�����б�ģ�mGY�϶�����0.
					if (1.0 <mGY && mGY< 4.0) // ������
						setKeyState(KeyEvent.KEYCODE_DPAD_UP, true);
					else if (mGY >= 7.0) { // ������
						setKeyState(KeyEvent.KEYCODE_DPAD_DOWN, true);
					}
				}
				//ҡһҡ������
				int minValue=19;
				if (Math.abs(mGX) > minValue || Math.abs(mGY) > minValue || Math.abs(mGZ) > minValue) {
					roleId=(roleId+1)%2;
	            } 
				
			}
			else if(event.sensor.getType() == Sensor.TYPE_LIGHT){
				float lv=event.values[0];
				if(lv>10.0f){
					isNight=false;
				}
				else{
					isNight=true;
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}

	}

	/**
	 * ��ָ������ʵ������
	 * 
	 * @param event
	 */
	public void setMultiTouch(MotionEvent event) {
		mAnimView.setKeyStateFalse();
		float x1 = event.getX(0);
		float y1 = event.getY(0);
		float x2 = event.getX(1);
		float y2 = event.getY(1);
		if (isFirst) {
			// �õ���һ�δ���ʱ�߶εĳ���
			oldLineDistance = (float) Math.sqrt(Math.pow(x2 - x1, 2)
					+ Math.pow(y2 - y1, 2));
			isFirst = false;
		} else {
			// mScreenHeight=800>SCENCE_HEIGHT=640....
			float maxrate=4;
			float minrate = Math.min((float) mAnimView.mScreenHeight
					/ mAnimView.SCENCE_HEIGHT, (float) mAnimView.mScreenWidth
					/ mAnimView.SCENCE_WIDTH);
			//Log.v("ratio", "min:" + minrate + " " + mAnimView.mScreenHeight+ "," + mAnimView.mScreenWidth);
			// �õ��ǵ�һ�δ���ʱ�߶εĳ���
			float newLineDistance = (float) Math.sqrt(Math.pow(x2 - x1, 2)
					+ Math.pow(y2 - y1, 2));
			if (newLineDistance > oldLineDistance) {
				isShrink = false;
				rate = oldRate * rvalue;
				if(rate>maxrate)
					rate=maxrate;
			} else {
				isShrink = true;
				rate = oldRate / rvalue;
				if (rate < minrate)
					rate = minrate;
			}
			// ��ȡ���ε����ű���
			// rate = oldRate * newLineDistance / oldLineDistance;
		}
	}

	/**
	 * ����ʵ��
	 */
	private boolean isFirst = true;
	private float oldLineDistance = 0; // ��ָ��һ��������Ļʱ�ľ���
	private float oldRate = 1;
	private float rate = 1;
	private float rvalue = (float) 1.5;
	private boolean isShrink = false; // ��Ϊtrue����ʾ��ͼ����

	@SuppressLint("NewApi")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getPointerCount() == 2) {
			setMultiTouch(event);
		}
		float x = event.getX();
		float y = event.getY();
		float dx, dy;
		float mX = mAnimView.mHeroScreenX, mY = mAnimView.mHeroScreenY;
		switch (event.getAction()) {
		// ��������
		case MotionEvent.ACTION_UP:
			isFirst = true;
			oldRate = rate;
			// ��ָ�뿪��Ļ�������ﲻ����ʹ���з���Ϊfalse
			mAnimView.setKeyStateFalse();
			return false;

			// ���ҲҪʵ�֣�������ָ����һ���ط�����������Ҳ���߶�����������ʵ�֣���ô��ָ���봥���ƶ�����������߶���
		case MotionEvent.ACTION_DOWN:
			// mAnimView.setKeyStateFalse();
			dx = x - mX;
			dy = y - mY;
			int i,j;
			float adx=Math.abs(dx);
			float ady=Math.abs(dy);
			// if (Math.abs(dx) >= TOUCH_TOLERANCE
			// || Math.abs(dy) >= TOUCH_TOLERANCE) {
			// mAnimView.setKeyStateFalse();
			//if(adx<=20.0 && )
			if (adx >= ady) { // move from left -> right
												// or right -> left
				
				if(adx<=32.0f){
					//i=mAnimView.mHeroPosX/mAnimView.TILE_WIDTH;
					//j=mAnimView.mHeroPosY/mAnimView.TILE_HEIGHT;
					//�������ָ�����ĵط��ڵ�ͼ�ж�Ӧ����������
					i=(-mAnimView.mMapPosX+(int)x)/mAnimView.TILE_WIDTH;
					j=(-mAnimView.mMapPosY+(int)y)/mAnimView.TILE_HEIGHT;
//Log.v("ij", j+" "+i+" "+mAnimView.mScreenWidth+" "+mAnimView.mScreenHeight);
//Log.v("xy",x+","+mX+" "+y+","+mY);
					if(mAnimView.mMapAcotor[j][i]==-1){
						//���һ��ũ��Ʒ
						int tmp=rand.nextInt(mAnimView.totOfProds);
						mAnimView.mMapAcotor[j][i]=tmp;
						//mAnimView.numOfProds[tmp]++;
					}
					else{
						mAnimView.isHarvest=true; //�������ã���Ļû����ʾ�ո����
						//mAnimView.numOfProds[mAnimView.mMapAcotor[j][i]]--;
						mAnimView.mMapAcotor[j][i]=-1;
					}
				}
				else if (dx > 35.0f) {
					mAnimView.setKeyState(KeyEvent.KEYCODE_DPAD_RIGHT, true);
				} else if (dx<-35.0f){
					mAnimView.setKeyState(KeyEvent.KEYCODE_DPAD_LEFT, true);
				}
			} else { // move from top -> bottom or bottom -> top
				if(ady<=45.0f){
					//i=mAnimView.mHeroPosX/mAnimView.TILE_WIDTH;
					//j=mAnimView.mHeroPosY/mAnimView.TILE_HEIGHT;
					i=(-mAnimView.mMapPosX+(int)x)/mAnimView.TILE_WIDTH;
					j=(-mAnimView.mMapPosY+(int)y)/mAnimView.TILE_HEIGHT;
					if(mAnimView.mMapAcotor[j][i]==-1){
						int tmp=rand.nextInt(mAnimView.totOfProds);
						mAnimView.mMapAcotor[j][i]=tmp;
						mAnimView.numOfProds[tmp]++;
					}
					else{
						mAnimView.isHarvest=true;
						mAnimView.numOfProds[mAnimView.mMapAcotor[j][i]]--;
						mAnimView.mMapAcotor[j][i]=-1;
						
					}
//Log.v("ij", j+" "+i);
				}
				else if (dy > 48.0f) {
					mAnimView.setKeyState(KeyEvent.KEYCODE_DPAD_DOWN, true);
				} else if(dy<-48.0f){
					mAnimView.setKeyState(KeyEvent.KEYCODE_DPAD_UP, true);
				}
			}
			return true;
			/**
			 * MOVE�ǽ���MotionEvent.ACTION_DOWN��MotionEvent.ACTION_UP֮��ģ�
			 * ����ͨ��MOVE�����������ƶ� �����MotionEvent.ACTION_DOWN�Ļ�����ֻ��ÿ�ΰ�һ����һ����̫��������
			 */
		case MotionEvent.ACTION_MOVE:
			dx = x - mX;
			dy = y - mY;
			if (Math.abs(dx) >= Math.abs(dy)) { // move from left -> right
				// or right -> left
				if (dx > 0.0f) {
					mAnimView.setKeyState(KeyEvent.KEYCODE_DPAD_RIGHT, true);
				} else {
					mAnimView.setKeyState(KeyEvent.KEYCODE_DPAD_LEFT, true);
				}
			} else { // move from top -> bottom or bottom -> top
				if (dy > 0.0f) {
					mAnimView.setKeyState(KeyEvent.KEYCODE_DPAD_DOWN, true);
				} else {
					mAnimView.setKeyState(KeyEvent.KEYCODE_DPAD_UP, true);
				}
			}
			return true;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * Activity�еķ���
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// ---- ��ʾ�Ƿ��˳���Ϸ ----
			stopMusic();
			System.exit(0);
			return false;
		}
		else{
			mAnimView.setKeyState(keyCode, true);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		mAnimView.setKeyState(keyCode, false);
		return super.onKeyUp(keyCode, event);
	}
	/**
	 * ���ڲ�������ʱ�����ֺ���
	 */
	private void initMusic() {
		try {
			int position1 = StartActivity.music.musics;
			if (position1 == 0)
				mMediaPlayer = MediaPlayer.create(SurfaceViewAcitvity.this, R.raw.fallingstar);
			if (position1 == 1)
				mMediaPlayer = MediaPlayer.create(SurfaceViewAcitvity.this, R.raw.qguose);
			if (position1 == 2)
				mMediaPlayer = MediaPlayer.create(SurfaceViewAcitvity.this,
						R.raw.qhuanyin);
			if (position1 == 3)
				mMediaPlayer = MediaPlayer.create(SurfaceViewAcitvity.this,
						R.raw.qlanxi);
			if (position1 == 4)
				mMediaPlayer = MediaPlayer.create(SurfaceViewAcitvity.this,
						R.raw.qyishui);
			if (position1 == 5)
				mMediaPlayer = MediaPlayer.create(SurfaceViewAcitvity.this,
						R.raw.qyueguang);
			//����Ĳ���ɾ�����������־Ͳ��Ų�����������
			if (mMediaPlayer != null) {
				mMediaPlayer.stop();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//System.out.println("error");
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
	private class TRun implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				mMediaPlayer.prepare();//֮ǰ������仰�ˣ����±������ֲ��Ų�����
				mMediaPlayer.start();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	}
	
}
