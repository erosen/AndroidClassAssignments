package edu.rutgers.touchscreengestures;

import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends Activity {

	final int[] x = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	final int[] y = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
	final int[] index = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	@SuppressLint("ClickableViewAccessibility")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		GestureCanvas myGestures = new GestureCanvas(this);

		setContentView(myGestures);

		myGestures.set_x(x);
		myGestures.set_y(y);
		myGestures.set_index(index);

		myGestures.invalidate();

		myGestures.setOnTouchListener(new View.OnTouchListener() {

		
			public boolean onTouch(View v, MotionEvent event) {
				
				int fingers = event.getPointerCount() >= 10 ? 10 : event.getPointerCount();
				
				for (int i = 0; i < fingers; i++) {
					x[i] = (int) event.getX(i);
					y[i] = (int) event.getY(i);
					index[i] = 1;

					v.invalidate();

				}
				
				if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
					for (int i = 1; i < 10; i++) {
						index[i] = 0;
						v.invalidate();
					}
				}
				
				if (event.getAction() == MotionEvent.ACTION_UP) {
					for (int i = 0; i < 10; i++) {
						index[i] = 0;
						v.invalidate();
					}
				}
				
				if (event.getAction() == MotionEvent.ACTION_CANCEL) {
					for (int i = 0; i < 10; i++) {
						index[i] = 0;
						v.invalidate();
					}
				}
				
				return true;
			}

		});
	}

}

class GestureCanvas extends View {
	private final int radius = 150;
	private int[] x, y, index;
	private Paint[] myColor = new Paint[10];
	private Paint paint = new Paint();

	private final Random rnd = new Random();

	public GestureCanvas(Context context) {
		super(context);
		setFocusable(true);

		paint.setColor(Color.BLACK);
		paint.setTextSize(45);

		for (int i = 0; i < 10; i++) {
			myColor[i] = new Paint();
			myColor[i].setARGB(255, rnd.nextInt(256), rnd.nextInt(256),
					rnd.nextInt(256));
		}

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		for (int i = 0; i < 10; i++) {
			if (index[i] == 1) {

				canvas.drawCircle(x[i], y[i], radius, myColor[i]);
				canvas.drawText("ID: " + i, x[i] + 153, y[i] - 45, paint);
				canvas.drawText("X : " + x[i], x[i] + 153, y[i], paint);
				canvas.drawText("Y : " + y[i], x[i] + 153, y[i] + 45, paint);
			}
		}
	}

	public int[] get_x() {
		return x;
	}

	public int[] get_y() {
		return y;
	}

	public int[] get_index() {
		return index;
	}

	void set_x(int[] my_x) {
		x = my_x;
	}

	void set_y(int[] my_y) {
		y = my_y;
	}

	void set_index(int[] my_index) {
		index = my_index;
	}

}
