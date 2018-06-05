package com.nulldreams.bemusic.activity;



import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lantouzi.wheelview.WheelView;
import com.nulldreams.bemusic.SleepActivity;
import com.nulldreams.bemusic.Test;
import com.nulldreams.bemusic.widget.*;
import com.nulldreams.bemusic.Intents;
import com.nulldreams.bemusic.R;
import com.nulldreams.bemusic.fragment.AlbumListFragment;
import com.nulldreams.bemusic.fragment.RvFragment;
import com.nulldreams.bemusic.fragment.SongListFragment;
import com.nulldreams.bemusic.play.SimpleAgent;
import com.nulldreams.media.manager.PlayManager;
import com.nulldreams.media.manager.ruler.Rule;
import com.nulldreams.media.manager.ruler.Rulers;
import com.nulldreams.media.model.Album;
import com.nulldreams.media.model.Song;
import com.nulldreams.media.service.PlayService;
import com.nulldreams.bemusic.widget.ProgressBar;
import com.nulldreams.bemusic.widget.RatioImageView;
import com.nulldreams.media.utils.MediaUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;



public class MainActivity extends AppCompatActivity
        implements PlayManager.Callback, PlayManager.ProgressCallback{

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static final String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;
    private CoordinatorLayout mCoorLayout;
    private Toolbar mTb;
    private ViewPager mVp;
    private TabLayout mTl;
    private View mMiniPanel, mSongInfoLayout;
    private ImageView mMiniThumbIv, mPlayPauseIv, mPreviousIv, mNextIv, mAvatarIv;
    private TextView mMiniTitleTv, mMiniArtistAlbumTv;
    private ProgressBar mMiniPb;
    private NavigationView mNavView;
    private RatioImageView mHeaderCover;

    private int mLength = 2;
    private RvFragment[] mFragmentArray = null;

    //睡眠窗口
    public static int themeColor = Color.parseColor("#B24242");
    boolean isSleepTimerEnabled = false;
    public Context ctx;
    public static NotificationManager notificationManager;
    public Activity main;
    private boolean isResumed;
    private ActionBarDrawerToggle mDrawerToggle;

    List<String> minuteList; //分钟

    boolean isSleepTimerTimeout = false;
    long timerSetTime = 0;
    int timerTimeOutDuration = 0;
    DownloadManager.Request request;
    public static boolean DEF_STREAM_ONLY_ON_WIFI;
    private boolean streamOnlyOnWifiEnabled = DEF_STREAM_ONLY_ON_WIFI;
  //  DEF_STREAM_ONLY_ON_WIFI

    Handler sleepHandler;




    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int id = v.getId();
            if (id == mPlayPauseIv.getId()) {
                PlayManager.getInstance(v.getContext()).dispatch();
            } else if (id == mMiniPanel.getId()) {
                showPlayDetail();
            } else if (id == mPreviousIv.getId()) {
                PlayManager.getInstance(v.getContext()).previous();
            } else if (id == mNextIv.getId()) {
                PlayManager.getInstance(v.getContext()).next();
            } else if (id == mHeaderCover.getId()) {

                //点击头像，设置监听器实现URL的跳转...
                Intents.openUrl(MainActivity.this, "https://github.com/KuiperSirius/Minor_Music");
            }
        }
    };
    public void setStreamOnlyOnWifiEnabled(boolean streamOnlyOnWifiEnabled) {
        this.streamOnlyOnWifiEnabled = streamOnlyOnWifiEnabled;
    }
    private NavigationView.OnNavigationItemSelectedListener mNavListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            final int id = item.getItemId();
            if (id == R.id.action_github) {
                Intents.openUrl(MainActivity.this, "https://github.com/KuiperSirius/Minor_Music");
            } else if (id == R.id.action_star_me) {
                //Intents.viewMyAppOnStore(MainActivity.this);
                Intents.openUrl(MainActivity.this, "https://www.duskwood.net/android_develop_music_player/");
            } else if (id == R.id.action_help) {
                final String message = getString(R.string.text_help);
                TextView messageTv = new TextView(MainActivity.this);
                final int padding = (int)(getResources().getDisplayMetrics().density * 24);
                messageTv.setPadding(padding, padding, padding, padding);
                messageTv.setAutoLinkMask(Linkify.WEB_URLS);
                messageTv.setText(message);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.title_menu_help)
                        .setView(messageTv)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();

            }  /*应用程序Wifi设定
            郭一昊 2018-05-30
                    */
            else if (id == R.id.action_wifi){
            //    Intent intent=new Intent(MainActivity.this, Configuration.class);
             //startActivityForResult(intent,0x11);
                final String message = getString(R.string.title_menu_wifiInfo);
                TextView messageTv = new TextView(MainActivity.this);
                final int padding = (int)(getResources().getDisplayMetrics().density * 24);
                messageTv.setPadding(padding, padding, padding, padding);
                messageTv.setAutoLinkMask(Linkify.WEB_URLS);
                messageTv.setText(message);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.title_menu_wifiTitle)
                        .setView(messageTv)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                /*
                                 * 第一个参数：当前上下午的环境。可用getApplicationContext()或this
                                 * 第二个参数：要显示的字符串
                                 * 第三个参数：显示时间的长短。Toast有默认的两个LENGTH_SHORT（短）和LENGTH_LONG（长），也可以使用毫秒2000ms
                                 * */
                                /*调用DownloadManager.Request设置下载，思路来自 https://blog.csdn.net/qingzi635533/article/details/17398735
                                2018-05-31
                                By 郭一昊
                             Ps:   request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
表示下载允许的网络类型，默认在任何网络下都允许下载。有NETWORK_MOBILE、NETWORK_WIFI、NETWORK_BLUETOOTH三种及其组合可供选择。如果只允许wifi下载，而当前网络为3g，则下载会等待。
request.setAllowedOverRoaming(boolean allow)移动网络情况下是否允许漫游。
                                 */
                              //  DownloadManager.Request request=null;// new DownloadManager.Request(Uri.parse(apkUrl));
                                String APK_URL="该字段为您自定义的下载链接";
                               // request=new DownloadManager.Request(Uri.parse(APK_URL));
                              //  request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
                                setStreamOnlyOnWifiEnabled(true);
                                Toast.makeText(MainActivity.this,"您的设置已经生效",Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel,null)
                        .show();
            }//定义音乐播放器的睡眠时间
            else if (id == R.id.action_sleep){
                Intent intent=new Intent(MainActivity.this, SleepActivity.class);
                startActivityForResult(intent,0x11);
                //showSleepDialog();
            }
            mDrawerLayout.closeDrawers();
            return true;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*定义睡眠时间对话框

        2018-05-29 13:18
        郭一昊*/
    public void showSleepDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
       // dialog.setContentView(R.layout.sleep_timer_dialog);

        final WheelView wheelPicker = (WheelView) dialog.findViewById(R.id.wheelPicker);
  /*      wheelPicker.setItems(minuteList);

        TextView title = (TextView) dialog.findViewById(R.id.sleep_dialog_title_text);
//预览界面.应用程序加载使用
        /*if (SplashActivity.tf4 != null)
            title.setTypeface(SplashActivity.tf4);
--
        Button setBtn = (Button) dialog.findViewById(R.id.set_button);
        Button cancelBtn = (Button) dialog.findViewById(R.id.cancel_button);
        final Button removerBtn = (Button) dialog.findViewById(R.id.remove_timer_button);

        final LinearLayout buttonWrapper = (LinearLayout) dialog.findViewById(R.id.button_wrapper);

        final TextView timerSetText = (TextView) dialog.findViewById(R.id.timer_set_text);

        setBtn.setBackgroundColor(themeColor);
        removerBtn.setBackgroundColor(themeColor);
        cancelBtn.setBackgroundColor(Color.WHITE);

        if (isSleepTimerEnabled) {
            wheelPicker.setVisibility(View.GONE);
            buttonWrapper.setVisibility(View.GONE);
            removerBtn.setVisibility(View.VISIBLE);
            timerSetText.setVisibility(View.VISIBLE);

            long currentTime = System.currentTimeMillis();
            long difference = currentTime - timerSetTime;

            int minutesLeft = (int) (timerTimeOutDuration - ((difference / 1000) / 60));
            if (minutesLeft > 1) {
                timerSetText.setText("Timer set for " + minutesLeft + " minutes from now.");
            } else if (minutesLeft == 1) {
                timerSetText.setText("Timer set for " + 1 + " minute from now.");
            } else {
                timerSetText.setText("Music will stop after completion of current song");
            }

        } else {
            wheelPicker.setVisibility(View.VISIBLE);
            buttonWrapper.setVisibility(View.VISIBLE);
            removerBtn.setVisibility(View.GONE);
            timerSetText.setVisibility(View.GONE);
        }

        removerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSleepTimerEnabled = false;
                isSleepTimerTimeout = false;
                timerTimeOutDuration = 0;
                timerSetTime = 0;
                sleepHandler.removeCallbacksAndMessages(null);
                Toast.makeText(ctx, "Timer removed", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSleepTimerEnabled = true;
                int minutes = Integer.parseInt(wheelPicker.getItems().get(wheelPicker.getSelectedPosition()));
                timerTimeOutDuration = minutes;
                timerSetTime = System.currentTimeMillis();
                sleepHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isSleepTimerTimeout = true;
                       // if (playerFragment.mMediaPlayer == null || !playerFragment.mMediaPlayer.isPlaying()) {

                            main.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ctx, "Sleep timer timed out, closing app", Toast.LENGTH_SHORT).show();
                                  /*  if (playerFragment != null && playerFragment.timer != null)
                                        playerFragment.timer.cancel();
                                    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    try {
                                        notificationManager.cancel(1);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        finish();
                                    }
                                }
                            });
                      //  }
                    }
                }, minutes * 60 * 1000);
                Toast.makeText(ctx, "Timer set for " + minutes + " minutes", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSleepTimerEnabled = false;
                isSleepTimerTimeout = false;
                dialog.dismiss();
            }
        });
*/
        dialog.show();

    }




//    private PlayDetailFragment mDetailFragment = PlayDetailFragment.newInstance();
    private void showPlayDetail () {
        Intent it = new Intent(this, PlayDetailActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Pair<View, String> thumb = new Pair<View, String>(mMiniThumbIv, getString(R.string.translation_thumb));
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(this, thumb);
            startActivity(it, options.toBundle());
        } else {
            startActivity(it);
        }
        overridePendingTransition(R.anim.anim_bottom_in, 0);
    }







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        minuteList = new ArrayList<>();
        for (int i = 1; i < 25; i++) {
            minuteList.add(String.valueOf(i * 5));
        }

        sleepHandler = new Handler();


        mDrawerLayout = (DrawerLayout)findViewById(R.id.main_drawer);
        mCoorLayout = (CoordinatorLayout)findViewById(R.id.main_coordinator_layout);
        mTb = (Toolbar)findViewById(R.id.main_tool_bar);
        mVp = (ViewPager)findViewById(R.id.main_view_pager);
        mTl = (TabLayout)findViewById(R.id.main_tab_layout);

        mMiniPanel = findViewById(R.id.main_mini_panel);
        mMiniThumbIv = (ImageView)findViewById(R.id.main_mini_thumb);
        mSongInfoLayout = findViewById(R.id.main_mini_song_info_layout);
        mMiniTitleTv = (TextView)findViewById(R.id.main_mini_title);
        mMiniArtistAlbumTv = (TextView)findViewById(R.id.main_mini_artist_album);

        mPlayPauseIv = (ImageView)findViewById(R.id.main_mini_action_play_pause);
        mPreviousIv = (ImageView)findViewById(R.id.main_mini_action_previous);
        mNextIv = (ImageView)findViewById(R.id.main_mini_action_next);

        mMiniPb = (ProgressBar)findViewById(R.id.main_mini_progress_bar);

        mNavView = (NavigationView)findViewById(R.id.main_nav);
        mHeaderCover = (RatioImageView)mNavView.getHeaderView(0).findViewById(R.id.header_cover);
        mAvatarIv = (ImageView)mNavView.getHeaderView(0).findViewById(R.id.header_avatar);
        mHeaderCover.setOnClickListener(mClickListener);

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
            );
        }*/

        setSupportActionBar(mTb);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mTb, R.string.app_name, R.string.title_song_list);

        mMiniPanel.setOnClickListener(mClickListener);
        mPlayPauseIv.setOnClickListener(mClickListener);
        mPreviousIv.setOnClickListener(mClickListener);
        mNextIv.setOnClickListener(mClickListener);

        mNavView.setNavigationItemSelectedListener(mNavListener);

        Glide.with(this).load(R.drawable.avatar).asBitmap()
                .transform(new CropCircleTransformation(this)).into(mAvatarIv);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        } else {
            init();
            MediaUtils.getAlbumList(this);
        }
    }

    private void init () {

        mFragmentArray = new RvFragment[mLength];
        mFragmentArray[0] = new SongListFragment();
        mFragmentArray[1] = new AlbumListFragment();

        mVp.setAdapter(new VpAdapter(getSupportFragmentManager()));
        mTl.setupWithViewPager(mVp);

        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
        int id = sharedPreferences.getInt("rule", 0);
        Rule rule = null;
        switch (id) {
            case 0:
                rule = Rulers.RULER_LIST_LOOP;
                break;
            case 1:
                rule = Rulers.RULER_SINGLE_LOOP;
                break;
            case 2:
                rule = Rulers.RULER_RANDOM;
                break;
        }
        PlayManager.getInstance(this).setRule(rule);
        PlayManager.getInstance(this).setNotificationAgent(new SimpleAgent());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init();
                MediaUtils.getAlbumList(this);
            } else {
                new AlertDialog.Builder(this)
                        .setMessage(R.string.text_permission)
                        .setPositiveButton(android.R.string.ok, null)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                finish();
                            }
                        })
                        .show();
            }
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mNavView)) {
            mDrawerLayout.closeDrawers();
            return;
        }
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            moveTaskToBack(true);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPlayPauseIv.setSelected(PlayManager.getInstance(this).isPlaying());
        Song song = PlayManager.getInstance(this).getCurrentSong();
        showSong(song);

        PlayManager.getInstance(this).registerCallback(this);
        PlayManager.getInstance(this).registerProgressCallback(this);
        //PlayManager.getInstance(this).unlockScreenControls();
        isResumed = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        PlayManager.getInstance(this).unregisterCallback(this);
        PlayManager.getInstance(this).unregisterProgressCallback(this);
        //PlayManager.getInstance(this).lockScreenControls();
        isResumed = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDrawerLayout.removeDrawerListener(mDrawerToggle);
    }

    @Override
    public void onPlayListPrepared(List<Song> songs) {
    }

    @Override
    public void onAlbumListPrepared(List<Album> albums) {

    }

    @Override
    public void onPlayStateChanged(@PlayService.State int state, Song song) {
        switch (state) {
            case PlayService.STATE_INITIALIZED:
                showSong(song);
                break;
            case PlayService.STATE_STARTED:
                mPlayPauseIv.setSelected(PlayManager.getInstance(this).isPlaying());
                break;
            case PlayService.STATE_PAUSED:
                mPlayPauseIv.setSelected(PlayManager.getInstance(this).isPlaying());
                break;
            case PlayService.STATE_STOPPED:
                mPlayPauseIv.setSelected(PlayManager.getInstance(this).isPlaying());
                break;
            case PlayService.STATE_COMPLETED:
                mPlayPauseIv.setSelected(PlayManager.getInstance(this).isPlaying());
                break;
            case PlayService.STATE_RELEASED:
                mPlayPauseIv.setSelected(PlayManager.getInstance(this).isPlaying());
                mMiniPb.setProgress(0);
                break;
            case PlayService.STATE_ERROR:
                mPlayPauseIv.setSelected(PlayManager.getInstance(this).isPlaying());
                mMiniPb.setProgress(0);
                break;
        }
    }

    @Override
    public void onShutdown() {

    }

    @Override
    public void onPlayRuleChanged(Rule rule) {

    }

    private void showSong(Song song) {
        if (song != null) {
            mMiniTitleTv.setText(song.getTitle());
            mMiniArtistAlbumTv.setText(song.getArtistAlbum());
            Album album = song.getAlbumObj();
            if (album == null) {
                album = PlayManager.getInstance(this).getAlbum(song.getAlbumId());
            }
            if (album != null) {
                Glide.with(this).load(album.getAlbumArt()).asBitmap().placeholder(R.mipmap.ic_launcher).animate(android.R.anim.fade_in).into(mMiniThumbIv);
                Glide.with(this).load(album.getAlbumArt()).asBitmap().animate(android.R.anim.fade_in).transform(new BlurTransformation(this))
                        .into(mHeaderCover);
            }
        } else {
            mMiniTitleTv.setText(R.string.app_name);
            mMiniArtistAlbumTv.setText(R.string.text_github_name);
            Glide.with(this).load(R.drawable.avatar).asBitmap().animate(android.R.anim.fade_in).into(mMiniThumbIv);
            Glide.with(this).load(R.drawable.avatar).asBitmap().animate(android.R.anim.fade_in).transform(new BlurTransformation(this))
                    .into(mHeaderCover);
        }

    }

    @Override
    public void onProgress(int progress, int duration) {
        if (mMiniPb.getMax() != duration) {
            mMiniPb.setMax(duration);
        }
        mMiniPb.setProgress(progress);
        //mProgressDurationTv.setText(MediaUtils.formatTime(progress) + "/" + MediaUtils.formatTime(duration));
        //mProgressBar.setProgress(progress);
        //Log.v(TAG, "onProgress progress=" + progress + " duration=" + duration);
    }

    private class VpAdapter extends FragmentStatePagerAdapter {

        public VpAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    if (mFragmentArray[0] == null) {
                        mFragmentArray[0] = new SongListFragment();
                    }
                    return mFragmentArray[0];
                case 1:
                    if (mFragmentArray[1] == null) {
                        mFragmentArray[1] = new AlbumListFragment();
                    }
                    return mFragmentArray[1];
            }
            return null;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            RvFragment fragment = (RvFragment)super.instantiateItem(container, position);
            mFragmentArray[position] = fragment;
            return fragment;
        }

        @Override
        public int getCount() {
            return mFragmentArray.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentArray[position].getTitle(MainActivity.this);
        }
    }
}
