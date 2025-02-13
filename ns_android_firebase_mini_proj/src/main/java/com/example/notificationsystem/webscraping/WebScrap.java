package com.example.notificationsystem.webscraping;
import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notificationsystem.R;

import org.jsoup.Jsoup;
import org.w3c.dom.Document;


public class WebScrap extends AppCompatActivity
{
    private Document doc;
    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_scrap);
        tv=findViewById(R.id.textView1098);
        new doit().execute();
    }
    class doit extends AsyncTask<Void,Void,Void>
    {
        String words = "";

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                doc = (Document) Jsoup.connect("http://www.cbit.ac.in/about_post/exam-notices/").get();
                words = doc.getDocumentElement().getAttribute("adm-right-col");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            tv.setText(words);
            Toast.makeText(WebScrap.this,words,Toast.LENGTH_LONG).show();
        }
    }
}

