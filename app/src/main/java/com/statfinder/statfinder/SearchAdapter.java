package com.statfinder.statfinder;

/**
 * Created by Kenny on 10/17/2015.
 */

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;


import com.firebase.client.DataSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SearchAdapter extends BaseAdapter implements ListAdapter {
    private ArrayList<HashMap<String, Object>> list = new ArrayList();
    private Context context;
    private View view;

    public SearchAdapter(ArrayList<HashMap<String, Object>> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        //return list.get(pos).;
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(android.R.layout.simple_list_item_1, null);
        }

        //Handle TextView and display string from your list
        final TextView listItemText = (TextView) view.findViewById(android.R.id.text1);

        HashMap<String, Object> questionInfo = (HashMap) list.get(position).entrySet().iterator().next().getValue();
        String listText = ((String) questionInfo.get("Name")).replace('_', ' ');
        System.out.println(questionInfo);

        listItemText.setText(listText);


        return view;
    }
}
