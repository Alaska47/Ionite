package com.akotnana.ionite.activities;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.akotnana.ionite.R;
import com.akotnana.ionite.fragments.TimerDialogFragment;
import com.akotnana.ionite.utils.JSONBase;
import com.akotnana.ionite.utils.OnSwipeTouchListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements TimerDialogFragment.TimerDialogListener, DatePickerDialog.OnDateSetListener {

    public static String TAG = "MainActivity";

    private ScrollView scrollView;
    private ConstraintLayout conView;
    private LinearLayout mainView;
    private LinearLayout defaultView;

    public static boolean currentDayLoaded = false;
    public static int currentYear;
    public static boolean defaultViewOn = false;
    public static Object[] currentSchedule;
    public int offsetDays = 0;

    public static DialogFragment currentDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_ionite);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        */
        setContentView(R.layout.activity_main);
        scrollView = (ScrollView) findViewById(R.id.scroller);
        conView = (ConstraintLayout) findViewById(R.id.root);
        mainView = (LinearLayout) findViewById(R.id.schedule_view);
        defaultView = (LinearLayout) findViewById(R.id.default_view);

        OnSwipeTouchListener swiperListener = new OnSwipeTouchListener(getApplicationContext()) {
            public void onSwipeLeft() {
                if (MainActivity.currentDayLoaded) {
                    offsetDays++;
                    getCurrentDay(offsetDays, 1);
                    Log.d("SPECIAL", ""+offsetDays);
                } else {
                    return;
                }
            }

            public void onSwipeRight() {
                if (MainActivity.currentDayLoaded) {
                    offsetDays--;
                    getCurrentDay(offsetDays, -1);
                    Log.d("SPECIAL", ""+offsetDays);
                } else {
                    return;
                }
            }

            public void openTimeRemaining() {
                if (MainActivity.currentDayLoaded) {
                    findViewById(R.id.progress).setVisibility(View.VISIBLE);
                    scrollView.setVisibility(View.GONE);
                    getCurrentDay(0, 0);
                    offsetDays = 0;
                    showDialog();
                } else {
                    return;
                }
            }
        };

        conView.setOnTouchListener(swiperListener);
        scrollView.setOnTouchListener(swiperListener);
        mainView.setOnTouchListener(swiperListener);
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        getCurrentDay(0, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public void onDialogTimeFinished(DialogFragment dialog) {
        Log.d(TAG, "Timer Finished!");
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        currentDialog.dismiss();
                        findViewById(R.id.progress).setVisibility(View.VISIBLE);
                        scrollView.setVisibility(View.GONE);
                        getCurrentDay(0, 0);
                        offsetDays = 0;
                    }
                });
            }
        }, 1000);
    }

    public void showDialog() {
        /*
        FragmentManager fragmentManager = getFragmentManager();
        TimerDialogFragment newFragment = new TimerDialogFragment();
        newFragment.show(fragmentManager, "dialog");
        */
        boolean inPeriod = true;
        boolean passing = false;
        String[] details = getCurrentClass();
        if(details.length == 0) {
            inPeriod = false;
            details = getNextClass();
            if(details[0].contains("PASSING")) {
                inPeriod = true;
                passing = true;
                details[0] = details[0].substring(7);
            }
        }
        currentDialog = new TimerDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putStringArray("nextClassStart", details);
        bundle.putBoolean("inPeriod", inPeriod);
        bundle.putBoolean("passing", passing);
        currentDialog.setArguments(bundle);
        currentDialog.show(getFragmentManager(), "NoticeDialogFragment");
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = "You picked the following date: "+dayOfMonth+"/"+(monthOfYear+1)+"/"+year;
        Log.d(TAG, date);

        Calendar now = Calendar.getInstance();
        Calendar chosenDate = Calendar.getInstance();
        chosenDate.set(Calendar.YEAR, year);
        chosenDate.set(Calendar.MONTH, monthOfYear);
        chosenDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        int daysBetween = (int) TimeUnit.MILLISECONDS.toDays(Math.abs(chosenDate.getTimeInMillis() - now.getTimeInMillis()));
        offsetDays = daysBetween;
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        getCurrentDay(daysBetween, 0);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                findViewById(R.id.progress).setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.GONE);
                getCurrentDay(0, 0);
                offsetDays = 0;
                return true;
            case R.id.search:
                Calendar now = Calendar.getInstance();
                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        MainActivity.this,
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DAY_OF_MONTH)
                );
                dpd.show(getFragmentManager(), "Datepickerdialog");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String[] getNextClass() {
        DateFormat dateFormat = new SimpleDateFormat("EEE, MMM. dd");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        Date curr = new Date();
        String currentDate = dateFormat.format(curr).toString();
        String currentTime = timeFormat.format(curr).toString();
        Log.d(TAG, currentDate + " " + currentTime);
        ArrayList<String[]> timings = (ArrayList<String[]>) currentSchedule[2];
        if (currentDate.equals(currentSchedule[0].toString()) && convertToMinutes(timings.get(timings.size() - 1)[1].split(" - ")[1]) > convertToMinutes(currentTime) && convertToMinutes(timings.get(0)[1].split(" - ")[0]) < convertToMinutes(currentTime)) {

            for (int i = 0; i < timings.size()-1; i++) {
                String[] times = timings.get(i)[1].split(" - ");
                String[] times1 = timings.get(i+1)[1].split(" - ");
                if (compareMilitaryTime(currentTime, times[1], times1[0])) {
                    String[] finalized = timings.get(i+1);
                    finalized[0] = "PASSING" + finalized[0] + "~" + currentDate;
                    finalized[1] = times[1] + " - " + times1[0];
                    return finalized;
                }
            }
        }

        String[] finalized = timings.get(0);
        finalized[0] = finalized[0] + "~" + currentSchedule[0].toString();
        return finalized;
    }

    public String[] getCurrentClass() {
        DateFormat dateFormat = new SimpleDateFormat("EEE, MMM. dd");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        Date curr = new Date();
        String currentDate = dateFormat.format(curr).toString();
        String currentTime = timeFormat.format(curr).toString();
        Log.d(TAG, currentDate + " " + currentTime);

        if (currentDate.equals(currentSchedule[0].toString())) {
            ArrayList<String[]> timings = (ArrayList<String[]>) currentSchedule[2];
            for (int i = 0; i < timings.size(); i++) {
                String[] times = timings.get(i)[1].split(" - ");
                if (compareMilitaryTime(currentTime, times[0], times[1])) {
                    String[] finalized = timings.get(i);
                    finalized[0] = finalized[0] + "~" + currentDate;
                    return finalized;
                }
            }
        }
        return new String[0];
    }

    public void getCurrentDay(int offsetDays, int direction) {

        Date d1 = new Date();
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        c1.add(Calendar.DATE, offsetDays);
        if(direction == -1) {
            if ((c1.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) || (c1.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
                while (c1.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
                    c1.add(Calendar.DATE, -1);
                    this.offsetDays--;
                }
            }
        } else if (direction == 1) {
            if ((c1.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) || (c1.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
                while (c1.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                    c1.add(Calendar.DATE, 1);
                    this.offsetDays++;
                }
            }
        } else {
            if ((c1.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) || (c1.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
                while (c1.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                    c1.add(Calendar.DATE, 1);
                }
            }
        }




        DateFormat dateFormat1 = new SimpleDateFormat("EEE, MMM. dd");
        Date curr = new Date();
        String currentDate = dateFormat1.format(curr).toString();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String date = dateFormat.format(c1.getTime()).toString();
        String time = timeFormat.format(c1.getTime()).toString();
        Log.d(TAG, "Retrieving for: " + date);

        Object[] currentSchedule = null;
        try {
            currentSchedule = new Retriever().execute("https://ion.tjhsst.edu/api/schedule/" + date + "?format=json").get();
        } catch (InterruptedException e) {
            displayDefaultMessage();
            e.printStackTrace();
        } catch (ExecutionException e) {
            displayDefaultMessage();
            e.printStackTrace();
        }
        if (currentSchedule == null) {
            displayDefaultMessage();
        } else {
            if (currentSchedule.length < 4) {
                String debug = currentSchedule[0].toString() + "\n" + currentSchedule[1].toString() + "\n" + TextUtils.join(", ", (ArrayList<String[]>) currentSchedule[2]);
                Log.d(TAG, debug);
            } else {
                String debug = currentSchedule[0].toString() + "\n" + currentSchedule[1].toString() + "\n" + currentSchedule[3].toString() + "\n" + TextUtils.join(", ", (ArrayList<String[]>) currentSchedule[2]);
                Log.d(TAG, debug);
            }
            ArrayList<String[]> currentSched = ((ArrayList<String[]>) currentSchedule[2]);
            if(currentSched.size() < 1) {
                displaySchedule(currentSchedule);
            } else {
                if (convertToMinutes(currentSched.get(currentSched.size() - 1)[1].split(" - ")[1]) < convertToMinutes(time) && currentDate.equals(currentSchedule[0].toString())) {
                    getCurrentDay(1, 0);
                } else {
                    displaySchedule(currentSchedule);
                }
            }
        }
    }

    public String getCurrentClass(Object[] schedule) {
        DateFormat dateFormat = new SimpleDateFormat("EEE, MMM. dd");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        Date curr = new Date();
        String currentDate = dateFormat.format(curr).toString();
        String currentTime = timeFormat.format(curr).toString();
        Log.d(TAG, currentDate + " " + currentTime);

        if (currentDate.equals(schedule[0].toString())) {
            ArrayList<String[]> timings = (ArrayList<String[]>) schedule[2];
            for (int i = 0; i < timings.size(); i++) {
                String[] times = timings.get(i)[1].split(" - ");
                if (compareMilitaryTime(currentTime, times[0], times[1]))
                    return timings.get(i)[0];
            }
        }
        return "None";
    }

    private int convertToMinutes(String time) {
        String[] currentStr = time.split(":");
        return Integer.parseInt(currentStr[0]) * 60 + Integer.parseInt(currentStr[1]);
    }

    private boolean compareMilitaryTime(String current, String start, String end) {
        int currentMin = convertToMinutes(current);
        int startMin = convertToMinutes(start);
        int endMin = convertToMinutes(end);
        return (currentMin > startMin && currentMin < endMin);
    }

    public void displaySchedule(Object[] schedule) {
        currentDayLoaded = true;

        mainView.removeAllViews();
        currentSchedule = schedule;
        String currentClass = getCurrentClass(schedule);

        Log.d("MainActivity", "about to add components");

        TextView dater = new TextView(this);
        dater.setText(schedule[0].toString());
        dater.setTextSize(30);
        dater.setGravity(Gravity.CENTER);
        dater.setTextColor(Color.BLACK);
        mainView.addView(dater);

        TextView type = new TextView(this);
        type.setText(schedule[1].toString());
        type.setTypeface(type.getTypeface(), Typeface.BOLD);
        type.setTextSize(30);
        type.setGravity(Gravity.CENTER);
        if (schedule[1].toString().contains("Red"))
            type.setTextColor(Color.RED);
        else if (schedule[1].toString().contains("Blue"))
            type.setTextColor(Color.BLUE);
        else if (schedule[1].toString().contains("Anchor"))
            type.setTextColor(Color.rgb(0, 153, 0));
        else
            type.setTextColor(Color.BLACK);
        mainView.addView(type);

        ArrayList<String[]> sched = (ArrayList<String[]>) schedule[2];
        String o = "";
        for (String[] pair : sched) {
            o += pair[0] + "|" + pair[1] + ", ";
        }
        Log.d("Sched", o);

        LinearLayout subView = new LinearLayout(this);
        subView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        subView.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout periods = new LinearLayout(this);
        periods.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        periods.setOrientation(LinearLayout.VERTICAL);

        LinearLayout blocks = new LinearLayout(this);
        blocks.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        blocks.setOrientation(LinearLayout.VERTICAL);

        for (String[] pair : sched) {
            if (!pair[0].contains("Passing")) {

                TextView periodBlock = new TextView(this);
                periodBlock.setText(pair[0] + ":");
                periodBlock.setTextSize(24);
                periodBlock.setGravity(Gravity.RIGHT);
                periodBlock.setTextColor(Color.BLACK);


                TextView classBlock = new TextView(this);
                String[] times = pair[1].split(" - ");
                String[] start = times[0].split(":");
                String[] end = times[1].split(":");
                String finalStart = ((Integer.parseInt(start[0]) > 12) ? Integer.parseInt(start[0]) - 12 : Integer.parseInt(start[0])) + ":" + start[1];
                String finalEnd = ((Integer.parseInt(end[0]) > 12) ? Integer.parseInt(end[0]) - 12 : Integer.parseInt(end[0])) + ":" + end[1];
                classBlock.setText("   " + finalStart + " - " + finalEnd);
                classBlock.setTextSize(24);
                classBlock.setTextColor(Color.BLACK);
                if (pair[0] == currentClass) {
                    classBlock.setTypeface(classBlock.getTypeface(), Typeface.BOLD);
                    periodBlock.setTypeface(periodBlock.getTypeface(), Typeface.BOLD);
                }
                periods.addView(periodBlock);
                blocks.addView(classBlock);
            }
        }
        subView.addView(periods);
        subView.addView(blocks);
        if(sched.size() > 0)
            mainView.addView(subView);
        findViewById(R.id.progress).setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
        Log.d(TAG, "reached end of display");
    }

    public void displayDefaultMessage() {
        Log.d(TAG, "Default displayed");
        scrollView.setVisibility(GONE);
        defaultView.setVisibility(View.VISIBLE);
        defaultViewOn = true;
        currentDayLoaded = false;
    }

    private Animation inFromRightAnimation() {

        Animation inFromRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                0.0f);
        inFromRight.setDuration(300);
        inFromRight.setInterpolator(new OvershootInterpolator());
        return inFromRight;
    }

    private Animation outToLeftAnimation() {
        Animation outtoLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        outtoLeft.setDuration(300);
        outtoLeft.setInterpolator(new OvershootInterpolator());
        return outtoLeft;
    }

    private Animation inFromLeftAnimation() {
        Animation inFromLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT,
                0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromLeft.setDuration(300);
        inFromLeft.setInterpolator(new OvershootInterpolator());
        return inFromLeft;
    }

    private Animation outToRightAnimation() {
        Animation outtoRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        outtoRight.setDuration(300);
        outtoRight.setInterpolator(new OvershootInterpolator());
        return outtoRight;
    }


}

class Retriever extends AsyncTask<String, Void, Object[]> {
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Object[] doInBackground(String... website) {
        try {

            String doc = readUrl(website[0]);
            Gson gson = null;
            try {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
            } catch (Exception e) {
                e.printStackTrace();
            }
            doc = doc.replaceAll("\\<br\\>", "");
            Log.d(MainActivity.TAG, doc);
            JSONBase arr = gson.fromJson(doc, JSONBase.class);
            Object[] a = arr.getData();
            String da = (String) a[0];
            String[] temp = da.split("-");
            GregorianCalendar date1 = new GregorianCalendar(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]) - 1,
                    Integer.parseInt(temp[2]));
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMMM, d");
            da = sdf.format(date1.getTime());
            String[] thingy = da.split(", ");
            if (thingy[1].length() > 5) {
                sdf = new SimpleDateFormat("EEE, MMM. d");
            } else {
                sdf = new SimpleDateFormat("EEE, MMMM d");
            }
            da = sdf.format(date1.getTime());
            Object[] obj = new Object[3];
            obj[0] = da;
            obj[1] = a[1];
            obj[2] = (ArrayList<String[]>) a[2];
            MainActivity.currentYear = Integer.parseInt(temp[0]);
            return obj;

        } catch (NullPointerException e) {
            e.printStackTrace();
            //ArrayList<String[]> schedule = new ArrayList<String[]>();
            //Object[] output = { "", "No Internet Connection", schedule };
            //return output;
        }
        return null;
    }

    private static String readUrl(String urlString) {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return "";
    }

    protected void onPostExecute(Boolean result) {

    }
}
