package br.com.fatec.audiodic;

//Importing

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
//End of imports

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    //Setting common variables
    private Intent recognizerIntent;
    private SpeechRecognizer speech = null;
    private String LOG_TAG = "VoiceRecognitionActivity";
    private int transitionTime = 300;
    public TextToSpeech textToSpeech;
    final Locale myLocale = new Locale("pt", "BR");
    DicionarioAbertoWord dicionarioAbertoWord;
    private Context mainContext;
    //End of Setting common variables

    //Setting flags
    private boolean longClickFlag = false;
    //End of setting flags

    //Setting fields
    private ProgressBar progressBarMic;
    private ImageView imageViewMic;
    private EditText editTextSearch;
    private Button buttonSearch;
    private LinearLayout linearLayoutSearch;
    private LinearLayout linearLayoutMic;
    private LinearLayout linearLayoutMain;
    private RelativeLayout relativeLayoutMain;
    private TabHost tabHostResults;
    private TextView textViewDefinitions;
    private TextView textViewWordWanted;
    //End of Settigns fields

    //Animation stuff
    private LayoutTransition layoutTransitionRelativeLayoutMain = new LayoutTransition();
    private LayoutTransition layoutTransitionLinearLayoutMain = new LayoutTransition();
    private LayoutTransition layoutTransitionLinearLayoutSearch = new LayoutTransition();
    //End of animation stuff

    //Soft keyboard control
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
    //End of soft keyboard control

    //Inflating menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    //End inflating menu

    //Setting back function
    @Override
    public void onBackPressed() {
        tabHostResults.setVisibility(View.GONE);
        textViewWordWanted.setVisibility(View.GONE);
        linearLayoutSearch.setVisibility(View.GONE);
        linearLayoutSearch.setVisibility(View.VISIBLE);
        linearLayoutSearch.setOrientation(LinearLayout.VERTICAL);
    }
    //End setting back function

    //Configuring Menu Items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.itemSearch:
                if(linearLayoutSearch.getVisibility() == View.GONE) {
                    textViewWordWanted.setVisibility(View.GONE);
                    linearLayoutSearch.setVisibility(View.VISIBLE);
                }
                else {
                    linearLayoutSearch.setVisibility(View.GONE);
                    textViewWordWanted.setVisibility(View.VISIBLE);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //End of configuring Menu Items

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainContext = this;

        //Global config
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        //End of global config

        //Getting fields
        progressBarMic = (ProgressBar) findViewById(R.id.progressBarMic);
        imageViewMic = (ImageView) findViewById(R.id.imageViewMic);
        editTextSearch = (EditText) findViewById(R.id.editTextSearch);
        buttonSearch = (Button) findViewById(R.id.buttonSearch);
        linearLayoutSearch = (LinearLayout) findViewById(R.id.linearLayoutSearch);
        linearLayoutMic = (LinearLayout) findViewById(R.id.linearLayoutMic);
        linearLayoutMain = (LinearLayout) findViewById(R.id.linearLayoutMain);
        relativeLayoutMain = (RelativeLayout) findViewById(R.id.relativeLayoutMain);
        tabHostResults = (TabHost) findViewById(R.id.tabHostResults);
        textViewDefinitions = (TextView) findViewById(R.id.textViewDefinitions);
        textViewWordWanted = (TextView) findViewById(R.id.textViewWordWanted);
        //End of Getting fields

        //Setting Voice recognition
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, myLocale);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        //End of Setting voice recognition

        //Setting animations
        relativeLayoutMain.setLayoutTransition(layoutTransitionRelativeLayoutMain);
        layoutTransitionRelativeLayoutMain.enableTransitionType(LayoutTransition.CHANGING);
        layoutTransitionRelativeLayoutMain.setDuration(transitionTime);

        linearLayoutMain.setLayoutTransition(layoutTransitionLinearLayoutMain);
        layoutTransitionLinearLayoutMain.enableTransitionType(LayoutTransition.CHANGING);
        layoutTransitionLinearLayoutMain.setDuration(transitionTime);

        linearLayoutSearch.setLayoutTransition(layoutTransitionLinearLayoutSearch);
        layoutTransitionLinearLayoutSearch.enableTransitionType(LayoutTransition.CHANGING);
        layoutTransitionLinearLayoutSearch.setDuration(transitionTime);
        //End of setting animations

        //Configuring tabs
        tabHostResults.setup();
        TabHost.TabSpec tabSpecResults = tabHostResults.newTabSpec("Definições");
        tabSpecResults.setContent(R.id.linearLayoutDefinitions);
        tabSpecResults.setIndicator("Definições");
        tabHostResults.addTab(tabSpecResults);
        //End of Configuring

        //Fields customizations
        editTextSearch.setImeActionLabel("Ir!", KeyEvent.KEYCODE_ENTER);
        editTextSearch.setImeOptions(EditorInfo.IME_ACTION_DONE);
        //End of Fields customizations

        //Text to Speech
        textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(myLocale);
                }
            }
        });
        //End of Text to speech

        //Setting listeners
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String wantedWord = editTextSearch.getText().toString();
                hideKeyboard();
                editTextSearch.clearFocus();
                editTextSearch.setSelected(false);
                onlineSearchWord(editTextSearch.getText().toString());
                textViewWordWanted.setVisibility(View.VISIBLE);
                textViewWordWanted.setText(wantedWord.substring(0,1).toUpperCase() + wantedWord.substring(1));
                linearLayoutSearch.setOrientation(LinearLayout.HORIZONTAL);
                tabHostResults.setVisibility(View.VISIBLE);
                linearLayoutSearch.setVisibility(View.GONE);
            }
        });

        editTextSearch.setOnEditorActionListener(new EditText.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    buttonSearch.performClick();
                    return true;
                }
                return false;
            }
        });
        //End of setting listeners
    }
    //Detecting Screen Touch
    final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent event) {
            Log.i("", "Down Detected");
            longClickFlag = false;
            return true;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            Log.i("", "Longpress detected");
            longClickFlag = true;
            micControl(true);
        }
    });

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        if (longClickFlag && event.getAction() == MotionEvent.ACTION_UP) {
            Log.i("", "Longpress up detected");
            micControl(false);
        }
        return gestureDetector.onTouchEvent(event);
    }
    //End of Detecting Screen Touch

    //Control the init of speech
    public void micControl(Boolean flag){
        if(flag){
            textToSpeech.stop();
            linearLayoutMic.setVisibility(View.VISIBLE);
            linearLayoutMain.setVisibility(View.GONE);
            progressBarMic.setIndeterminate(true);
            speech.startListening(recognizerIntent);
        }
        else{
            linearLayoutMain.setVisibility(View.VISIBLE);
            linearLayoutMic.setVisibility(View.GONE);
            progressBarMic.setIndeterminate(false);
            speech.stopListening();
        }
    }
    //End of speech

    //Online Search mechanism
    public void onlineSearchWord(String word){
        textViewDefinitions.setText("");
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://dicionario-aberto.net/search-json/"+word;

        // Request a string response from the provided URL.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest (Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dicionarioAbertoWord = new DicionarioAbertoWord(response, mainContext);
                        textViewDefinitions.setText(Html.fromHtml(dicionarioAbertoWord.getFinalText()));
                        informResults();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        if(error.networkResponse != null) {
                            if (error.networkResponse.statusCode == 404) {
                                textViewDefinitions.setText("Palavra Não Existe ou Não Encontrada.");
                            }
                            if (error.networkResponse.statusCode == 503) {
                                textViewDefinitions.setText("Serviço de Busca Indisponível");
                            }
                        }
                    }
                });
        // Add the request to the RequestQueue.
        queue.add(jsObjRequest);
    }
    //End of Online search mechanism

    //Offline Search
    public void offlineSearchWord(String word){

    }
    //End of offline search

    //Read Definitions
    public void readDefinitions(){
        String toSpeak = Html.fromHtml(dicionarioAbertoWord.getFinalVoiceText()).toString();
        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }
    //End of read definitions

    //Inform Results
    public void informResults(){
        String toSpeak = "A pesquisa retornou "+dicionarioAbertoWord.getOrigins().size()+" origens e o total de "+dicionarioAbertoWord.getDefinitions().size()+" definições";
        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }
    //End of inform results

    //No Command
    public void noCommand(String command){
        String toSpeak = "Comando "+command+" não reconhecido.";
        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }
    //End of No command

    //Voice commands control
    public void doVoiceCommand(ArrayList<String> match){
        String originalMatch = "";
        for(String temp : match){
            originalMatch += " "+temp;
        }
        switch (match.get(0)) {
            case "pesquisar":
                editTextSearch.setText(match.get(1));
                buttonSearch.performClick();
                break;
            case "ler":
                switch (match.get(1)){
                    case "definições":
                        readDefinitions();
                        break;
                    default:
                        noCommand(originalMatch);
                }
                break;
            default:
                noCommand(originalMatch);

        }
    }
    //End of voice commands control

    //Speech functions
    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        progressBarMic.setIndeterminate(false);
        progressBarMic.setMax(10);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBarMic.setProgress((int) rmsdB);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        progressBarMic.setIndeterminate(true);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onError(int error) {
        String errorMessage = getErrorText(error);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        String result = "";

        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        doVoiceCommand(new ArrayList<String>(Arrays.asList(matches.get(0).split(" "))));
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.i(LOG_TAG, "onEvent");
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
    //End of Speech functions
}
