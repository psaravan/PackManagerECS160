package com.ecs160.packmanager.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.ecs160.packmanager.R;
import com.ecs160.packmanager.utils.App;
import com.ecs160.packmanager.utils.Transaction;
import com.ecs160.packmanager.views.PackagesAdapter;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;

public class PackagesFragment extends Fragment {

    private static Context mActivity;
    private static ListView mPackagesList;
    private FloatingActionButton mFAB;
    private static PackagesAdapter adapter;
    private static ArrayList<Transaction> list;

    public static PackagesFragment newInstance() {
        PackagesFragment fragment = new PackagesFragment();
        return fragment;
    }

    public PackagesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_packages, container, false);
        mActivity = getActivity();
        mPackagesList = (ListView) view.findViewById(R.id.packages_list);
        mFAB = (FloatingActionButton) view.findViewById(R.id.packages_fab);
        mFAB.attachToListView(mPackagesList);
        mFAB.setOnClickListener(clickListener);

        list = App.getDBAccessHelper().getAllTransactions();
        adapter = new PackagesAdapter(getActivity(), R.layout.row_layout_friend, list);
        mPackagesList.setAdapter(adapter);

        mPackagesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String time = list.get(position).getTimeStamp();
                Toast.makeText(App.getContext(), "Meet at " + time + ".", Toast.LENGTH_LONG).show();
                return false;
            }
        });

        return view;
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(App.getContext(), AddNewPackageActivity.class);
            startActivity(intent);
        }
    };

    public static void updateListView() {
        list = App.getDBAccessHelper().getAllTransactions();
        adapter = new PackagesAdapter(mActivity, R.layout.row_layout_friend, list);
        mPackagesList.setAdapter(adapter);
        mPackagesList.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateListView();
    }

}
