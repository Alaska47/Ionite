package com.akotnana.ionite.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.akotnana.ionite.R;
import at.grabner.circleprogress.AnimationState;
import at.grabner.circleprogress.AnimationStateChangedListener;
import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;
import at.grabner.circleprogress.UnitPosition;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by anees on 10/27/2017.
 */

public class TimerDialogFragment extends DialogFragment {
    public interface TimerDialogListener {
        public void onDialogTimeFinished(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    TimerDialogListener mListener;
    String[] nextClassStart;



    private long totalTimeMillis;
    private CountDownTimer countDownTimer;
    ProgressBar mprogressBar;
    TextView mTextView;
    CircleProgressView mCircleView;
    private boolean inPeriod;
    private boolean passing;
    private String currPeriod;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (TimerDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    /*@Override
    public void timerElapsed() {
        mListener.onDialogTimeFinished(TimerDialogFragment.this);
    }
    */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            nextClassStart = bundle.getStringArray("nextClassStart");
            inPeriod = bundle.getBoolean("inPeriod");
            passing = bundle.getBoolean("passing");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            nextClassStart = bundle.getStringArray("nextClassStart");
            inPeriod = bundle.getBoolean("inPeriod");
        }

        Log.d("DialogFragment", Arrays.toString(nextClassStart));

        // Inflate the layout to use as dialog or embedded fragment
        View v = inflater.inflate(R.layout.timer_dialog1, container, false);

        mCircleView = (CircleProgressView) v.findViewById(R.id.circleView);
        mTextView = (TextView) v.findViewById(R.id.second);
        //mprogressBar = (ProgressBar) v.findViewById(R.id.circular_progress_bar);
        //mTextView = (TextView) v.findViewById(R.id.progress_text);

        currPeriod = nextClassStart[0].split("~")[0];
        //Log.d("DialogFragment", nextClassStart[0].split("~")[1]);
        //Log.d("DialogFragment", nextClassStart[0].split("~")[0]);

        DateFormat dateFormat = new SimpleDateFormat("EEE, MMM. dd");
        final Calendar c1 = Calendar.getInstance();
        try {
            c1.setTime(dateFormat.parse(nextClassStart[0].split("~")[1]));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar c2 = Calendar.getInstance();
        try {
            c2.setTime(dateFormat.parse(nextClassStart[0].split("~")[1]));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.d("DialogFragment", ""+inPeriod);
        String[] timing = nextClassStart[1].split(" - ")[0].split(":");
        String[] timing1 = nextClassStart[1].split(" - ")[1].split(":");
        int hours = Integer.parseInt(timing[0]);
        int minutes = Integer.parseInt(timing[1]);
        c1.set(Calendar.HOUR, hours);
        c1.set(Calendar.MINUTE, minutes);
        int hours1 = Integer.parseInt(timing1[0]);
        int minutes1 = Integer.parseInt(timing1[1]);
        c2.set(Calendar.HOUR, hours1);
        c2.set(Calendar.MINUTE, minutes1);

        mCircleView.setShowTextWhileSpinning(true); // Show/hide text in spinning mode

        final Calendar currentTime = Calendar.getInstance();
        c1.set(Calendar.YEAR, currentTime.get(Calendar.YEAR));
        c2.set(Calendar.YEAR, currentTime.get(Calendar.YEAR));

        totalTimeMillis = c2.getTimeInMillis() - c1.getTimeInMillis();
        Log.d("DialogFragment", "Goal: " + String.valueOf(c1.getTimeInMillis()));
        Log.d("DialogFragment", "Goal2: " + String.valueOf(c2.getTimeInMillis()));
        Log.d("DialogFragment", "Today: " + String.valueOf(currentTime.getTimeInMillis()));
        Log.d("DialogFragment", "Goal - Today: " + String.valueOf(c1.getTimeInMillis() - currentTime.getTimeInMillis()));
        Log.d("DialogFragment", "Goal - Today2: " + String.valueOf(c2.getTimeInMillis() - currentTime.getTimeInMillis()));
        Log.d("DialogFragment", "Total time: " + String.valueOf(totalTimeMillis));

        long countDownTime;
        if(inPeriod && !passing) {
            countDownTime = c2.getTimeInMillis() - currentTime.getTimeInMillis();
            mTextView.setText("until " + currPeriod + " ends");
        } else if(passing) {
            countDownTime = c2.getTimeInMillis() - currentTime.getTimeInMillis();
            mTextView.setText("until " + currPeriod + " starts");
        } else {
            countDownTime = c1.getTimeInMillis() - currentTime.getTimeInMillis();
            mTextView.setText("until " + currPeriod + " starts");
        }



        countDownTimer = new CountDownTimer(countDownTime, 1000) {

            public void onTick(long millisUntilFinished) {
                //donutProgress.setDonut_progress("HI");
                double percent = ((double) millisUntilFinished/totalTimeMillis);
                if(!inPeriod) {
                    mCircleView.setValue(100f);
                } else {
                    if(!passing) {
                        mCircleView.setValue(((float) ((double) millisUntilFinished/totalTimeMillis*100)));
                    } else {
                        mCircleView.setValue(((float) ((double) millisUntilFinished/totalTimeMillis*100)));
                    }

                }

                Log.d("DialogFragment", "Percent of time left: " + percent);
                String seconds = String.valueOf((int) (millisUntilFinished / 1000) % 60);
                String minutes = String.valueOf((int) ((millisUntilFinished / (1000*60)) % 60));
                String hours   = String.valueOf(((int) ((millisUntilFinished / (1000*60*60)) % 24)) + (c1.get(Calendar.DAY_OF_MONTH) - currentTime.get(Calendar.DAY_OF_MONTH) -1 )*24);



                if(seconds.length() == 1)
                    seconds = "0" + seconds;
                if(minutes.length() == 1)
                    minutes = "0" + minutes;
                if(hours.length() == 1)
                    hours = "0" + hours;

                //mTextView.setText(hours + ":" + minutes +":" + seconds);
                 //shows the given text in the circle view
                mCircleView.setTextMode(TextMode.TEXT); // Set text mode to text to show text
                mCircleView.setText(hours + ":" + minutes +":" + seconds);
            }

            public void onFinish() {
                mListener.onDialogTimeFinished(TimerDialogFragment.this);
                Log.d("DialogFragment", "Finished countdown!");
            }
        };
        countDownTimer.start();


        return v;
    }

    private double round (double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        countDownTimer.cancel();
    }
}
