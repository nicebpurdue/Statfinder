package com.statfinder.statfinder;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class AnsweredQuestionsFragment extends Fragment{

    public AnsweredQuestionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentActivity faActivity = (FragmentActivity) super.getActivity();
        RelativeLayout llLayout = (RelativeLayout) inflater.inflate(R.layout.fragment_answered_history, container, false);
        final ListView answeredList = (ListView) llLayout.findViewById(R.id.answeredList);
        final ArrayList<HashMap<String, Object>> answeredQuestions = new ArrayList();
        final HistoryAdapter answeredAdapter = new HistoryAdapter(answeredQuestions, faActivity);
        final TextView title = (TextView) llLayout.findViewById(R.id.title);

        answeredList.setAdapter(answeredAdapter);

        Firebase.setAndroidContext(faActivity);
        final User currentUser = ((MyApplication) faActivity.getApplication()).getUser();
        Firebase answeredRef = new Firebase("https://statfinderproject.firebaseio.com/Users/" + currentUser.getId() + "/AnsweredQuestions/");
        answeredRef.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren())
                {
                    if (!child.getKey().equals("-1"))
                    {
                        String currentCity = currentUser.getCity();
                        String currentState = currentUser.getState();
                        String currentCountry = currentUser.getCountry();
                        HashMap<String, Object> questionInfo = (HashMap<String, Object>) child.getValue();
                        String questionCity = (String) questionInfo.get("City");
                        String questionState = (String) questionInfo.get("State");
                        String questionCountry = (String) questionInfo.get("Country");

                        if (currentCity.equals(questionCity) && currentState.equals(questionState) && currentCountry.equals(questionCountry))
                        {
                            answeredQuestions.add(questionInfo);
                        }
                    }
                }

                if (answeredQuestions.size() == 0)
                {
                    title.setVisibility(View.VISIBLE);
                }
                else
                {
                    title.setVisibility(View.GONE);
                    answeredAdapter.notifyDataSetChanged();
                }



            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }


        });
        return llLayout; // We must return the loaded Layout
    }

}