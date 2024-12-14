package com.mobilecomputing.trackandtaste;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeFragment extends Fragment {

    private DBHelper dbHelper;
    private LinearLayout locationCardContainer;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        dbHelper = new DBHelper(requireContext());
        locationCardContainer = view.findViewById(R.id.locationCardContainer);
        FloatingActionButton addLocationB = view.findViewById(R.id.addLocationB);
        addLocationB.setOnClickListener(v -> {
            showModal();
        });

        dispLocationsOnLoad();

        return view;
    }

    public void showModal(){
        LayoutInflater inflater = getLayoutInflater();
        View modalView = inflater.inflate(R.layout.modal_layout, null);

        EditText inputLabel = modalView.findViewById(R.id.input_label);
        EditText inputLat = modalView.findViewById(R.id.input_lat);
        EditText inputLon = modalView.findViewById(R.id.input_lon);
        Button addLocationSubmit = modalView.findViewById(R.id.addLocationSubmit);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(modalView);
        AlertDialog dialog = builder.create();

        addLocationSubmit.setOnClickListener(v -> {
            String label = inputLabel.getText().toString();
            Double lat = Double.parseDouble(inputLat.getText().toString());
            Double lon = Double.parseDouble(inputLon.getText().toString());
            addLocation(label, lat, lon);
            dialog.dismiss();
        });
        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    public void dispLocationsOnLoad(){
        locationCardContainer.removeAllViews();
        Cursor cur = dbHelper.getAllLocations();
        if (cur == null || cur.getCount() == 0) {
            Tools.showLongToast(getContext(), "No saved locations");
        } else {
            // Iterate through the cursor and display each location
            while (cur.moveToNext()) {
                String label = cur.getString(cur.getColumnIndexOrThrow(DBHelper.Column_label));
                double lat = cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.Column_lat));
                double lon = cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.Column_lon));
                // Make a card for each location and add it to the fragment
                CardView cardView = new CardView(requireContext());
                cardView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                cardView.setRadius(8);
                cardView.setElevation(4);
                cardView.setPadding(16, 16, 16, 16);
                cardView.setClickable(true);

                TextView labelTextView = new TextView(getContext());
                labelTextView.setText("Name : " + label);
                labelTextView.setTextSize(18);
                cardView.addView(labelTextView);

                cardView.setOnClickListener(v ->{
                    Fragment mapFg = new MapFragment();
                    Bundle bundle = new Bundle();
                    bundle.putDouble("lat", lat);
                    bundle.putDouble("lon", lon);
                    mapFg.setArguments(bundle);

                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_fragment);
                    navController.navigate(R.id.action_home_to_map, bundle);

                });
                locationCardContainer.addView(cardView);
            }
        }
        if(cur != null){
            cur.close();
        }
    }

    public void addLocation(String label, double lat, double lon){
        boolean rest = dbHelper.insertLocation(label, lat, lon);
        Tools.showLongToast(getContext(), "Location added: " + rest);
    }
}