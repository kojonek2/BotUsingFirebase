package kojonek2.adamzmuda.firebasetest;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    TextView informationText;
    WebView webView;
    Button confirmBtn;
    String strJquery;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        informationText = (TextView) findViewById(R.id.informationText);
        webView = (WebView) findViewById(R.id.webView);
        confirmBtn = (Button) findViewById(R.id.confirmButton);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                switch (url) {
                    case "http://nauczyciel.edu.pl/login.php":
                        informationText.setText(R.string.please_login);
                        break;
                    case "http://nauczyciel.edu.pl/user.php?page=pytania_online&arg=1115":
                            checkForAnswer();
                        break;
                    default:
                        webView.loadUrl("http://nauczyciel.edu.pl/user.php?page=pytania_online&arg=1115");
                        break;

                }
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://nauczyciel.edu.pl/user.php?page=pytania_online&arg=1115");
    }

    private int checkForAnswer() {

        return 0;
    }

    public void onConfirmBtnClicked(View view) {

    }
}
