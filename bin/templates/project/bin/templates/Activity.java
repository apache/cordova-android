package __ID__;

import android.app.Activity;
import android.os.Bundle;
import com.phonegap.*;

public class __ACTIVITY__ extends DroidGap
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        super.loadUrl("file:///android_asset/www/index.html");
    }
}

