package com.ea7jmf.nytarticles.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ea7jmf.nytarticles.R;
import com.ea7jmf.nytarticles.models.Doc;

import java.util.List;
import java.util.NoSuchElementException;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;

import static com.bumptech.glide.load.engine.DiskCacheStrategy.NONE;

public class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.ViewHolder> {

    public static final String TAG = "ArticlesAdapter";

    private final PublishSubject<Doc> onClickSubject = PublishSubject.create();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.card) CardView card;
        @BindView(R.id.ivThumbnail) ImageView ivThumbnail;
        @BindView(R.id.tvHeadline) TextView tvHeadline;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bind(Doc doc, Context context) {
            if (doc.getHeadline() != null && doc.getHeadline().getMain() != null) {
                tvHeadline.setText(doc.getHeadline().getMain());
            }

            Observable<String> thumbnail = Observable.from(doc.getMultimedia())
                    .filter(multimedia -> multimedia.getWidth() > 150)
                    .map(multimedium -> String.format("https://nytimes.com/%s", multimedium.getUrl()))
                    .first();

            ivThumbnail.setVisibility(View.GONE);
            thumbnail.subscribe(
                    thumbUrl -> {
                        Glide.with(context)
                                .load(thumbUrl)
                                .diskCacheStrategy(NONE)
                                .skipMemoryCache(true)
                                .into(ivThumbnail);
                        ivThumbnail.setVisibility(View.VISIBLE);
                    },
                    throwable -> {
                        if (throwable instanceof NoSuchElementException) {
                            ivThumbnail.setVisibility(View.GONE);
                            return;
                        }
                        Log.i(TAG, "error while filtering thumbnail", throwable);
                    },
                    () -> {}
            );

        }
    }

    private List<Doc> mDocs;
    private Context mContext;

    public ArticlesAdapter(Context context, List<Doc> articles) {
        mDocs = articles;
        mContext = context;
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View articleView = inflater.inflate(R.layout.item_article, parent, false);
        ViewHolder vh = new ViewHolder(articleView);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Doc doc = mDocs.get(position);
        holder.bind(doc, getContext());
        holder.card.setOnClickListener(v -> onClickSubject.onNext(doc));
    }

    @Override
    public int getItemCount() {
        return mDocs.size();
    }

    public PublishSubject<Doc> getOnClickSubject() {
        return onClickSubject;
    }
}
