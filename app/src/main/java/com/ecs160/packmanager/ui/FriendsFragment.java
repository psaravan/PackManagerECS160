package com.ecs160.packmanager.ui;

import android.content.Intent;
import android.net.Uri;
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
import com.ecs160.packmanager.utils.User;
import com.ecs160.packmanager.views.FriendsListAdapter;
import com.melnykov.fab.FloatingActionButton;

import java.util.ArrayList;


/**
 * Fragment that displays all the current user's friends.
 */
public class FriendsFragment extends Fragment {

    private ListView mFriendsList;
    private FloatingActionButton mFAB;
    private static ArrayList<User> list;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FriendsFragment newInstance() {
        FriendsFragment fragment = new FriendsFragment();
        return fragment;
    }

    public FriendsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendsList = (ListView) view.findViewById(R.id.friends_list);
        mFAB = (FloatingActionButton) view.findViewById(R.id.friends_fab);
        mFAB.attachToListView(mFriendsList);
        mFAB.setOnClickListener(clickListener);

        list = App.getDBAccessHelper().getAllFriends();
        FriendsListAdapter adapter = new FriendsListAdapter(getActivity(), R.layout.row_layout_friend, list);
        mFriendsList.setAdapter(adapter);

        mFriendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String receiver = list.get(position).getUsername();
                Intent intent = new Intent(App.getContext(), AddNewPackageActivity.class);
                intent.putExtra("receiver", receiver);
                startActivity(intent);
            }
        });

        mFriendsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String phoneNumber = list.get(position).getPhoneNumber();
                String name = list.get(position).getRealName();
                Toast.makeText(App.getContext(), "Opening texting app to send feedback to " + name + ".", Toast.LENGTH_LONG).show();

                Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse("sms:"));
                sendIntent.putExtra("address", phoneNumber);
                startActivity(sendIntent);

                return true;
            }
        });

        return view;
    }
    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(App.getContext(), AddFriendActivity.class);
            startActivity(intent);
        }
    };
}
