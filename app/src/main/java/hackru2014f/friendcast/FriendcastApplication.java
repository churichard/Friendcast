package hackru2014f.friendcast;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;

public class FriendcastApplication extends Application {
    public static final String parseAppId = "DYNquOP8ZIpkdkuEXi3iisJsZUpgnk8Kftz4b6D9";
    public static final String parseClientKey = "WfQ392MmrJs8LAxhkCpm66CNtUILqT8gsdXAHwsX";

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this, parseAppId, parseClientKey);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

}
