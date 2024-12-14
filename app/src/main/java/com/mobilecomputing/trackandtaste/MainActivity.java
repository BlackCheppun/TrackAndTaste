package com.mobilecomputing.trackandtaste;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.mobilecomputing.trackandtaste.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavController navController = Navigation.findNavController(this,R.id.nav_fragment);

        NavigationUI.setupWithNavController(binding.navigationBar, navController);
        binding.navigationBar.setOnItemSelectedListener(item ->{
            if(item.getItemId() == R.id.home){
                navController.popBackStack(R.id.home, true);
                navController.navigate(R.id.home);
            }
            else{
                if(item.getItemId() == R.id.map){
                    navController.popBackStack(R.id.map, false);
                    navController.navigate(R.id.map);
                }
                else{
                    navController.popBackStack(R.id.settings, false);
                    navController.navigate(R.id.settings);
                }
            }
            return true;
        });
    }
}