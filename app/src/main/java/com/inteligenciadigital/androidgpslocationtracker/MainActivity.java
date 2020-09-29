package com.inteligenciadigital.androidgpslocationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private static final int DEFAULT_UPDATE_INTERVAL = 5;
	private static final int FAST_UPDATE_INTERVAL = 2;
	private static final int PERMISSIONS_FINE_LOCATION = 1;

	private String[] permissoes = new String[]{
			Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.ACCESS_COARSE_LOCATION
	};

	private TextView latitude;
	private TextView longitude;
	private TextView accuracy;
	private TextView speed;
	private TextView altitude;
	private TextView sensor;
	private TextView updates;
	private TextView address;

	private Switch location;
	private Switch gps;

	private Location currentLocation;
	private LocationRequest locationRequest;
	private LocationCallback locationCallback;
	private FusedLocationProviderClient fusedLocationProviderClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.inicializar();
		this.updateGPS();
	}

	private void inicializar() {
		this.latitude = this.findViewById(R.id.textView_latitude);
		this.longitude = this.findViewById(R.id.textView_longitude);
		this.accuracy = this.findViewById(R.id.textView_accuracy);
		this.altitude = this.findViewById(R.id.textView_altitude);
		this.speed = this.findViewById(R.id.textView_speed);
		this.sensor = this.findViewById(R.id.textView_sensor);
		this.updates = this.findViewById(R.id.textView_updates);
		this.address = this.findViewById(R.id.textView_address);

		this.location = this.findViewById(R.id.switch_location);
		this.gps = this.findViewById(R.id.switch_gps);

		this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
		this.createLocationRequest();
		this.locationCallback = this.createdLocationCallback();

		this.gps.setOnClickListener(this.ativarGPS);
		this.location.setOnClickListener(this.locationUpdate);
	}

	private void createLocationRequest() {
		this.locationRequest = new LocationRequest();
		this.locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
		this.locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);
		this.locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
	}

	private LocationCallback createdLocationCallback() {
		return new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				if (locationResult == null)
					return;

				for (Location location : locationResult.getLocations()) {
					Log.i("fused-2", "lat: " + location.getLatitude() + " lon: " + location.getLongitude());
					updateUIValues(location);
				}
			}
		};
	}

	private View.OnClickListener ativarGPS = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (gps.isChecked()) {
				locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
				sensor.setText("Using GPS sensors");
			} else {
				locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
				sensor.setText("Using Towers + WIFI");
			}
			updateGPS();
		}
	};

	private View.OnClickListener locationUpdate = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			if (location.isChecked()) {
				startLocationUpdates();
			} else {
				stopLocationUpdates();
			}
		}
	};

	private void startLocationUpdates() {
		this.updates.setText("Location is being tracked");
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}
		this.fusedLocationProviderClient.requestLocationUpdates(
				this.locationRequest, this.locationCallback, null);
		this.updateGPS();
	}

	private void stopLocationUpdates() {
		this.updates.setText("Location is NOT being tracked");
		this.latitude.setText("Not tracking location");
		this.longitude.setText("Not tracking location");
		this.accuracy.setText("Not tracking location");

		this.altitude.setText("Not tracking location");
		this.altitude.setText("Not tracking location");

		this.speed.setText("Not tracking location");
		this.speed.setText("Not tracking location");
		this.sensor.setText("Not tracking location");
		this.address.setText("Not tracking location");

		this.fusedLocationProviderClient.removeLocationUpdates(this.locationCallback);
	}

	private void updateGPS() {
		// obter permissões do usuário para rastreador GPS
		// obter a localização atual usando fusedClient

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			this.fusedLocationProviderClient.getLastLocation()
					.addOnSuccessListener(this, new OnSuccessListener<Location>() {
						@Override
						public void onSuccess(Location location) {
							if (location != null) {
								updateUIValues(location);
								currentLocation = location;
								Log.i("fused-1", "lat: " + location.getLatitude() + " lon: " + location.getLongitude());
							}
						}
					});
		} else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				this.requestPermissions(this.permissoes, PERMISSIONS_FINE_LOCATION);
			}
		}
	}

	private void updateUIValues(Location location) {
		this.latitude.setText(String.valueOf(location.getLatitude()));
		this.longitude.setText(String.valueOf(location.getLongitude()));
		this.accuracy.setText(String.valueOf(location.getAccuracy()));

		if (location.hasAltitude())
			this.altitude.setText(String.valueOf(location.getAltitude()));
		else
			this.altitude.setText("Not available");

		if (location.hasSpeed())
			this.speed.setText(String.valueOf(location.getSpeed()));
		else
			this.speed.setText("Not available");

		Geocoder geocoder = new Geocoder(this);

		try {
			List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			Address endereco = addresses.get(0);
			this.address.setText(endereco.getAddressLine(0));
		} catch (IOException e) {
			this.address.setText("Unable to get street address");
			e.printStackTrace();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == PERMISSIONS_FINE_LOCATION) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				this.updateGPS();
			} else {
				Toast.makeText(
						this,
						"not permissions",
						Toast.LENGTH_SHORT).show();

				this.finish();
			}
		}
	}
}