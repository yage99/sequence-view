/**
 * create: 2014年9月12日 下午9:11:13
 * author: ya
 * 
 * this is a file created by ya, for any question,
 * please contact zhangya998@gmail.com
 */
package com.yage.android.support;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.jni.bitmap_operations.JniBitmapHolder;

/**
 * @author ya
 *
 */
public class SequenceView extends SurfaceView implements
		SurfaceHolder.Callback, OnTouchListener {

	private SurfaceHolder holder;
	private JniBitmapHolder bmp;
	private float x;
	private int target = 0;
	private File externalPath;
	private Paint paint;
	private ValueAnimator anim;
	private int last;
	private String path;
	private int seqLen;
	private boolean moving = true;
	private int direction = -1;
	private boolean rounding = true;
	private Options option;

	private Map<Integer, JniBitmapHolder> cache = new HashMap<Integer, JniBitmapHolder>();
	private boolean isCache = true;
	private String backgroundUrl = null;
	private boolean containAlpha = false;
	protected boolean running;
	private Rect updateRect;
	private Bitmap background;
	private Bitmap enterImg;
	private Map<Integer, List<HotSpot>> hotspots = new TreeMap<Integer, List<HotSpot>>();
	private int rangestart;
	private int rangeend;
	private boolean listenHotspot;
	private HotspotCallback callback;
	private Thread autoPlayThread;
	private volatile boolean autoPlay;
	private Callback eventCallback = new Callback();
	protected int fixWidth;
	protected int fixHeight;
	private boolean defautButton = true;
	private int screenWidth;
	private int screenHeight;

	@Override
	protected void onDraw(Canvas canvas) {
		// canvas.drawColor(Color.BLACK);
		// if (canvas != null) {
		// if (containAlpha)
		// canvas.drawBitmap(trans, updateRect.left, updateRect.top, null);
		// canvas.drawBitmap(bmp.getBitmap(), updateRect.left, updateRect.top,
		// paint);
		// }
	}

	/**
	 * @param button
	 */
	public void setDefaultButton(boolean button) {
		this.defautButton = button;
	}

	/**
	 * @param url
	 */
	public void setBackgroundUrl(String url) {
		this.backgroundUrl = url;
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public SequenceView(Context context) {
		super(context);
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public SequenceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public SequenceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * 
	 */
	public void init() {
		holder = getHolder();
		getHolder().addCallback(this);
		// this.setZOrderMediaOverlay(true);

		// ActivityManager activityManager = (ActivityManager) getContext()
		// .getSystemService(Context.ACTIVITY_SERVICE);
		// activityManager.getLargeMemoryClass();

		// this.setZOrderMediaOverlay(false);
		// this.setZOrderOnTop(false);
		// Use 1/8th of the available memory for this memory cache.
		// final int cacheSize = maxMemory / 2;
		// Log.i("allocate mem", cacheSize + "kb");
		//
		// mMemoryCache = new LruCache<Integer, Bitmap>(cacheSize) {
		// @Override
		// protected int sizeOf(Integer key, Bitmap bitmap) {
		// // The cache size will be measured in kilobytes rather than
		// // number of items.
		// return bitmap.getByteCount() / 1024;
		// }
		// };
		// prepare();
	}

	/**
	 * 
	 */
	public void prepare() {
		externalPath = Environment.getExternalStorageDirectory();
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (containAlpha) {
					option = new BitmapFactory.Options();
					option.inPreferredConfig = Bitmap.Config.ARGB_8888;
					holder.setFormat(PixelFormat.RGBA_8888);
				} else {
					option = new BitmapFactory.Options();
					option.inPreferredConfig = Bitmap.Config.RGB_565;
					holder.setFormat(PixelFormat.RGB_565);
				}

				setWillNotDraw(true);
				paint = new Paint(Paint.FILTER_BITMAP_FLAG);

				Bitmap background = BitmapFactory.decodeFile(externalPath
						+ String.format(path, 0), option);
				if (backgroundUrl != null) {
					background = BitmapFactory
							.decodeFile(backgroundUrl, option);
				}
				Bitmap bmptmp = BitmapFactory.decodeFile(
						externalPath + String.format(path, 0), option);

				int left = 0;
				int top = 0;
				if (backgroundUrl != null) {
					left = (background.getWidth() - bmptmp.getWidth()) / 2;
					top = (background.getHeight() - bmptmp.getHeight()) / 2;
				}
				updateRect = new Rect(left, top, bmptmp.getWidth() + left,
						bmptmp.getHeight() + top);

				background.recycle();
				bmptmp.recycle();
				running = true;
				prepared(background.getWidth(), background.getHeight());

				if (isCache)
					loadAll();
			}

		}).start();
	}

	/**
	 * @param key
	 * @param bitmap
	 */
	public synchronized void addBitmapToMemoryCache(Integer key,
			JniBitmapHolder bitmap) {
		if (!isCache)
			return;
		// mMemoryCache.put(key, bitmap);
		if (!cache.containsKey(key)) {
			cache.put(key, bitmap);
		}
	}

	/**
	 * load all images to cache
	 */
	public void loadAll() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				getBitmap(0);
				for (int i = 1; i <= seqLen && running; i++)
					getBitmap(i);

				SequenceView.this.post(new Runnable() {

					@Override
					public void run() {
						eventCallback.cached();
					}
				});

			}

		}).start();

	}

	/**
	 * 
	 */
	@SuppressLint("WrongCall")
	public synchronized void rend() {
		if (holder == null)
			return;

		if (bmp != null) {
			Rect dirty = new Rect(updateRect);
			Canvas canvas = holder.lockCanvas(dirty);

			// if (target % 2 == 1) {
			// this.onDraw(c);
			// paint.setAlpha(127);
			// } else
			// paint.setAlpha(255);
			if (canvas != null) {
				if (dirty.left == 0) {
					drawBackground(canvas);
				}
				if (background != null) {
					canvas.drawBitmap(background, updateRect.left,
							updateRect.top, paint);
				}
				canvas.drawBitmap(bmp.getBitmap(), updateRect.left,
						updateRect.top, paint);
				holder.unlockCanvasAndPost(canvas);
			}
		}

	}

	/**
	 * @param key
	 * @return
	 */
	public JniBitmapHolder getBitmapFromMemCache(Integer key) {
		if (!cache.containsKey(key))
			return null;
		// return mMemoryCache.get(key);

		JniBitmapHolder holder = cache.get(key);
		if (holder == null)
			return null;
		return holder;
	}

	/**
	 * @param canvas
	 */
	private void drawBackground(Canvas canvas) {
		if (backgroundUrl != null) {
			Bitmap back = BitmapFactory.decodeFile(backgroundUrl, option);
			canvas.drawBitmap(back, 0, 0, paint);
			background = Bitmap.createBitmap(back, updateRect.left,
					updateRect.top, updateRect.width(), updateRect.height());
			back.recycle();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder
	 * , int, int, int)
	 */
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		rend();
	}

	/**
	 * 
	 */
	private void updateSize() {
		Bitmap bmptmp = BitmapFactory.decodeFile(
				externalPath + String.format(path, 0), option);
		int left = 0;
		int top = 0;
		if (backgroundUrl != null) {
			left = (this.getWidth() - bmptmp.getWidth()) / 2;
			top = (this.getHeight() - bmptmp.getHeight()) / 2;
		}
		updateRect = new Rect(left, top, bmptmp.getWidth() + left,
				bmptmp.getHeight() + top);
		bmptmp.recycle();

		Canvas c = holder.lockCanvas();
		drawBackground(c);
		holder.unlockCanvasAndPost(c);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder
	 * )
	 */
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		DisplayMetrics metrics = this.getResources().getDisplayMetrics();
		this.screenWidth = metrics.widthPixels;
		this.screenHeight = metrics.heightPixels;
		// render = new MyThread();
		// render.start();

		// for (int i = 0; i < 251; i++)
		// holder.setFormat(PixelFormat.TRANSPARENT);

		setOnTouchListener(this);
		// running = new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// while (true) {
		// if (buffupdate) {
		// int tt = getFrame();
		// for (int i = 0; i < 5; i++) {
		// int toload = convertId(tt + i);
		// // Log.d("buff", "" + toload);
		// Bitmap temp = BitmapFactory.decodeFile(externalPath
		// + String.format(path, toload));
		// addBitmapToMemoryCache(toload, temp);
		// }
		// buffupdate = false;
		// } else {
		// try {
		// Thread.sleep(100);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
		// }
		// }
		//
		// });
		// running.start();
	}

	protected void prepared(final int width, final int height) {
		this.post(new Runnable() {

			@Override
			public void run() {
				if (holder != null) {
					holder.setFixedSize(width, height);
					fixWidth = width;
					fixHeight = height;
					updateTarget(0);
					startAutoPlay();
					eventCallback.ready();
				}
			}
		});
	}

	/**
	 * @author ya
	 *
	 */
	public static class Callback {

		/**
		 * 
		 */
		public void ready() {
		};

		/**
		 * 
		 */
		public void cached() {
		};

		public void drawHotSpot(float realLeft, float realTop, int id) {
		}

		/**
		 * 
		 */
		public void clearHotspots() {
			// TODO Auto-generated method stub

		};
	}

	/**
	 * @param callback
	 */
	public void setEventCallback(Callback callback) {
		this.eventCallback = callback;
	}

	/**
	 * @param pathFormat
	 * @param i
	 */
	public void setPath(String pathFormat, int i) {
		this.path = pathFormat;
		this.seqLen = i;
		this.rangeend = i;
	}

	/**
	 * @param moving
	 */
	public void setMoving(boolean moving) {
		this.moving = moving;
	}

	/**
	 * @return
	 */
	private Integer getFrame() {
		return target;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.
	 * SurfaceHolder)
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// if (render != null && render.isAlive()) {
		// render.end();
		// }
		if (anim != null)
			anim.end();
		running = false;

		holder = null;
		new Thread(new Runnable() {

			@Override
			public void run() {
				List<JniBitmapHolder> templist = new ArrayList<JniBitmapHolder>();
				templist.addAll(cache.values());
				for (JniBitmapHolder bmp : templist) {
					if (bmp != null)
						bmp.freeBitmap();
				}
			}

		}).start();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View,
	 * android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		if (!this.isEnabled())
			return false;
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
			x = event.getX();
			if (anim != null)
				anim.end();
			last = target;
			setrange(last);
			return true;
		case MotionEvent.ACTION_MOVE:
			float thisx = event.getX();
			listenHotspot = false;
			eventCallback.clearHotspots();
			// Log.d("Bigniao", "moveing:" + thisx);
			updateImage(thisx);
			return true;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			moving(event.getX() - x);
			drawHotspots(event.getX(), event.getY());
			arg0.performClick();
			return true;
		}
		return false;
	}

	/**
	 * @param callback
	 */
	public void setCallback(HotspotCallback callback) {
		this.callback = callback;
	}

	/**
	 * @param g
	 * @param f
	 * 
	 */
	private void drawHotspots(float x, float y) {
		List<HotSpot> points = hotspots.get(getFrame());
		if (points == null) {
			listenHotspot = false;
			return;
		}
		if (listenHotspot) {
			for (HotSpot point : points) {
				int left = point.x - enterImg.getWidth() / 2;
				int top = point.y - enterImg.getHeight() / 2;
				Rect rect = new Rect(left, top, left + enterImg.getWidth(), top
						+ enterImg.getHeight());
				int ix = (int) (x / this.getWidth() * fixWidth);
				int iy = (int) (y / this.getHeight() * fixHeight);
				if (rect.contains(ix, iy)) {
					callback.fireHotspot(point.Id);
				}
			}
		} else {
			for (HotSpot point : points) {
				if (defautButton) {
					int left = point.x - enterImg.getWidth() / 2;
					int top = point.y - enterImg.getHeight() / 2;
					Rect rect = new Rect(left, top, left + enterImg.getWidth(),
							top + enterImg.getHeight());
					Canvas c = holder.lockCanvas(rect);
					c.drawBitmap(enterImg, point.x - enterImg.getWidth() / 2,
							point.y - enterImg.getHeight() / 2, null);
					holder.unlockCanvasAndPost(c);
					listenHotspot = true;
				}
				float realLeft = (float) point.x / fixWidth * this.screenWidth;
				float realTop = (float) point.y / fixHeight * this.screenHeight;
				eventCallback.drawHotSpot(realLeft, realTop, point.getId());
			}
		}
	}

	/**
	 * @param center
	 */
	public void setrange(int center) {
		if (hotspots.isEmpty()) {
			rangestart = 0;
			rangeend = seqLen;
			return;
		}
		for (Integer i : hotspots.keySet()) {
			if (i < center)
				rangestart = i;
			else if (i == center)
				continue;
			else {
				rangeend = i;
				break;
			}
		}
		if (rangestart >= center)
			rangestart = 0;
		if (rangeend <= center)
			rangeend = seqLen;
	}

	/**
	 * @param f
	 * 
	 */
	private void moving(float f) {
		if (!moving)
			return;

		if (Math.abs(f) < 100)
			return;
		int start = 0;
		if (f > 0)
			start = -2;
		else
			start = 2;
		anim = ValueAnimator.ofFloat(start, 0);
		anim.setDuration(2000);
		anim.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				updateTarget(convertId((int) (target + (float) arg0
						.getAnimatedValue())));
				// rend();
			}
		});
		anim.start();
	}

	protected void updateImage(float newx) {
		int delta = direction * (int) ((newx - x) / 10);
		if (Math.abs(delta) < 1)
			return;
		updateTarget(convertId(last + delta));

		// rend();
	}

	protected void updateTarget(int target) {
		if (target <= rangestart)
			this.target = rangestart;
		else if (target >= rangeend)
			this.target = rangeend;
		else
			this.target = target;
		bmp = getBitmap(getFrame());
		rend();
	}

	protected JniBitmapHolder getBitmap(int key) {
		JniBitmapHolder tempbmp = getBitmapFromMemCache(key);
		if (tempbmp == null) {
			Bitmap map = BitmapFactory.decodeFile(
					externalPath + String.format(path, key), option);
			// Log.i("allocate mem", "image" + bmp.getAllocationByteCount() /
			// 1024
			// + "kb");
			if (map != null) {
				tempbmp = new JniBitmapHolder(map);
				addBitmapToMemoryCache(key, tempbmp);
				map.recycle();
			}
		}
		return tempbmp;
	}

	/**
	 * @param pid
	 * @return
	 */
	public int convertId(int pid) {
		if (rounding) {
			pid = pid % seqLen;
			return pid < 0 ? pid + seqLen : pid;
		} else {
			if (pid > seqLen)
				pid = seqLen;
			else if (pid < 0)
				pid = 0;
			return pid;
		}
	}

	/**
	 * @param i
	 */
	public void setDirection(int i) {
		this.direction = i;
	}

	/**
	 * @param b
	 */
	public void setRounding(boolean b) {
		this.rounding = b;
	}

	/**
	 * @param b
	 */
	public void setCache(boolean b) {
		this.isCache = b;
	}

	/**
	 * @param b
	 */
	public void setContainAlpha(boolean b) {
		this.containAlpha = b;
	}

	/**
	 * @param frame
	 * @param x
	 * @param y
	 */
	public void addHotSpot(int frame, List<HotSpot> points) {
		if (enterImg == null) {
			enterImg = BitmapFactory.decodeFile(Environment
					.getExternalStorageDirectory()
					+ "/Pictures/building/enter.png");
		}
		hotspots.put(frame, points);
	}

	/**
	 * @author ya
	 *
	 */
	public static class HotSpot extends Point {
		private int Id;

		/**
		 * @param x
		 * @param y
		 * @param id
		 */
		public HotSpot(int x, int y, int id) {
			super(x, y);
			this.Id = id;
		}

		/**
		 * @return the id
		 */
		public int getId() {
			return Id;
		}

		/**
		 * @param id
		 *            the id to set
		 */
		public void setId(int id) {
			Id = id;
		}

	}

	/**
	 * @author ya
	 *
	 */
	public static interface HotspotCallback {
		public void fireHotspot(int id);
	}

	/**
	 * 
	 */
	private void startAutoPlay() {
		autoPlayThread = new Thread() {
			@Override
			public void run() {
				while (autoPlay && running) {
					updateTarget(convertId(target + 1));
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		autoPlayThread.start();
	}

	/**
	 * @param b
	 */
	public void setAutoPlay(boolean b) {
		this.autoPlay = b;
	}

}
