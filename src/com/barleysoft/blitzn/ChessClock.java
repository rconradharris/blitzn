package com.barleysoft.blitzn;

import android.app.Activity;
import android.graphics.Color;
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
		OnClickListener player1ClickListener = new OnClickListener() {
			public void onClick(View v) {
				onPlayerClick(PLAYER1);
			}
		};
		player1Clock.setOnClickListener((android.view.View.OnClickListener) player1ClickListener);
		
		player2Clock = (TextView) findViewById(R.id.player2Clock);
		OnClickListener player2ClickListener = new OnClickListener() {
			public void onClick(View v) {
				onPlayerClick(PLAYER2);
			}
		};	
		player2Clock.setOnClickListener((android.view.View.OnClickListener) player2ClickListener);
		
		resetClock();
		updateClockDisplays();
	}

	void onPlayerClick(int which) {
		if (which == PLAYER1) {
			switch (clockState) {
			case READY:
				initiateClock(PLAYER1);
				break;
			case PLAYER1_RUNNING:
				toggleClock();
				break;
			case PLAYER1_PAUSED:
				pauseClock(); // unpause
				break;
			}	
		} else {
			switch (clockState) {
			case READY:
				initiateClock(PLAYER2);
				break;
			case PLAYER2_RUNNING:
				toggleClock();
				break;
			case PLAYER2_PAUSED:
				pauseClock(); // unpause
				break;
			}	
		}
		
	}
	
	void initiateClock(int which) {
		if (clockState != READY) {
			// throw new ClockStateException("already started");
		}
		
		activateClock(which);

		handler.removeCallbacks(updateTimeTask);
		handler.postDelayed(updateTimeTask, 100);
	}
	
	void activateClock(int which) {
		if (which == PLAYER1) {
			// TODO(sirp): theme-up colors
			player1Clock.setClickable(true);
			player1Clock.setBackgroundColor(Color.RED);
			deactivateClock(PLAYER2, false);
			clockState = PLAYER1_RUNNING;
		} else {
			// TODO(sirp): theme-up colors
			player2Clock.setClickable(true);
			player2Clock.setBackgroundColor(Color.RED);
			deactivateClock(PLAYER1, false);
			clockState = PLAYER2_RUNNING;
		}
	}
	
	void deactivateClock(int which, boolean clickable) {
		TextView clock = (which == PLAYER1) ? player1Clock : player2Clock;
		clock.setBackgroundColor(Color.BLACK);
		clock.setClickable(clickable);
	}
	
	void resetClock() {
		handler.removeCallbacks(updateTimeTask);
		player1TimeLeft = duration;
		player2TimeLeft = duration;
		deactivateClock(PLAYER1, true);
		deactivateClock(PLAYER2, true);
		clockState = READY;
	}
	
	void toggleClock() {
		switch (clockState) {
		case PLAYER1_RUNNING:
			activateClock(PLAYER2);
			break;
		case PLAYER2_RUNNING:
			activateClock(PLAYER1);
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
		player1Clock.setClickable(false);
		player2Clock.setClickable(false);
		clockState = STOPPED;	    
	}
	
	private Runnable updateTimeTask = new Runnable() {
		public void run() {
			// Check for either clock expiring
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