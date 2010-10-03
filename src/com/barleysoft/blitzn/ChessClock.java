package com.barleysoft.blitzn;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ChessClock extends Activity {
	
	// Constants
	public static int PLAYER1 = 1;
	public static int PLAYER2 = 2;
	private static int CLOCK_RESOLUTION = 100; // ms
	
	// Clock State Machine
	public static final int NOSTATE = 0;
	public static final int READY = 1;
	public static final int PLAYER1_RUNNING = 2;
	public static final int PLAYER2_RUNNING = 3;
	public static final int PLAYER1_PAUSED = 4;
	public static final int PLAYER2_PAUSED = 5;
	public static final int STOPPED = 6;
	
	// Member variables
	private int clockState = NOSTATE;
	private long duration = 5 * 1 * 1000L;
	private long player1TimeLeft = 0L;
	private long player2TimeLeft = 0L;

	private TextView player1Clock;
	private TextView player2Clock;
	
	private Handler handler = new Handler();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		player1Clock = (TextView) findViewById(R.id.player1Clock);
		player1Clock.setOnClickListener((android.view.View.OnClickListener) player1ClickListener);
		
		player2Clock = (TextView) findViewById(R.id.player2Clock);
		player2Clock.setOnClickListener((android.view.View.OnClickListener) player2ClickListener);
		
		resetClock();
		updateClockDisplays();
	}

	OnClickListener player1ClickListener = new OnClickListener() {
		public void onClick(View v) {
			switch (clockState) {
			case READY:
				startClock(PLAYER1);
				break;
			case PLAYER1_RUNNING:
				toggleClock();
				break;
			case PLAYER1_PAUSED:
				pauseClock(); // unpause
				break;
			}
		}
	};
	
	OnClickListener player2ClickListener = new OnClickListener() {
		public void onClick(View v) {
			switch (clockState) {
			case READY:
				startClock(PLAYER2);
				break;
			case PLAYER2_RUNNING:
				toggleClock();
				break;
			case PLAYER2_PAUSED:
				pauseClock(); // unpause
				break;
			}
		}
	};	
	
	void startClock(int which) {
		if (clockState != READY) {
			// throw new ClockStateException("already started");
		}

		if (which == PLAYER1) {
			clockState = PLAYER1_RUNNING;
		} else {
			clockState = PLAYER2_RUNNING;
		}
		handler.removeCallbacks(updateTimeTask);
		handler.postDelayed(updateTimeTask, 100);
	}
	
	void resetClock() {
		handler.removeCallbacks(updateTimeTask);
		player1TimeLeft = duration;
		player2TimeLeft = duration;
		clockState = READY;
	}
	
	void toggleClock() {
		switch (clockState) {
		case PLAYER1_RUNNING:
			clockState = PLAYER2_RUNNING;
			break;
		case PLAYER2_RUNNING:
			clockState = PLAYER1_RUNNING;
			break;
		default:
			// throw ClockStateException("wrong state");
		}
	}
	
	void pauseClock() {
		switch (clockState) {
		case PLAYER1_RUNNING:
			handler.removeCallbacks(updateTimeTask);
			clockState = PLAYER1_PAUSED;
			break;
		case PLAYER2_RUNNING:
			handler.removeCallbacks(updateTimeTask);
			clockState = PLAYER2_PAUSED;
			break;
		case PLAYER1_PAUSED:
			handler.removeCallbacks(updateTimeTask);
			handler.postDelayed(updateTimeTask, 100);
			clockState = PLAYER1_RUNNING;
			break;
		case PLAYER2_PAUSED:
			handler.removeCallbacks(updateTimeTask);
			handler.postDelayed(updateTimeTask, 100);
			clockState = PLAYER2_RUNNING;
			break;
		default:
			// throw ClockStateException("wrong state");
		}
	}
	
	void stopClock() {
		handler.removeCallbacks(updateTimeTask);
	    clockState = STOPPED;
	}
	
	private Runnable updateTimeTask = new Runnable() {
		public void run() {
			
			if ((player1TimeLeft < CLOCK_RESOLUTION) ||
			    (player2TimeLeft < CLOCK_RESOLUTION)) {
				stopClock();
				return;
			}
			
			switch (clockState) {
			case PLAYER1_RUNNING:
				player1TimeLeft -= CLOCK_RESOLUTION;
				break;
			case PLAYER2_RUNNING:
				player2TimeLeft -= CLOCK_RESOLUTION;
				break;
			}
			
			updateClockDisplays();
			
			// Reschedule the next tick
			long nextUpdate = SystemClock.uptimeMillis() + CLOCK_RESOLUTION;
			handler.postAtTime(this, nextUpdate);
		}
	};
	
	void updateClockDisplays() {
		updateClockForPlayer(player1Clock, player1TimeLeft);
		updateClockForPlayer(player2Clock, player2TimeLeft);
	}
	
	void updateClockForPlayer(TextView clockView, long timeLeft) {
		int seconds = (int) timeLeft / 1000;
		int minutes = seconds / 60;
		seconds = seconds % 60;
		String clockText = String.format("%02d:%02d", minutes, seconds);
		clockView.setText(clockText);	
	}
		

		

}