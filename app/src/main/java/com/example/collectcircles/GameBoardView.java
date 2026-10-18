package com.example.collectcircles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;


public class GameBoardView extends View {
    private static final float TARGET_RADIUS_DP = 64f;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Target target;
    private Circle[] previewCircles = new Circle[0];

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
    @Override
    protected void onSizeChanged(int width, int height,
                                 int oldWidth, int oldHeight) {
        float centerX = width / 2f;
        float centerY = height / 2f;
        float targetRadius = Math.min(
                dp(64f),
                Math.min(width, height) / 5f
        );
        float circleRadius = targetRadius / 2f;

        target = new Target(
                centerX, centerY, targetRadius,
                color(R.color.target_red),
                color(R.color.target_cross),
                dp(2f)
        );

        previewCircles = new Circle[] {
                new Circle(circleRadius * 1.5f, circleRadius * 1.5f,
                        circleRadius, color(R.color.circle_green)),
                new Circle(width - circleRadius * 1.5f, circleRadius * 2.2f,
                        circleRadius, color(R.color.circle_green)),
                new Circle(circleRadius * 1.8f, height - circleRadius * 1.7f,
                        circleRadius, color(R.color.circle_green))
        };
    }

    /**
     * מצייר את המטרה ואת עיגולי הדוגמה על הלוח.
     *
     * @param canvas משטח הציור שסיפקה מערכת Android
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (target == null) {
            return;
        }

        target.draw(canvas, paint);
        for (Circle circle : previewCircles) {
            circle.draw(canvas, paint);
        }
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
}