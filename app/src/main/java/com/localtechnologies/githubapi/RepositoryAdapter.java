package com.localtechnologies.githubapi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.localtechnologies.githubapi.entity.Repository;

import java.util.ArrayList;

public class RepositoryAdapter extends ArrayAdapter<Repository> {

    int listLayout;
    ArrayList<Repository> list;
    Context context;

    public RepositoryAdapter(Context context, int listLayout, int field,
                             ArrayList<Repository> list) {
        super(context, listLayout, field, list);
        this.context = context;
        this.listLayout = listLayout;
        this.list = list;
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent){
        LayoutInflater layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("ViewHolder") View itemView =
                layoutInflater.inflate(listLayout, parent, false);
        TextView nameRepo = itemView.findViewById(R.id.name_repo);
        TextView description = itemView.findViewById(R.id.description);
        TextView language = itemView.findViewById(R.id.language);

        nameRepo.setText(list.get(position).getName());
        description.setText(list.get(position).getDescription());
        language.setText(list.get(position).getLanguage());

        return itemView;
    }
}
