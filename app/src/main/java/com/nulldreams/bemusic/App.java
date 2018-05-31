package com.nulldreams.bemusic;

import android.app.Application;
import android.content.SharedPreferences;

import com.nulldreams.bemusic.play.SimpleAgent;
import com.nulldreams.media.manager.PlayManager;
import com.nulldreams.media.manager.ruler.Rule;
import com.nulldreams.media.manager.ruler.Rulers;
import com.squareup.leakcanary.LeakCanary;

/*

Created by
Yihao Guo
2018-06-01
思路来自CSDN论坛
Ps:https://blog.csdn.net/liuhongwei123888/article/details/50454871
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance(this);

        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        //可使内存泄漏检测工具LeakCanary生效
       // LeakCanary.install(this);
        //测试GitHub push是否生效
    }
}
