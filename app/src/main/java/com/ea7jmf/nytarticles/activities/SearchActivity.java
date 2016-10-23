package com.ea7jmf.nytarticles.activities;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.ea7jmf.nytarticles.NYTApplication;
import com.ea7jmf.nytarticles.R;
import com.ea7jmf.nytarticles.adapters.ArticlesAdapter;
import com.ea7jmf.nytarticles.apis.NYTArticleSearchApiEndpoint;
import com.ea7jmf.nytarticles.fragments.FiltersFragment;
import com.ea7jmf.nytarticles.models.Doc;
import com.ea7jmf.nytarticles.models.SearchQuery;
import com.ea7jmf.nytarticles.thirdparty.EndlessRecyclerViewScrollListener;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class SearchActivity extends AppCompatActivity {

    public static String TAG = "SearchActivity";
    public static String NYT_API_BASE = "https://api.nytimes.com/";

    private NYTArticleSearchApiEndpoint apiService;

    private SearchQuery query;

    private ArrayList<Doc> articles;
    private ArticlesAdapter articlesAdapter;

    private final PublishSubject<SearchQuery> apiRequestSubject = PublishSubject.create();

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer) DrawerLayout drawerLayout;
    @BindView(R.id.rvSearchResults) RecyclerView rvSearchResults;
    FiltersFragment filtersFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        apiService = ((NYTApplication)getApplication()).getNytRetrofit()
                .create(NYTArticleSearchApiEndpoint.class);

        // Create an empty instance of querys
        query = new SearchQuery.Builder().build();

        setSupportActionBar(toolbar);

        setupFiltersDrawer();
        setupRecyclerView();

        apiRequestSubject
                .distinct()
                .sample(1, TimeUnit.SECONDS)
                .subscribe(
                        searchQuery -> getArticlesByQuery(searchQuery),
                        throwable -> Log.e(TAG, "apiRequestSubject error", throwable),
                        () -> Log.i(TAG, "apiRequestSubject complete")
                );
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
                searchView.clearFocus();
                clearResults();
                getSupportActionBar().setTitle(queryString);
                apiRequestSubject.onNext(
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_filters:
                if (drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                    drawerLayout.closeDrawer(Gravity.RIGHT);
                } else {
                    drawerLayout.openDrawer(Gravity.RIGHT);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupRecyclerView() {
        articles = new ArrayList<>();
        articlesAdapter = new ArticlesAdapter(this, articles);
        rvSearchResults.setAdapter(articlesAdapter);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvSearchResults.setLayoutManager(layoutManager);
        rvSearchResults.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                apiRequestSubject.onNext(
                        new SearchQuery.Builder(query)
                                .page(page)
                                .build()
                );
            }
        });

        articlesAdapter.getOnClickSubject().subscribe(
                doc -> {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_share_black_24dp);

                    // Create share intent
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, doc.getWebUrl());

                    // Create pending intent for action
                    PendingIntent pendingIntent = PendingIntent.getActivity(this,
                            100,
                            shareIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    builder.setActionButton(bitmap, getString(R.string.share_link_button), pendingIntent, true);
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(this, Uri.parse(doc.getWebUrl()));
                },
                throwable -> Log.e(TAG, "RecyclerView onClick subject error", throwable),
                () -> Log.i(TAG, "RecyclerView onClick complete")
        );
    }

    private void setupFiltersDrawer() {
        filtersFragment = (FiltersFragment)
                getSupportFragmentManager().findFragmentById(R.id.filtersFragment);

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                clearResults();
                query = new SearchQuery.Builder(filtersFragment.getFilters())
                        .query(query.getQuery())
                        .build();
                apiRequestSubject.onNext(query);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    private void getArticlesByQuery(SearchQuery query) {
        this.query = query;

        if (!isNetworkAvailable()) {
            Snackbar.make(drawerLayout, R.string.no_internet, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry, view -> getArticlesByQuery(query))
                    .show();
            return;
        }

        Observable<Doc> call = apiService
                .articleSearch(
                        query.getQuery(),
                        query.getPage(),
                        query.getFormattedBeginDate(),
                        query.getFormattedEndDate(),
                        query.getFormattedSort(),
                        query.getFormattedNewsDesks(),
                        ((NYTApplication)getApplication()).getNytApiKey())
                .flatMap(searchResponse -> rx.Observable.from(searchResponse.getResponse().getDocs()));

        call.subscribeOn(Schedulers.io())
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

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private void clearResults() {
        int size = articles.size();
        articles.clear();
        articlesAdapter.notifyItemRangeRemoved(0, size);
    }
}
