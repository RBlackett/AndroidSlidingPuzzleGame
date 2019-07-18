package mobile.labs.acw;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

public class Puzzle extends AppCompatActivity {
String m_puzzleString;
String m_FullJSON;
String m_PictureSet;
String m_Layout;
String[][] m_LayoutArray;
HashMap<String, Bitmap> m_Bitmaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);
        TextView textView = (TextView)findViewById(R.id.textPuzzleName);
        m_puzzleString = getIntent().getStringExtra("Puzzle");
        textView.setText(m_puzzleString);

        new downloadJSON().execute("http://www.simongrey.net/08027/slidingPuzzleAcw/puzzles/" + m_puzzleString);
    }

    private class downloadJSON extends AsyncTask<String, String, String> {
        protected String doInBackground(String... args) {
            String result= "";
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
                m_PictureSet = json.getString("PictureSet");
                m_Layout = json.getString("layout");
                m_FullJSON = result;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
        protected void onPostExecute(String pResult) {
            TextView textView1 = (TextView)findViewById(R.id.textPictureSet);
            TextView textView2 = (TextView)findViewById(R.id.textLayout);
            TextView textView3 = (TextView)findViewById(R.id.textFullJSON);
            textView1.setText(m_PictureSet);
            textView2.setText(m_Layout);
            textView3.setText(pResult);
            new downloadLayoutJSON().execute("http://www.simongrey.net/08027/slidingPuzzleAcw/layouts/" + m_Layout);
        }
    }
    private class downloadLayoutJSON extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... args) {
            String result = "";
            try {
                InputStream stream = (InputStream) new URL(args[0]).getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line = "";
                while (line != null) {
                    result += line;
                    line = reader.readLine();
                }
                JSONObject json = new JSONObject(result);
                JSONArray jsonarray = json.getJSONArray("layout");
                m_LayoutArray = new String[jsonarray.length()][jsonarray.getJSONArray(0).length()];

                for (int x=0; x < m_LayoutArray.length; x++) {
                    for (int y=0; y < m_LayoutArray[x].length; y++) {
                        m_LayoutArray[x][y] = jsonarray.getJSONArray(x).getString(y);
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
        protected void onPostExecute(String pResult) {
            TextView textView1 = (TextView)findViewById(R.id.textLayoutCheck);
            String line = "";
            for (int x=0; x < m_LayoutArray.length; x++) {
                for (int y=0; y < m_LayoutArray[x].length; y++) {
                    line += m_LayoutArray[x][y];
                }
                line += "\n";
            }
            textView1.setText(line);
            new downloadBitmaps().execute();
        }
    }
    private class downloadBitmaps extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... args) {
            m_Bitmaps = new HashMap<>();
            Bitmap bitmap = null;
            for (int x = 0; x < m_LayoutArray.length; x++) {
                for (int y = 0; y < m_LayoutArray[x].length; y++) {
                    String urlString = "http://www.simongrey.net/08027/slidingPuzzleAcw/images/" + m_PictureSet + "/" + m_LayoutArray[x][y] + ".jpg";
                    String fileName = m_PictureSet + m_LayoutArray[x][y] + ".jpg";
                    try {
                        if (m_LayoutArray[x][y].contains("empty")) {
                            m_Bitmaps.put("empty", null);
                        }
                        else {
                            try {
                                FileInputStream reader = getApplicationContext().openFileInput(fileName);
                                bitmap = BitmapFactory.decodeStream(reader);
                                m_Bitmaps.put(m_LayoutArray[x][y], bitmap);
                            }
                            catch (FileNotFoundException fileNotFound){
                                try {
                                    bitmap = BitmapFactory.decodeStream((InputStream) new URL(urlString).getContent());
                                    m_Bitmaps.put(m_LayoutArray[x][y], bitmap);
                                    FileOutputStream writer = null;
                                    try {
                                        writer = getApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, writer);
                                    }
                                    catch (Exception e) {
                                        //boom
                                    }
                                    finally {
                                        writer.close();
                                    }
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
        protected  void onPostExecute(Void Void) {
            displayGrid();
        }
    }
    protected void displayGrid() {
        Context context = Puzzle.this;
        //GridLayout gridLayout = new GridLayout(context);
        GridLayout gridLayout = (GridLayout)findViewById(R.id.gridPuzzle);
        int total = m_LayoutArray.length * m_LayoutArray[0].length;
        int columns = m_LayoutArray.length;
        int rows = m_LayoutArray[0].length;
        //gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        //gridLayout.setColumnCount(columns);
        //gridLayout.setRowCount(rows + 1);
        for (int x = 0; x < m_LayoutArray.length; x++) {
            for (int y = 0; y < m_LayoutArray[x].length; y++) {
                String fileName = m_LayoutArray[x][y];
                ImageView imageView = new ImageView(context);
                imageView.setContentDescription(fileName + " currently at: " + (x + 1) + (y + 1));
                if (!fileName.contains("empty")) {
                    imageView.setImageBitmap(m_Bitmaps.get(fileName));
                }
                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
                layoutParams.columnSpec = GridLayout.spec(y);
                layoutParams.rowSpec = GridLayout.spec(x);
                imageView.setLayoutParams(layoutParams);
                gridLayout.addView(imageView);
            }
        }
    }
}
















