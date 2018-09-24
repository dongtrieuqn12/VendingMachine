package com.felhr.serialportexample.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.serialportexample.API.GetAccessToken;
import com.felhr.serialportexample.API.SaleTransaction;
import com.felhr.serialportexample.Model.RequestInvoices.DeliveryDetail;
import com.felhr.serialportexample.Model.RequestInvoices.InvoiceDetail;
import com.felhr.serialportexample.Model.RequestInvoices.RequestCreateInvoices;
import com.felhr.serialportexample.Model.ReturnInvoices.ReturnInvoices;
import com.felhr.serialportexample.Others.Constants;
import com.felhr.serialportexample.MifareDesfire.CardData;
import com.felhr.serialportexample.MifareDesfire.DesfireDep;
import com.felhr.serialportexample.MifareDesfire.PocketData;
import com.felhr.serialportexample.R;
import com.felhr.serialportexample.Retrofit2.APIUtils;
import com.felhr.serialportexample.Retrofit2.SOService;
import com.felhr.serialportexample.SQLite.DatabaseHelperConnectKiot;
import com.felhr.serialportexample.SQLite.DatabaseHelperTransaction;
import com.felhr.serialportexample.SQLite.KiotVietModel;
import com.felhr.serialportexample.SQLite.TransactionModel;
import com.felhr.serialportexample.Others.UsbService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity {


    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private UsbService usbService;
    private MyHandler mHandler;
    private NfcAdapter mNfcAdapter;

    private String display;


    private String amount;
    private String index;

    private ImageView wave,arrow,card;

    TextView turtorial;

    AlertDialog alerDialog;


    //Card infor
    CardData cardData;
    PocketData pocketData;
    DesfireDep desfireDep;
    String PocketID;

    android.support.v7.widget.Toolbar toolbar;

    //token
    public static String accessTokenKiot;
    public static String accessTokenMCP;
    AlertDialog alert;

    //
    private static int state_tap = 0;
    private static int load_sqlite = 0;

    //for SQLite
    public DatabaseHelperTransaction databaseHelperTransaction;
    public TransactionModel transactionModel;

    private DatabaseHelperConnectKiot databaseHelperConnectKiot;

    String branchIds,SoldById;

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_final);

        InitObjectAndView();

        TakeTokenTopUp();
        TakeAccessToken_KiotViet();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                TakeTokenTopUp();
                TakeAccessToken_KiotViet();
            }
        },600*1000,600*1000);

        if(load_sqlite == 0){
            LoadDataSqlite();
            load_sqlite++;
        }

    }

    private void TakeAccessToken_KiotViet(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String client_id = preferences.getString("client_id", getString(R.string.client_id));
        String client_secret = preferences.getString("client_secret",getString(R.string.client_secret));
        branchIds = preferences.getString("idChinhanh", getString(R.string.idChinhanh));
        SoldById = preferences.getString("idsold",getString(R.string.idsold));
        GetAccessToken getAccessToken = new GetAccessToken("scopes=PublicApi.Access&grant_type=client_credentials&client_id=" + client_id + "&client_secret=" + client_secret);
        getAccessToken.execute();
    }

    private void InitObjectAndView(){
        databaseHelperTransaction = new DatabaseHelperTransaction(this);
        databaseHelperConnectKiot = new DatabaseHelperConnectKiot(this);
        transactionModel = new TransactionModel();
        alerDialog = new AlertDialog.Builder(this).create();
        alert = new AlertDialog.Builder(this).create();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mHandler = new MyHandler(this);
        wave = findViewById(R.id.image);
        arrow = findViewById(R.id.arrow);
        pocketData = new PocketData();
        cardData = new CardData();
        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        //init animation
//        turtorial = findViewById(R.id.turtorial);
        card = findViewById(R.id.card);
        card.setVisibility(View.GONE);
        SetAnimation(1500);
        SetAnimForArrow();
    }

    private RequestCreateInvoices CreateRequestInvoices(int index){
        KiotVietModel kiotVietModel = databaseHelperConnectKiot.GetIndex(index);
        RequestCreateInvoices requestCreateInvoices = new RequestCreateInvoices();
        Toast.makeText(this,branchIds,Toast.LENGTH_SHORT).show();
        requestCreateInvoices.setBranchId(branchIds);
        requestCreateInvoices.setBranchName("Chi nhánh trung tâm");
        requestCreateInvoices.setTotalPayment(amount);
        requestCreateInvoices.setMethod("Card");
        requestCreateInvoices.setSoldById(SoldById);
        requestCreateInvoices.setUsingCod(false);
        requestCreateInvoices.setStatus(1);
        List<InvoiceDetail> invoiceDetailList = new ArrayList<>();
        InvoiceDetail invoiceDetail = new InvoiceDetail();
        invoiceDetail.setProductCode(kiotVietModel.getProductCode());
        invoiceDetail.setPrice(amount);
        invoiceDetail.setQuantity(String.valueOf(1));
        invoiceDetail.setDiscount(0);
        invoiceDetail.setDiscountRatio(0);
        invoiceDetailList.add(invoiceDetail);
        requestCreateInvoices.setInvoiceDetails(invoiceDetailList);
        return requestCreateInvoices;
    }

    private void SendRequestCreateInvoices(){
        APIUtils apiUtils = new APIUtils("https://public.kiotapi.com/");
        SOService mService = apiUtils.getSOService();
        mService.getReturnInvoicesAfterCreate("Bearer " + accessTokenKiot,CreateRequestInvoices(Integer.parseInt(index))).enqueue(new Callback<ReturnInvoices>() {
            @Override
            public void onResponse(Call<ReturnInvoices> call, final retrofit2.Response<ReturnInvoices> response) {
                if (response.message().equals("OK")) {
                    //do something...
                    transactionModel.setId_invoices(response.body().getId().toString());
                    try {
                        SaleTransaction saleTransaction = new SaleTransaction(CreateRequestSale(pocketData.getPocketID(),amount),accessTokenMCP,MainActivity.this);
                        saleTransaction.execute();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Kiot Viet fail: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call<ReturnInvoices> call, Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Kiot Viet fail connect", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void LoadDataSqlite() {
        this.databaseHelperConnectKiot = new DatabaseHelperConnectKiot(getApplicationContext());
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                progressDialog.setMessage("waiting...");
                progressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                Log.d(Constants.TAG,"start: " + System.currentTimeMillis());
                databaseHelperConnectKiot.clearDatabase();
                for(int i = 0 ; i < 60 ; i ++) {
                    KiotVietModel kiotVietModel = new KiotVietModel();
                    if( i < 6) {
                        kiotVietModel.setProductCode(Constants.productCode[0]);
                        databaseHelperConnectKiot.AddCatelogy(kiotVietModel);
                    } else if ( i < 12) {
                        kiotVietModel.setProductCode(Constants.productCode[1]);
                        databaseHelperConnectKiot.AddCatelogy(kiotVietModel);
                    } else if( i < 18 ) {
                        kiotVietModel.setProductCode(Constants.productCode[2]);
                        databaseHelperConnectKiot.AddCatelogy(kiotVietModel);
                    } else if ( i < 24 ) {
                        kiotVietModel.setProductCode(Constants.productCode[3]);
                        databaseHelperConnectKiot.AddCatelogy(kiotVietModel);
                    } else if ( i < 30 ) {
                        kiotVietModel.setProductCode(Constants.productCode[4]);
                        databaseHelperConnectKiot.AddCatelogy(kiotVietModel);
                    } else if( i < 36 ) {
                        kiotVietModel.setProductCode(Constants.productCode[5]);
                        databaseHelperConnectKiot.AddCatelogy(kiotVietModel);
                    } else if (i < 42) {
                        kiotVietModel.setProductCode(Constants.productCode[6]);
                        databaseHelperConnectKiot.AddCatelogy(kiotVietModel);
                    } else if(i < 48) {
                        kiotVietModel.setProductCode(Constants.productCode[7]);
                        databaseHelperConnectKiot.AddCatelogy(kiotVietModel);
                    } else if(i < 54) {
                        kiotVietModel.setProductCode(Constants.productCode[8]);
                        databaseHelperConnectKiot.AddCatelogy(kiotVietModel);
                    } else {
                        kiotVietModel.setProductCode(Constants.productCode[9]);
                        databaseHelperConnectKiot.AddCatelogy(kiotVietModel);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                Log.d(Constants.TAG,"end: " + System.currentTimeMillis());
                progressDialog.dismiss();
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void TakeTokenTopUp(){
        new AsyncTask<Void, Void, Response>() {
            Response response = null;
            StringBuilder stringBuilder = new StringBuilder();
            @Override
            protected Response doInBackground(Void... voids) {
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, "{\n\t\"username\": \"admin\",\n\t\"password\": \"admin\"\n}");
                Request request = new Request.Builder()
                        .url(Constants.URL_MCP_ACCESS_TOKEN)
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();
                try {
                    response = client.newCall(request).execute();
                } catch (IOException e) {
                    return null;
                }
                return response;
            }
            @Override
            protected void onPostExecute(Response response) {
                super.onPostExecute(response);
                try {
                    accessTokenMCP = response.header("Authorization");
                } catch (NullPointerException e){
                    Log.d(Constants.TAG,e.toString());
                }
            }
        }.execute();
    }

    private void Default(){
        card.setVisibility(View.GONE);
        state_tap = 0;
        alert.dismiss();
        alerDialog.dismiss();
    }

    private void inTapCard(){
        card.setVisibility(View.VISIBLE);
        SetAnimationForCard1();
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SetAnimationForCard2();
                        timer.cancel();
                    }
                });
            }
        },1000);
    }

    private void SetAnimationForCard1(){
        Animation sizingAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_for_card_out);
        AnimationSet animationSet = new AnimationSet(true);
        Animation fadeOut = new AlphaAnimation(0, 1);
        fadeOut.setDuration(1000);
        animationSet.addAnimation(sizingAnimation);
        animationSet.addAnimation(fadeOut);
        card.startAnimation(animationSet);
    }

    private void SetAnimationForCard2(){
        Animation sizingAnimation1 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_for_card_in);
        AnimationSet animationSet1 = new AnimationSet(true);
        animationSet1.addAnimation(sizingAnimation1);
        card.startAnimation(animationSet1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Toast.makeText(this,"Chạm thẻ để kết toán giao dịch",Toast.LENGTH_SHORT).show();
            state_tap = Constants.TAP_FOR_SETTLEMENT;
            final Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    state_tap = Constants.TAP_FOR_START_VEND;
                    timer.cancel();
                }
            },3000);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ResourceAsColor")
    private void NotificationFor_Config(String name){
        final AlertDialog alerDialog_config = new AlertDialog.Builder(this).create();
        alerDialog_config.setTitle("Xin chào: " + name);
        alerDialog_config.setCancelable(false);
        alerDialog_config.setMessage("Mời bạn chọn nội dung cài đặt");
        alerDialog_config.setButton(AlertDialog.BUTTON_POSITIVE,"Setting", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                startActivity(new Intent(MainActivity.this,SettingActivity.class));
            }
        });
        alerDialog_config.setButton(AlertDialog.BUTTON_NEGATIVE,"Reload", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                state_tap = Constants.TAP_FOR_START_VEND;
                dialogInterface.cancel();
                LoadDataSqlite();
            }
        });
        alerDialog_config.setButton(AlertDialog.BUTTON_NEUTRAL,"Sell", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Start_Vend();
                dialogInterface.cancel();
            }
        });
        alerDialog_config.show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(mNfcAdapter.EXTRA_TAG);
        for (String tech : tag.getTechList()){
            if(tech.equals(IsoDep.class.getName())){
                if (intent.hasExtra(mNfcAdapter.EXTRA_TAG)){
                    try {
                        Check_config(intent);
                        if(PocketID.equals(Constants.CARD_CONFIG)){
                            state_tap = Constants.TAP_FOR_CONFIG_VEND;
                        } else {
                            state_tap = Constants.TAP_FOR_START_VEND;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    switch (state_tap) {
                        case Constants.TAP_FOR_START_VEND:
                            Start_Vend();
                            break;
                        case Constants.TAP_FOR_PAY_VEND:
                            try {
                                PaymentAction(intent);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case Constants.TAP_FOR_CONFIG_VEND:
                            //do something
                            NotificationFor_Config("Hồ Đông Triều");
                            break;
                        case Constants.TAP_FOR_SETTLEMENT:
                            //go settlement
                            break;
                    }
                }
            }
        }
    }



    int count = 0;

    private void PaymentAction(Intent intent) throws IOException {
        Tag tag = intent.getParcelableExtra(mNfcAdapter.EXTRA_TAG);
        desfireDep = new DesfireDep(tag);
        desfireDep.connect();
        desfireDep.pocketDataInit(pocketData);
        if(pocketData.getPocketID().equals(PocketID)) {
            if (pocketData.getPocketBalance() >= Long.valueOf(amount)) {
                desfireDep.selectApp("0200DF");
                desfireDep.authenDivInp("14", "04", Constants.DIVINP);
                desfireDep.payment(amount);
                usbService.write((Constants.APPROVE + index + "\r").getBytes());
                if (desfireDep.commitTransaction()) {
                    //record log.
                    transactionModel.setCard_number(PocketID);
                    transactionModel.setAmount(amount);
                    transactionModel.setId_invoices("00");
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    String currentDateandTime = sdf.format(new Date());
                    transactionModel.setDate_time(currentDateandTime);
                    SendRequestCreateInvoices();
                    usbService.write((Constants.APPROVE + index + "\r").getBytes());
//                    Default();
                } else {
                    Toast.makeText(this, "Commit fail", Toast.LENGTH_SHORT).show();
                    Default();
                }
            } else {
                usbService.write("deny-vend\r".getBytes());
                Default();
                Toast.makeText(this, "Không đủ tiền trong thẻ :'(", Toast.LENGTH_LONG).show();
            }
        } else {
            //Default();
            switch (count) {
                case 0:
                    Toast.makeText(this,"Sai thẻ, mời bạn chọn thẻ đã chạm ban đầu",Toast.LENGTH_SHORT).show();
                    count++;
                    break;
                case 1:
                    Toast.makeText(this,"Sai thẻ, mời bạn chọn thẻ đã chạm ban đầu",Toast.LENGTH_SHORT).show();
                    count++;
                    break;
                case 2:
                    Toast.makeText(this,"Giao dịch đã bị hủy",Toast.LENGTH_SHORT).show();
                    count = 0;
                    Default();
                    break;
            }

        }
    }

    private String CreateRequestSale(String cardNumber,String amount) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mti",Constants.MTI_TOPUP_SALE);
        jsonObject.put("cardNumber",cardNumber);
        jsonObject.put("processCode",Constants.ProCode_SALTE);
        jsonObject.put("amount",amount);
        Random rand = new Random();
        int n = rand.nextInt(899) + 100;
        int n1 = rand.nextInt(899) + 100;
        String traceNumber = String.valueOf(n) + String .valueOf(n1);
        jsonObject.put("traceNumber",traceNumber);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssZ");
        String currentDateandTime = sdf.format(new Date());
        jsonObject.put("time",currentDateandTime.substring(6,12));
        jsonObject.put("date",currentDateandTime.substring(2,6));
        jsonObject.put("terminalId","30100002");
        jsonObject.put("merchantId","111111111111111");
        return jsonObject.toString();
    }

    private void CardAccess(Intent intent) throws IOException {
        KiotVietModel kiotVietModel = databaseHelperConnectKiot.GetIndex(1);
        Log.d(Constants.TAG,kiotVietModel.getProductCode());
        Tag tag = intent.getParcelableExtra(mNfcAdapter.EXTRA_TAG);
        desfireDep = new DesfireDep(tag);
        desfireDep.connect();
        desfireDep.pocketDataInit(pocketData);
        PocketID = pocketData.getPocketID();
        transactionModel.setType(Constants.SALE_TYPE);
        transactionModel.setStatus(Constants.IS_NOT_SYNC);
        usbService.write(Constants.START.getBytes());
    }

    private void Start_Vend(){
        Log.d(Constants.TAG,"start vend");
        KiotVietModel kiotVietModel = databaseHelperConnectKiot.GetIndex(1);
        Log.d(Constants.TAG,kiotVietModel.getProductCode());
        transactionModel.setType(Constants.SALE_TYPE);
        transactionModel.setStatus(Constants.IS_NOT_SYNC);
        usbService.write(Constants.START.getBytes());
    }

    private void Check_config(Intent intent) throws IOException {
        Tag tag = intent.getParcelableExtra(mNfcAdapter.EXTRA_TAG);
        desfireDep = new DesfireDep(tag);
        desfireDep.connect();
        desfireDep.pocketDataInit(pocketData);
        PocketID = pocketData.getPocketID();
    }

    private void SetAnimation(int time){
        Animation sizingAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.anim_nfc_tap);
        sizingAnimation.setDuration(time);
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(time);
        fadeOut.setRepeatCount(Animation.INFINITE);
        fadeOut.setRepeatMode(Animation.RESTART);
        AnimationSet animation = new AnimationSet(true);
        animation.addAnimation(sizingAnimation);
        animation.addAnimation(fadeOut);
        wave.startAnimation(animation);
    }

    private void SetAnimForArrow(){
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(1000);
        fadeOut.setRepeatCount(Animation.INFINITE);
        fadeOut.setRepeatMode(Animation.RESTART);
        AnimationSet animation = new AnimationSet(true);
        animation.addAnimation(fadeOut);
        arrow.startAnimation(animation);
    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentfilter = new IntentFilter[]{};

        mNfcAdapter.enableForegroundDispatch(this, pendingIntent, intentfilter, null);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    @SuppressLint("ResourceAsColor")
    private void NotificationForAction(String name, String amount_pocket){
        alerDialog.setTitle("Xin chào: " + name);
        alerDialog.setMessage("Số dư trên thẻ: " + amount_pocket);
        alerDialog.setCancelable(false);
        TextView textView = new TextView(this);
        textView.setText("Xin mời nhấn phím chọn nước\r\n");
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(R.color.black);
        textView.setTextSize(30);
        textView.layout(4,0,4,0);
        textView.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        textView.setLayoutParams(lp);
        alerDialog.setView(textView);
        alerDialog.setCancelable(false);
        alerDialog.show();
    }

    @SuppressLint("ResourceAsColor")
    private void SetNotificationForPay(String amount_pay){
        alert.setTitle("Số tiền phải trả: " + amount_pay + " VND");
        alert.setMessage("Vị trí đã chọn:");
        Toast.makeText(MainActivity.this,"Tap thẻ để thanh toán",Toast.LENGTH_SHORT).show();
        TextView textView = new TextView(this);
        textView.setText(index);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(R.color.black);
        textView.setTextSize(100);
        textView.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        textView.setLayoutParams(lp);
        alert.setView(textView);
        alert.setCancelable(false);
        inTapCard();
        alert.show();
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;
        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }
        String data;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    data = (String) msg.obj;
                    mActivity.get().display += data;
                    String data1 = mActivity.get().display;
                    if(data1.endsWith("\r\n")){
                        mActivity.get().display = "";
                        data = "";
                        if(data1.startsWith(Constants.ENABLE)){
                            mActivity.get().usbService.write(Constants.START_SESSION.getBytes());
                            Toast.makeText(mActivity.get(),Constants.START_SESSION,Toast.LENGTH_SHORT).show();
                            mActivity.get().NotificationForAction("Hồ Đông Triều",mActivity.get().pocketData.getPocketBalance() + "");
                        }else if(data1.startsWith(Constants.VEND_REQUEST)){
                            //do something
                            mActivity.get().alerDialog.dismiss();
                            String[] s = data1.split("\\s+");
                            mActivity.get().amount = s[1];
                            mActivity.get().index = s[2].replace("\r\n","");
                            MainActivity.state_tap = 1;
                            mActivity.get().SetNotificationForPay(s[1]);
                        }else if( data1.startsWith(Constants.ALREADY_START)){
                            mActivity.get().Default();
                            mActivity.get().NotificationForAction("Hồ Đông Triều",mActivity.get().pocketData.getPocketBalance() + "");
                        } else if(data1.startsWith(Constants.FAIL)){
                            Toast.makeText(mActivity.get(),"Giao dịch xảy ra lỗi, mời tap thẻ để trả lại tiền",Toast.LENGTH_SHORT).show();
                            mActivity.get().Default();
                        } else if(data1.startsWith(Constants.CANCEL)){
                            mActivity.get().Default();
                            Toast.makeText(mActivity.get(),"Giao dịch xảy ra lỗi",Toast.LENGTH_SHORT).show();
                        } else if(data1.startsWith(Constants.SUCCESS)){
                            mActivity.get().Default();
                            Toast.makeText(mActivity.get(),"Giao dịch thành công :D",Toast.LENGTH_SHORT).show();
                        } else if(data1.startsWith("session-complete")){
                            mActivity.get().mHandler.removeMessages(msg.what);
                        }
                    }
                    break;
            }
        }
    }
}