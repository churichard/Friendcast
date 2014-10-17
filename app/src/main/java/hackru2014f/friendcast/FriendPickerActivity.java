package hackru2014f.friendcast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FriendPickerActivity extends Activity {
    public static final String NAME = "hackru2014f.friendcast.USER_NAME";

    private FriendListAdapter friendListAdapter;
    private ArrayList<User> friendList;
    private String fbname;
    private String restaurant;
    private String vicinity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_picker);

        Intent intent = getIntent();
        fbname = intent.getStringExtra(NAME);
        restaurant = intent.getStringExtra(RestaurantPickerActivity.RESTAURANT);
        vicinity = intent.getStringExtra(RestaurantPickerActivity.VICINITY);

        friendList = new ArrayList<User>();
        friendListAdapter = new FriendListAdapter(getApplicationContext(), R.layout.friend_list_item, friendList);
        ListView friendListView = (ListView) findViewById(R.id.friendPickerListView);
        friendListView.setAdapter(friendListAdapter);

        new Request(
                Session.getActiveSession(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        try {
                            JSONArray friends = response.getGraphObject().getInnerJSONObject().getJSONArray("data");

                            for(int i = 0; i < friends.length(); i++) {
                                String name = friends.getJSONObject(i).getString("name");
                                String id = friends.getJSONObject(i).getString("id");

                                User user = new User(name, id);
                                friendList.add(user);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        friendListAdapter.notifyDataSetChanged();
                    }
                }
        ).executeAsync();
    }

    public void invite(View v) {
        ListView friendListView = (ListView) findViewById(R.id.friendPickerListView);

        for(int i = 0; i < friendList.size(); i++) {
            CheckBox checkBox = (CheckBox) friendListView.getChildAt(i).findViewById(R.id.friendListItemCheckbox);

            if(checkBox.isChecked()) {
                pushToFbId(friendList.get(i).id, fbname, restaurant, vicinity);
            }
        }
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

    public void pushToFbId(String fbId, String fbName, String placeName, String vicinity) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("fbid", fbId);
        params.put("fbname", fbName);
        params.put("placename", placeName);
        params.put("vicinity", vicinity);

        ParseCloud.callFunctionInBackground("push", params, null);
    }

    private class FriendListAdapter extends ArrayAdapter<User> {
        private List<User> users;

        public FriendListAdapter(Context context, int resource, List<User> users) {
            super(context, resource, users);
            this.users = users;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.friend_list_item, null);
            }

            ((TextView) convertView.findViewById(R.id.friendListItemName)).setText(users.get(position).name);
            return convertView;
        }
    }

    private class User {
        String name, id;

        public User(String name, String id) {
            this.name = name;
            this.id = id;
        }
    }
}