package org.duckdns.fool.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import org.duckdns.fool.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SmsListener extends BroadcastReceiver {
    private static final OkHttpClient client = new OkHttpClient();
    private static ExecutorService executor = Executors.newFixedThreadPool(3);

    private static String senderRegex = ".*";
    private static String url = "http://server.cc/sms";

    public void updateSetting(Context context, Intent intent){
        String sender_regex_name = context.getString(R.string.sender_regex_name);
        String http_url_name = context.getString(R.string.http_url_name);

        try{  //exception may lead to app crash !!
            if(intent.getExtras().containsKey(sender_regex_name)){
                senderRegex = intent.getExtras().get(sender_regex_name).toString();;
                System.out.printf("regex: %s\n", senderRegex);
            }
            if(intent.getExtras().containsKey(http_url_name)){
                url = intent.getExtras().get(http_url_name).toString().trim();
                System.out.printf("url: %s\n", url);
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action_setting_update_name = context.getString(R.string.action_setting_update_name);
        if(action_setting_update_name.equals(intent.getAction())){
            updateSetting(context, intent);
        }
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            convertMessage(context,intent);
        }
    }

    public void convertMessage(Context context, Intent intent){
        final String phoneNumber = getPhoneNumber(context);

        Map<String, List<String>> map = new HashMap<>();
        for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
            String sender = smsMessage.getOriginatingAddress();
            if(sender == null || !sender.matches(senderRegex)) {
                System.out.printf("The sender phone no doesn't match, regex: %s, sender: %s\n", senderRegex, sender);
                continue; // filter sms message based on regex
            }
            String message = smsMessage.getMessageBody();
            if(!map.containsKey(sender)){
                map.put(sender, new ArrayList<String>());
            }
            map.get(sender).add(message);
        }

        for(String key: map.keySet() ){
            final String sender = key;
            final String message = TextUtils.join(" ", map.get(sender));
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try{
                        execWithRetry(sender, message, url, phoneNumber);
                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    // send sms message to http server
    public void sendMessage(String from, String text, String url, String to) throws Exception {
        RequestBody formBody = new FormBody.Builder()
                .add("from", from)
                .add("to", to)
                .add("text", text)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            System.out.println(response.body().string());
        }
    }

    // retry wrapper of sendMessage
    public void execWithRetry(String from, String text, String url, String to) throws Exception {
        int maxRetries = 3;
        int numAttempts = 0;
        Exception exception;
        do {
            boolean isRetry = numAttempts > 0;
            try {
                if(isRetry) Thread.sleep(numAttempts * 1000);
                this.sendMessage(from, text, url, to);  // the retry
                return;
            } catch (Exception ex) {
                exception = ex;
            }
        } while ((++numAttempts) <= maxRetries);
        throw exception;
    }

    // get phone number
    public String getPhoneNumber(Context ctx){
        TelephonyManager tMgr = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = tMgr.getLine1Number();

        return phoneNumber;
    }

}
