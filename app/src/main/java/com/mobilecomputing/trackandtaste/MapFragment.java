package com.mobilecomputing.trackandtaste;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    private MapView mapView;
    private MyLocationNewOverlay locationoverlay;
    private FloatingActionButton centerLocationB;
    private SearchView searchBar;
    private RecyclerView searchResults;
    private Button btnGoTo;

    private GeoPoint userlocation;
    private GeoPoint destination;

    private List<Marker> markers = new ArrayList<>();
    private List<Polyline> polylines = new ArrayList<>();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_map, container, false);

        Tools.askPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, getContext(), getActivity());

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        mapView = view.findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // Add location overlay
        locationoverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getContext()),mapView);
        locationoverlay.enableMyLocation();
        IMapController mapController = mapView.getController();
        mapController.setZoom(15.0);
        mapController.setCenter(locationoverlay.getMyLocation());
        mapView.getOverlays().add(locationoverlay);


        // add the sclare bar overlay
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        scaleBarOverlay.setAlignBottom(true);

        mapView.getOverlays().add(scaleBarOverlay);

        centerLocationB = view.findViewById(R.id.center_locationB);
        centerLocationB.setOnClickListener(v ->{
            centerOnUserLocation();
        });



        return view;

    }


    private void centerOnUserLocation() {

        if (locationoverlay.getMyLocation() != null) {
            IMapController mapController = mapView.getController();
            mapController.setZoom(21.0);
            mapController.setCenter(locationoverlay.getMyLocation());
        } else {
            Tools.showLongToast(getContext(), "Unable to determine location");
        }
    }

    private void performGeocoding(String query) {
        AsyncTask.execute(() -> {
            try {
                // Build the OpenRouteService API URL
                String apiKey = "5b3ce3597851110001cf6248ac44ba4948b9423399fb8f2bbff9f884";
                Double userLat = locationoverlay.getMyLocation().getLatitude(); // Replace with actual latitude
                Double userLon = locationoverlay.getMyLocation().getLongitude(); // Replace with actual longitude
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
                    double lat = (double) result.get("lat");
                    double lon = (double) result.get("lon");

                    GeoPoint selectedLocation = new GeoPoint(lat, lon);

                    // Move the map camera to the selected location
                    mapView.getController().setCenter(selectedLocation);
                    mapView.getController().setZoom(15.0);
                    Marker resultMark = new Marker(mapView);
                    resultMark.setPosition(selectedLocation);
                    resultMark.setTitle(result.get("label").toString());
                    mapView.getOverlays().add(resultMark);
                    markers.add(resultMark);
                    searchResults.setVisibility(View.GONE);

                    // Show the "Go To" button for navigation
                    Button btnGoTo = requireView().findViewById(R.id.btn_go_to);
                    btnGoTo.setVisibility(View.VISIBLE);
                    btnGoTo.setOnClickListener(view->performDirections(lat, lon));
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


    private void performDirections(double endLat, double endLon) {
        btnGoTo.setVisibility(View.GONE);
        AsyncTask.execute(() -> {
            try {
                String apiKey = "5b3ce3597851110001cf6248ac44ba4948b9423399fb8f2bbff9f884";
                Double userLat = locationoverlay.getMyLocation().getLatitude();
                Double userLon = locationoverlay.getMyLocation().getLongitude();
                String urlString = "https://api.openrouteservice.org/v2/directions/driving-car?api_key=" + apiKey +
                        "&start=" + userLon + "," + userLat +
                        "&end=" + endLon + "," + endLat;

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    Log.d("Directions", "Response: " + response.toString());

                    requireActivity().runOnUiThread(() -> {
                        try {
                            displayDirectionsOnMap(mapView, parseDirections(response.toString()));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Tools.showToast(getContext(), "An error occurred during directions")
                );
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }


}