package mobile.labs.acw;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class PuzzleList extends AppCompatActivity {
JSONArray m_PuzzleList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.puzzlelist);

        TextView textView = (TextView)findViewById(R.id.JSONTextView);
        textView.setText("Downloading JSON!");
        new downloadJSON().execute("http://www.simongrey.net/08027/slidingPuzzleAcw/index.json");

        final ListView listView = (ListView)findViewById(R.id.PuzzleListView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), Puzzle.class);
                intent.putExtra("Puzzle", listView.getAdapter().getItem(position).toString());
                startActivity(intent);
            }
        });
    }
    private class downloadJSON extends AsyncTask<String, String, String[]> {
        protected String[] doInBackground(String... args) {
            String result= "";
            String[] formatted = null;
            try {
                InputStream stream = (InputStream)new URL(args[0]).getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line = "";
                while(line != null)
                {
                    result += line;
                    line = reader.readLine();
                }
                JSONObject json = new JSONObject(result);
                m_PuzzleList = json.getJSONArray("PuzzleIndex");

                formatted = new String[m_PuzzleList.length()];
                for(int i = 0; i < m_PuzzleList.length(); i++)
                {
                    formatted[i] = m_PuzzleList.get(i).toString();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
                return formatted;
        }
        protected void onPostExecute(String[] pResult) {
            ListView listView = (ListView)findViewById(R.id.PuzzleListView);
            Context context = PuzzleList.this;
            ArrayAdapter<String> puzzleArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, pResult);
            listView.setAdapter(puzzleArrayAdapter);
        }
    }
}