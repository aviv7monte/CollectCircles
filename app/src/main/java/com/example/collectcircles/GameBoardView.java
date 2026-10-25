package com.example.collectcircles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import  android.view.MotionEvent;

import androidx.core.content.ContextCompat;


public class GameBoardView extends View {
    private static final float TARGET_RADIUS_DP = 64f;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Game game;

    private Runnable onCircleCollected;
    private Runnable onGameFinished;

    public void setOnCircleCollectedListener(Runnable listener) {
        onCircleCollected = listener;
    }

    /**
     * מגדיר פעולה שתופעל לאחר איסוף העיגול האחרון.
     *
     * @param listener הפעולה שתופעל בסיום המשחק, או {@code null} להסרתה
     */
    public void setOnGameFinishedListener(Runnable listener) {
        onGameFinished = listener;
    }
    /**
     * יוצר לוח משחק מתוך קוד Java.
     *
     * @param context ההקשר שבו הלוח פועל
     */
    public GameBoardView(Context context) {
        this(context, null);
    }

    /**
     * יוצר לוח משחק מתוך XML.
     *
     * @param context ההקשר שבו הלוח פועל
     * @param attrs המאפיינים שנקראו מן ה-XML
     */
    public GameBoardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * יוצר לוח משחק מתוך XML עם סגנון ברירת מחדל.
     *
     * @param context ההקשר שבו הלוח פועל
     * @param attrs המאפיינים שנקראו מן ה-XML
     * @param defStyleAttr מאפיין הסגנון שיוחל כברירת מחדל
     */
    public GameBoardView(Context context, AttributeSet attrs,
                         int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(color(R.color.board_background));
    }

    /**
     * מתאים את המטרה ואת עיגולי הדוגמה למידות החדשות של הלוח.
     *
     * @param width רוחב הלוח החדש בפיקסלים
     * @param height גובה הלוח החדש בפיקסלים
     * @param oldWidth רוחב הלוח הקודם בפיקסלים
     * @param oldHeight גובה הלוח הקודם בפיקסלים
     */
    /**
     * מתחיל סיבוב חדש לפי הגודל הנוכחי של לוח המשחק.
     * אם הלוח עדיין לא נמדד, הפעולה אינה משנה את המשחק.
     */
    public void startNewGame() {
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        float targetRadius = Math.min(
                dp(TARGET_RADIUS_DP),
                Math.min(getWidth(), getHeight()) / 5f
        );
        game = new Game(
                getWidth(),
                getHeight(),
                targetRadius,
                color(R.color.target_red),
                color(R.color.target_cross),
                dp(2f),
                color(R.color.circle_green)
        );
        invalidate();
    }
    /**
     * מצייר את המטרה ואת עיגולי הדוגמה על הלוח.
     *
     * @param canvas משטח הציור שסיפקה מערכת Android
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (game == null) {
            return;
        }

        game.draw(canvas, paint);
    }

    /**
     * ממיר יחידות dp לפיקסלים בהתאם לצפיפות המסך.
     *
     * @param value הגודל ביחידות dp
     * @return הגודל המתאים בפיקסלים
     */
    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    /**
     * קורא ערך צבע ממשאבי היישום.
     *
     * @param colorResource מזהה משאב הצבע
     * @return ערך הצבע שבו אפשר להשתמש לציור
     */
    private int color(int colorResource) {
        return ContextCompat.getColor(getContext(), colorResource);
    }


    /**
     * מטפל בבחירה, בגרירה ובשחרור של עיגול באמצעות אירועי מגע.
     *
     * @param event אירוע המגע שסיפקה מערכת Android
     * @return {@code true} אם הלוח טיפל באירוע
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (game == null) {
            return false;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                boolean selected =
                        game.selectCircleAt(event.getX(), event.getY());
                if (selected) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return selected;

            case MotionEvent.ACTION_MOVE:
                game.moveSelectedCircle(event.getX(), event.getY());
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                game.moveSelectedCircle(event.getX(), event.getY());
                boolean collected = game.releaseSelectedCircle();
                getParent().requestDisallowInterceptTouchEvent(false);
                invalidate();
                performClick();
                if (collected && onCircleCollected != null) {
                    onCircleCollected.run();
                }
                if (collected
                        && game.isFinished()
                        && onGameFinished != null) {
                    onGameFinished.run();
                }
                return true;

            case MotionEvent.ACTION_CANCEL:
                game.cancelSelection();
                getParent().requestDisallowInterceptTouchEvent(false);
                invalidate();
                return true;

            default:
                return super.onTouchEvent(event);
        }
    }

    /**
            * מדווח לכלי הנגישות שהלחיצה על הלוח הושלמה.
 *
         * @return תמיד {@code true}, מפני שהלחיצה טופלה
 */
    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }
}