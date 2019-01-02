package com.rexcola.catchnorah;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.rexcola.catchnorah.Utilities;
import com.rexcola.catchnorah.Utilities.Coords;

import java.util.Random;

public class CatchView extends SurfaceView implements SurfaceHolder.Callback {

    class CatchJamesThread extends Thread {

        public CatchJamesThread(SurfaceHolder surfaceHolder, Context context,
                                Handler handler) {
            // get handles to some important objects
            mSurfaceHolder = surfaceHolder;
            mHandler = handler;

            // cache handles to our key sprites & other drawables
            jamesImage = context.getResources().getDrawable(
                    R.drawable.norah2,context.getTheme());

            jamesWidth = jamesImage.getIntrinsicWidth();
            jamesHeight = jamesImage.getIntrinsicHeight();

        }

        /**
         * Starts the game, setting parameters for the current difficulty.
         */
        public void doStart() {
            doReset();

        }

        public void doReset()
        {
            synchronized (syncObject) {

                mDifficulty = DIFFICULTY_EASY;
                wins = 0;
                mDifficultyFixed = false;

                // pick a convenient initial location
                mX = mCanvasWidth / 2;
                mY = mCanvasHeight - jamesHeight / 2;

                setTransformation();
                justChanged = true;

            }

        }

        @Override
        public void run() {
            doStart();

            while (mRun) {
                Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (syncObject) {

                        doDraw(c);
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }



        public void setRunning(boolean b) {
            mRun = b;
        }


        /* Callback invoked when the surface dimensions change. */
        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (syncObject) {
                mCanvasWidth = width;
                mCanvasHeight = height;
            }
        }

        void doTouch(MotionEvent motion)
        {
            synchronized (syncObject) {
                mX = motion.getX(motion.getPointerCount()-1);
                mY = motion.getY(motion.getPointerCount()-1);
            }

        }


        private void doDraw(Canvas canvas) {
            Paint p = new Paint();
            Rect totalRect = new Rect(0,0,mCanvasWidth,mCanvasHeight);

            // Transform the positions
            Coords touchPos = new Coords((int) mX, (int) mY);
            Coords disp = doTransformation(touchPos);

            // Special message first time we draw the view (because it isn't a win yet)
            if (firstDraw)
            {
                firstDraw = false;
                if (wins == 0)
                {
                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("text", "Try to catch me");
                    b.putInt("viz", View.VISIBLE);
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                } else
                {
                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("text", "Welcome back");
                    b.putInt("viz", View.VISIBLE);
                    msg.setData(b);
                    mHandler.sendMessage(msg);

                }

            }

            if (Utilities.distance(disp,touchPos) < ((jamesHeight + jamesWidth)/2.1) || justChanged)
            {
                justChanged = false;
                //  playSoundEffect(SoundEffectConstants.CLICK); // FIXME: Add sound effects
                setTransformation();
                wins++;
                if (!mDifficultyFixed)
                {
                    if (wins > 20)
                    {
                        mDifficulty = DIFFICULTY_HARD;
                    }
                    else if (wins > 5)
                    {
                        mDifficulty = DIFFICULTY_MEDIUM;
                    }
                    else
                    {
                        mDifficulty = DIFFICULTY_EASY;
                    }
                }
                latestEndTime = System.currentTimeMillis();
                long averageTime = (latestEndTime - startTime)/wins;
                Message msg = mHandler.obtainMessage();
                Bundle b = new Bundle();
                String s;
                // String s = wins + " wins taking an average of " + averageTime + " milliseconds each";
                switch (mDifficulty)
                {
                    case DIFFICULTY_EASY:
                        s = "Easy level";
                        break;
                    case DIFFICULTY_MEDIUM:
                        s = "Medium level";
                        break;
                    default:
                        s = "Hardest level";
                }
                b.putString("text", s);
                b.putInt("viz", View.VISIBLE);
                msg.setData(b);
                mHandler.sendMessage(msg);
                Random r = new Random();
                p.setColor(Color.rgb(r.nextInt(200)+56,r.nextInt(200)+56,r.nextInt(200)+56));
                canvas.drawRect(totalRect,p);


            }
            else
            {
                p.setColor(Color.BLACK);
                canvas.drawRect(totalRect,p);

                int yTop = (int) disp.y - jamesHeight / 2;
                int xLeft = (int) disp.x - jamesWidth / 2;

                jamesImage.setBounds(xLeft, yTop, xLeft + jamesWidth, yTop
                        + jamesHeight);
                jamesImage.draw(canvas);
            }
        }

        private void setTransformation()
        {
            originX = ( new Random()).nextInt(mCanvasWidth);
            if (mDifficulty == DIFFICULTY_EASY)
            {
                XX = -1;
                XY = 0;
                YY = -1;
                YX = 0;
            }
            else if (mDifficulty == DIFFICULTY_MEDIUM)
            {
                XY = 0;
                YX = 0;
                XX = Utilities.randomFloat(1,2);
                YY = Utilities.randomFloat(1,2);
//    			if (Utilities.randomBoolean())
//    			{
//    				XX = 0.5;
//    			}
//    			else
//    			{
//    				XX = -0.5;
//    			}
//    			if (Utilities.randomBoolean())
//    			{
//    				YY = 0.5;
//    			}
//    			else
//    			{
//    				YY = -0.5;
//    			}
            }
            else
            {
                XX = Utilities.randomFloat(1,2);
                XY = Utilities.randomFloat(1,2);
                YX = Utilities.randomFloat(1,2);
                YY = Utilities.randomFloat(1,2);
            }
            originY = ( new Random()).nextInt(mCanvasHeight);
        }

        private Coords doTransformation(Coords inPoint)
        {
            int displayX;
            int displayY;
            int offsetX = (int) inPoint.x - originX;
            int offsetY = (int) inPoint.y - originY;

            displayX = (originX + mCanvasWidth + (int) (XX*offsetX + XY*offsetY)) % mCanvasWidth;
            displayY = (originY + mCanvasHeight + (int) (YX*offsetX + YY*offsetY)) % mCanvasHeight;

//        	if (XX > 1.0)
//        	{
//            	displayX = (offsetX + originX + mCanvasWidth) % mCanvasWidth;
//
//        	} else
//        	{
//            	displayX = (((int) inPoint.x - originX) * -1 + mCanvasWidth) % mCanvasWidth;
//
//        	}
//        	if (YY > 1.0)
//        	{
//            	displayY = (offsetY + originY + mCanvasHeight) % mCanvasHeight;
//
//        	}
//        	else
//        	{
//            	displayY = (((int) inPoint.y - originY) * -1 + mCanvasHeight) % mCanvasHeight;
//
//        	}

            return new Coords(displayX,displayY);
        }

    }

    /** Pointer to the text view to display "Paused.." etc. */
    private TextView statusText;

    /** The thread that actually draws the animation */
    private CatchJamesThread thread;

    private Context context;

    public CatchView(Context inContext, AttributeSet attrs) {
        super(inContext, attrs);
        context = inContext;

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        setFocusable(true); // make sure we get key events
    }

    /**
     * Fetches the animation thread corresponding to this LunarView.
     *
     * @return the animation thread
     */
    public CatchJamesThread getThread() {
        return thread;
    }

    public boolean onTouchEvent(MotionEvent motion)
    {
        thread.doTouch(motion);
        return true;
    }


    /**
     * Installs a pointer to the text view used for messages.
     */
    public void setTextView(TextView textView) {
        statusText = textView;
    }

    /* Callback invoked when the surface dimensions change. */
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        thread.setSurfaceSize(width, height);
    }

    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder) {

        thread = new CatchJamesThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
                statusText.setVisibility(m.getData().getInt("viz"));
                statusText.setText(m.getData().getString("text"));
            }
        });
        if (pauseStart == 0)
        {
            startTime = System.currentTimeMillis();
        }
        else
        {
            pauseEnd = System.currentTimeMillis();
            startTime += pauseEnd - pauseStart;
            pauseStart = 0;
        }
        firstDraw = true;
        thread.setRunning(true);
        thread.start();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        pauseStart = System.currentTimeMillis();
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    public synchronized void restoreState(Bundle savedState) {
        synchronized (syncObject) {

            mDifficulty = savedState.getInt(KEY_DIFFICULTY);
            mX = savedState.getDouble(KEY_X);
            mY = savedState.getDouble(KEY_Y);

            jamesWidth = savedState.getInt(KEY_LANDER_WIDTH);
            jamesHeight = savedState.getInt(KEY_LANDER_HEIGHT);
            wins = savedState.getInt(KEY_WINS);
            Long totalTime = savedState.getLong(KEY_TOTAL_TIME);
            startTime = System.currentTimeMillis() - totalTime;
        }
    }

    /*
     * Difficulty setting constants
     */
    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_HARD = 1;
    public static final int DIFFICULTY_MEDIUM = 2;

    // Bundle strings
    private static final String KEY_DIFFICULTY = "difficulty";
    private static final String KEY_LANDER_HEIGHT = "landerHeight";
    private static final String KEY_LANDER_WIDTH = "landerWidth";
    private static final String KEY_WINS = "winsInARow";
    private static final String KEY_TOTAL_TIME = "TotalTimeSoFar";
    private static final String KEY_X = "mX";
    private static final String KEY_Y = "mY";

    /*
     * Member (state) fields
     */

    private boolean firstDraw = true;
    private boolean justChanged = true;
    private int mCanvasHeight = 1;
    private int mCanvasWidth = 1;
    private int mDifficulty;
    private boolean mDifficultyFixed = false;
    private Handler mHandler;
    private int jamesHeight;
    private Drawable jamesImage;
    private int jamesWidth;
    private boolean mRun = false;
    private SurfaceHolder mSurfaceHolder;
    private int wins = 0;
    private long startTime;
    private long latestEndTime;
    private double mX;
    private double mY;
    private int originX;
    private double XY, XX, YX, YY;

    private int originY;

    private long pauseStart = 0;
    private long pauseEnd;

    private Integer syncObject = 1;

    public Bundle saveState(Bundle map) {
        synchronized (syncObject) {
            if (map != null) {
                map.putInt(KEY_DIFFICULTY, Integer.valueOf(mDifficulty));
                map.putDouble(KEY_X, Double.valueOf(mX));
                map.putDouble(KEY_Y, Double.valueOf(mY));
                map.putInt(KEY_LANDER_WIDTH, Integer.valueOf(jamesWidth));
                map.putInt(KEY_LANDER_HEIGHT, Integer
                        .valueOf(jamesHeight));
                map.putInt(KEY_WINS, Integer.valueOf(wins));
                map.putLong(KEY_TOTAL_TIME, Long.valueOf(System.currentTimeMillis()- startTime));
            }
        }
        return map;
    }

    public void setDifficulty(int difficulty) {
        synchronized (syncObject) {
            mDifficulty = difficulty;
            mDifficultyFixed = true;
            justChanged = true;
        }
    }

}
