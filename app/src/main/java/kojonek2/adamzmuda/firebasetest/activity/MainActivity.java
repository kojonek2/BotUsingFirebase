package kojonek2.adamzmuda.firebasetest.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
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

    static final String STATE_REQUESTING_ANSWER = "requestingAnswer";
    static final String STATE_CHECK_CORRECT_OF_ANSWER = "checkCorrectOfAnswer";
    static final String STATE_TEMPORARY_ANSWER = "temporaryAnswer";
    static final String STATE_TEMPORARY_QUESTION = "temporaryQuestion";
    static final String STATE_SOLVED_QUESTIONS = "solvedQuestions";


    TextView informationText;
    WebView webView;
    Button confirmBtn;

    ProgressDialog progressDialog;

    String question;
    String answer;
    String temporaryAnswer;
    String temporaryQuestion;

    Boolean requestingAnswer;
    Boolean checkCorrectOfAnswer;

    int solvedQuestions;

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

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(R.string.solving_questions));
        progressDialog.setMessage(getString(R.string.solved_count) + Integer.toString(solvedQuestions));
        progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.turn_off), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAndRemoveTask();
                System.exit(0);
            }
        });

        myReference = FirebaseDatabase.getInstance().getReference().child("Question");



        //////// reading data from saved instance or setting default one
        requestingAnswer = savedInstanceState != null && savedInstanceState.getBoolean(STATE_REQUESTING_ANSWER);
        checkCorrectOfAnswer = savedInstanceState != null && savedInstanceState.getBoolean(STATE_CHECK_CORRECT_OF_ANSWER);
        if(savedInstanceState != null) {
            temporaryAnswer = savedInstanceState.getString(STATE_TEMPORARY_ANSWER);
            temporaryQuestion = savedInstanceState.getString(STATE_TEMPORARY_QUESTION);
            solvedQuestions = savedInstanceState.getInt(STATE_SOLVED_QUESTIONS);
        } else {
            solvedQuestions = 0;
        }


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        confirmBtn.setVisibility(View.GONE);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                switch (url) {
                    case "http://nauczyciel.edu.pl/login.php":
                        informationText.setText(getString(R.string.please_login));
                        allowClicking(true);
                        if(checkCorrectOfAnswer) {
                            //given answer is incorrect
                            checkCorrectOfAnswer = false;
                            temporaryAnswer = null;
                            temporaryQuestion = null;
                        }
                        break;
                    case "http://nauczyciel.edu.pl/user.php?page=pytania_online&arg=1115":
                        informationText.setText(getString(R.string.information));
                        if(checkCorrectOfAnswer) {
                            //given answer is correct
                            questionReference = myReference.child(temporaryQuestion);
                            questionReference.setValue(temporaryAnswer);

                            temporaryQuestion = null;
                            temporaryAnswer = null;
                            checkCorrectOfAnswer = false;
                        }
                        checkForAnswer();
                        break;
                    default:
                        if(checkCorrectOfAnswer) {
                            //given answer is incorrect
                            temporaryAnswer = null;
                            temporaryQuestion = null;
                            checkCorrectOfAnswer = false;
                        }
                        webView.loadUrl("http://nauczyciel.edu.pl/user.php?page=pytania_online&arg=1115");
                        break;
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                confirmBtn.setVisibility(View.GONE);
                allowClicking(false);
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://nauczyciel.edu.pl/user.php?page=pytania_online&arg=1115");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(STATE_REQUESTING_ANSWER, requestingAnswer);
        outState.putBoolean(STATE_CHECK_CORRECT_OF_ANSWER, checkCorrectOfAnswer);
        outState.putString(STATE_TEMPORARY_ANSWER, temporaryAnswer);
        outState.putString(STATE_TEMPORARY_QUESTION, temporaryQuestion);
        outState.putInt(STATE_SOLVED_QUESTIONS, solvedQuestions);
    }

    private void checkForAnswer() {
        webView.evaluateJavascript(Jquery.getJqueryCode(), null);
        webView.evaluateJavascript("$(\"p\").text()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                question = value.replaceAll("[\\.$#\\[\\]]", "");
                questionReference = myReference.child(question);
                questionReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            //knows answer
                            solvedQuestions++;
                            progressDialog.setMessage(getString(R.string.solved_count) + Integer.toString(solvedQuestions));
                            progressDialog.show();
                            answer = dataSnapshot.getValue().toString();
                            selectAnswer(answer);
                        } else {
                            //doesn't know answer
                            progressDialog.dismiss();
                            solvedQuestions = 0;
                            progressDialog.setMessage(getString(R.string.solved_count) + Integer.toString(solvedQuestions));
                            requestAnswerFromUser();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });

            }
        });
    }

    private void requestAnswerFromUser() {
        informationText.setText(getString(R.string.give_answer));
        requestingAnswer = true;
        confirmBtn.setVisibility(View.VISIBLE);
        webView.evaluateJavascript("$(\"input\").eq(10).hide()", null);
        allowClicking(true);

    }

    private void selectAnswer(final String answerPassed) {
        webView.evaluateJavascript("$(\"td\").eq(1).text()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if(value.equals(answerPassed)) {
                    webView.evaluateJavascript("$(\"input\").get(4).click(); $(\"input\").get(10).click();", null);
                }
            }
        });
        webView.evaluateJavascript("$(\"td\").eq(3).text()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if(value.equals(answerPassed)) {
                    webView.evaluateJavascript("$(\"input\").get(5).click(); $(\"input\").get(10).click();", null);
                }
            }
        });
        webView.evaluateJavascript("$(\"td\").eq(5).text()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if(value.equals(answerPassed)) {
                    webView.evaluateJavascript("$(\"input\").get(6).click(); $(\"input\").get(10).click();", null);
                }
            }
        });
        webView.evaluateJavascript("$(\"td\").eq(7).text()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if(value.equals(answerPassed)) {
                    webView.evaluateJavascript("$(\"input\").get(7).click(); $(\"input\").get(10).click();", null);
                }
            }
        });
    }

    public void onConfirmBtnClicked(View view) {
        if(requestingAnswer) {
            confirmBtn.setVisibility(View.GONE);
            webView.evaluateJavascript("$(\"p\").text()", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    temporaryQuestion = value.replaceAll("[\\.$#\\[\\]]", "");
                }
            });

            webView.evaluateJavascript("$(\"input\").eq(4).prop(\"checked\")", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    if (value.equals("true")) {

                        webView.evaluateJavascript("$(\"td\").eq(1).text()", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                temporaryAnswer = value;
                            }
                        });
                    }
                }
            });
            webView.evaluateJavascript("$(\"input\").eq(5).prop(\"checked\")", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    if(value.equals("true")) {

                        webView.evaluateJavascript("$(\"td\").eq(3).text()", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                temporaryAnswer = value;
                            }
                        });
                    }
                }
            });
            webView.evaluateJavascript("$(\"input\").eq(6).prop(\"checked\")", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    if(value.equals("true")) {

                        webView.evaluateJavascript("$(\"td\").eq(5).text()", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                temporaryAnswer = value;
                            }
                        });
                    }
                }
            });
            webView.evaluateJavascript("$(\"input\").eq(7).prop(\"checked\")", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    if(value.equals("true")) {

                        webView.evaluateJavascript("$(\"td\").eq(7).text()", new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                temporaryAnswer = value;
                            }
                        });
                    }
                }
            });
            requestingAnswer = false;
            checkCorrectOfAnswer = true;
            webView.evaluateJavascript("$(\"input\").get(10).click();", null);
        }
    }

    private void allowClicking(final boolean b) {
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return !b;
            }
        });
    }
}
