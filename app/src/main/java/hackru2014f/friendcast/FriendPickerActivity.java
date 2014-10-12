package hackru2014f.friendcast;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class FriendPickerActivity extends Activity {
    FriendListAdapter friendListAdapter;
    ArrayList<User> friendList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_picker);

        friendList = new ArrayList<User>();
        friendListAdapter = new FriendListAdapter(getApplicationContext(), R.layout.friend_list_item, friendList);
        ListView friendListView = (ListView) findViewById(R.id.friendListView);
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
                            Log.d("Friends", friends.length() + "");

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
