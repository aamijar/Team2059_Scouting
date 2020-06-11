package com.team2059.scouting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class NavigationDrawer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        MenuFragment.MenuFragmentListener, OpenFragment.OpenFragmentListener, BluetoothFragment.BluetoothFragmentListener {


    private DrawerLayout drawer;
    private Fragment fragment;


    private ArrayList<BluetoothHandler> bluetoothHandlers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        //hides status bar
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Toolbar toolbar = findViewById(R.id.navigation_toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.navigation_drawer);

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new MenuFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_new);
        }

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {}

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if(fragment != null){

                    if(fragment instanceof TabFragment){
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                        ft.setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out);
                        ft.addToBackStack(null);
                        ft.replace(R.id.fragment_container, fragment, "tab").commit();
                        fragment = null;
                    }
                    else if(fragment instanceof BluetoothFragment){
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                        ft.setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out);
                        ft.addToBackStack(null);
                        ft.replace(R.id.fragment_container, fragment, "BluetoothFragment").commit();
                        fragment = null;
                    }
                    else{
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                        ft.setCustomAnimations(R.anim.fade_in_fast, R.anim.fade_out);
                        ft.addToBackStack(null);
                        ft.replace(R.id.fragment_container, fragment).commit();
                        fragment = null;
                    }
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {}

        });

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){

            case R.id.nav_new:
                fragment = new MenuFragment();
                break;
            case R.id.nav_open:
                fragment = new OpenFragment();
                break;
            case R.id.nav_templates:
                fragment = new TabFragment();
                break;
            case R.id.nav_bluetooth:

                BluetoothFragment bluetoothFragment = (BluetoothFragment) getSupportFragmentManager().findFragmentByTag("BluetoothFragment");
                if(bluetoothFragment != null){
                    fragment = bluetoothFragment;
                }
                else{
                    fragment = new BluetoothFragment();
                }
                break;
            case R.id.nav_about:
                Toast.makeText(this, "About", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed(){
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    public void onInputSend(String[] input, String dirName) {
        TabFragment tabFragment = TabFragment.newInstance(input, dirName, bluetoothHandlers);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, tabFragment);
        ft.commit();
        ft.addToBackStack(null);
    }

    @Override
    public void onInputOpenSend(String[] input, String dirName) {
        TabFragment tabFragment = TabFragment.newInstance(input, dirName, bluetoothHandlers);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, tabFragment);
        ft.commit();
        ft.addToBackStack(null);
    }

    @Override
    public void onBluetoothHandlerAttached(ArrayList<BluetoothHandler> bluetoothHandlers) {
        this.bluetoothHandlers = bluetoothHandlers;
    }




}
