package com.team2059.scouting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.team2059.scouting.core.Match;
import org.team2059.scouting.core.Team;
import org.team2059.scouting.core.frc2020.IrMatch;
import org.team2059.scouting.core.frc2020.IrTeam;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class AnalyzeFragment extends Fragment {
    private Activity activity;

    private com.team2059.scouting.Team[] teams;
    private String dirName;

    private static final String ARG_TEAMS = "arg_teams";
    private static final String ARG_DIRNAME = "arg_dirName";

    private ArrayList<Team> teamsList;
    private RecyclerView recyclerView;
    private RecyclerViewAdapterTeam adapter;
    private RecyclerView.LayoutManager layoutManager;

    public static AnalyzeFragment newInstance(com.team2059.scouting.Team[] teams, String dirName){
        AnalyzeFragment analyzeFragment = new AnalyzeFragment();
        Bundle args = new Bundle();

        //args.putParcelableArray(ARG_TEAMS, teams);
        //args.putString(ARG_TEAMS, jsonTeamsArr);
        args.putString(ARG_DIRNAME, dirName);

        analyzeFragment.setArguments(args);
        return analyzeFragment;
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analyze, container, false);
        Gson gson = new Gson();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setExitTransition(null);
        }


        if(getArguments() != null && (teamsList == null || dirName == null)){
            //teams = (com.team2059.scouting.Team[]) getArguments().getParcelableArray(ARG_TEAMS);
            //String jsonTeamsArr = getArguments().getString(ARG_TEAMS);
            //teams = gson.fromJson(jsonTeamsArr, Team[].class);
            dirName = getArguments().getString(ARG_DIRNAME);
        }

        SharedPreferences sharedPreferences = activity.getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        String jsonTeamsArr = sharedPreferences.getString("com.team2059.scouting." + dirName, null);
        Team [] tmpteams = gson.fromJson(jsonTeamsArr, Team[].class);

        teams = new com.team2059.scouting.Team[tmpteams.length];
        for(int i = 0; i < tmpteams.length; i ++){
            teams[i] = new com.team2059.scouting.Team(tmpteams[i].getTeamName(), tmpteams[i].getTeamNumber(), tmpteams[i].getbyteMapString());
        }

        //In future add different Type objects to correlate with new Competitions
        String gsonStr = FileManager.readFile(dirName + "/my-data/Competition.json", activity);
        Type irMatchType = new TypeToken<ArrayList<IrMatch>>(){}.getType();
        ArrayList<IrMatch> irMatchArr = gson.fromJson(gsonStr, irMatchType);
        if(irMatchArr != null){
            prepareTeamsArr(irMatchArr);
            recyclerView = view.findViewById(R.id.analyze_recycleview);
            recyclerView.setHasFixedSize(true);
            layoutManager = new LinearLayoutManager(activity);
            adapter = new RecyclerViewAdapterTeam(teamsList);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
            adapter.setViewHolderListener(new RecyclerViewAdapterTeam.ViewHolderListener() {
                @Override
                public void onTeamClick(int position, ImageView avatar, TextView teamName, TextView teamNumber) {
                    //Toast.makeText(activity, "" + position, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(activity, TeamActivity.class);

                    Pair[] pairs = new Pair[3];
                    pairs[0] = new Pair<View, String>(avatar, "avatarTransition");
                    pairs[1] = new Pair<View, String>(teamName, "teamNameTransition");
                    pairs[2] = new Pair<View, String>(teamNumber, "teamNumberTransition");

                    //ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                    //        avatar, ViewCompat.getTransitionName(avatar));
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, pairs);

                    intent.putExtra("avatar", teamsList.get(position).getbyteMapString());
                    intent.putExtra("teamName", teamName.getText().toString());
                    intent.putExtra("teamNumber", teamNumber.getText().toString());
                    startActivity(intent, options.toBundle());
                }
            });

        }



        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }
    public void prepareTeamsArr(ArrayList<? extends Match> matches){
        teamsList = FileManager.createTeamsArr(matches);

        for(Team team : teamsList){
            Log.e("TEAM", "team name: " + team.getTeamName());
            Log.e("TEAM", "team number: " + team.getTeamNumber());
            Log.e("TEAM", "total points: " + team.getTotalPoints());

            for (IrMatch irMatch : team.getIrMatches()){
                Log.e("MATCH", "match number: " + irMatch.getMatchNumber());
            }
            //set team avatars
            for (com.team2059.scouting.Team team1: teams){
                if(team.getTeamName().equals(team1.getTeamName()) && team.getTeamNumber().equals(team1.getTeamNumber())){
                    team.setByteMapString(team1.getByteMapArr());
                }
            }
        }
    }


}