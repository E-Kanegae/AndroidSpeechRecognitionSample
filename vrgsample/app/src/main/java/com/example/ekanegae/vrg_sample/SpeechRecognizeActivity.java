package com.example.ekanegae.vrg_sample;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by E.KANEGAE on 2017/11/09.
 */
public class SpeechRecognizeActivity extends Activity implements View.OnClickListener {

   private static final int REQUEST_CODE = 1;
   private EditText inpText;
   private TextView candidatesText;
   private String preInp = null;
   private static final String DEFAULT_LANGUAGE = "English";
    private static final String DEFAULT_LANGUAGE_KEY = "en-US";
   private String selectedLang = DEFAULT_LANGUAGE;
   private Button inpBtn, canBtn, clsBtn, langBtn;
   private Spinner langSpinner;
   private Map<String, String> langMap = new HashMap<String, String>(){
       {
           put("en-US", "American English");
           put("ja-JP", "Japanese");
           put("en-IN", "Indian English");
           put("en-GB", "British English");
       }
   };

    /**
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inpText = (EditText)findViewById(R.id.result_id);
        candidatesText = (TextView)findViewById(R.id.candidates_id);
        inpBtn = (Button)findViewById(R.id.speech_id);
        inpBtn.setOnClickListener(this);
        canBtn = (Button)findViewById(R.id.cancel_id);
        canBtn.setOnClickListener(this);
        clsBtn = (Button)findViewById(R.id.clear_id);
        clsBtn.setOnClickListener(this);
        langBtn = (Button)findViewById(R.id.lang_id);
        langBtn.setOnClickListener(this);

        langSpinner = (Spinner)findViewById(R.id.spinner_id);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, createLangArray());
        langSpinner.setAdapter(adapter);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        if(v == inpBtn) {
            try {
                candidatesText.setText(null);
                // 音声認識の準備
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                // Intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "音声を入力してください。");
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectLanguage(selectedLang));

                // Android 6.1 以降は一度requestPermissions呼ばないと、パーミッションエラーになる。
                requestPermission(Manifest.permission.RECORD_AUDIO);

                // インテント発行
                startActivityForResult(intent, REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(SpeechRecognizeActivity.this, "Not found Activity", Toast.LENGTH_LONG).show();
              }
        } else if(v == canBtn) { // 認識結果の取り消し
            candidatesText.setText(null);
            if(StringUtils.isNotEmpty(preInp)) {
                inpText.setText(preInp); // 前の入力文字列を表示
                inpText.setSelection(preInp.length()); // カーソルを移動
            }
        }else if(v == clsBtn) { // テキストのクリア
            candidatesText.setText(null);
            inpText.setText(null);
        }else if(v == langBtn){
            selectedLang = langSpinner.getSelectedItem().toString();
        }
    }


    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String input = null;
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
                preInp = inpText.getText().toString();
                // 認識結果を取得
                ArrayList<String> candidates = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                Log.v("Speech", "Candidate Num = " + candidates.size());
                if(candidates.size() > 0) {
                    input = preInp + candidates.get(0); // 認識結果(1位候補)
                }

                if(candidates.size() > 1){ //他の候補を表示
                    String candidateLine = "Other Candidates: \n";
                    for(int i = 1 ; i < candidates.size(); i++){
                        candidateLine = candidateLine + candidates.get(i) + "\n";
                    }
                    candidatesText.setText(candidateLine);
                }

                if(StringUtils.isNotEmpty(input)) {
                    inpText.setText(input); // 入力文字列を表示
                    inpText.setSelection(input.length()); // カーソルを移動
                }
        }
    }


    /**
     * Android 6.1 以降は一度requestPermissions呼ばないと、パーミッションエラーになる。
     * @param permission
     */
    private void requestPermission(String permission){
        if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{permission}, 1);
        }
    }


    private String selectLanguage(String selected){
        for(Map.Entry<String, String> langEntry : langMap.entrySet()){
            if(langEntry.getValue().equals(selected)) {
                return langEntry.getKey();
            }
        }
        return DEFAULT_LANGUAGE_KEY;
    }

    private String[] createLangArray(){
        List<String> items = new ArrayList<String>();
        for(Map.Entry<String, String> langEntry : langMap.entrySet()){
            items.add(langEntry.getValue());
        }
        return items.toArray(new String[langMap.size()]);
    }
}
