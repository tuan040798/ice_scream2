package com.icreamguide2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.tabs.TabLayout;
import com.icreamguide2.utils.BackUpModel;
import com.icreamguide2.utils.HttpHandler;
import com.neufkg.adx.service.InterstitialAdsManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import vn.aib.ratedialog.RatingDialog;

public class MainActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private AdView mAdView;
    private BackUpModel backUpModel;
    private String TAG = MainActivity.class.getSimpleName();
    public static String INTER_ID = "ca-app-pub-3940256099942544/1033173712";
    public static String BANNER_ID = "ca-app-pub-3940256099942544/6300978111";
    public static int PERCENT_SHOW_BANNER_AD = 100;
    private InterstitialAdsManager adsManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        setupViewPager();
        tabLayout.setupWithViewPager(viewPager);
        try {
            Void aVoid = new GetBackUp().execute().get();
            if(backUpModel != null){
                if(!backUpModel.isLive){
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Notice from developer")
                            .setMessage("Please update the new application to continue using it. We are really sorry for this issue.")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    showApp(MainActivity.this, backUpModel.newAppPackage);
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setCancelable(false)
                            .show();
                }
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


        adsManager = new InterstitialAdsManager();

        if(backUpModel != null) {
            adsManager.init(true, this, backUpModel.inter, backUpModel.fb_inter, backUpModel.isShowGG, "#282726", getString(R.string.app_name));
        } else {
            adsManager.init(true, this, INTER_ID, "", true, "#282726", getString(R.string.app_name));
        }
        adsManager.setPermissions(new String[]{ Manifest.permission.INTERNET,});
        SharedPreferences prefs = getSharedPreferences("rate_dialog", MODE_PRIVATE);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        Boolean rated = prefs.getBoolean("rate", false);
        if(!rated){
            showRateDialog();
        }


        View adContainer = findViewById(R.id.adMobView);

        AdView mAdView = new AdView(this);
        mAdView.setAdSize(AdSize.BANNER);
        mAdView.setAdUnitId(BANNER_ID);
        ((RelativeLayout)adContainer).addView(mAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Random r = new Random();
        int ads = r.nextInt(100);

        if (ads >= MainActivity.PERCENT_SHOW_BANNER_AD){
            mAdView.destroy();
            mAdView.setVisibility(View.GONE);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        adsManager.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    private void showRateDialog() {
        RatingDialog ratingDialog = new RatingDialog(this);
        ratingDialog.setRatingDialogListener(new RatingDialog.RatingDialogInterFace() {
            @Override
            public void onDismiss() {
            }

            @Override
            public void onSubmit(float rating) {
                rateApp(MainActivity.this);
                SharedPreferences.Editor editor = getSharedPreferences("rate_dialog", MODE_PRIVATE).edit();
                editor.putBoolean("rate", true);
                editor.commit();
            }

            @Override
            public void onRatingChanged(float rating) {
            }
        });
        ratingDialog.showDialog();
    }

    public static void rateApp(Context context) {
        Intent intent = new Intent(new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    private class GetBackUp extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String url = "";
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    String appPackage = jsonObj.getString("appPackage");
                    Boolean isLive = jsonObj.getBoolean("isLive");
                    String newAppPackage = jsonObj.getString("newAppPackage");
                    Boolean isAdsShow = jsonObj.getBoolean("isAdsShow");
                    String inter = jsonObj.getString("inter");
                    String fb_inter = jsonObj.getString("fb_inter");
                    Boolean isShowGG = jsonObj.getBoolean("isShowGG");
                    String banner = jsonObj.getString("banner");
                    String nativeAd = jsonObj.getString("nativeAd");
                    String rewarded = jsonObj.getString("rewarded");

                    backUpModel = new BackUpModel();
                    backUpModel.appPackage = appPackage;
                    backUpModel.isLive = isLive;
                    backUpModel.newAppPackage = newAppPackage;
                    backUpModel.isAdsShow = isAdsShow;
                    backUpModel.inter = inter;
                    backUpModel.fb_inter = fb_inter;
                    backUpModel.isShowGG = isShowGG;
                    backUpModel.banner = banner;
                    backUpModel.nativeAd = nativeAd;
                    backUpModel.rewarded = rewarded;

//                    INTER_ID = backUpModel.inter;
//                    BANNER_ID = backUpModel.banner;

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
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
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    public static void showApp(Context context, String pkg) {
        Intent intent = new Intent(new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=" + pkg)));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new icescream1(), "Ice Scream 1");
        adapter.addFragment(new icescream2(), "Ice Scream 2");
        viewPager.setAdapter(adapter);
    }
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }

}
