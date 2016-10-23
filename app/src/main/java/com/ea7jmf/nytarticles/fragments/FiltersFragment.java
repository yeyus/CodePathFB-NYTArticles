package com.ea7jmf.nytarticles.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Switch;

import com.ea7jmf.nytarticles.R;
import com.ea7jmf.nytarticles.adapters.NewsDeskAdapter;
import com.ea7jmf.nytarticles.models.SearchQuery;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FiltersFragment extends Fragment {

    @BindView(R.id.swBeginDate) Switch swBeginDate;
    @BindView(R.id.dpBeginDate) DatePicker dpBeginDate;
    @BindView(R.id.swEndDate) Switch swEndDate;
    @BindView(R.id.dpEndDate) DatePicker dpEndDate;
    @BindView(R.id.swSort) Switch swSort;
    @BindView(R.id.spSort) AppCompatSpinner spSort;
    ListView lvNewsDesk;

    NewsDeskAdapter ndAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filters, parent, false);
        View headerView = inflater.inflate(R.layout.header_filters, null);
        ButterKnife.bind(this, headerView);
        lvNewsDesk = (ListView) view.findViewById(R.id.lvNewsDesks);
        lvNewsDesk.addHeaderView(headerView);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        dpBeginDate.setVisibility(View.GONE);
        dpEndDate.setVisibility(View.GONE);
        spSort.setVisibility(View.GONE);
        swBeginDate.setOnCheckedChangeListener((compoundButton, b) -> dpBeginDate.setVisibility(b ? View.VISIBLE : View.GONE));
        swEndDate.setOnCheckedChangeListener((compoundButton, b) -> dpEndDate.setVisibility(b ? View.VISIBLE : View.GONE));
        swSort.setOnCheckedChangeListener((compoundButton, b) -> spSort.setVisibility(b ? View.VISIBLE : View.GONE));

        ndAdapter = new NewsDeskAdapter(getContext());
        lvNewsDesk.setAdapter(ndAdapter);
    }

    public SearchQuery getFilters() {
        SearchQuery.Builder builder = new SearchQuery.Builder();

        if (swBeginDate.isChecked()) {
            final Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, dpBeginDate.getYear());
            c.set(Calendar.MONTH, dpBeginDate.getMonth());
            c.set(Calendar.DAY_OF_MONTH, dpBeginDate.getDayOfMonth());
            builder.beginDate(c.getTime());
        }

        if (swEndDate.isChecked()) {
            final Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, dpEndDate.getYear());
            c.set(Calendar.MONTH, dpEndDate.getMonth());
            c.set(Calendar.DAY_OF_MONTH, dpEndDate.getDayOfMonth());
            builder.endDate(c.getTime());
        }

        if (swSort.isChecked()) {
            if (spSort.getSelectedItemPosition() == 0) {
                builder.sort(SearchQuery.SortDirection.NEWEST);
            } else {
                builder.sort(SearchQuery.SortDirection.OLDEST);
            }
        }

        if (ndAdapter.getCheckedItems().size() > 0) {
            builder.newsDesks(ndAdapter.getCheckedItems());
        }

        return builder.build();
    }
}
