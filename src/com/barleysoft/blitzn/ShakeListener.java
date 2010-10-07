/* The following code was written by Matthew Wiggins
 * and is released under the APACHE 2.0 license
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package com.barleysoft.blitzn;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;
import android.content.Context;
import java.lang.UnsupportedOperationException;

public class ShakeListener implements SensorEventListener {
	private static final int FORCE_THRESHOLD = 700; // 350;
	private static final int TIME_THRESHOLD = 100;
	private static final int SHAKE_TIMEOUT = 250; // 500;
	private static final int SHAKE_DURATION = 1000;
	private static final int SHAKE_COUNT = 3;

	private SensorManager mSensorMgr;
	private Sensor mAccel;
	private float mLastX = -1.0f, mLastY = -1.0f, mLastZ = -1.0f;
	private long mLastTime;
	private OnShakeListener mShakeListener;
	private Context mContext;
	private int mShakeCount = 0;
	private long mLastShake;
	private long mLastForce;

	public interface OnShakeListener {
		public void onShake();
	}

	public ShakeListener(Context context) {
		mContext = context;
		resume();
	}

	public void setOnShakeListener(OnShakeListener listener) {
		mShakeListener = listener;
	}

	public void resume() {
		mSensorMgr = (SensorManager) mContext
				.getSystemService(Context.SENSOR_SERVICE);
		if (mSensorMgr == null) {
			throw new UnsupportedOperationException("Sensors not supported");
		}

		mAccel = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		boolean supported = mSensorMgr.registerListener(this, mAccel,
				SensorManager.SENSOR_DELAY_GAME);

		if (!supported) {
			mSensorMgr.unregisterListener(this, mAccel);
			throw new UnsupportedOperationException(
					"Accelerometer not supported");
		}
	}

	public void pause() {
		if (mSensorMgr != null) {
			mSensorMgr.unregisterListener(this, mAccel);
			mSensorMgr = null;
			mAccel = null;
		}
	}

	public boolean isListening() {
		return (mSensorMgr != null);
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
			return;

		long now = System.currentTimeMillis();

		if ((now - mLastForce) > SHAKE_TIMEOUT) {
			mShakeCount = 0;
		}

		if ((now - mLastTime) > TIME_THRESHOLD) {
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];

			// Log.i("ShakeListener", String.format("x=%f, y=%f, z=%f", x, y,
			// z));

			long diff = now - mLastTime;
			float speed = Math.abs(x + y + z - mLastX - mLastY - mLastZ) / diff
					* 10000;
			if (speed > FORCE_THRESHOLD) {
				if ((++mShakeCount >= SHAKE_COUNT)
						&& (now - mLastShake > SHAKE_DURATION)) {
					mLastShake = now;
					mShakeCount = 0;
					if (mShakeListener != null) {
						Log.i("ShakeListener", "shake detected");
						mShakeListener.onShake();
					}
				}
				mLastForce = now;
			}
			mLastTime = now;
			mLastX = x;
			mLastY = y;
			mLastZ = z;
		}

	}

}
