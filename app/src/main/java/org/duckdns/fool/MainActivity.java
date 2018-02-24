package org.duckdns.fool;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;


public class MainActivity extends ActionBarActivity {

    private EditText mPhoneNoRegexEditText;
    private EditText mHttpUrlEditText;
    private void initComponent(){
        mPhoneNoRegexEditText = (EditText) findViewById(R.id.text_phone_no_regex);
        mHttpUrlEditText = (EditText) findViewById(R.id.text_http_url);

        final String action_setting_update_name = this.getString(R.string.action_setting_update_name);
        final String sender_regex_name = this.getString(R.string.sender_regex_name);
        final String http_url_name = this.getString(R.string.http_url_name);

        mPhoneNoRegexEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Intent intent = new Intent(action_setting_update_name);
                intent.putExtra(sender_regex_name, s);
                sendBroadcast(intent);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
        });

        mHttpUrlEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Intent intent = new Intent(action_setting_update_name);
                intent.putExtra(http_url_name, s);
                sendBroadcast(intent);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
