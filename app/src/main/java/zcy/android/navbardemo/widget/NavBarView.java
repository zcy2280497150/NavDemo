package zcy.android.navbardemo.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 新增过度动画 切换大于1个item 需要新增前段动画，
 * 2280497150@qq.com 有BUG请联系我
 * @author ZCY
 * @date 2020-07-07
 */
public class NavBarView extends View {

//    public static final float CUBIC = 0.552284749831F;//3介贝塞尔曲线，画1/4圆弧的常量（0，1）（CUBIC， 1）（1， CUBIC）（1， 0）

    public static final long MIN_DURATION = 200L;//切换动画最小时长 毫秒

    public static final @ColorInt int BG_COLOR_DEFAULT = Color.WHITE;

    private int position;//选中的下标

    private int oldPosition;//在此之前的选中下标

    private float spreadPercent;//切换的百分比//动画开始 1 - 0 动画结束

    private float translationPercent;//平移百分比 考虑到跳多个item切换，中间需要增加平移动画 开始 1 - 0 结束

    private long duration;//切换动画持续时间

    private long translationDuration;//切换动画持续时间

    private float downX;//按下点的坐标，用于判断是否是有效点击

    private List<Bitmap[]> iconBitmapList;

    private OnSelectListener onSelectListener;//选中监听

    private Path path;
    private Paint paint;

    public NavBarView(Context context) {
        super(context);
        init(context, null);
    }

    public OnSelectListener getOnSelectListener() {
        return onSelectListener;
    }

    public void setOnSelectListener(OnSelectListener onSelectListener) {
        this.onSelectListener = onSelectListener;
        onSelectListener.onSelect(position);
    }

    public NavBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        iconBitmapList = new ArrayList<>();
        path = new Path();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(BG_COLOR_DEFAULT);
        setDuration(MIN_DURATION);
        setTranslationDuration(MIN_DURATION);
    }

    public void setBgColorInt(@ColorInt int colorInt){
        paint.setColor(colorInt);
        postInvalidate();
    }

    public void setBgColorRes(@ColorRes int colorRes){
        paint.setColor(getResources().getColor(colorRes));
        postInvalidate();
    }

    public long getTranslationDuration() {
        return translationDuration;
    }

    public void setTranslationDuration(long translationDuration) {
        this.translationDuration = Math.max(translationDuration, MIN_DURATION);
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = Math.max(duration, MIN_DURATION);
    }

    public NavBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void startAnimation(){
        // Item动画
        AnimatorSet animatorSet = new AnimatorSet();
        if (Math.abs(position - oldPosition) > 1){
            ValueAnimator animator = ValueAnimator.ofFloat(2F, 0F).setDuration(duration + translationDuration);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    if (value > 1.5F){
                        spreadPercent = value - 1F;
                        translationPercent = 1F;
                    }else if (value > 0.5F){
                        spreadPercent = 0.5F;
                        translationPercent = value - 0.5F;
                    }else {
                        spreadPercent = value;
                        translationPercent = 0F;
                    }
                    postInvalidate();
                }
            });
            animatorSet.play(animator);
        }else {
            ValueAnimator animator = ValueAnimator.ofFloat(1F, 0F)
                    .setDuration(duration);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    spreadPercent = (float) animation.getAnimatedValue();
                    postInvalidate();
                }
            });

            animatorSet.play(animator);
        }
        animatorSet.addListener(new AnimatorListenerAdapter(){
            @Override
            public void onAnimationEnd(Animator animation) {
                if (null != onSelectListener)onSelectListener.onSelect(position);
            }
        });
        animatorSet.start();
    }

    public void addItem(Bitmap selectBitmap, Bitmap normalBitmap){
        iconBitmapList.add(new Bitmap[]{selectBitmap, normalBitmap});
        postInvalidate();
    }

    public void addItem(@DrawableRes int selectId, @DrawableRes int normalId){
        Drawable sd = getResources().getDrawable(selectId);
        Drawable nd = getResources().getDrawable(normalId);
        Bitmap sbmp = null;
        Bitmap nbmp = null;
        if (sd instanceof BitmapDrawable){
            sbmp = ((BitmapDrawable) sd).getBitmap();
        }
        if (nd instanceof BitmapDrawable){
            nbmp = ((BitmapDrawable) nd).getBitmap();
        }

        //TODO 此处只解析了BitmapDrawable，可以添加其他资源的解析方式

        addItem(sbmp, nbmp);
    }

    //初始化绘制路径
    private void initPath(){
        path.reset();
        path.moveTo(0F, 0F);
    }

    //添加凹槽
    private void pathAddOC(float x, float r){
        path.lineTo(x - 3F * r, 0F);
        float percent = 1F;
        if (spreadPercent > 0F && Math.abs(position - oldPosition) > 1){
            if (spreadPercent == 0.5F){
                //矩形啥也不需要做
                return;
            }else if (spreadPercent > 0.5F){
                //凹槽逐渐消失，需要得到一个从1到0的变化参数
                percent = (spreadPercent - 0.5F) * 2F;
            }else {
                //凹槽逐渐出现，需要得到一个从0到1的变化参数
                percent = (0.5F - spreadPercent) * 2F;
            }
        }
        path.rQuadTo(r * 1.3F,0F, r * 2F, r * percent);
        path.rCubicTo(r * 0.5F, r * 0.6F * percent, r * 1.5F, r * 0.6F * percent,r * 2F , 0F);
        path.rQuadTo(r * 0.7F,- r * percent, r * 2F, -r * percent);
    }

    //添加圆
    private void pathAddCircle(float x, float r, float itemLen){
        if (spreadPercent > 0F){
            if (Math.abs(position - oldPosition) > 1){
                //跨越两个以上的item
                if (spreadPercent == 0.5F){
                    //第二阶段 平移
                    path.addCircle(x, r * 0.5F, r, Path.Direction.CW);
                } else if (spreadPercent > 0.5F){
                    //第一阶段 入
                    path.addCircle(x, r * (1F - spreadPercent), r, Path.Direction.CW);
                }else{
                    //第三阶段 出
                    path.addCircle(x, r * spreadPercent, r, Path.Direction.CW);
                }
            }else {
                //相邻的item切换
                path.addCircle(itemLen * (position + 0.5F) + (position > oldPosition ? 1 : -1) * 0.5F * itemLen * spreadPercent, 2F * r * spreadPercent, r, Path.Direction.CW);
                path.addCircle(itemLen * (oldPosition + 0.5F) + (position > oldPosition ? -1 : 1) * 0.5F * itemLen * (1 - spreadPercent), 2F * r * (1 - spreadPercent), r, Path.Direction.CW);
            }
        }else {
            path.addCircle(x, 0F, r, Path.Direction.CW);
        }
    }

    //闭合路径 形成完整图形
    private void closePath(){
        path.lineTo(getWidth(), 0F);
        path.rLineTo(0F, getHeight());
        path.rLineTo(-getWidth(), 0F);
        path.close();
    }

    //计算中间一段动画执行过程中相对于目标点的移动距离
    private float calcDX2Percent(){
        int abs = Math.abs(position - oldPosition);
        if (abs > 1){
            return (position > oldPosition ? -1 : 1) * (abs - 1) * translationPercent;
        }
        return 0F;
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();

        if (null == iconBitmapList || iconBitmapList.isEmpty()){
            canvas.drawRect(0F, 0F, getWidth(), getHeight(), paint);
            return;
        }

        float itemLen = width / iconBitmapList.size();

        //当前选中所在点X
        float x = itemLen * (position + 0.5F);

        //进出动画执行距离 x方向
        float dx1 = (position > oldPosition ? -1 : 1) * spreadPercent * itemLen;

        //平移动画执行距离 x方向
        float dx2 = calcDX2Percent() * itemLen;

        initPath();

        pathAddOC(x + dx1 + dx2, Math.min(itemLen * 0.5F, height * 0.5F));

        closePath();

        pathAddCircle(x + dx1 + dx2, Math.min(itemLen * 0.5F, height * 0.5F), itemLen);

        canvas.drawPath(path, paint);

        for (int i = 0 ; i < iconBitmapList.size() ; i++){
            Bitmap[] bitmaps = iconBitmapList.get(i);

            float y = (height - bitmaps[1].getHeight()) * 0.5F;//Y坐标
            if (i == position && spreadPercent < 0.5F){
                canvas.drawBitmap(bitmaps[0], itemLen * i + (itemLen - bitmaps[0].getWidth()) * 0.5F,  y - (0.5F - spreadPercent) * height, paint);
            }else if (i == oldPosition && spreadPercent > 0.5F){
                canvas.drawBitmap(bitmaps[1], itemLen * i + (itemLen - bitmaps[0].getWidth()) * 0.5F, y - height * (spreadPercent - 0.5F), paint);
            }else {
                canvas.drawBitmap(bitmaps[1], itemLen * i + (itemLen - bitmaps[0].getWidth()) * 0.5F, y, paint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (1 == event.getPointerCount()){
            //单点
            if (MotionEvent.ACTION_DOWN == event.getAction()){
                //按下
                downX = event.getX();
            }else if (MotionEvent.ACTION_UP == event.getAction()){
                //抬起
                float upY = event.getY();
                if (upY > 0F && upY < getHeight()){
                    float upX = event.getX();
                    float width = getWidth();
                    float itemLen = width / iconBitmapList.size();
                    int position = (int) (downX / itemLen);
                    if (upX > itemLen * position && upX < itemLen * (position + 1)){
                        changePosition(position);
                    }
                }
            }
        }
        super.onTouchEvent(event);
        return true;
    }

    /**
     * @hide
     * @param position
     */
    private void changePosition(int position) {
        if (this.position == position || 0F != spreadPercent)return;
        this.oldPosition = this.position;
        this.position = position;
        startAnimation();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    public interface OnSelectListener{
        void onSelect(int position);
    }
}
