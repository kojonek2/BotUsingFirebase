package kojonek2.adamzmuda.firebasetest;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    TextView informationText;
    WebView webView;
    Button confirmBtn;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        informationText = (TextView) findViewById(R.id.informationText);
        webView = (WebView) findViewById(R.id.webView);
        confirmBtn = (Button) findViewById(R.id.confirmButton);

        webView.setWebChromeClient(new WebChromeClient() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                informationText.setText(Integer.toString(newProgress));
            }
        });

        webView.setWebViewClient(new WebViewClient());

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://nauczyciel.edu.pl/user.php?page=pytania_online&arg=1115");

    }
}
