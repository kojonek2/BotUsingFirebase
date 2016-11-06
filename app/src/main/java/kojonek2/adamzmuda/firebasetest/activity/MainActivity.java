package kojonek2.adamzmuda.firebasetest.activity;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import kojonek2.adamzmuda.firebasetest.R;
import kojonek2.adamzmuda.firebasetest.helper.Jquery;


public class MainActivity extends AppCompatActivity {

    TextView informationText;
    WebView webView;
    Button confirmBtn;

    String question;
    String answer;

    DatabaseReference myReference;
    DatabaseReference questionReference;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        informationText = (TextView) findViewById(R.id.informationText);
        webView = (WebView) findViewById(R.id.webView);
        confirmBtn = (Button) findViewById(R.id.confirmButton);

        myReference = FirebaseDatabase.getInstance().getReference().child("Question");

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
        webView.evaluateJavascript(new Jquery().getJqueryCode(), null);
        webView.evaluateJavascript("$(\"p\").text()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                question = value;
                questionReference = myReference.child(question);
                questionReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            //knows answer

                            selectAnswer(answer);
                        } else {
                            //doesn't know answer
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) { }

                });

            }
        });

        return 0;
    }

    private void selectAnswer(String answerPassed) {

    }

    public void onConfirmBtnClicked(View view) {

    }
}
