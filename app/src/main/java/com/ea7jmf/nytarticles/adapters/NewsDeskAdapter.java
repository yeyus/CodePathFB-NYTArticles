package com.ea7jmf.nytarticles.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import com.ea7jmf.nytarticles.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsDeskAdapter extends ArrayAdapter<String> {

    private Map<String, Boolean> newsDesks;

    @BindView(R.id.cbDesk) CheckBox cbDesk;

    public NewsDeskAdapter(Context context) {
        super(context, 0, context.getResources().getStringArray(R.array.news_desk_array));
        String[] deskEntries = context.getResources().getStringArray(R.array.news_desk_array);
        newsDesks = new HashMap<>();
        for (String deskEntry : deskEntries) {
            newsDesks.put(deskEntry, false);
        }
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String newsDesk = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_news_desk, parent, false);
            ButterKnife.bind(this, convertView);
        }

        cbDesk.setText(newsDesk);
        cbDesk.setOnCheckedChangeListener((compoundButton, b) -> newsDesks.put(newsDesk, b));

        return convertView;
    }

    public List<String> getCheckedItems() {
        ArrayList<String> checkedItems = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : newsDesks.entrySet()) {
            if (entry.getValue()) {
                checkedItems.add(entry.getKey());
            }
        }

        return checkedItems;
    }

    @Override
    public int getCount() {
        return newsDesks.size();
    }
}
