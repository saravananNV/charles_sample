/**
 * eGov suite of products aim to improve the internal efficiency,transparency, accountability and the service delivery of the
 * government organizations.
 * 
 * Copyright (C) <2015> eGovernments Foundation
 * 
 * The updated version of eGov suite of products as by eGovernments Foundation is available at http://www.egovernments.org
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * http://www.gnu.org/licenses/ or http://www.gnu.org/licenses/gpl.html .
 * 
 * In addition to the terms of the GPL license to be adhered to in using this program, the following additional terms are to be
 * complied with:
 * 
 * 1) All versions of this program, verbatim or modified must carry this Legal Notice.
 * 
 * 2) Any misrepresentation of the origin of the material is prohibited. It is required that all modified versions of this
 * material be marked in reasonable ways as different from the original version.
 * 
 * 3) This license does not grant any rights to any user of the program with regards to rights under trademark law for use of the
 * trade names or trademarks of eGovernments Foundation.
 * 
 * In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

package org.egov.android.view.activity;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.egov.android.R;
import org.egov.android.service.GeoLocation;
import org.egov.android.view.component.Header;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends BaseFragmentActivity implements OnClickListener {

    private GoogleMap googleMap;
    private double latitude = 0.0;
    private double longitude = 0.0;

    /**
     * To set the layout for the MapActivity and set click listener to confirm location button.
     * Create instance of DGeoLocation class and call _initilizeMap() function.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        ((Button) findViewById(R.id.get_location)).setOnClickListener(this);

        new GeoLocation(this);
        _initilizeMap();
    }

    /**
     * Initialize the map view and set map click listener to map view. When tap on any location will
     * show the marker on the location.
     */
    private void _initilizeMap() {
        googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMap();

        if (googleMap == null) {
            Toast.makeText(getApplicationContext(), R.string.map_load_failure, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (GeoLocation.getGpsStatus()) {
            _setLocation(GeoLocation.getLatitude(), GeoLocation.getLongitude(), false);
        } else {
            _showSettingsAlert();
        }

        googleMap.setOnMapClickListener(new OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                latitude = latLng.latitude;
                longitude = latLng.longitude;
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title("latitude" + latLng.latitude + ":" + latLng.longitude);
                markerOptions.position(latLng);
                googleMap.clear();
                googleMap.addMarker(markerOptions);
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        });
    }

    /**
     * Function used to set the marker in the current location/ lat and lng location
     * 
     * @param lat
     *            => latitude
     * @param lng
     *            => longitude
     * @param isDefault
     */
    private void _setLocation(double lat, double lng, boolean isDefault) {

        latitude = lat;
        longitude = lng;

        int zoom = (isDefault) ? 4 : 15;

        if (latitude == 0 && longitude == 0) {
            _showMsg("Unable to get your current location");
            _setLocation(20.593684, 78.962880, true);
            return;
        }

        LatLng coordinate = new LatLng(lat, lng);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title("latitude" + lat + ":" + lng);
        markerOptions.position(coordinate);
        googleMap.clear();
        googleMap.addMarker(markerOptions);
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, zoom);
        googleMap.animateCamera(yourLocation);
    }

    /**
     * Function used to send the location name tapped by the user in the map to the
     * CreateComplaintActivity
     */
    private void _getLocationName() {
        try {
            if (latitude == 0 && longitude == 0) {
                _showMsg(_getMessage(R.string.location_empty));
                return;
            }
            List<Address> addresses;
            Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                String cityName = addresses.get(0).getAddressLine(0);
                Intent intent = new Intent();
                intent.putExtra("latitude", latitude);
                intent.putExtra("longitute", longitude);
                intent.putExtra("city_name", cityName);
                setResult(2, intent);
                finish();
            } else {
                _showMsg(_getMessage(R.string.unknown_location));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function used to get string from string resource using id.
     * 
     * @param id
     * @return
     */
    private String _getMessage(int id) {
        return getResources().getString(id);
    }

    /**
     * Function used to show message in toast.
     * 
     * @param message
     */
    private void _showMsg(String message) {
        if (message != null && !message.equals("")) {
            Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 120);
            toast.show();
        }
    }

    /**
     * Event triggered when click on the item having click listener. When click on confirm location
     * button will call _getLocationName() function. When click on refresh icon will call the
     * _initilizeMap() function.
     */
    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.get_location:
                _getLocationName();
                break;
            case Header.ACTION_REFRESH:
                _initilizeMap();
                break;
        }
    }

    /**
     * Function called if the user not enabled GPS/Location in their device. Give options to enable
     * GPS/Location and cancel the pop up.
     */
    private void _showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Settings");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                MapActivity.this.startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }
}
