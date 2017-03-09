package makers.ar_d.bandara;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;

    EditText keywordText;
    Button searchButton;

    // URL to get contacts JSON
    private static String url = "http://crocodic.net/linda/public/json.json";
    private static String urlget = "";

    ArrayList<HashMap<String, String>> bandaraList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bandaraList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);

        keywordText = (EditText) findViewById(R.id.keywords_edit_text);
        searchButton = (Button) findViewById(R.id.search_button);

        new GetBandaras().execute();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String src = keywordText.getText().toString();
                src.replace(" ", "+");
                urlget = url + src;
                GetBandaras task = new GetBandaras();
                task.execute();

                pDialog.show();

            }
        });
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetBandaras extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray bandaras = jsonObj.getJSONArray("all_flight");

                    // looping through All Contacts
                    for (int i = 0; i < bandaras.length(); i++) {
                        JSONObject b = bandaras.getJSONObject(i);

                        String name = b.getString("location_name");
                        String label = b.getString("label");

                        // tmp hash map for single contact
                        HashMap<String, String> bandara = new HashMap<>();

                        // adding each child node to HashMap key => value

                        bandara.put("location_name", name);
                        bandara.put("label", label);

                        // adding contact to contact list
                        bandaraList.add(bandara);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, bandaraList,
                    R.layout.list_item, new String[]{"location_name", "label"}, new int[]{R.id.name,
                    R.id.label});

            lv.setAdapter(adapter);
        }

    }
}