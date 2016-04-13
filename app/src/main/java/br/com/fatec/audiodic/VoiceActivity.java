package br.com.fatec.audiodic;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public abstract class VoiceActivity extends AppCompatActivity implements RecognitionListener {
    //Intent Variables
    public final static String RESULTS = "RESULTS";
    public final static String DICIONARIO_ABERTO_WORD = "DICIONARIO_ABERTO_WORD";
    public final static String WANTED_WORD = "WANTED_WORD";
    //End of Intent Variables

    //Setting flags
    private boolean longClickFlag = false;
    //End of setting flags

    //Setting common Variables
    private String LOG_TAG = "VoiceRecognitionActivity";
    private TextToSpeech textToSpeech;
    private final Locale myLocale = new Locale("pt", "BR");
    protected static DicionarioAbertoWord dicionarioAbertoWord;
    private Context mainContext = this;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private Vibrator vibrator;
    // Instantiate the RequestQueue.
    private RequestQueue requestQueue;
    //End of Setting Common Variables

    //Setting Fields
    private ProgressBar progressBarMic;
    private LinearLayout linearLayoutLoading;
    private LinearLayout linearLayoutMic;
    private LinearLayout linearLayoutToHide;
    //End of Setting Fields

    public void startVoiceService(LinearLayout linearLayoutToHide){
        this.linearLayoutToHide = linearLayoutToHide;

        //Getting Fields
        progressBarMic = (ProgressBar) findViewById(R.id.progressBarMic);
        linearLayoutMic = (LinearLayout) findViewById(R.id.linearLayoutMic);
        linearLayoutLoading = (LinearLayout) findViewById(R.id.linearLayoutLoading);
        //End of getting Fields

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

        //Setting Voice recognition
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, myLocale);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        //End of Setting voice recognition
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);

        vibrator = (Vibrator) this.mainContext.getSystemService(Context.VIBRATOR_SERVICE);
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }

    //Soft keyboard control
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
    //End of soft keyboard control

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

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            detailedHelp();
            return true;
        }
    });

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        textToSpeech.stop();
        dicionarioAbertoWord = null;
        cancelRequests();
    }

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

    //Cancelling all requests
    public void cancelRequests(){
        requestQueue.cancelAll(this);
    }
    //End of Cancelling all requests

    //Control the init of speech
    public void micControl(Boolean flag){
        if(flag){
            textToSpeech.stop();
            speech.startListening(recognizerIntent);
            vibrator.vibrate(125);
            linearLayoutToHide.setVisibility(View.GONE);
            linearLayoutMic.setVisibility(View.VISIBLE);
            progressBarMic.setIndeterminate(true);
        }
        else{
            speech.stopListening();
            linearLayoutMic.setVisibility(View.GONE);
            linearLayoutToHide.setVisibility(View.VISIBLE);
            progressBarMic.setIndeterminate(false);
        }
    }
    //End of speech

    //Online Search mechanism
    public String onlineSearchWord(final String word, final boolean goToResultsActivity, final TextView target){
        linearLayoutLoading.setVisibility(View.VISIBLE);
        final String[] finalText = new String[1];
        String url ="http://dicionario-aberto.net/search-json/"+word;

        // Request a string response from the provided URL.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest (Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        linearLayoutLoading.setVisibility(View.GONE);
                        dicionarioAbertoWord = new DicionarioAbertoWord(response, mainContext);
                        finalText[0] = dicionarioAbertoWord.getFinalText();
                        informResults(word);
                        if(goToResultsActivity){
                            openResultsActivity(finalText[0], word);
                        }
                        if(target != null)
                            target.setText(Html.fromHtml(finalText[0]));
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                if(error.networkResponse != null) {
                    linearLayoutLoading.setVisibility(View.GONE);
                    if (error.networkResponse.statusCode == 404) {
                        finalText[0] = "Palavra Não Existe ou Não Encontrada.";
                        informAction(finalText[0]);
                        if(goToResultsActivity){
                            openResultsActivity(finalText[0], word);
                        }
                        if(target != null)
                            target.setText(Html.fromHtml(finalText[0]));
                    }
                    if (error.networkResponse.statusCode == 503) {
                        finalText[0] = "Serviço de Busca Indisponível";
                        informAction(finalText[0]);
                        if(goToResultsActivity){
                            openResultsActivity(finalText[0], word);
                        }
                        if(target != null)
                            target.setText(Html.fromHtml(finalText[0]));
                    }
                }
                else{
                    System.out.println(error.getClass());
                    linearLayoutLoading.setVisibility(View.GONE);
                    if (error.getClass().equals(TimeoutError.class)) {
                        // Show timeout error message
                        finalText[0] = "Serviço de Busca Indisponível ou Muito Lento. Tente Novamente Mais Tarde.";
                        informAction(finalText[0]);
                        if(goToResultsActivity){
                            openResultsActivity(finalText[0], word);
                        }
                        if(target != null)
                            target.setText(Html.fromHtml(finalText[0]));
                    }
                    else if(error.getClass().equals(NoConnectionError.class)){
                        // Show timeout error message
                        finalText[0] = "Sem Conexão com a Internet. Favor Verificar a Situação do seu Aparelho.";
                        informAction(finalText[0]);
                        if(goToResultsActivity){
                            openResultsActivity(finalText[0], word);
                        }
                        if(target != null)
                            target.setText(Html.fromHtml(finalText[0]));
                    }
                }
            }
        });
        int socketTimeout = 2500;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        // Add the request to the RequestQueue.
        requestQueue.add(jsObjRequest);
        return finalText[0];
    }
    //End of Online search mechanism

    //Inform action
    public void informAction(String action){
        String toSpeak = action;
        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_ADD, null);
    }
    //End of Inform Action

    //Inform action flush
    public void informActionFlush(String action){
        String toSpeak = action;
        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }
    //End of Inform Action

    //Read Definitions
    public void readDefinitions(){
        if(dicionarioAbertoWord != null) {
            String toSpeak = Html.fromHtml(dicionarioAbertoWord.getFinalVoiceText()).toString();
            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_ADD, null);
        } else{
            informAction("Não há definições para serem lidas");
        }
    }
    //End of read definitions

    //Inform Results
    public void informResults(String word){
        int originsSize = dicionarioAbertoWord.getOrigins().size();
        int definitionsSize = dicionarioAbertoWord.getDefinitions().size();
        String toSpeak = "A pesquisa por "+word+" retornou "+
                originsSize+(originsSize > 1 ? (" origems ") : (" origem "))+
                "e o total de "+
                definitionsSize+(definitionsSize > 1 ? (" definições ") : (" definição "));
        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_ADD, null);
    }
    //End of inform results

    //No Command
    public void noCommand(String command){
        String toSpeak = "Comando "+command+" não reconhecido.";
        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_ADD, null);
    }
    //End of No command

    //Voice Help
    public void voiceCommands(){
        String toSpeak = "<p><big>Comandos por Voz Disponíveis</big><br/><br/>" +
                "<strong>- Pesquisar \"palavra\" : </strong>" +
                "Pesquisa a palavra solicitada<br/>" +
                "<strong>- Ler Definições : </strong>" +
                "Lê a definição da palavra pesquisada;<br/>" +
                "<strong>- Cancelar : </strong>" +
                "Cancela a pesquisa;</br></p>" +
                "<strong>- Comandos : </strong>" +
                "Informa os comandos ofericidos pelo aplicativo.";
        textToSpeech.speak(Html.fromHtml(toSpeak).toString(), TextToSpeech.QUEUE_ADD, null);
    }
    //End of Voice Help

    //Detailed help
    public void detailedHelp(){
        String toSpeak = "Para começar a pesquisa : segure a tela até ouvir-se um som, e fale um comando, depois solte-a. Pava ouvir os comandos disponíveis, utilize o comando \"commandos\".";
        textToSpeech.speak(Html.fromHtml(toSpeak).toString(), TextToSpeech.QUEUE_ADD, null);
    }
    //End of Detailed help

    public void openResultsActivity(String results, String word){
        Intent intent;
        intent = new Intent(this, ResultsActivity.class);
        intent.putExtra(WANTED_WORD, word);
        intent.putExtra(RESULTS, results);
        intent.putExtra(DICIONARIO_ABERTO_WORD, dicionarioAbertoWord);
        startActivity(intent);
        if (!this.getClass().getSimpleName().equalsIgnoreCase("MainActivity"))
            this.finish();
    }

    //Voice commands control
    public void doVoiceCommand(ArrayList<String> match){
        String originalMatch = "";
        for(String temp : match){
            originalMatch += " "+temp;
        }
        System.out.println(originalMatch);
        switch (match.get(0).toLowerCase()) {
            case "ajuda":
            case "comando":
            case "comandos":
                voiceCommands();
                break;
            case "busca":
            case "buscar":
            case "pesquisa":
            case "pesquisar":
                if(match.size() > 1) {
                    informAction("Pesquisando...");
                    if (this.getClass().getSimpleName().equalsIgnoreCase("ResultsActivity")) {
                        onlineSearchWord(match.get(1), true, (TextView) findViewById(R.id.textViewDefinitions));
                    } else {
                        onlineSearchWord(match.get(1), true, null);
                    }
                }
                else
                    informAction("Diga o comando pesquisar e logo em seguida a palavra desejada.");
                break;
            case "ver":
            case "ler":
                switch (match.get(1)){
                    case "definições":
                        readDefinitions();
                        break;
                    default:
                        noCommand(originalMatch);
                }
                break;
            case "cancelar":
                linearLayoutLoading.setVisibility(View.GONE);
                informAction("Cancelando pesquisas...");
                cancelRequests();
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
        //Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
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
