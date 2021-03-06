package org.team2059.scouting;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.simple.JSONArray;
import org.team2059.scouting.core.Team;
import org.team2059.scouting.core.frcapiclient.Competition;
import org.team2059.scouting.core.frcapiclient.Event;
import org.team2059.scouting.core.frcapiclient.HttpHandler;


import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MenuFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private Context context;

    private HttpHandler hh = new HttpHandler();

    private ArrayList<Competition> districtComps = new ArrayList<>();
    private ArrayList<Competition> regionalComps = new ArrayList<>();
    private ArrayList<Competition> champComps = new ArrayList<>();

    private ArrayList<String> display = new ArrayList<>();
    private ArrayList<String> display2 = new ArrayList<>();
    private String [] teams;
    private int index;
    private int indexDistrict;
    private String [] temp = {"-"};
    private Event[] events;

    private Spinner spinner1;
    private Spinner spinner2;
    private Spinner spinner3;

    private JSONArray teamJsonArray;

    private MenuFragmentListener listener;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface MenuFragmentListener{
        void onInputSend(String dirName);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_menu, container, false);

        context = getContext();

        // TODO remove unused references

        // VOID key, does not work
        final String AUTHORIZATION_KEY = "aamijar:E9B244D8-B9FF-4BD6-9BF7-AA763A72292B";
        final String HOST = "https://frc-api.firstinspires.org/v2.0/";

        final String [] CREDENTIALS = AUTHORIZATION_KEY.split(":");
        final String USER = CREDENTIALS[0];
        final String PASS = CREDENTIALS[1];


        hh.setToken(AUTHORIZATION_KEY);


        final SwipeRefreshLayout refreshLayout = view.findViewById(R.id.swipe);

        Button button = view.findViewById(R.id.menu_button);
        TextView sync = view.findViewById(R.id.menu_sync);

        Typeface eagleLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/eagle_light.otf");
        Typeface eagleBook = Typeface.createFromAsset(getActivity().getAssets(), "fonts/eagle_book.otf");
        //Typeface eagleBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/eagle_bold.otf");

        button.setTypeface(eagleBook);
        sync.setTypeface(eagleLight);


        final String[] array = {"District", "Regional", "Championship"};


        spinner1 = view.findViewById(R.id.menu_spinner1);
        spinner2 =  view.findViewById(R.id.menu_spinner2);
        spinner3 = view.findViewById(R.id.menu_spinner3);

        spinner1.setOnItemSelectedListener(this);
        spinner2.setOnItemSelectedListener(this);


        final ArrayAdapter<String> adapter1 = new ArrayAdapter<>(context, R.layout.spinner_item, array);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(context, R.layout.spinner_item, temp);
        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(context, R.layout.spinner_item, temp);

        spinner1.setAdapter(adapter1);
        spinner2.setAdapter(adapter2);
        spinner3.setAdapter(adapter3);


        /*load comp data from user prefs*/
        loadData();


        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                String selected = spinner1.getSelectedItem().toString();
                String selected2 = spinner2.getSelectedItem().toString();
                indexDistrict = spinner2.getSelectedItemPosition();




                if(selected.equals("District")){
                    if(!selected2.equals("-")){
                        String districtCode = districtComps.get(indexDistrict).getCode();
//                        String query = "2020/events?districtCode=" + districtCode;
//                        Request request = hh.getRequest(query);
//                        startCall(request, "event");

                        DocumentReference docRef = db.document("cloud functions/districts/district events/" + districtCode);
                        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists()){
                                    @SuppressWarnings("unchecked")
                                    List<Map<String, String>> districtList = (List<Map<String, String>>)
                                            documentSnapshot.getData().get("district event");
                                    display2.clear();

                                    Event [] events = new Event[districtList.size()];
                                    for(int i = 0; i < districtList.size(); i ++){
                                        events[i] = new Event(districtList.get(i).get("name"), districtList.get(i).get("code"));
                                    }
                                    districtComps.get(indexDistrict).setEvents(events);
                                    for(Event e: districtComps.get(indexDistrict).getEvents()) {
                                        display2.add(e.getName());
                                    }

                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, display2);
                                    spinner3.setAdapter(adapter);
                                }
                            }

                        });
                        isNetworkConnected();

                    }
                    else{
//                        String query = "2020/districts";
//                        Request request = hh.getRequest(query);
//                        startCall(request, "district");

                        DocumentReference docRef = db.collection("cloud functions").document("districts");
                        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists()){

                                    @SuppressWarnings("unchecked")
                                    List<Map<String, String>> districtList = (List<Map<String, String>>)
                                            documentSnapshot.getData().get("districtCodes");
                                    districtComps.clear();
                                    display.clear();
                                    for(Map<String, String> district: districtList){
                                        districtComps.add(new Competition("District", district.get("name"), district.get("code")));
                                        display.add(district.get("name"));
                                    }
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, display);
                                    spinner2.setAdapter(adapter);
                                }
                            }

                        });
                        isNetworkConnected();

                    }
                }
                else if(selected.equals("Regional")){
//                    String query = "2020/events";
//                    Request request = hh.getRequest(query);
//                    startCall(request, "regional");
                    DocumentReference docRef = db.document("cloud functions/regionals");
                    docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.exists()){

                                @SuppressWarnings("unchecked")
                                List<Map<String, String>> regionalList = (List<Map<String, String>>)
                                        documentSnapshot.getData().get("regionalCodes");
                                regionalComps.clear();
                                display2.clear();
                                for(Map<String, String> regional: regionalList){
                                    regionalComps.add(new Competition("District", regional.get("name"), regional.get("code")));
                                    display2.add(regional.get("name"));
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, display2);
                                spinner3.setAdapter(adapter);
                            }
                        }

                    });
                    isNetworkConnected();



                }
                else{
//                    String query = "2020/events";
//                    Request request = hh.getRequest(query);
//                    startCall(request, "championship");
                    DocumentReference docRef = db.document("cloud functions/championships");
                    docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.exists()){

                                @SuppressWarnings("unchecked")
                                List<Map<String, String>> championshipList = (List<Map<String, String>>)
                                        documentSnapshot.getData().get("championshipCodes");
                                champComps.clear();
                                display2.clear();
                                for(Map<String, String> champ: championshipList){
                                    champComps.add(new Competition("District", champ.get("name"), champ.get("code")));
                                    display2.add(champ.get("name"));
                                }
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, display2);
                                spinner3.setAdapter(adapter);
                            }
                        }

                    });
                    isNetworkConnected();
                }
                saveData();
                refreshLayout.setRefreshing(false);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(spinner3.getSelectedItem().toString().equals("-")){
                    Toast.makeText(context, "No Event Specified", Toast.LENGTH_LONG).show();
                }
                else{
                    index = spinner3.getSelectedItemPosition();
                    if(!spinner2.getSelectedItem().equals("-")){
                        String districtCode = districtComps.get(indexDistrict).getCode();
                        String eventCode = districtComps.get(indexDistrict).getEvents()[index].getCode();
//                        String query = "2020/teams?eventCode=" + eventCode;
//                        Request request = hh.getRequest(query);
//                        startCall(request, "teamOfEvent:" + eventCode);
                        DocumentReference docRef = db.document("cloud functions/districts/district events/" + districtCode + "/teams/" + eventCode);
                        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists()){

                                    @SuppressWarnings("unchecked")
                                    List<Map<String, Object>> teamsList = (List<Map<String, Object>>)
                                            documentSnapshot.getData().get("teams");

                                    Team [] teams = new Team[teamsList.size()];
                                    for(int i = 0; i < teamsList.size(); i ++){
                                        // number in firestore database is integer and must be converted to string first
                                        teams[i] = new Team((String) teamsList.get(i).get("name"), teamsList.get(i).get("number").toString(), (String) teamsList.get(i).get("avatar"));
                                    }

                                    districtComps.get(indexDistrict).getEvents()[index].setTeams(teams);
                                    openMainActivity(districtComps.get(indexDistrict).getEvents()[index].getTeams());
                                }
                            }

                        });
                        docRef.get().addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                                Toast.makeText(context, "Oops something went wrong", Toast.LENGTH_SHORT).show();
                            }
                        });
                        isNetworkConnected();



                    }
                    else{
                        String eventCode;
                        if(spinner1.getSelectedItem().toString().equals("Regional")){
                            eventCode = regionalComps.get(index).getCode();
                            DocumentReference docRef = db.document("cloud functions/regionals/teams/" + eventCode);
                            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.exists()){

                                        @SuppressWarnings("unchecked")
                                        List<Map<String, Object>> teamsList = (List<Map<String, Object>>)
                                                documentSnapshot.getData().get("teams");

                                        Team [] teams = new Team[teamsList.size()];
                                        for(int i = 0; i < teamsList.size(); i ++){
                                            // number in firestore database is integer and must be converted to string first
                                            teams[i] = new Team((String) teamsList.get(i).get("name"), teamsList.get(i).get("number").toString(), (String) teamsList.get(i).get("avatar"));
                                        }

                                        regionalComps.get(index).setTeams(teams);
                                        openMainActivity(regionalComps.get(index).getTeams());
                                    }
                                }

                            });
                            docRef.get().addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(context, "Oops something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            });
                            isNetworkConnected();



                        }
                        else{
                            eventCode = champComps.get(index).getCode();
                            DocumentReference docRef = db.document("cloud functions/championships/teams/" + eventCode);
                            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.exists()){

                                        @SuppressWarnings("unchecked")
                                        List<Map<String, Object>> teamsList = (List<Map<String, Object>>)
                                                documentSnapshot.getData().get("teams");

                                        Team [] teams = new Team[teamsList.size()];
                                        for(int i = 0; i < teamsList.size(); i ++){
                                            // number in firestore database is integer and must be converted to string first
                                            teams[i] = new Team((String) teamsList.get(i).get("name"), teamsList.get(i).get("number").toString(), (String) teamsList.get(i).get("avatar"));
                                        }

                                        champComps.get(index).setTeams(teams);
                                        openMainActivity(champComps.get(index).getTeams());
                                    }
                                }

                            });
                            docRef.get().addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(context, "Oops something went wrong", Toast.LENGTH_SHORT).show();
                                }
                            });
                            isNetworkConnected();



                        }
//                        String query = "2020/teams?eventCode=" + eventCode;
//                        Request request = hh.getRequest(query);
//                        startCall(request, "team:" + eventCode);


                    }

                }
                saveData();
            }
        });


        return view;

    }


    private void isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null && !(cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected())) {
            Toast.makeText(context, "Not connected to network", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if(context instanceof MenuFragmentListener){
            listener = (MenuFragmentListener) context;
        }else {
            throw new RuntimeException(context.toString() + "must implement MenuFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private void openMainActivity(Team [] teams){

        String dirName = spinner3.getSelectedItem().toString();

        //make default directories
        FileManager.makeDir(dirName, context);
        FileManager.makeDir(dirName + "/my-data", context);
        FileManager.makeDir(dirName + "/checkpoints", context);

        SharedPreferences sharedPreferences = context.getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();

        String jsonTeamsArr = gson.toJson(teams);
        editor.putString("com.team2059.scouting." + dirName, jsonTeamsArr);
        editor.apply();

        listener.onInputSend(dirName);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.e("SPINNER", Integer.toString(parent.getSelectedItemPosition()));
        ArrayAdapter<String> tempAdapter = new ArrayAdapter<>(context, R.layout.spinner_item, temp);




        if(parent.getSelectedItem().toString().equals("District")){
            spinner3.setAdapter(tempAdapter);
            display.clear();
            for(Competition comp:districtComps)
            {
                display.add(comp.getName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, display);
            if(!display.isEmpty()){
                Log.e("NULL", display.toString());
                spinner2.setAdapter(adapter);
            }


            //showAsDialog((Spinner) parent);


        }
        else if(parent.getSelectedItem().toString().equals("Regional")){
            spinner2.setAdapter(tempAdapter);
            display2.clear();
            for(Competition comp:regionalComps)
            {
                display2.add(comp.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, display2);
            if(!display2.isEmpty()){
                spinner3.setAdapter(adapter);
            }
            else{
                spinner3.setAdapter(tempAdapter);
            }
        }
        else if(parent.getSelectedItem().toString().equals("Championship")){
            spinner2.setAdapter(tempAdapter);
            display2.clear();
            for(Competition comp:champComps)
            {
                display2.add(comp.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, display2);
            if(!display2.isEmpty()){
                spinner3.setAdapter(adapter);
            }
            else{
                spinner3.setAdapter(tempAdapter);
            }
        }
        else if(parent.getId() == R.id.menu_spinner2 && !spinner2.getSelectedItem().equals("-")){
            indexDistrict = spinner2.getSelectedItemPosition();


            if(!districtComps.isEmpty() && districtComps.get(indexDistrict).getEvents() != null)
            {
                display2.clear();
                for(Event e :districtComps.get(indexDistrict).getEvents()){
                    display2.add(e.getName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, display2);
                if(!display2.isEmpty()){
                    spinner3.setAdapter(adapter);
                }
            }
            else{
                spinner3.setAdapter(tempAdapter);
            }

        }



    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

//    public void startSyncCall(final Request request, final String key){
//        OkHttpClient client = new OkHttpClient();
//
//        Response response;
//        String myResponse = "";
//        try {
//            response = client.newCall(request).execute();
//            myResponse = response.body().string();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try{
//            if(key.equals("team")){
//                JSONArray jsonArray = hh.fetchAsJsonArr(myResponse, "teams");
//
//                if(spinner1.getSelectedItem().equals("Regional")){
//                    teams = hh.getTeams(regionalComps.get(index), jsonArray);
//                }
//                else{
//                    teams = hh.getTeams(champComps.get(index), jsonArray);
//                }
//            }
//            else{
//                JSONArray jsonArray = hh.fetchAsJsonArr(myResponse, "teams");
//                teams = hh.getTeams(districtComps.get(indexDistrict).getEvents()[index], jsonArray);
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }





    /*first events api server call*/
    public void startCall(final Request request, final String key)
    {
        OkHttpClient client = new OkHttpClient();


        /*Asynchronous call using enqueue thread*/
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("HTTP", "NO INTERNET");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "NO INTERNET", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                if(response.isSuccessful())
                {
                    final String myresponse = response.body().string();
                    try {
                        //comps.clear();
                        if(key.equals("district")){
                            display.clear();
                            JSONArray jsonArray = hh.fetchAsJsonArr(myresponse, "districts");
                            districtComps = hh.getDistricts(jsonArray);

                        }
                        else if(key.equals("regional")){
                            display2.clear();
                            JSONArray jsonArray = hh.fetchAsJsonArr(myresponse, "Events");
                            regionalComps = hh.getRegionals(jsonArray);
                        }
                        else if(key.equals("championship")){
                            display2.clear();
                            JSONArray jsonArray = hh.fetchAsJsonArr(myresponse, "Events");
                            champComps = hh.getChampionships(jsonArray);
                        }
                        else if(key.contains("team:")){
                            teamJsonArray = hh.fetchAsJsonArr(myresponse, "teams");

                            if(spinner1.getSelectedItem().equals("Regional")){
                                //teams = hh.getTeams(regionalComps.get(index), jsonArray);
                            }
                            else{
                                //teams = hh.getTeams(champComps.get(index), jsonArray);
                            }
                            String [] code = key.split(":");
                            String query2 = "2020/avatars?eventCode=" + code[1];
                            Request request1 = hh.getRequest(query2);
                            startCall(request1, "avatar");

                        }
                        else if(key.equals("event")){
                            display2.clear();
                            JSONArray jsonArray = hh.fetchAsJsonArr(myresponse, "Events");
                            events = hh.getEvents(districtComps.get(indexDistrict), jsonArray);
                        }
                        else if(key.contains("teamOfEvent:")){
                            teamJsonArray = hh.fetchAsJsonArr(myresponse, "teams");
                            //teams = hh.getTeams(districtComps.get(indexDistrict).getEvents()[index], jsonArray);

                            String [] code = key.split(":");
                            Log.e("HTTP here", code[1]);
                            String query2 = "2020/avatars?eventCode=" + code[1];
                            Request request1 = hh.getRequest(query2);
                            startCall(request1, "avatarOfEvent");

                        }
                        else if(key.equals("avatar")){
                            JSONArray jsonArray = hh.fetchAsJsonArr(myresponse, "teams");
                            if(spinner1.getSelectedItem().equals("Regional")){
                                hh.setTeams(regionalComps.get(index), teamJsonArray, jsonArray);
                            }
                            else{
                                hh.setTeams(champComps.get(index), teamJsonArray, jsonArray);
                            }
                        }
                        else{
                            JSONArray jsonArray = hh.fetchAsJsonArr(myresponse, "teams");
                            Log.e("HTTP here", jsonArray.toString());
                            hh.setTeams(districtComps.get(indexDistrict).getEvents()[index], teamJsonArray, jsonArray);
                        }

                        switch (key) {
                            case "district":
                                for (Competition comp : districtComps) {
                                    display.add(comp.getName());
                                }
                                break;
                            case "regional":
                                for (Competition comp : regionalComps) {
                                    display2.add(comp.getName());
                                }
                                break;
                            case "championship":
                                for (Competition comp : champComps) {
                                    display2.add(comp.getName());
                                }
                                break;
                            case "event":
                                for (Event e : districtComps.get(indexDistrict).getEvents()) {
                                    display2.add(e.getName());
                                }
                                break;
                        }

                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        Log.e("HTTP", Integer.toString(districtComps.get(indexDistrict).getEvents()[index].getTeams().length), e);
                    }


                    /*Run on main thread*/
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            switch (key) {
                                case "district": {
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, display);
                                    spinner2.setAdapter(adapter);
                                    break;
                                }
                                case "regional":
                                case "championship":
                                case "event": {
                                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.spinner_item, display2);
                                    spinner3.setAdapter(adapter);
                                    break;
                                }
                                case "avatar":
                                case "avatarOfEvent":
                                    if (spinner1.getSelectedItem().equals("Regional")) {
                                        openMainActivity(regionalComps.get(index).getTeams());
                                    } else if (spinner1.getSelectedItem().equals("Championship")) {
                                        openMainActivity(champComps.get(index).getTeams());
                                    } else {
                                        openMainActivity(districtComps.get(indexDistrict).getEvents()[index].getTeams());
                                    }

                                    //openMainActivity(teams);
                                    break;
                            }

                        }
                    });
                }
                else{
                    Log.e("HTTP", "Not Success");
                    Log.e("HTTP", request.headers().toString());
                    Log.e("HTTP", response.message());

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "HTTP Error: " + response.message(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }
                /*save comp data in user prefs*/
                saveData();
            }
        });
    }


    private void saveData(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String districtCompsJson = gson.toJson(districtComps);
        String regionalCompsJson = gson.toJson(regionalComps);
        String champCompsJson = gson.toJson(champComps);

        editor.putString("districtCompsJson", districtCompsJson);
        editor.putString("regionalCompsJson", regionalCompsJson);
        editor.putString("champCompsJson", champCompsJson);
        editor.apply();
    }

    private void loadData(){
        SharedPreferences sharedPreferences = context.getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String districtCompsJson = sharedPreferences.getString("districtCompsJson", null);
        String regionalCompsJson = sharedPreferences.getString("regionalCompsJson", null);
        String champCompsJson = sharedPreferences.getString("champCompsJson", null);

        Type type = new TypeToken<ArrayList<Competition>>(){}.getType();

        districtComps = gson.fromJson(districtCompsJson, type);
        regionalComps = gson.fromJson(regionalCompsJson, type);
        champComps = gson.fromJson(champCompsJson, type);

        if(districtComps == null) {
            districtComps = new ArrayList<>();
        }
        if(regionalComps == null){
            regionalComps = new ArrayList<>();
        }
        if(champComps == null){
            champComps = new ArrayList<>();
        }

    }



    /*for spinner dialog box
     * not in use*/
    public void showAsDialog(final Spinner spinner){

        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setTitle("Select an Option");

//        LayoutInflater layoutInflater = this.getLayoutInflater();
//
//        View view = layoutInflater.inflate(R.layout.spinner_item_test, null);
//
//
//        b.setView(view);
//
//        Spinner mSpinner = view.findViewById(R.id.dia_spinner);
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, types);
//
//        mSpinner.setAdapter(adapter);

        String [] items = new String[spinner.getAdapter().getCount()];

        for(int i = 0; i < spinner.getAdapter().getCount(); i ++){
            items[i] = (String) spinner.getAdapter().getItem(i);
        }


        b.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                spinner.setSelection(which);
            }
        });

        b.show();
    }



}
