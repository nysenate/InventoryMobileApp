package gov.nysenate.inventory.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * A simple view to capture a path traced onto the screen. Initially intended to
 * be used to captures signatures.
 *
 * @author Andrew Crichton
 * @version 0.1
 * @modifications made by Brian Heitner
 */
public class SignatureView extends View {
    @SuppressWarnings("unused")
    private Path mPath;
    private Paint mPaint;
    private Paint bgPaint = new Paint(Color.WHITE);
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private int minWidth, minHeight, maxWidth, maxHeight;
    private int stateToSave;
    private Bitmap initialBitmap;
    private Context currentContext = null;

    private boolean signed = false;

    private float curX, curY;
    private float origCurX, origCurY;

    private static final int TOUCH_TOLERANCE = 4;
    private static final int STROKE_WIDTH = 4;

    public SignatureView(Context context) {
        super(context);
        init(context);
    }

    public SignatureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SignatureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context curContext) {
        initialBitmap = this.getImage();
        this.currentContext = curContext;
        if (initialBitmap == null || initialBitmap.getWidth() < 1
                || initialBitmap.getHeight() < 1) {
            initialBitmap = BitmapFactory.decodeResource(currentContext
                            .getApplicationContext().getResources(),
                    R.drawable.simplethinborder);
            if (initialBitmap == null) {
                Log.i("Initial", "Initial Bitmap not created");
            }
        }
        setFocusable(true);
        mPath = new Path();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(STROKE_WIDTH);
    }

    public void setInitialBitmap(Bitmap bmap) {
        this.initialBitmap = bmap;
    }

    public void setSigColor(int color) {
        mPaint.setColor(color);
    }

    public void setSigColor(int a, int red, int green, int blue) {
        mPaint.setARGB(a, red, green, blue);
    }

    public boolean clearSignature() {
        if (mBitmap != null)
            // System.out.println(" mBitmap createFakeMotionEvents");
            // createFakeMotionEvents();
            if (mCanvas != null) {
                // System.out.println("*mCanvas CLEAR");
                // mCanvas.drawColor(Color.GREEN);
                // mCanvas.drawPaint(new Paint(Color.GREEN));
                // mPaint.setColor(Color.BLUE);
                // mCanvas.drawRect(3, 3, this.getWidth()-6, this.getHeight()-6,
                // new Paint(Color.MAGENTA));

                clearSignatureWorkaround();
                mPath.reset();
                invalidate();
            } else {
                // System.out.println("CLEAR ELSE");
                return false;
            }
        this.signed = false;

        return true;
    }

    public void setMinDimensions(int minWidth, int minHeight) {
        this.minWidth = minWidth;
        this.minWidth = minHeight;
    }

    public void setMaxDimensions(int maxWidth, int maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public void setMinWidth(int minWidth) {
        this.minWidth = minWidth;
    }

    public void setMinHeight(int minHeight) {
        this.minHeight = minHeight;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        // begin boilerplate code that allows parent classes to save state
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);
        // end

        ss.stateToSave = this.stateToSave;

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        // begin boilerplate code so parent classes can restore state
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;

        super.onRestoreInstanceState(ss.getSuperState());
        // end

        this.stateToSave = ss.stateToSave;
    }

    public void clearSignatureWorkaround() {

        if (initialBitmap == null) {
            Log.i("ClearSIG", "InitialBitmap was null");
            for (int x = 1; x < this.getWidth(); x++) {
                for (int y = 1; y < this.getHeight(); y++) {
                    mBitmap.setPixel(x, y, Color.TRANSPARENT);

                }

            }

        } else {
            this.setImage(initialBitmap);
        }
        this.signed = false;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }

    public boolean isSigned() {
        return this.signed;
    }

    public Bitmap getImage() {
        return this.mBitmap;
    }

    public void setImage(Bitmap bitmap) {
        this.mBitmap = bitmap;
        this.invalidate();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth,
                                 int oldHeight) {
        int bitmapWidth = mBitmap != null ? mBitmap.getWidth() : 0;
        int bitmapHeight = mBitmap != null ? mBitmap.getWidth() : 0;

        if (minWidth > 0 && width < minWidth) {
            width = minWidth;
        }
        if (minHeight > 0 && height < minHeight) {
            height = minHeight;
        }
        if (maxWidth > 0 && width > maxWidth) {
            width = maxWidth;
        }
        if (maxHeight > 0 && height > maxHeight) {
            height = maxHeight;
        }

        if (minWidth > 0 && bitmapWidth < minWidth) {
            bitmapWidth = minWidth;
        }
        if (minHeight > 0 && bitmapHeight < minHeight) {
            bitmapHeight = minHeight;
        }
        if (maxWidth > 0 && bitmapWidth > maxWidth) {
            bitmapWidth = maxWidth;
        }
        if (maxHeight > 0 && bitmapHeight > maxHeight) {
            bitmapHeight = maxHeight;
        }

        if (bitmapWidth >= width && bitmapHeight >= height)
            return;
        if (bitmapWidth < width)
            bitmapWidth = width;
        if (bitmapHeight < height)
            bitmapHeight = height;

        if (bitmapHeight < 1) {
            bitmapHeight = 1;
        }
        if (bitmapWidth < 1) {
            bitmapWidth = 1;
        }

        if (height < 1) {
            height = 1;
        }
        if (width < 1) {
            width = 1;
        }

        // Log.i("I",
        // " PROBLEM AREA: NEW WIDTH:"+bitmapWidth+" NEW HEIGHT:"+bitmapHeight);
        Bitmap newBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight,
                Bitmap.Config.ARGB_8888);
        Canvas newCanvas = new Canvas();
        newCanvas.setBitmap(newBitmap);
        if (mBitmap != null)
            newCanvas.drawBitmap(mBitmap, 0, 0, null);
        mBitmap = newBitmap;
        mCanvas = newCanvas;
    }

    private void createFakeMotionEvents() {
        MotionEvent downEvent = MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_DOWN, 1f,
                1f, 0);
        MotionEvent upEvent = MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_UP, 1f,
                1f, 0);
        onTouchEvent(downEvent);
        onTouchEvent(upEvent);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDown(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                break;
        }
        invalidate();
        return true;
    }

    /**
     * ---------------------------------------------------------- Private
     * methods ---------------------------------------------------------
     */

    private void touchDown(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        curX = x;
        curY = y;
        origCurX = curX;
        origCurY = curY;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - curX);
        float dy = Math.abs(y - curY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(curX, curY, (x + curX) / 2, (y + curY) / 2);
            curX = x;
            curY = y;
        }
    }

    private void touchUp() {
        if (curX == origCurX && curY == origCurY) {
            mPath.addCircle(curX, curY, 2, Path.Direction.CW);
        } else {
            mPath.lineTo(curX, curY);
        }
        if (mCanvas == null) {
            mCanvas = new Canvas();
            mCanvas.setBitmap(mBitmap);
        }
        mCanvas.drawPath(mPath, mPaint);
        this.signed = true;
        mPath.reset();
    }

    static class SavedState extends BaseSavedState {
        int stateToSave;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.stateToSave = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.stateToSave);
        }

        // required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}