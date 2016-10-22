package com.ea7jmf.nytarticles.activies;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ea7jmf.nytarticles.R;
import com.ea7jmf.nytarticles.adapters.ArticlesAdapter;
import com.ea7jmf.nytarticles.apis.NYTArticleSearchApiEndpoint;
import com.ea7jmf.nytarticles.models.Doc;
import com.ea7jmf.nytarticles.models.SearchQuery;
import com.ea7jmf.nytarticles.thirdparty.EndlessRecyclerViewScrollListener;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SearchActivity extends AppCompatActivity {

    public static String TAG = "SearchActivity";
    public static String NYT_API_BASE = "https://api.nytimes.com/";

    private NYTArticleSearchApiEndpoint apiService;
    private String nytApiKey;

    private SearchQuery query;

    private ArrayList<Doc> articles;
    private ArticlesAdapter articlesAdapter;

    @BindView(R.id.rvSearchResults) RecyclerView rvSearchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

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

        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NYT_API_BASE)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(rxAdapter)
                .build();

        apiService = retrofit.create(NYTArticleSearchApiEndpoint.class);

        query = new SearchQuery.Builder("obama")
                .build();

        articles = new ArrayList<>();
        articlesAdapter = new ArticlesAdapter(this, articles);
        rvSearchResults.setAdapter(articlesAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvSearchResults.setLayoutManager(layoutManager);
        rvSearchResults.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                getArticlesByQuery(
                        new SearchQuery.Builder(query)
                                .page(page)
                                .build()
                );
            }
        });
        articlesAdapter.getOnClickSubject().subscribe(
                doc -> {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(this, Uri.parse(doc.getWebUrl()));
                },
                throwable -> {},
                () -> {}
        );

        getArticlesByQuery(query);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryString) {
                // perform query here

                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();
                int size = articles.size();
                articles.clear();
                articlesAdapter.notifyItemRangeRemoved(0, size);
                getArticlesByQuery(
                        new SearchQuery.Builder(query)
                            .query(queryString)
                            .build()
                );
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void getArticlesByQuery(SearchQuery query) {
        Observable<Doc> call = apiService
                .articleSearch(
                        query.getQuery(),
                        query.getPage(),
                        query.getFormattedBeginDate(),
                        query.getFormattedSort(),
                        null,
                        nytApiKey)
                .flatMap(searchResponse -> rx.Observable.from(searchResponse.getResponse().getDocs()));

        Subscription subscription = call
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        article -> {
                            articles.add(article);
                            articlesAdapter.notifyItemInserted(articles.size() - 1);
                        },
                        throwable -> Log.e(TAG, "HTTP call failed", throwable),
                        () -> Log.i(TAG, "HTTP fetch complete")
                );
    }
}
