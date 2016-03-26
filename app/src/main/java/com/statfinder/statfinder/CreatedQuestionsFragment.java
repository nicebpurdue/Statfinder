package com.statfinder.statfinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class CreatedQuestionsFragment extends Fragment{

    public CreatedQuestionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final FragmentActivity faActivity = (FragmentActivity) super.getActivity();
        RelativeLayout llLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_created_history, container, false);
        final ListView createdList = (ListView) llLayout.findViewById(R.id.createdList);
        final ArrayList<HashMap<String, Object>> createdQuestions = new ArrayList();
        final HistoryAdapter createdAdapter = new HistoryAdapter(createdQuestions, faActivity);
        final TextView title = (TextView) llLayout.findViewById(R.id.title);

        createdList.setAdapter(createdAdapter);

        createdList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent init = new Intent(faActivity, QuestionActivityFromHistory.class);
                init.putExtra("List", createdQuestions);
                init.putExtra("CurrentQuestion", position);
                init.putExtra("CameFrom", "CreatedHistory");
                startActivity(init);
            }
        });

        Firebase.setAndroidContext(faActivity);
        final User currentUser = ((MyApplication) faActivity.getApplication()).getUser();
        Firebase createdRef = new Firebase("https://statfinderproject.firebaseio.com/Users/" + currentUser.getId() + "/CreatedQuestions/");
        createdRef.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                createdQuestions.clear();
                for (DataSnapshot child : dataSnapshot.getChildren())
                {
                    if (!child.getKey().equals("-1"))
                    {
                        String questionId = child.getKey();
                        String currentCity = currentUser.getCity();
                        String currentState = currentUser.getState();
                        String currentCountry = currentUser.getCountry();
                        HashMap<String, Object> question = new HashMap<String, Object>();
                        HashMap<String, Object> questionInfo = (HashMap<String, Object>) child.getValue();
                        String questionCity = (String) questionInfo.get("City");
                        String questionState = (String) questionInfo.get("State");
                        String questionCountry = (String) questionInfo.get("Country");
                        question.put(questionId, questionInfo);

                        if (currentCity.equals(questionCity) && currentState.equals(questionState) && currentCountry.equals(questionCountry))
                        {
                            createdQuestions.add(question);
                        }
                    }
                }

                if (createdQuestions.size() == 0)
                {
                    //title.setVisibility(View.VISIBLE);
                    createdAdapter.notifyDataSetChanged();
                }
                else
                {
                    //title.setVisibility(View.GONE);
                    createdAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }


        });
        return llLayout; // We must return the loaded Layout
    }

}
