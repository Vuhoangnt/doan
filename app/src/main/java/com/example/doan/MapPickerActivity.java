package com.example.doan;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

/**
 * Chọn tọa độ trên bản đồ OpenStreetMap (Leaflet) — không cần Google Maps API key.
 */
public class MapPickerActivity extends AppCompatActivity {

    public static final String EXTRA_LAT = "map_lat";
    public static final String EXTRA_LNG = "map_lng";

    private static final double DEF_LAT = 12.2388;
    private static final double DEF_LNG = 109.1967;

    private WebView webView;
    private TextView txtCoords;
    private double selLat;
    private double selLng;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        MaterialToolbar toolbar = findViewById(R.id.toolbarMapPicker);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        double inLat = getIntent().getDoubleExtra(EXTRA_LAT, Double.NaN);
        double inLng = getIntent().getDoubleExtra(EXTRA_LNG, Double.NaN);
        if (Double.isNaN(inLat) || Double.isNaN(inLng)) {
            selLat = DEF_LAT;
            selLng = DEF_LNG;
        } else {
            selLat = inLat;
            selLng = inLng;
        }

        txtCoords = findViewById(R.id.txtMapPickerCoords);
        updateCoordLabel();

        webView = findViewById(R.id.webMapPicker);
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new JsBridge(this), "AndroidMapBridge");
        webView.loadDataWithBaseURL(
                "https://localhost/",
                buildHtml(selLat, selLng),
                "text/html",
                "utf-8",
                null);

        MaterialButton btn = findViewById(R.id.btnMapPickerConfirm);
        btn.setOnClickListener(v -> {
            Intent data = new Intent();
            data.putExtra(EXTRA_LAT, selLat);
            data.putExtra(EXTRA_LNG, selLng);
            setResult(RESULT_OK, data);
            finish();
        });
    }

    void applyPickFromJs(double lat, double lng) {
        selLat = lat;
        selLng = lng;
        if (txtCoords != null) {
            runOnUiThread(this::updateCoordLabel);
        }
    }

    private void updateCoordLabel() {
        txtCoords.setText(String.format(Locale.getDefault(),
                "%s\n%.7f  ·  %.7f",
                getString(R.string.map_picker_coords_label),
                selLat, selLng));
    }

    private static String buildHtml(double lat, double lng) {
        return "<!DOCTYPE html><html><head><meta charset='utf-8'/>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1,maximum-scale=1'/>"
                + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>"
                + "<style>html,body,#map{margin:0;padding:0;height:100%;width:100%;}</style>"
                + "</head><body>"
                + "<div id='map'></div>"
                + "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>"
                + "<script>"
                + "var lat=" + lat + ",lng=" + lng + ";"
                + "var map=L.map('map').setView([lat,lng],16);"
                + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{maxZoom:19,attribution:'© OpenStreetMap'}).addTo(map);"
                + "var m=L.marker([lat,lng],{draggable:true}).addTo(map);"
                + "function send(p){AndroidMapBridge.onPick(p.lat,p.lng);}"
                + "m.on('dragend',function(e){send(e.target.getLatLng());});"
                + "map.on('click',function(e){m.setLatLng(e.latlng);map.panTo(e.latlng);send(e.latlng);});"
                + "</script></body></html>";
    }

    public static final class JsBridge {
        private final MapPickerActivity activity;

        public JsBridge(MapPickerActivity activity) {
            this.activity = activity;
        }

        @android.webkit.JavascriptInterface
        public void onPick(double lat, double lng) {
            activity.applyPickFromJs(lat, lng);
        }
    }

}
