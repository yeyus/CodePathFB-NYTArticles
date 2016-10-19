package com.ea7jmf.nytarticles.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ea7jmf.nytarticles.R;
import com.ea7jmf.nytarticles.models.Doc;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jesusft on 10/18/16.
 */

public class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvHeadline) TextView tvHeadline;
        @BindView(R.id.tvSnippet) TextView tvSnippet;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bind(Doc doc) {
            if (doc.getHeadline() != null && doc.getHeadline().getMain() != null) {
                tvHeadline.setText(doc.getHeadline().getMain());
            }
            if (doc.getSnippet() != null) {
                tvSnippet.setText(doc.getSnippet());
            }
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
        holder.bind(doc);
    }

    @Override
    public int getItemCount() {
        return mDocs.size();
    }
}
