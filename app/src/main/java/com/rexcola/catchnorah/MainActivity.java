package com.rexcola.catchnorah;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.rexcola.catchnorah.CatchView.CatchJamesThread;

public class MainActivity extends AppCompatActivity {
    private static final int MENU_EASY = 1;

    private static final int MENU_HARD = 2;

    private static final int MENU_MEDIUM = 3;

    private static final int MENU_RESET = 4;

    private static final int MENU_STOP = 5;

    private CatchView catchJamesView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_RESET, 0, R.string.menu_reset);
        menu.add(0, MENU_STOP, 0, R.string.menu_stop);
        menu.add(0, MENU_EASY, 0, R.string.menu_easy);
        menu.add(0, MENU_MEDIUM, 0, R.string.menu_medium);
        menu.add(0, MENU_HARD, 0, R.string.menu_hard);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                catchJamesView.getThread().doReset();
                return true;
            case MENU_STOP:
                finish();
                return true;
            case MENU_EASY:
                catchJamesView.setDifficulty(CatchView.DIFFICULTY_EASY);
                return true;
            case MENU_MEDIUM:
                catchJamesView.setDifficulty(CatchView.DIFFICULTY_MEDIUM);
                return true;
            case MENU_HARD:
                catchJamesView.setDifficulty(CatchView.DIFFICULTY_HARD);
                return true;
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // tell system to use the layout defined in our XML file
        setContentView(R.layout.activity_main);

        // get handles to the CatchView from XML, and its thread
        catchJamesView = (CatchView) findViewById(R.id.catchView);

        // give the CatchView a handle to the TextView used for messages
        catchJamesView.setTextView((TextView) findViewById(R.id.text));

        if (savedInstanceState == null) {
            // we were just launched: set up a new game
            //catchJamesThread.setState(CatchJamesThread.STATE_READY);
        } else {
            // we are being restored: resume a previous game
            catchJamesView.restoreState(savedInstanceState);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // just have the View save its state into our Bundle
        super.onSaveInstanceState(outState);
        catchJamesView.saveState(outState);
    }
}


