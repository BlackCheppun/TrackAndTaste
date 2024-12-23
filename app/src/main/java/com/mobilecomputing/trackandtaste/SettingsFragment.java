package com.mobilecomputing.trackandtaste;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.Calendar;

public class SettingsFragment extends Fragment implements SensorEventListener {

    // UI components
    private EditText hourInput, minuteInput;
    private TextView selectedTimeText;
    private Button setReminderButton;
    private Switch darkModeSwitch, gestureServiceSwitch;

    // Preferences keys
    private static final String PREFS_NAME = "SettingsPrefs";
    private static final String DARK_MODE_KEY = "dark_mode";
    private static final String SHAKE_DETECTION_KEY = "shake_detection_enabled";

    // Shake detection
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime;
    private static final int SHAKE_THRESHOLD = 20;
    private static final int SHAKE_INTERVAL = 300;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize UI components
        hourInput = view.findViewById(R.id.hour_input);
        minuteInput = view.findViewById(R.id.minute_input);
        selectedTimeText = view.findViewById(R.id.selected_time_text);
        setReminderButton = view.findViewById(R.id.set_reminder_button);
        darkModeSwitch = view.findViewById(R.id.dark_mode_switch);
        gestureServiceSwitch = view.findViewById(R.id.gesture_service_switch);

        // Load preferences
        SharedPreferences preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Dark mode toggle
        darkModeSwitch.setChecked(preferences.getBoolean(DARK_MODE_KEY, false));
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            preferences.edit().putBoolean(DARK_MODE_KEY, isChecked).apply();
            Toast.makeText(getContext(), "Dark mode " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        // Gesture service toggle
        gestureServiceSwitch.setChecked(preferences.getBoolean(SHAKE_DETECTION_KEY, false));
        gestureServiceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(SHAKE_DETECTION_KEY, isChecked).apply();
            if (isChecked) enableShakeDetection();
            else disableShakeDetection();
        });

        // TimePickerDialog on input click
        View.OnClickListener timePickerListener = v -> showTimePickerDialog();
        hourInput.setOnClickListener(timePickerListener);
        minuteInput.setOnClickListener(timePickerListener);

        // Set reminder button action
        setReminderButton.setOnClickListener(v -> setReminder());

        return view;
    }

    private void showTimePickerDialog() {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        new TimePickerDialog(
                getContext(),
                (view, selectedHour, selectedMinute) -> {
                    hourInput.setText(String.valueOf(selectedHour));
                    minuteInput.setText(String.valueOf(selectedMinute));
                    selectedTimeText.setText(String.format("Selected Time: %02d:%02d", selectedHour, selectedMinute));
                },
                hour,
                minute,
                true
        ).show();
    }

    private void setReminder() {
        try {
            int hour = Integer.parseInt(hourInput.getText().toString());
            int minute = Integer.parseInt(minuteInput.getText().toString());

            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                Toast.makeText(getContext(), "Invalid time (0-23 for hours, 0-59 for minutes).", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
            intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
            intent.putExtra(AlarmClock.EXTRA_MINUTES, minute);
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, "Restaurant Reminder!");
            startActivity(intent);

            Toast.makeText(getContext(), "Reminder set for " + String.format("%02d:%02d", hour, minute), Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Enter valid hour and minute.", Toast.LENGTH_SHORT).show();
        }
    }

    private void enableShakeDetection() {
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            Toast.makeText(getContext(), "Shake detection enabled", Toast.LENGTH_SHORT).show();
        }
    }

    private void disableShakeDetection() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            Toast.makeText(getContext(), "Shake detection disabled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float shakeMagnitude = Math.abs(x) + Math.abs(y) + Math.abs(z);

            if (shakeMagnitude > SHAKE_THRESHOLD && System.currentTimeMillis() - lastShakeTime > SHAKE_INTERVAL) {
                lastShakeTime = System.currentTimeMillis();
                Toast.makeText(getContext(), "Shake detected!", Toast.LENGTH_SHORT).show();
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_fragment);
                navController.navigate(R.id.action_settings_to_map);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not required for this implementation
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (preferences.getBoolean(SHAKE_DETECTION_KEY, false)) {
            enableShakeDetection();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        disableShakeDetection();
    }
}
