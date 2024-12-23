package com.mobilecomputing.trackandtaste;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private DBHelper dbHelper;
    private LinearLayout locationCardContainer;

    SearchView searchView;
    RecyclerView searchResults;
    Button addLocationSubmit;

    String label ="";
    private Double lat = 0.0;
    private Double lon = 0.0;
    String address="";
    TextView inputAddress;



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
        inputAddress = modalView.findViewById(R.id.labelAddress);
        searchView = modalView.findViewById(R.id.modalsearchbar);
        searchResults = modalView.findViewById(R.id.modalsearchResults);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                performGeocoding(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });




        addLocationSubmit = modalView.findViewById(R.id.addLocationSubmit);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(modalView);
        AlertDialog dialog = builder.create();

        addLocationSubmit.setOnClickListener(v -> {
            label = inputLabel.getText().toString();

            if(label.isEmpty() || address.isEmpty() || lat == 0.0 || lon == 0.0){
                Tools.showLongToast(getContext(), "Please make sure to fill all fields");
                return;
            }

            addLocation(label, address, lat, lon);
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
                String address = cur.getString(cur.getColumnIndexOrThrow(DBHelper.Column_address));
                double lat = cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.Column_lat));
                double lon = cur.getDouble(cur.getColumnIndexOrThrow(DBHelper.Column_lon));
                // Make a card for each location and add it to the fragment
                CardView cardView = new CardView(requireContext());

                LinearLayout.LayoutParams cardviewparams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

                cardviewparams.setMargins(0, 16, 0, 0);
                cardView.setLayoutParams(cardviewparams);
                cardView.setRadius(12 * getResources().getDisplayMetrics().density);
                cardView.setElevation(4);
                cardView.setPadding(16, 16, 16, 16);
                cardView.setClickable(true);

                cardView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.border));


                LinearLayout linearLayout = new LinearLayout(requireContext());
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setPadding(
                        (int) (16 * getResources().getDisplayMetrics().density), // Convert dp to px
                        (int) (16 * getResources().getDisplayMetrics().density),
                        (int) (16 * getResources().getDisplayMetrics().density),
                        (int) (16 * getResources().getDisplayMetrics().density)
                );
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));

                TextView labelTextView = new TextView(getContext());
                labelTextView.setText(label);
                labelTextView.setTextSize(20);

                TextView addressTextView = new TextView(getContext());
                addressTextView.setText(address);
                addressTextView.setTextSize(18);
                addressTextView.setPadding(0, 8, 0, 0);

                linearLayout.addView(labelTextView);
                linearLayout.addView(addressTextView);

                cardView.addView(linearLayout);

                cardView.setOnClickListener(v ->{
                    Bundle bundle = new Bundle();
                    bundle.putDouble("lat", lat);
                    bundle.putDouble("lon", lon);
                    bundle.putString("label", label);

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

    public void addLocation(String label, String address ,double lat, double lon){
        boolean rest = dbHelper.insertLocation(label,address, lat, lon);
        Tools.showLongToast(getContext(), "Location added: " + rest);
        dispLocationsOnLoad();
    }

    private void performGeocoding(String query) {
        AsyncTask.execute(() -> {
            try {
                // Build the OpenRouteService API URL
                String apiKey = "5b3ce3597851110001cf6248ac44ba4948b9423399fb8f2bbff9f884";
                Double userLat = 48.866667; // Replace with actual latitude
                Double userLon = 2.333333; // Replace with actual longitude
                String urlString = "https://api.openrouteservice.org/geocode/search?api_key=" + apiKey +
                        "&text=" + URLEncoder.encode(query, "UTF-8") +
                        "&boundary.circle.lat=" + userLat +
                        "&boundary.circle.lon=" + userLon +
                        "&boundary.circle.radius=50&size=10";

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse the response on the UI thread
                requireActivity().runOnUiThread(() -> parseAndDisplayGeocodingResults(response.toString()));
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Geocoding request failed", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void parseAndDisplayGeocodingResults(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray features = jsonResponse.getJSONArray("features");

            List<Map<String, Object>> results = new ArrayList<>();
            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = features.getJSONObject(i);
                JSONObject geometry = feature.getJSONObject("geometry");
                JSONArray coordinates = geometry.getJSONArray("coordinates");
                String label = feature.getJSONObject("properties").getString("label");

                Map<String, Object> result = new HashMap<>();
                result.put("lat", coordinates.getDouble(1));
                result.put("lon", coordinates.getDouble(0));
                result.put("label", label);

                results.add(result);
            }

            updateSearchResults(results);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to parse geocoding results", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSearchResults(List<Map<String, Object>> results) {
        // Dynamically populate the RecyclerView
        RecyclerView.Adapter adapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(android.R.layout.simple_list_item_1, parent, false);
                return new RecyclerView.ViewHolder(view) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                TextView textView = (TextView) holder.itemView;
                Map<String, Object> result = results.get(position);
                textView.setText((String) result.get("label"));
                textView.setOnClickListener(v -> {
                    lat = (double) result.get("lat");
                    lon = (double) result.get("lon");
                    address = (String) result.get("label");

                    // update the ui with the address, lat and lon
                    inputAddress.setText(address);


                    searchResults.setVisibility(View.GONE);
                });
            }

            @Override
            public int getItemCount() {
                return results.size();
            }
        };

        searchResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        searchResults.setAdapter(adapter);
        searchResults.setVisibility(View.VISIBLE);
    }


}