package com.ea7jmf.nytarticles.activies;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.ea7jmf.nytarticles.R;
import com.ea7jmf.nytarticles.adapters.ArticlesAdapter;
import com.ea7jmf.nytarticles.apis.NYTArticleSearchApiEndpoint;
import com.ea7jmf.nytarticles.models.Doc;
import com.ea7jmf.nytarticles.models.SearchResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.HttpException;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class SearchActivity extends AppCompatActivity {

    public static String TAG = "SearchActivity";
    public static String NYT_API_BASE = "https://api.nytimes.com/";
    private String nytApiKey;
    private ArrayList<Doc> articles;
    private ArticlesAdapter articlesAdapter;

    @BindView(R.id.rvSearchResults) RecyclerView rvSearchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        articles = new ArrayList<>();
        articlesAdapter = new ArticlesAdapter(this, articles);
        rvSearchResults.setAdapter(articlesAdapter);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));

        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            nytApiKey = bundle.getString("NYT_API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
        }

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();

        RxJavaCallAdapterFactory rxAdapter = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NYT_API_BASE)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(rxAdapter)
                .build();



        NYTArticleSearchApiEndpoint apiService =
                retrofit.create(NYTArticleSearchApiEndpoint.class);

        Observable<Doc> call = apiService.articleSearch("20160112", "oldest", "news_desk:(\"Education\"%20\"Health\")", nytApiKey)
                .flatMap(new Func1<SearchResponse, Observable<Doc>>() {
                    @Override
                    public Observable<Doc> call(SearchResponse searchResponse) {
                        return rx.Observable.from(searchResponse.getResponse().getDocs());
                    }
                });

        Subscription subscription = call
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Doc>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        // cast to retrofit.HttpException to get the response code
                        if (e instanceof HttpException) {
                            HttpException response = (HttpException)e;
                            int code = response.code();
                            Log.e(TAG, "HTTP call failed with code " + code, e);
                        }
                    }

                    @Override
                    public void onNext(Doc response) {
                        Log.i(TAG, response.toString());
                        articles.add(response);
                        articlesAdapter.notifyItemInserted(articles.size() - 1);
                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here

                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();

                Toast.makeText(SearchActivity.this, query, Toast.LENGTH_LONG).show();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}
