package com.open.androidtvwidget.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import com.open.androidtvwidget.R;
import com.open.androidtvwidget.cache.BitmapMemoryCache;
import com.open.androidtvwidget.utils.DrawUtils;
import com.open.androidtvwidget.utils.Utils;

/**
 * 倒影，圆角控件.
 * <p/>
 * <p/>
 * run提的问题  倒影绘到那个layout那里去了 貌似绘到他的上层控件上面了
 *
 * @author hailongqiu 356752238@qq.com
 */
public class ReflectItemView extends FrameLayout {

    private static final String TAG = "ReflectItemView";
    //倒影的高度
    private static final int DEFUALT_REFHEIGHT = 80;
    private static final int DEFUALT_RADIUS = 12;

    private Paint mClearPaint = null;
    private Paint mShapePaint = null;
    private Paint mRefPaint = null;
    private int mRefHeight = DEFUALT_REFHEIGHT;

    private boolean mIsDrawShape = false;
    private boolean mIsReflection = false;

    private float mRadius = DEFUALT_RADIUS;
    private RadiusRect mRadiusRect = new RadiusRect(mRadius, mRadius, mRadius, mRadius);

    private BitmapMemoryCache mBitmapMemoryCache = BitmapMemoryCache.getInstance();
    private static int sViewIDNum = 0;
    private int viewIDNum = 0;

    public ReflectItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public ReflectItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ReflectItemView(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        setClipChildren(false);
        setClipToPadding(false);
        setWillNotDraw(false);
        // 初始化属性.
        if (attrs != null) {
            TypedArray tArray = context.obtainStyledAttributes(attrs, R.styleable.reflectItemView);// 获取配置属性
            mIsReflection = tArray.getBoolean(R.styleable.reflectItemView_isReflect, false);
            mRefHeight = (int) tArray.getDimension(R.styleable.reflectItemView_reflect_height, DEFUALT_REFHEIGHT);
            mIsDrawShape = tArray.getBoolean(R.styleable.reflectItemView_isShape, false);
            mRadius = tArray.getDimension(R.styleable.reflectItemView_radius, DEFUALT_RADIUS);
            setRadius(mRadius);
        }
        // 初始化圆角矩形.
        initShapePaint();
        // 初始化倒影参数.
        initRefPaint();
    }

    private void initShapePaint() {
        mShapePaint = new Paint();
        mShapePaint.setAntiAlias(true);
        // 取两层绘制交集。显示下层。
        mShapePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    private void initRefPaint() {
        if (mRefPaint == null) {
            mRefPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            // 倒影渐变.
            mRefPaint.setShader(
                    new LinearGradient(0, 0, 0, mRefHeight, new int[]{0x77000000, 0x66AAAAAA, 0x0500000, 0x00000000},
                            new float[]{0.0f, 0.1f, 0.9f, 1.0f}, Shader.TileMode.CLAMP));
            mRefPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        }
        if (mClearPaint == null) {
            mClearPaint = new Paint();
            mClearPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        }
    }

    /**
     * 设置是否倒影.
     */
    public void setReflection(boolean ref) {
        mIsReflection = ref;
        invalidate();
    }

    public boolean isReflection() {
        return this.mIsReflection;
    }

    /**
     * 设置绘制奇形怪状的形状.
     */
    public void setDrawShape(boolean isDrawShape) {
        mIsDrawShape = isDrawShape;
        invalidate();
    }

    public boolean isDrawShape() {
        return this.mIsDrawShape;
    }

    /**
     * 倒影高度.
     */
    public void setRefHeight(int height) {
        this.mRefHeight = height;
    }

    public int getRefHeight() {
        return this.mRefHeight;
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    private boolean isDrawShapeRadiusRect(RadiusRect radiusRect) {
        if (radiusRect.bottomLeftRadius <= 0 && radiusRect.bottomRightRadius <= 0 && radiusRect.topLeftRadius <= 0
                && radiusRect.topRightRadius <= 0)
            return false;
        return true;
    }

    /**
     * 获取缓存ID.
     */
    private int getViewCacheID() {
        if (viewIDNum == 0) {
            sViewIDNum++;
            viewIDNum = sViewIDNum;
        }
        return viewIDNum;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (viewIDNum != 0) {
            mBitmapMemoryCache.removeImageCache(viewIDNum + "");
        }
    }

    public Path getShapePath(int width, int height, float radius) {
        return DrawUtils.addRoundPath3(getWidth(), getHeight(), radius);
    }

    @Override
    public void draw(Canvas canvas) {
        try {
            if (canvas != null) {
                if (mIsDrawShape && isDrawShapeRadiusRect(mRadiusRect)) {
                    drawShapePathCanvas(canvas);
                } else {
                    super.draw(canvas);
                }
                /**
                 * 绘制倒影. 4.3 SDK-18,有问题，<br>
                 * 在使用Canvas.translate(dx, dy)会出现BUG. <br>
                 */
                if (Utils.getSDKVersion() == 18) {
                    drawRefleCanvas4_3_18(canvas);
                } else if (Utils.getSDKVersion() == 17) {
                    // 4.2 不需要倒影，绘制有问题，暂时屏蔽.
                    drawRefleCanvas(canvas);
                } else { // 性能高速-倒影(4.3有问题).
                    Log.d(TAG, "this is reflection");
                    drawRefleCanvas(canvas);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 绘制圆角控件. 修复使用clipPath有锯齿问题.
     */
    private void drawShapePathCanvas(Canvas shapeCanvas) {
        if (shapeCanvas != null) {
            int width = getWidth();
            int height = getHeight();
            int count = shapeCanvas.save();
            int count2 = shapeCanvas.saveLayer(0, 0, width, height, null, Canvas.ALL_SAVE_FLAG);
            //
            Path path = getShapePath(width, height, mRadius);
            super.draw(shapeCanvas);
            shapeCanvas.drawPath(path, mShapePaint);
            //
            shapeCanvas.restoreToCount(count2);
            shapeCanvas.restoreToCount(count);
        }
    }

    /**
     * 绘制倒影. 修复原先使用bitmap卡顿的问题.
     */
    private void drawRefleCanvas(Canvas refleCanvas) {
        if (mIsReflection) {
            refleCanvas.save();
            //run 移动到 绘制区域 中间留一个3px空隙
            int dy = getHeight()+3;
            int dx = 0;
            refleCanvas.translate(dx, dy);
            drawReflection(refleCanvas);
            refleCanvas.restore();
        }
    }

    private void drawRefleCanvas4_3_18(Canvas canvas) {
        if (mIsReflection) {
            // 创建一个画布.
            String cacheID = getViewCacheID() + "";
            //
            Bitmap reflectBitmap = mBitmapMemoryCache.getBitmapFromMemCache(cacheID);
            if (reflectBitmap == null) {
                reflectBitmap = Bitmap.createBitmap(getWidth(), mRefHeight, Bitmap.Config.ARGB_8888);
                mBitmapMemoryCache.addBitmapToMemoryCache(cacheID, reflectBitmap);
            }
            Canvas reflectCanvas = new Canvas(reflectBitmap);
            reflectCanvas.drawPaint(mClearPaint); // 清空画布.
            /**
             * 如果设置了圆角，倒影也需要圆角.
             */
            int width = reflectCanvas.getWidth();
            int height = reflectCanvas.getHeight();
            RectF outerRect = new RectF(0, 0, width, height);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            if (mIsDrawShape) {
                reflectCanvas.drawPath(getShapePath(width, height + 50, mRadius), paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            }
            reflectCanvas.saveLayer(outerRect, paint, Canvas.ALL_SAVE_FLAG);
            drawReflection4_3_18(reflectCanvas);
            reflectCanvas.restore();
            canvas.save();
            int dy = getHeight();
            int dx = 0;
            canvas.translate(dx, dy);
            canvas.drawBitmap(reflectBitmap, 0, 0, null);
            canvas.restore();
        }
    }

    public void drawReflection4_3_18(Canvas canvas) {
        canvas.save();
        canvas.clipRect(0, 0, getWidth(), mRefHeight);
        canvas.save();
        canvas.scale(1, -1);
        canvas.translate(0, -getHeight());
        super.draw(canvas);
        canvas.restore();
        canvas.drawRect(0, 0, getWidth(), mRefHeight, mRefPaint);
        canvas.restore();
    }

    /**
     * 绘制倒影.
     * <p/>
     * run 提问 绘制倒影和控件之间的间隙
     */
    public void drawReflection(Canvas reflectionCanvas) {
        int width = getWidth();
        int height = getHeight();
        int count = reflectionCanvas.save();
        int count2 = reflectionCanvas.saveLayer(0, 0, width, mRefHeight, null, Canvas.ALL_SAVE_FLAG);
        //
        reflectionCanvas.save();
        reflectionCanvas.clipRect(0, 0, getWidth(), mRefHeight);
        reflectionCanvas.save();
        reflectionCanvas.scale(1, -1);
        reflectionCanvas.translate(0, -getHeight());
        super.draw(reflectionCanvas);
        if (mIsDrawShape) {
            Path path = getShapePath(width, height, mRadius);
            reflectionCanvas.drawPath(path, mShapePaint);
        }
        reflectionCanvas.restore();

        reflectionCanvas.drawRect(0, 0, getWidth(), mRefHeight, mRefPaint);
        reflectionCanvas.restore();
        //
        reflectionCanvas.restoreToCount(count2);
        reflectionCanvas.restoreToCount(count);
    }

	/*
     * 设置/获取-圆角的角度.
	 */

    public void setRadius(float radius) {
        setRadius(radius, radius, radius, radius);
    }

    public void setRadius(float tlRadius, float trRaius, float blRadius, float brRaius) {
        setRadiusRect(new RadiusRect(tlRadius, trRaius, blRadius, brRaius));
    }

    public void setRadiusRect(RadiusRect rect) {
        mRadiusRect = rect;
        invalidate();
    }

    public RadiusRect getRadiusRect() {
        return this.mRadiusRect;
    }

    public class RadiusRect {
        public float topLeftRadius;
        public float topRightRadius;
        public float bottomLeftRadius;
        public float bottomRightRadius;

        public RadiusRect() {
        }

        public RadiusRect(float tlRadius, float trRaius, float blRadius, float brRaius) {
            topLeftRadius = tlRadius;
            topRightRadius = trRaius;
            bottomLeftRadius = blRadius;
            bottomRightRadius = brRaius;
        }

        public void set(float tlRadius, float trRaius, float blRadius, float brRaius) {
            topLeftRadius = tlRadius;
            topRightRadius = trRaius;
            bottomLeftRadius = blRadius;
            bottomRightRadius = brRaius;
        }

        public RadiusRect get() {
            return new RadiusRect(topLeftRadius, topRightRadius, bottomLeftRadius, bottomRightRadius);
        }
    }

}