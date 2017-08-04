package com.sydus.resultssample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.lang.String.*;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.sydus.resultsample.MESSAGE";

    ProgressDialog pd;

    Intent intent;
    //String res = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void year3(View v) {
            intent = new Intent(this, MainViewActivity.class);
            new Result().execute();
    }

    private class Result extends AsyncTask<Void, Void, Void> {

        String url = "http://sjce.ac.in/view-results";
        //String usn_pre = "";

        String res =  "";

        List<Double> l = new ArrayList<>();
        Map<String, Integer> grade_map;
        LinkedHashMap<String, Double> map = new LinkedHashMap<>();

        String[] parseGrades(Elements grades_list) {
            String grades_str = "";

            for(int i=2; i<grades_list.size(); i+=3) {      // storing all the grades in a string separated by ' '
                grades_str += (grades_list.get(i).text() + " ");
            }

            return grades_str.split(" ");
        }

        Double getCgpa(Document page) {
            Double cgpa = 0.0;
            Elements grades_list = page.select("td");        // <td> tags
            String grades[] = parseGrades(grades_list);

            for(int i=0; i<9; i++) {
                if(i < 6) {
                    cgpa += grade_map.get(grades[i]) * 4;
                }
                else  {
                    cgpa += grade_map.get(grades[i]) * 1.5;
                }
            }
            return (cgpa / 27);
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            grade_map = new HashMap<String, Integer>()
            {{
                put("S", 10);
                put("A", 9);
                put("B", 8);
                put("C", 7);
                put("D", 5);
                put("E", 4);
                put("PP", 0);
                put("F", 0);
                put("NE", 0);
                put("AB", 0);
            }};

            pd = new ProgressDialog(MainActivity.this);
            pd.setTitle("Results");
            pd.setMessage("Loading ...");
            pd.setIndeterminate(false);
            pd.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String name = "";

            try {//
                for (int i = 1; i < 5; i++) {
                    Document page = Jsoup.connect(url)
                            .data("USN", "4JC15CS" + format(Locale.getDefault(), "%03d", i))
                            .data("submit_result", "Fetch Result").post();
                    Elements name_list = page.select("h1");

                    if (name_list.size() == 2) {
                        continue;
                    }

                    name = name_list.get(2).text().substring(7);
                    Double cgpa = Double.parseDouble(format(Locale.getDefault(), "%.02f", getCgpa
                            (page)));

                    map.put(name, cgpa);
                    l.add(cgpa);
                }

                Collections.sort(l, Collections.reverseOrder());

                int i = 1;
                for (Double value : l) {
                    for (Map.Entry entry : map.entrySet()) {
                        if (value.equals(entry.getValue())) {
                            name = valueOf(entry.getKey());
                            break;
                        }
                    }

                    res = res.concat(format(Locale.getDefault(), "%03d", i) + " " + name +
                            "\t" + format(Locale.getDefault(), "%.2f", value) + "\n");
                    map.values().remove(value);
                    i++;
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            intent.putExtra(EXTRA_MESSAGE, res);
            pd.dismiss();
            startActivity(intent);
        }
    }
}
