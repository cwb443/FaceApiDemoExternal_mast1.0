package com.firefly.faceEngine.activity;

import static com.firefly.faceEngine.App.getContext;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.firefly.arcterndemo.R;
import com.firefly.faceEngine.App;
import com.firefly.faceEngine.dblib.SaveInfo;
import com.firefly.faceEngine.dblib.bean.Person;
import com.firefly.faceEngine.dblib.bean.Setting;
import com.firefly.faceEngine.goods.GoodsMessage;
import com.firefly.faceEngine.goods.GoodsUtils;
import com.firefly.faceEngine.goods.MyJsonParser;
import com.firefly.faceEngine.goods.bean.Goods;
import com.firefly.faceEngine.other.FaceInfo;
import com.firefly.faceEngine.utils.MatrixYuvUtils;
import com.firefly.faceEngine.utils.Tools;
import com.firefly.faceEngine.view.FaceView;
import com.firefly.faceEngine.view.GrayInterface;
import com.firefly.faceEngine.view.GraySurfaceView;
import com.firefly.faceEngine.view.LivingInterface;
import com.firefly.faceEngine.view.LivingListener;
import com.intellif.YTLFFaceManager;
import com.intellif.arctern.base.ArcternAttribute;
import com.intellif.arctern.base.ArcternImage;
import com.intellif.arctern.base.ArcternRect;
import com.intellif.arctern.base.AttributeCallBack;
import com.intellif.arctern.base.ExtractCallBack;
import com.intellif.arctern.base.SearchCallBack;
import com.intellif.arctern.base.TrackCallBack;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FaceDetectActivity extends BaseActivity implements TrackCallBack, AttributeCallBack, SearchCallBack, ExtractCallBack {
    private ArcternImage irImage = null;
    private ArcternImage rbgImage = null;
    private TextView txt1, txt2, txt3, texttxt;
    private FaceView faceView;
    private ImageView imgLandmark;
    private GraySurfaceView grayInterface;
    private Map<Long, Person> mMapPeople = new HashMap<>();
    private CountDownTimer mCountDownTimer;
    private YTLFFaceManager YTLFFace = YTLFFaceManager.getInstance();
    private ExecutorService executorService;
    private Future future;
    private FaceInfo faceInfo;

    private Long ID;

    private int view_width, view_height;
    private int frame_width, frame_height;
    private long lastOnAttributeCallBackTime;
    private long lastOnSearchCallBackTime;

    ImageView imageview;

    ImageView goods1,goods2,goods3,goods4,goods5,goods6;
    TextView Name1,Name2,Name3,Name4,Name5,Name6;
    TextView recPrice4,recPrice5,recPrice6;
    TextView description1,description2,description3;
    TextView query1,query2,query3;
    ImageView qrCode;
    TextView tileText;

    ImageView back;

    TextView prePrice1,prePrice2,prePrice3;

    TextView imageText;



    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detect);

        initView();
        getViewWH();
        startCountDownTimer();
        setInfraredFillLight(true); //?????????
        ActionBar actionBar = getSupportActionBar(); if (actionBar != null) { actionBar.hide(); }

        goods1=findViewById(R.id.goods1);
        goods2=findViewById(R.id.goods2);
        goods3=findViewById(R.id.goods3);
        goods4=findViewById(R.id.goods4);
        goods5=findViewById(R.id.goods5);
        goods6=findViewById(R.id.goods6);

        Name1=findViewById(R.id.name_text1);
        Name2=findViewById(R.id.name_text2);
        Name3=findViewById(R.id.name_text3);
        Name4=findViewById(R.id.name_text4);
        Name5=findViewById(R.id.name_text5);
        Name6=findViewById(R.id.name_text6);

        prePrice1=findViewById(R.id.price1);
        prePrice2=findViewById(R.id.price2);
        prePrice3=findViewById(R.id.price3);
        recPrice4=findViewById(R.id.price4);
        recPrice5=findViewById(R.id.price5);
        recPrice6=findViewById(R.id.price6);

        description1 = findViewById(R.id.tv_describe1);
        description2 = findViewById(R.id.tv_describe2);
        description3 = findViewById(R.id.tv_describe3);

        query1=findViewById(R.id.edit_query1);
        query2=findViewById(R.id.edit_query2);
        query3=findViewById(R.id.edit_query3);

        imageview = findViewById(R.id.image_view);

        imageText = findViewById(R.id.image_text);

//        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);


//        tileText=findViewById(R.id.text_title);
//
//        qrCode=findViewById(R.id.qr_code);


    }

    @Override
    protected void onStart() {
        super.onStart();
        noFace();
    }

    public void noFace(){
        List<Setting> saveInformation = SaveInfo.getSaveInformation(context);
        Integer recognition = saveInformation.get(0).getRecognition();
        if (recognition == 0){
            setForecastGoods(0l);
            setRecommendGoods(0l);
        }

    }

    public void getOut(View view){
        this.finish();
    }

    private void initView() {
//        setActionBarTitle(R.string.app_name);
        texttxt = findViewById(R.id.test);
        txt1 = findViewById(R.id.txt1);
        txt2 = findViewById(R.id.txt2);
        txt3 = findViewById(R.id.txt3);
        faceView = findViewById(R.id.faceview);
        imgLandmark = findViewById(R.id.img_landmark);

        grayInterface = findViewById(R.id.grayInterface);
//        grayInterface.setZOrderOnTop(true);
        grayInterface.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        LivingInterface.getInstance().init(this);
        LivingInterface.getInstance().setLivingCallBack(rgbLivingListener);

        GrayInterface.getInstance().init(this);
        GrayInterface.getInstance().setLivingCallBack(irLivingListener);

        YTLFFace.setOnTrackCallBack(this);
        YTLFFace.setOnSearchCallBack(this);
        YTLFFace.setOnAttributeCallBack(this);
    }



    @Override
    protected void onResume() {
        super.onResume();
        List<Person> mPeople = App.getInstance().getDbManager().getPersonList();
        for (Person person : mPeople) {
            mMapPeople.put(person.getId(), person);
        }
    }

    @Override
    public void onExtractFeatureListener(ArcternImage arcternImage, byte[][] features, ArcternRect[] arcternRects) {
    }

    @Override
    public void onTrackListener(ArcternImage arcternImage, long[] trackIds, ArcternRect[] arcternRects) {
        if (arcternRects != null) {
            faceView.setFaces(arcternRects, frame_width, frame_height, view_width, view_height);
        }
    }

    //?????????????????????
    @Override
    public void onAttributeListener(ArcternImage arcternImage, long[] trackIds, ArcternRect[] arcternRects, ArcternAttribute[][] arcternAttributes, int[] landmarks) {
        ArcternAttribute[] attributes = arcternAttributes[0];
        if (attributes.length == 0) {
            return;
        }

        lastOnAttributeCallBackTime = System.currentTimeMillis();
        faceInfo = new FaceInfo(arcternImage, arcternAttributes);
        handleAttribute();
        refreshLogTextView();
    }

    @Override
    public void onSearchListener(ArcternImage arcternImage, long[] trackIds, ArcternRect[] arcternRects, long[] searchIds, int[] landmarks, float[] socre) {
        if (searchIds.length <= 0 || arcternImage == null ||
                faceInfo == null || faceInfo.getFrameId() != arcternImage.frame_id) {
            return;
        }

        lastOnSearchCallBackTime = System.currentTimeMillis();
        faceInfo.setSearchId(searchIds[0]);
        handlePerson();
        handleLandmark(arcternImage, landmarks);
    }

    LivingListener rgbLivingListener = new LivingListener() {
        @Override
        public void livingData(ArcternImage arcternImage) {
            rbgImage = arcternImage;
            frame_width = rbgImage.width;
            frame_height = rbgImage.height;
            if (irImage != null) {
                doDelivery(rbgImage, irImage);
            }
        }
    };

    LivingListener irLivingListener = new LivingListener() {
        @Override
        public void livingData(ArcternImage image) {
            irImage = image;
        }
    };

    // ????????????
    private void doDelivery(final ArcternImage rbgImage, final ArcternImage irImage) {
        if (future != null && !future.isDone()) {
            return;
        }

        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }

        future = executorService.submit(new Runnable() {
            @Override
            public void run() {
                LivingInterface.rotateYUV420Degree90(rbgImage); //??????90???
                LivingInterface.rotateYUV420Degree90(irImage); //??????90???
                MatrixYuvUtils.mirrorForNv21(rbgImage.gdata, rbgImage.width, rbgImage.height);  //rbg ??????????????????
                Tools.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        YTLFFace.doDelivery(rbgImage, irImage);
                    }
                });
            }
        });
    }

    int isNewUser = 0;//0 ?????? 1 ????????? 2 ?????????
    int isUserId = 0;
    // ??????????????????
    private void handlePerson() {

        List<Setting> saveInformation = SaveInfo.getSaveInformation(context);
        Integer recognition = saveInformation.get(0).getRecognition();

        Boolean flag;

        Log.e("TAG", "handlePerson: "+recognition );

        if (recognition == 0)
            flag = false;
        else
            flag = true;

        if (flag){
            Person person = mMapPeople.get(faceInfo.getSearchId());
            if (person != null) {
                faceView.isRed = false;
                Long userId = person.getId();
                String name = person.getName();

                if(isNewUser != 2||isUserId!=userId) {
                    isNewUser = 2;
                    isUserId = Math.toIntExact(userId);
                    setTitle(userId,"Welcome "+name);
                    setForecastGoods(userId);
                    setRecommendGoods(userId);
                }
            } else {
                faceView.isRed = true;
                if(isNewUser != 1) {
                    setForecastGoods(0l);
                    setRecommendGoods(0l);
                    isNewUser = 1;
                    setTitle(0l,"Welcome new customer");
                }
            }
        }
    }

    // ????????????????????????
    private void handleAttribute() {
        ArcternAttribute[] attributes = faceInfo.getAttributes()[0];

        for (int i = 0; i < attributes.length; i++) {
            ArcternAttribute item = attributes[i];
            switch (i) {
                case ArcternAttribute.ArcternFaceAttrTypeEnum.QUALITY://????????????
                    faceInfo.setFaceQualityConfidence(item.confidence);
                    break;

                case ArcternAttribute.ArcternFaceAttrTypeEnum.LIVENESS_IR: //??????
                    faceInfo.setLiveLabel(item.label);
                    faceInfo.setLivenessConfidence(item.confidence);
                    break;

                case ArcternAttribute.ArcternFaceAttrTypeEnum.FACE_MASK: //??????
                    faceInfo.setFaceMask(item.label == ArcternAttribute.LabelFaceMask.MASK);
                    break;

                case ArcternAttribute.ArcternFaceAttrTypeEnum.GENDER: //??????
                    Tools.debugLog("??????:%s", item.toString());
                    faceInfo.setGender(item.label);
                    faceInfo.setGenderConfidence(item.confidence);
                    break;

                case ArcternAttribute.ArcternFaceAttrTypeEnum.AGE: //??????
                    Tools.debugLog("??????:%s", item.toString());
                    faceInfo.setAge((int) item.confidence);
                    break;
            }
        }
    }

    // ????????????????????????
    private void handleLandmark(ArcternImage arcternImage, int[] landmarks) {
        try {
            Bitmap bitmap = Tools.bgr2Bitmap(arcternImage.gdata, arcternImage.width, arcternImage.height);
            Bitmap landmarksBitmap = Tools.drawPointOnBitmap(bitmap, landmarks);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        imgLandmark.setImageBitmap(landmarksBitmap);
                        imgLandmark.setVisibility(View.VISIBLE);
                    } catch (Throwable e) {
                        Tools.printStackTrace(e);
                    }
                }
            });

            //??????bitmap????????????
            //saveBitmap2Jpeg(bitmap);
        } catch (Throwable e) {
            Tools.printStackTrace(e);
        }
    }

    // ??????log
    private void refreshLogTextView(){
        StringBuilder attribute = new StringBuilder();

        if (faceInfo.isFaceMask()) {    //??????????????????????????????????????????
            attribute.append(getString(R.string.ytlf_dictionaries8))
                    .append("\n");

        } else {
            attribute.append(getString(R.string.ytlf_dictionaries9))
                    .append("\n");

            attribute.append(getString(R.string.ytlf_dictionaries21))
                    .append(faceInfo.getFaceQualityConfidence())
                    .append("\n");

            if (faceInfo.isLiveness()) {
                attribute.append(getString(R.string.ytlf_dictionaries19))
                        .append(":")
                        .append(faceInfo.getLivenessConfidence())
                        .append("\n");

            } else if (faceInfo.isNotLiveness()) {
                attribute.append(getString(R.string.ytlf_dictionaries20))
                        .append("\n");
                faceView.isRed = true;
                showText(txt2, "--");
                setVisibility(imgLandmark, View.GONE);
            }

            attribute.append(getString(R.string.ytlf_dictionaries45))
                    .append(faceInfo.getGenderString())
                    .append("\n");

            attribute.append(getString(R.string.ytlf_dictionaries46))
                    .append(faceInfo.getAgeString())
                    .append("\n");
        }

        if (!faceInfo.isFaceMask() && faceInfo.getFaceQualityConfidence() < 0.4) {//?????????????????? < 0.4
            faceView.isRed = false;
            showText(txt1, "--");
            return;
        }

        showText(txt1, attribute);
    }

    protected void showText(TextView txt, CharSequence msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txt.setText(msg);
            }
        });
    }

    protected void setVisibility(View view, int visibility) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(visibility);
            }
        });
    }

    public static String getTimeShort() {
        return new SimpleDateFormat("HH:mm:ss:SSS ").format(new Date());
    }

    private int times=0;
    //??????bitmap???????????? 10???
    private void saveBitmap2Jpeg(Bitmap bitmap){
        if(times > 10){
            return;
        }

        times ++;
        //"/sdcard/firefly/ytlf_v2/"
        String path = YTLFFaceManager.getInstance().getYTIFFacthPath() + "img/" + System.currentTimeMillis() + ".jpg";
        boolean result = Tools.saveBitmap2Jpeg(bitmap, path);
        Tools.debugLog("result=%s, path=%s", result, path);
    }

    //?????????????????????
    private void getViewWH() {
        ViewTreeObserver vto = faceView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                faceView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                view_width = faceView.getWidth();
                view_height = faceView.getHeight();
            }
        });
    }

    //??????
    private void startCountDownTimer() {
        if (mCountDownTimer != null) {
            return;
        }

        mCountDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (lastOnAttributeCallBackTime + 3000 < System.currentTimeMillis()) {
                    showText(txt1, "--");
                    faceView.isRed = false;
                }

                if (lastOnSearchCallBackTime + 3000 < System.currentTimeMillis()) {
                    showText(txt2, "--");
                    setVisibility(imgLandmark, View.GONE);
                }
            }
            @Override
            public void onFinish() {
                cancel();
            }
        };

        mCountDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            if (mCountDownTimer != null) {
                mCountDownTimer.onFinish();
            }
            setInfraredFillLight(false);
        } catch (Exception e) {
            Tools.printStackTrace(e);
        }
    }

    public void setTitle(Long userId,String name){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (userId==0l){
                    imageview.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.bluepeople));
                }else {
                    imageview.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.greenpeople));
                }
                imageText.setText(name);
            }
        });
    }

    //????????????
    public void setForecastGoods(Long userId){

        Log.e("", "setForecastGoods: " );

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<Goods> forecastGoods = getPredict(userId);

                    for (int i = 0;i<forecastGoods.size();i++){
                        int q = i;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap bmp = getURLimage(forecastGoods.get(q).getPicture());
                                Message msg = new Message();
                                GoodsMessage goodsMessage = new GoodsMessage(bmp,forecastGoods.get(q).getName(),forecastGoods.get(q).getPrice(),forecastGoods.get(q).getDescription(),forecastGoods.get(q).getQuantity());
                                msg.what = q+1;
                                msg.obj = goodsMessage;
                                handle.sendMessage(msg);
                            }
                        }).start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void setRecommendGoods(Long userId){
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    ArrayList<Goods> recommendGoods = getRecommendGoods(userId);
                    for (int i=0;i<recommendGoods.size();i++){
                        Log.e("TAG", "onClick: 2" );
                        int q = i;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("TAG", "setRecommendGoods: " );
                                Bitmap bmp = getURLimage(recommendGoods.get(q).getPicture());
                                Message msg = new Message();
                                GoodsMessage goodsMessage = new GoodsMessage(bmp,recommendGoods.get(q).getName(),recommendGoods.get(q).getPrice(),recommendGoods.get(q).getDescription(),recommendGoods.get(q).getQuantity());
                                msg.what = q+4;
                                msg.obj = goodsMessage;
                                handle.sendMessage(msg);
                            }

                        }).start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //??????????????????????????????????????????
    private Handler handle = new Handler() {

        public void handleMessage(Message msg) {
            GoodsMessage goodsMessage = (GoodsMessage)msg.obj;
            switch (msg.what) {
                case 1:
                    goods1.setImageBitmap(goodsMessage.getBitmap());
                    Name1.setText(goodsMessage.getName());
                    description1.setText(goodsMessage.getDescription());
                    prePrice1.setText("$"+goodsMessage.getPrice());
                    query1.setText("Quantity:"+goodsMessage.getQuantity());
                    break;
                case 2:
                    goods2.setImageBitmap(goodsMessage.getBitmap());
                    Name2.setText(goodsMessage.getName());
                    description2.setText(goodsMessage.getDescription());
                    prePrice2.setText("$"+goodsMessage.getPrice());
                    query2.setText("Quantity:"+goodsMessage.getQuantity());
                    break;
                case 3:
                    goods3.setImageBitmap(goodsMessage.getBitmap());
                    Name3.setText(goodsMessage.getName());
                    description3.setText(goodsMessage.getDescription());
                    prePrice3.setText("$"+goodsMessage.getPrice());
                    query3.setText("Quantity:"+goodsMessage.getQuantity());
                    break;
                case 4:
                    goods4.setImageBitmap(goodsMessage.getBitmap());
                    Name4.setText(goodsMessage.getName());
                    recPrice4.setText("$"+goodsMessage.getPrice().toString());
                    break;
                case 5:
                    goods5.setImageBitmap(goodsMessage.getBitmap());
                    Name5.setText(goodsMessage.getName());
                    recPrice5.setText("$"+goodsMessage.getPrice().toString());
                    break;
                case 6:
                    goods6.setImageBitmap(goodsMessage.getBitmap());
                    Name6.setText(goodsMessage.getName());
                    recPrice6.setText("$"+goodsMessage.getPrice().toString());
                    break;

            }
        };

    };

    //????????????
    public Bitmap getURLimage(String url) {
        Bitmap bmp = null;
        try {
            URL myurl = new URL(url);
            // ????????????
            HttpURLConnection conn = (HttpURLConnection) myurl.openConnection();
            conn.setConnectTimeout(6000);//????????????
            conn.setDoInput(true);
            conn.setUseCaches(false);//?????????
            conn.connect();
            InputStream is = conn.getInputStream();//????????????????????????
            bmp = BitmapFactory.decodeStream(is);//??????????????????
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmp;
    }

    //??????????????????
    public ArrayList<Goods> getRecommendGoods(Long userId) throws Exception {
        String recommendGoods = GoodsUtils.getRecommendGoods(userId);
        if (recommendGoods.equals("Network connection failure"))
            Toast.makeText(getContext(),"Network connection failure!",Toast.LENGTH_LONG).show();
        return MyJsonParser.getGoods(recommendGoods);
    }

    //??????????????????
    public ArrayList<Goods> getPredict(Long userId) throws Exception {
        String predictGoods = GoodsUtils.getPredictGoods(userId);

        if (predictGoods.equals("Network connection failure"))
            Toast.makeText(FaceDetectActivity.this,"Network connection failure!",Toast.LENGTH_LONG).show();
        return MyJsonParser.getGoods(predictGoods) ;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus){
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY //(?????????????????????????????????????????????)
                        //???????????????????????????????????????????????????system bar??????????????????system bar?????????
                        //????????????Activity????????????resize???
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // ???????????????????????????
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    public void back(View view){
        showmyDialog();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){

            showmyDialog();
        }
        return super.onKeyDown(keyCode, event);
    }

    Dialog dialog;

    Context context = getContext();

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dialog=new CustomDialog(this,R.style.mystyle,R.layout.dialog2);
        dialog.show();
    }

    private void showmyDialog() {

        dialog=new CustomDialog(this,R.style.mystyle,R.layout.dialog2);
        dialog.show();
    }
    //??????
    class CustomDialog extends Dialog implements
            View.OnClickListener {

        /**
         * ????????????
         **/
        int layoutRes;

        /**
         * ???????????????
         **/
        Context context;

        /**
         * ????????????
         **/
        private Button bt_cancal;

        /**
         * ????????????
         **/
        private Button bt_confirm;

        /**
         * ????????????id
         */
        private int postion_1;
        private EditText password;

        public CustomDialog(Context context) {
            super(context);
            this.context = context;
        }

        /**
         * ??????????????????????????????
         *
         * @param context
         * @param resLayout
         */
        public CustomDialog(Context context, int resLayout) {
            super(context);
            this.context = context;
            this.layoutRes = resLayout;
        }

        /**
         * ???????????????????????????????????????
         *
         * @param context
         * @param theme
         * @param resLayout
         */
        public CustomDialog(Context context, int theme, int resLayout) {
            super(context, theme);
            this.context = context;
            this.layoutRes = resLayout;
        }


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // ????????????
            this.setContentView(layoutRes);
            // ??????id??????????????????????????????
            bt_cancal = (Button) findViewById(R.id.id_cancel_btn);
            bt_confirm = (Button) findViewById(R.id.id_comfirm_btn);
            password=(EditText)findViewById(R.id.id_password);

            // ????????????????????????????????????
            bt_cancal.setOnClickListener(this);
            bt_confirm.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            int id = v.getId();// ????????????
            if (id == R.id.id_comfirm_btn ) {// ??????

                if (password.getText().toString().equals("123456")){
                    dialog.dismiss();
                    finish();
                }else{
                    Toast.makeText(context,"Wrong Password",Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }


            }else {
                // ????????????

                dialog.dismiss();

            }


        }
    }

}
