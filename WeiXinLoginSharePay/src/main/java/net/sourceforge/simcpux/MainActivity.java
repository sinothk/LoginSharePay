package net.sourceforge.simcpux;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.sinothk.loginSharePay.weixin.R;
import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.modelpay.JumpToOfflinePay;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import net.sourceforge.simcpux.bean.AppPayRequest;
import net.sourceforge.simcpux.bean.PayResult;

import java.util.Map;


public class MainActivity extends Activity {

    private Button gotoBtn, regBtn, launchBtn, scanBtn, subscribeMsgBtn, subscribeMiniProgramMsgBtn;

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        checkPermission();
        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID, false);

        regBtn = (Button) findViewById(R.id.reg_btn);
        regBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                api.registerApp(Constants.APP_ID);
            }
        });

        gotoBtn = (Button) findViewById(R.id.goto_send_btn);
        gotoBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SendToWXActivity.class));
//		        finish();
            }
        });

        launchBtn = (Button) findViewById(R.id.launch_wx_btn);
        launchBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "launch result = " + api.openWXApp(), Toast.LENGTH_LONG).show();
            }
        });

        subscribeMsgBtn = (Button) findViewById(R.id.goto_subscribe_message_btn);
        subscribeMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SubscribeMessageActivity.class));
//				finish();
            }
        });

        subscribeMiniProgramMsgBtn = (Button) findViewById(R.id.goto_subscribe_mini_program_msg_btn);
        subscribeMiniProgramMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SubscribeMiniProgramMsgActivity.class));
            }
        });


        View jumpToOfflinePay = (Button) findViewById(R.id.jump_to_offline_pay);
        jumpToOfflinePay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int wxSdkVersion = api.getWXAppSupportAPI();
                if (wxSdkVersion >= Build.OFFLINE_PAY_SDK_INT) {
                    api.sendReq(new JumpToOfflinePay.Req());
                } else {
                    Toast.makeText(MainActivity.this, "not supported", Toast.LENGTH_LONG).show();
                }
            }
        });

        findViewById(R.id.jump_to_online_pay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                try {
//                    AppPayRequest vo = new AppPayRequest();
//                    vo.setAppid(Constants.APP_ID);
//
//                    PayReq req = new PayReq();
//                    //req.appId = "wxf8b4f85f3a794e77";  // 测试用appId
//                    req.appId = vo.getAppid();
//                    req.partnerId = vo.getPartnerid();
//                    req.prepayId = vo.getPrepayid();
//                    req.nonceStr = vo.getNoncestr();
//                    req.timeStamp = vo.getTimestamp();
//                    req.packageValue = vo.getPackageValue();
//                    req.sign = vo.getSign();
//                    req.extData = "app data"; // optional
//                    // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
//                    api.sendReq(req);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                Runnable payRunnable = new Runnable() {  //这里注意要放在子线程
                    @Override
                    public void run() {
                        AppPayRequest vo = new AppPayRequest();
                        vo.setAppid(Constants.APP_ID);

                        PayReq req = new PayReq();
                        //req.appId = "wxf8b4f85f3a794e77";  // 测试用appId
                        req.appId = vo.getAppid();
                        req.partnerId = vo.getPartnerid();
                        req.prepayId = vo.getPrepayid();
                        req.nonceStr = vo.getNoncestr();
                        req.timeStamp = vo.getTimestamp();
                        req.packageValue = vo.getPackageValue();
                        req.sign = vo.getSign();
                        req.extData = "app data"; // optional
                        // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
                        api.sendReq(req);
                    }
                };
                Thread payThread = new Thread(payRunnable);
                payThread.start();
            }
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SDK_PAY_FLAG:

                    PayResult payResult = new PayResult((Map<String, String>) msg.obj);
                    //同步获取结果
                    String resultInfo = payResult.getResult();
                    Log.i("Pay", "Pay:" + resultInfo);
                    String resultStatus = payResult.getResultStatus();
                    // 判断resultStatus 为9000则代表支付成功
                    if (TextUtils.equals(resultStatus, "9000")) {

//                        Toast.makeText(PayActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
//                        JumpUtil.overlay(PayActivity.this, OrderFormActivity.class);
//                        finish();
                    } else {
//                        Toast.makeText(PayActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    private static final int SDK_PAY_FLAG = 1001;



    private void checkPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.PERMISSIONS_REQUEST_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSIONS_REQUEST_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(MainActivity.this, "Please give me storage permission!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

}