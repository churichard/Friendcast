package hackru2014f.friendcast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RestaurantPickerActivity extends Activity {
    public static final String NAME = "hackru2014f.friendcast.USER_NAME";
    public static final String RESTAURANT = "hackru2014f.friendcast.RESTAURANT";
    public static final String VICINITY = "hackru2014f.friendcast.VICINITY";

    private RestaurantAdapter restaurantAdapter;
    private ArrayList<Restaurant> restaurantList;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_picker);

        Intent intent = getIntent();
        name = intent.getStringExtra(NAME);

        restaurantList = new ArrayList<Restaurant>();
        restaurantAdapter = new RestaurantAdapter(this, R.layout.restaurant_list_item, restaurantList);

        new GetLocalRestaurantsTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.restaurant_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void chooseRestaurant(View v) {
        if (restaurantAdapter.selectedIndex != -1) {
            Intent intent = new Intent(this, FriendPickerActivity.class);
            intent.putExtra(NAME, name);
            intent.putExtra(RESTAURANT, restaurantList.get(restaurantAdapter.selectedIndex).name);
            intent.putExtra(VICINITY, restaurantList.get(restaurantAdapter.selectedIndex).vicinity);

            startActivity(intent);
        }
    }

    private class GetLocalRestaurantsTask extends AsyncTask<Void, Void, String> {
        private static final String placesKey = "AIzaSyATQKEJVe6hrEvnNRuMbK68ySMnRM17uYQ";

        @Override
        protected String doInBackground(Void... voids) {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location location = null;
            double latitude;
            double longitude;

            boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isGPSEnabled && location == null) {
                location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (isNetworkEnabled && location == null) {
                location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            } else {
                // Add code to select custom location
                latitude = 40.502660;
                longitude = -74.451676;
            }

            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=" + placesKey
                    + "&location=" + latitude + "," + longitude + "&types=food&rankby=distance";

            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);

            try {
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();

                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }

                    return builder.toString();
                } else {
                    Log.e("Getter", "Failed to download file");
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject response = new JSONObject(result);
                JSONArray results = response.getJSONArray("results");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject jsonObject = results.getJSONObject(i);
                    String name = "", vicinity = "";
                    float rating = -1;

                    if (jsonObject.has("name")) {
                        name = jsonObject.getString("name");
                    }
                    if (jsonObject.has("vicinity")) {
                        vicinity = jsonObject.getString("vicinity");
                    }
                    if (jsonObject.has("rating")) {
                        rating = (float) jsonObject.getDouble("rating");
                    }

                    Restaurant restaurant = new Restaurant(name, vicinity, rating);
                    restaurantList.add(restaurant);
                }

                ListView restaurantListView = (ListView) findViewById(R.id.restaurantPickerListView);
                restaurantListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        restaurantAdapter.setSelectedIndex(position);
                        restaurantAdapter.notifyDataSetChanged();
                    }
                });
                restaurantListView.setAdapter(restaurantAdapter);
                restaurantAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class RestaurantAdapter extends ArrayAdapter<Restaurant> {
        private int selectedIndex;

        public RestaurantAdapter(Context context, int resource, List<Restaurant> restaurants) {
            super(context, resource, restaurants);
            selectedIndex = -1;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.restaurant_list_item, null);
            }

            ((TextView) convertView.findViewById(R.id.restaurantListItemName)).setText(restaurantList.get(position).name);
            ((TextView) convertView.findViewById(R.id.restaurantListItemVicinity)).setText(restaurantList.get(position).vicinity);

            if (restaurantList.get(position).rating > 0) {
                ((RatingBar) convertView.findViewById(R.id.restaurantListItemRating)).setRating(restaurantList.get(position).rating);
                convertView.findViewById(R.id.restaurantListItemRating).setVisibility(View.VISIBLE);
            }

            RadioButton radioButton = (RadioButton) convertView.findViewById(R.id.restaurantListItemRadioButton);
            radioButton.setClickable(false);
            radioButton.setFocusable(false);

            if (selectedIndex == position) {
                radioButton.setChecked(true);
            } else {
                radioButton.setChecked(false);
            }

            convertView.findViewById(R.id.restaurantListItemRadioButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    restaurantAdapter.setSelectedIndex(position);
                }
            });

            return convertView;
        }

        public void setSelectedIndex(int position) {
            selectedIndex = position;
        }
    }

    private class Restaurant {
        private String name, vicinity;
        private float rating;

        public Restaurant(String name, String vicinity, float rating) {
            this.name = name;
            this.vicinity = vicinity;
            this.rating = rating;
        }
    }
}
