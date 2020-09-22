package com.inteligenciadigital.androidgpslocationtracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

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
	private TextView sensor;
	private TextView updates;
	private TextView address;

	private Switch location;
	private Switch gps;

	private LocationRequest locationRequest;
	private FusedLocationProviderClient fusedLocationProviderClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.inicializar();
	}

	private void createLocationRequest() {
		this.locationRequest = new LocationRequest();
		this.locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
		this.locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);
		this.locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
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
		}
	};

	private void updateGPS() {
		// obter permissões do usuário para rastreador GPS
		// obter a localização atual usando fusedClient

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			this.fusedLocationProviderClient.getLastLocation()
					.addOnSuccessListener(this, new OnSuccessListener<Location>() {
						@Override
						public void onSuccess(Location location) {
							if (location != null) {
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

	private void inicializar() {
		this.latitude = this.findViewById(R.id.textView_latitude);
		this.longitude = this.findViewById(R.id.textView_longitude);
		this.accuracy = this.findViewById(R.id.textView_accuracy);
		this.speed = this.findViewById(R.id.textView_speed);
		this.sensor = this.findViewById(R.id.textView_sensor);
		this.updates = this.findViewById(R.id.textView_updates);
		this.address = this.findViewById(R.id.textView_address);

		this.location = this.findViewById(R.id.switch_location);
		this.gps = this.findViewById(R.id.switch_gps);

		this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
		this.createLocationRequest();

		this.gps.setOnClickListener(this.ativarGPS);
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