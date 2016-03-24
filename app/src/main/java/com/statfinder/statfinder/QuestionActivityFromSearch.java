package com.statfinder.statfinder;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by michaelrollberg on 3/24/16.
 */
public class QuestionActivityFromSearch extends FragmentActivity {
    MyPagerAdapter mPagerAdapter;
    boolean answered = false;
    String cameFrom = null;
    boolean modStatus = false;
    Button flag;
    Button home;
    Button skip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        Intent init = getIntent();

        final ArrayList<HashMap<String, Object>> searchQuestions = (ArrayList) init.getSerializableExtra("List");
        final int position = init.getIntExtra("CurrentQuestion", 0);
        final User currentUser = ((MyApplication) getApplication()).getUser();
        final String name = ((String) ((HashMap) searchQuestions.get(position).entrySet().iterator().next().getValue()).get("Name")).replace('_', ' ');
        final String category = (String) ((HashMap) searchQuestions.get(position).entrySet().iterator().next().getValue()).get("Category");
        final String questionID = searchQuestions.get(position).entrySet().iterator().next().getKey();

        final TextView questionNameLabel = (TextView) findViewById(R.id.qText);
        questionNameLabel.setText(name);

        flag = (Button) findViewById(R.id.flagButton);

        home = (Button) findViewById(R.id.homeButton);
        home.setText("Search");
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        skip = (Button) findViewById(R.id.skipButton);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (skip.getText().equals("Skip"))
                {
                    Firebase userRef = new Firebase("https://statfinderproject.firebaseio.com/Users/" + ((MyApplication) getApplication()).getUser().getId() + "/SkippedQuestions/" + questionID);
                    HashMap historyMap = new HashMap();
                    Long tsLong = System.currentTimeMillis() / 1000;
                    historyMap.put("TimeCreated", tsLong);
                    historyMap.put("City", currentUser.getCity());
                    historyMap.put("State", currentUser.getState());
                    historyMap.put("Country", currentUser.getCountry());
                    historyMap.put("Category", category);
                    historyMap.put("Name", name);
                    userRef.setValue(historyMap);
                    userRef.setPriority(0 - tsLong);
                }

                if (position + 1 >= searchQuestions.size())
                {

                    Toast.makeText(QuestionActivityFromSearch.this, "You have reached the end of the search list",
                            Toast.LENGTH_SHORT).show();

                }
                else {
                    Intent init = new Intent(QuestionActivityFromSearch.this, QuestionActivityFromSearch.class);
                    init.putExtra("List", searchQuestions);
                    init.putExtra("CurrentQuestion", position + 1);
                    startActivity(init);
                }
                finish();
            }
        });



        String country = currentUser.getCountry();
        String state = currentUser.getState();
        String city = currentUser.getCity();


        TextView categoryLabel = (TextView) findViewById(R.id.categoryLabel);
        if (category.equals("SciTech"))
        {
            categoryLabel.setText("Category: Science and Technology");
        }
        else {
            categoryLabel.setText("Category: " + category);
        }

        final Firebase questionRef = new Firebase("https://statfinderproject.firebaseio.com/Questions/" + country + "/" + state + "/" + city + "/" + category + "/" + questionID);
        questionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final ArrayList<String> answers = new ArrayList();

                for (DataSnapshot child : dataSnapshot.getChildren())
                {
                    if (child.getKey().equals("Answers"))
                    {
                        for (DataSnapshot answerChild : child.getChildren())
                        {
                            answers.add(answerChild.getKey());
                        }
                    }
                    else if (child.getKey().equals("Moderated"))
                    {
                        modStatus = (boolean) child.getValue();
                    }
                }

                if (modStatus) {
                    questionNameLabel.setTextColor(getResources().getColor(R.color.lightBlue));
                }

                final String[] answersArray = Arrays.copyOf(answers.toArray(), answers.size(), String[].class);

                final Firebase answeredRef = new Firebase("https://statfinderproject.firebaseio.com/Users/" + currentUser.getId() + "/AnsweredQuestions/");
                final Firebase skippedRef = new Firebase("https://statfinderproject.firebaseio.com/Users/" + currentUser.getId() + "/SkippedQuestions/");
                final Firebase createdRef = new Firebase("https://statfinderproject.firebaseio.com/Users/" + currentUser.getId() + "/CreatedQuestions/");

                answeredRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren())
                        {
                            if (questionID.equals(child.getKey()))
                            {
                                cameFrom = "AnsweredHistory";
                                skip.setText("Next");
                            }
                        }
                        if (cameFrom == null)
                        {
                            skippedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot child: dataSnapshot.getChildren())
                                    {
                                        if (questionID.equals(child.getKey()))
                                        {
                                            cameFrom = "SkippedHistory";
                                        }
                                    }
                                    if (cameFrom == null)
                                    {
                                        createdRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                for (DataSnapshot child: dataSnapshot.getChildren())
                                                {
                                                    if (questionID.equals(child.getKey()))
                                                    {
                                                        cameFrom = "CreatedHistory";
                                                        HashMap<String, Object> questionInfo = (HashMap) child.getValue();
                                                        answered = (boolean) questionInfo.get("hasBeenAnswered");
                                                        if (answered)
                                                        {
                                                            skip.setText("Next");
                                                        }
                                                    }
                                                }
                                                if (cameFrom == null)
                                                {
                                                    setUpViewPager(questionID, answersArray, category, modStatus, "Search");
                                                }
                                                else
                                                {
                                                    setUpViewPager(questionID, answersArray, category, modStatus, cameFrom);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(FirebaseError firebaseError) {

                                            }
                                        });
                                    }
                                    else
                                    {
                                        setUpViewPager(questionID, answersArray, category, modStatus, cameFrom);
                                    }
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {

                                }
                            });
                        }
                        else
                        {
                            setUpViewPager(questionID, answersArray, category, modStatus, cameFrom);
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });



            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });





    }

    public boolean getAnswered()
    {
        return answered;
    }

    public void setAnswered(boolean answered)
    {
        this.answered = answered;
    }

    public void setUpViewPager(String questionID, String[] answersArray, String category, boolean modStatus, String cameFrom)
    {
        if (!modStatus) {
            flag.setVisibility(View.VISIBLE);
        }
        skip.setVisibility(View.VISIBLE);
        home.setVisibility(View.VISIBLE);

        Bundle bundle = new Bundle();
        bundle.putStringArray("answers", answersArray);
        bundle.putString("id", questionID);
        bundle.putString("category", category);
        bundle.putBoolean("modStatus", modStatus);
        bundle.putString("cameFrom", cameFrom);

        AnswersFragment answerFragment = new AnswersFragment();
        answerFragment.setArguments(bundle);

        ResultsFragment resultFragment = new ResultsFragment();
        resultFragment.setArguments(bundle);


        //For each answer add a button
        ArrayList<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(answerFragment);
        fragments.add(resultFragment);

        if (modStatus) {
            StateResultsFragment stateFragment = new StateResultsFragment();
            stateFragment.setArguments(bundle);
            fragments.add(stateFragment);

            CountryResultsFragment countryFragment = new CountryResultsFragment();
            countryFragment.setArguments(bundle);
            fragments.add(countryFragment);

            GlobalResultsFragment globalFragment = new GlobalResultsFragment();
            globalFragment.setArguments(bundle);
            fragments.add(globalFragment);
        }

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), fragments);
        final MyViewPager pager = (MyViewPager) findViewById(R.id.viewpager);
        pager.setAdapter(mPagerAdapter);
    }
}

