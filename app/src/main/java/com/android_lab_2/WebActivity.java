package com.android_lab_2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebActivity extends AppCompatActivity {
    private static final String TAG = "WebActivity";
    private WebView webView;


    // opens a website based on a url parameter
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Bundle bundle = getIntent().getExtras();

        String url = (String) bundle.getSerializable(getString(R.string.url_param_key));

        try {
            webView = findViewById(R.id.Web_View);
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl(url);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,"something Wrong with URL", Toast.LENGTH_SHORT ).show();
            finish();
        }
    }
}
