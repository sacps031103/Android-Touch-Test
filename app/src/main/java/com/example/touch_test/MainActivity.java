package com.example.touch_test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {
    private static final int CIRCLE_RADIUS_DP = 20;
    int height=0;//螢幕高度
    int width=0;//螢幕闊度
    String heightString="";//螢幕高度2
    String widtStringh="";//螢幕闊度2
    int TouchTime;//時間
    //觸控點
    private Paint paint = new Paint();
    private Paint TestPaint = new Paint();
    private Paint drawText = new Paint();
    int TestQuantity = 0;
    //觸控點 X Y 坐標
    int number=0;//觸發點數量
    private List<Point> pointer = new ArrayList<>();//點
    //觸控點 是否被觸發
    ArrayList<Boolean> ListBoolean= new ArrayList<Boolean>();
    boolean MultiTouch=false;//單點觸控 & 多點觸控
    boolean Touchpass=false;//單點是否通過
    boolean MultiTouchpass=false;//多點是否通過
    private int totalTouches;
    private int circleRadius;
    boolean TouchUP=false;//手指是否離開螢幕
    //源始值
    int MAX_ANGLE = 100; //觸控點觸發極限 的半徑
    int MultiTouch_MAX = 5;//多點觸控通過值
    int Touch=4;//觸發點數量
    int Time=20;//顯示對話方塊時間
    //單點觸控
    private List<Point> pointerLocations = new ArrayList<>();//點
    private int[] pointerColors = new int[] { 0xFFFFFFFF, 0xFFFF4040, 0xFF40FF40, 0xFF4040FF, 0xFFFF40FF, 0xFFFFFF40, 0xFF40FFFF };//內顏色
    private int[] pointerColorsDark = new int[] { 0xFFA0A0A0, 0xFFA00000, 0xFF00A000, 0xFF0000A0, 0xFFA000A0, 0xFFA0A000, 0xFF00A0A0 };//外顏色
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //取得螢幕數據 高 闊
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        if(checkDeviceHasNavigationBar(getApplicationContext())){
            width = size.x+getNavigationBarHeight(getApplicationContext());
        }else {
            width = size.x;
        }
        height = size.y;
        //String 高 闊 是為了減去最後一個數字 使得不會因為當像數太大觸發點過於接觸螢幕邊緣
        widtStringh =Integer.toString(width);
        heightString=Integer.toString(height);
        /**
         *   _______________________設定觸發點坐標___________________________
         */
        int mak = Touch/2;
        if(mak==0)mak=1;//防止除以0
        int ScreenX = (Integer.parseInt(widtStringh.substring(0,widtStringh.length()-1)))/(mak);//X坐標是減去最後一個數字
        int ScreenY = (Integer.parseInt(heightString.substring(0,heightString.length()-1)))/(mak);//Y坐標是減去最後一個數字
        int ScreenLongX =  width-ScreenX;//X最大值
        int ScreenLongY =  height-ScreenY;//Y最大值
        int LongX =ScreenLongX - ScreenX;//X從最小到最大的值
        int LongY =ScreenLongY - ScreenY;//Y從最小到最大的值
        int paintLongX = LongX / Touch;//X每隔一個點的距離
        int paintLongY = LongY / Touch;//Y每隔一個點的距離
        for(int x=0 ;x<=Touch;x++){
            for(int y=0 ;y<=Touch;y++) {
                if(y%2==0){//當是雙數切换模式,一前一後
                    pointer.add(new Point());//新增點
                    ListBoolean.add(false);//新增判斷
                    pointer.get(number).x= ScreenX + x * paintLongX;//乘以每隔一個點的距離再加上起點
                    pointer.get(number).y= ScreenY + y * paintLongY;//乘以每隔一個點的距離再加上起點
                }else {
                    if(x>Touch-1){//單數是以X每隔一個點的距離的一半再開始新增,所以到最後會大於螢幕而要停止
                        continue;
                    }
                    pointer.add(new Point());//新增點
                    ListBoolean.add(false);//新增判斷
                    pointer.get(number).x= (ScreenX + x * paintLongX)+(paintLongX/2);//以X每隔一個點的距離的一半再開始新增再加上起點,再乘以每隔一個點的距離
                    pointer.get(number).y= ScreenY + y * paintLongY;//乘以每隔一個點的距離再加上起點
                }
                Log.v(TAG, "\n");
                Log.v(TAG, "X "+pointer.get(number).x);
                Log.v(TAG, "\n");
                Log.v(TAG, "Y "+ pointer.get(number).y);
                Log.v(TAG, "\n");
                number++;//點的數量
            }
        }
        /**
         *   ____________________________________________________________________________
         */
        handlerTouch.post(runnableTouch);//開始計時
        TouchTime=0;//開始計時時間為0
        init();//準備用到的點
        setContentView( new View(this){
            @Override
            protected void onDraw(Canvas canvas) {
                // TODO Auto-generated method stub
                super.onDraw(canvas);
                canvas.drawColor(Color.BLACK);
                if(MultiTouch){//測試多點
                    for (int i = 0; i < totalTouches; i++) {//有多少隻手指
                        //劃出觸控點
                        Point p = pointerLocations.get(i);
                        paint.setColor(pointerColorsDark[i % pointerColorsDark.length]);
                        canvas.drawLine(0, p.y, canvas.getWidth(), p.y, paint);
                        canvas.drawLine(p.x, 0, p.x, canvas.getHeight(), paint);
                        canvas.drawCircle(p.x, p.y, circleRadius * 5 / 4, paint);
                        paint.setColor(pointerColors[i % pointerColors.length]);
                        canvas.drawCircle(p.x, p.y, circleRadius, paint);
                    }
                    canvas.drawText(String.valueOf(totalTouches), width/2 ,height/2 ,drawText);
                    drawText.setTextSize(300);
                    drawText.setColor(Color.CYAN);
                    drawText.setStrokeWidth(2);
                    drawText.setTextAlign(Paint.Align.CENTER);
                    if(totalTouches>=MultiTouch_MAX){//如果手指數大過設定值
                        MultiTouchpass=true;
                        handlerTouch.removeCallbacks(runnableTouch);//停止計時
                        back();//完成
                        return;
                    }else {
                        TestQuantity=0;
                        invalidate();//重畫
                    }
                }else {//測試單點
                    for (int i = 0; i < number; i++) {//檢查每一個點是否有通過 ListBoolean true就劃紅色 false就劃白色
                        Point p = pointer.get(i);
                        if(ListBoolean.get(i)) {
                            TestPaint.setColor(pointerColors[2]);
                            TestQuantity++;
                        }else{
                            TestPaint.setColor(pointerColors[1]);
                        }
                        canvas.drawCircle(p.x, p.y, circleRadius * 5 / 4, TestPaint);//劃出觸控點
                    }
                    if (!TouchUP) {//手指是否離開螢幕
                        Point p = pointerLocations.get(0);//只能一隻手指
                        //劃出觸控點
                        paint.setColor(pointerColorsDark[0 % pointerColorsDark.length]);
                        canvas.drawLine(0, p.y, canvas.getWidth(), p.y, paint);
                        canvas.drawLine(p.x, 0, p.x, canvas.getHeight(), paint);
                        canvas.drawCircle(p.x, p.y, circleRadius * 5 / 4, paint);
                        paint.setColor(pointerColors[0 % pointerColors.length]);
                        canvas.drawCircle(p.x, p.y, circleRadius, paint);
                    } else {
                        for (int i = 0; i < number; i++) {//手指離開螢幕 重新來過
                            ListBoolean.set(i, false);
                            TestQuantity=0;
                        }
                    }
                    int passX=0;
                    for (int i = 0; i < number; i++) {//檢查每一個點是否有通過 ListBoolean
                        if(ListBoolean.get(i)){
                            passX++;//通過點
                        }
                    }
                    canvas.drawText(String.valueOf(TestQuantity), width/2 ,height/2 ,drawText);
                    drawText.setTextSize(300);
                    drawText.setColor(Color.CYAN);
                    drawText.setStrokeWidth(2);
                    drawText.setTextAlign(Paint.Align.CENTER);
                    if (passX>=number){//點都通過了
                        showOneDialog_multi_touch();//跳出對話方塊
                        MultiTouch=true;//換多點
                        Touchpass=true;
                        //完成
                    }
                    TestQuantity=0;
                    invalidate();//重畫
                }
            }
        });
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerIndex = event.getActionIndex();
        int action = event.getActionMasked();
        int numTouches = event.getPointerCount();
        if(MultiTouch) {//測試單點
            if (numTouches > totalTouches) {//記錄手指數量
                totalTouches = numTouches;
            }
            for (int i = 0; i < numTouches; i++) {
                Log.v(TAG, "\n");
                Log.v(TAG, String.valueOf(width));
                Log.v(TAG, "\n");
                Log.v(TAG, String.valueOf(height));
                Log.v(TAG, "\n");
                //存入點的坐標
                pointerLocations.get(i).x = (int) event.getX(i);
                pointerLocations.get(i).y = (int) event.getY(i);
                Log.v(TAG, String.valueOf(event.getX(i)));
                Log.v(TAG, "\n");
                Log.v(TAG, String.valueOf(event.getY(i)));
            }
        }else {//測試多點
            TouchUP=false;//手指在螢幕上
            //存入點的坐標
            pointerLocations.get(0).x = (int) event.getX(0);
            pointerLocations.get(0).y = (int) event.getY(0);
            int x = (int) event.getX(0);
            int y = (int) event.getY(0);
            //檢查點是否滿足條件
            for (int i = 0; i < number; i++) {
                Log.v(TAG, "\n");
                Log.v(TAG, "X: "+i+" "+String.valueOf(MAX_ANGLE+ pointer.get(i).x+"..>.."+x+"..<.."+pointer.get(i).x));
                Log.v(TAG, "\n");
                Log.v(TAG, "Y: "+i+" "+String.valueOf(MAX_ANGLE+pointer.get(i).y+"..>.."+y+"..<.."+pointer.get(i).y));
                Log.v(TAG, "\n");
                if(MAX_ANGLE+pointer.get(i).x >= x && x >= pointer.get(i).x-MAX_ANGLE && MAX_ANGLE+pointer.get(i).y >= y && y >= pointer.get(i).y-MAX_ANGLE) {//如果坐標跟圓心坐標 小於觸控點觸發極限的半徑
                    ListBoolean.set(i,true);
                }
            }
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                totalTouches = numTouches;//檢查多點觸控數量
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:{
                TouchUP=true;//手指離開
            }
            case MotionEvent.ACTION_CANCEL: {
                //將指數下移，並將最後一個指數上調
                if (pointerIndex < numTouches - 1) {
                    Point p = pointerLocations.get(pointerIndex);
                    pointerLocations.get(numTouches - 1).x = p.x;
                    pointerLocations.get(numTouches - 1).y = p.y;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE:
                break;
            default:
                break;
        }


        return true;
    }
    private void init() {
        circleRadius = (int)(CIRCLE_RADIUS_DP * getResources().getDisplayMetrics().density);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(3);
        final int maxPointers = 100;
        for (int i = 0; i < maxPointers; i++) {
            pointerLocations.add(new Point());
        }
    }
    private void back() {
        finish();
    }

    private void showOneDialog() {
        final android.app.AlertDialog build = new android.app.AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.splash_dialog, null);
        build.setView(view, 0, 0, 0, 0);
        build.setCanceledOnTouchOutside(false);
        build.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyEvent.KEYCODE_BACK)
                {
                    handlerTouch.post(runnableTouch);//開始計時
                    TouchTime=0;//開始計時時間為0
                    build.dismiss();
                    return false;
                }
                else
                {
                    return false; //默认返回 false，这里false不能屏蔽返回键，改成true就可以了
                }
            }
        });
        build.show();
        int width = getWindowManager().getDefaultDisplay().getWidth();
        WindowManager.LayoutParams params = build.getWindow().getAttributes();
        params.width = width - (width / 6);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        build.getWindow().setAttributes(params);
        Button leftButton = (Button) view.findViewById(R.id.splash_dialog_left);
        Button rightButton = (Button) view.findViewById(R.id.splash_dialog_right);
        TextView warnMessage = (TextView) view.findViewById(R.id.warnmessage);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlerTouch.removeCallbacks(runnableTouch);//停止計時
                finish();
            }
        });
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlerTouch.post(runnableTouch);//開始計時
                TouchTime=0;//開始計時時間為0
                build.dismiss();
            }
        });
    }
    Handler handlerTouch=new Handler();
    Runnable runnableTouch=new Runnable() {
        @Override
        public void run() {
            if (  TouchTime > Time){//時間大於20秒
                showOneDialog();//跳出對話方塊
                handlerTouch.removeCallbacks(runnableTouch);//停止計時
            }else {
                TouchTime++;//時間+1
                Log.v(TAG, "1000\n"+TouchTime);
                handlerTouch.postDelayed(this, 1000);
            }
        }
    };
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:    //返回键
                return true;   //这里由于break会退出，所以我们自己要处理掉 不返回上一层
        }
        return super.onKeyDown(keyCode, event);
    }
    private void showOneDialog_multi_touch() {
        final android.app.AlertDialog build = new android.app.AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.splash_dialog_multi_touch, null);
        build.setView(view, 0, 0, 0, 0);
        build.show();
        int width = getWindowManager().getDefaultDisplay().getWidth();
        WindowManager.LayoutParams params = build.getWindow().getAttributes();
        params.width = width - (width / 6);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        build.getWindow().setAttributes(params);
        Button Button = (Button) view.findViewById(R.id.splash_dialog);
        TextView warnMessage = (TextView) view.findViewById(R.id.warnmessage);
        warnMessage.setText("請吧"+MultiTouch_MAX+"隻手指按在螢幕上");
        Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                build.dismiss();
            }
        });
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if(hasFocus){
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }
    private int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height","dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        Log.v("dbw", "Navi height:" + height);
        return height;
    }
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {

        }
        return hasNavigationBar;
    }
}