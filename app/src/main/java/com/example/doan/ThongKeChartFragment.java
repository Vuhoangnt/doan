package com.example.doan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.doan.DAO.ThongKeDAO;
import com.example.doan.model.ChartPoint;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
public class ThongKeChartFragment extends Fragment implements DataRefreshable {

    private static final int MODE_THANG = 0;
    private static final int MODE_NGAY = 1;
    private static final int MODE_NAM = 2;

    private ThongKeDAO dao;
    private BarChart barChart;
    private LineChart lineChart;
    private Spinner spinnerMode;
    private Spinner spinnerNam;
    private Spinner spinnerThang;
    private LinearLayout layoutThang;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_thong_ke, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dao = new ThongKeDAO(requireContext());
        barChart = view.findViewById(R.id.barChart);
        lineChart = view.findViewById(R.id.lineChart);
        spinnerMode = view.findViewById(R.id.spinnerMode);
        spinnerNam = view.findViewById(R.id.spinnerNam);
        spinnerThang = view.findViewById(R.id.spinnerThang);
        layoutThang = view.findViewById(R.id.layoutThang);

        setupChartsLook(barChart);
        setupChartsLook(lineChart);

        String[] modes = {
                getString(R.string.thong_ke_mode_thang),
                getString(R.string.thong_ke_mode_ngay),
                getString(R.string.thong_ke_mode_nam)
        };
        spinnerMode.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, modes));

        int cy = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int y = cy - 5; y <= cy + 1; y++) {
            years.add(String.valueOf(y));
        }
        spinnerNam.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, years));
        spinnerNam.setSelection(years.indexOf(String.valueOf(cy)));

        List<String> months = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            months.add(String.valueOf(m));
        }
        spinnerThang.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, months));
        spinnerThang.setSelection(Calendar.getInstance().get(Calendar.MONTH));

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                updateVisibility();
                loadCharts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinnerMode.setOnItemSelectedListener(listener);
        spinnerNam.setOnItemSelectedListener(listener);
        spinnerThang.setOnItemSelectedListener(listener);

        updateVisibility();
        loadCharts();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void refreshData() {
        if (getView() == null || dao == null || barChart == null) {
            return;
        }
        loadCharts();
    }

    private void updateVisibility() {
        int mode = spinnerMode.getSelectedItemPosition();
        layoutThang.setVisibility(mode == MODE_NGAY ? View.VISIBLE : View.GONE);
    }

    private void setupChartsLook(BarLineChartBase<?> chart) {
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(true);
        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setGranularity(1f);
        x.setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
    }

    private int primaryColor() {
        android.util.TypedValue tv = new android.util.TypedValue();
        if (requireContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, tv, true)) {
            return tv.data;
        }
        return ContextCompat.getColor(requireContext(), R.color.theme_green_primary);
    }

    private void loadCharts() {
        int mode = spinnerMode.getSelectedItemPosition();
        int year = Integer.parseInt(spinnerNam.getSelectedItem().toString());
        int month = spinnerThang.getSelectedItemPosition() + 1;

        List<ChartPoint> points;
        if (mode == MODE_THANG) {
            points = dao.tongTienTheoThangTrongNam(year);
        } else if (mode == MODE_NGAY) {
            points = dao.tongTienTheoNgayTrongThang(year, month);
        } else {
            int ySel = Integer.parseInt(spinnerNam.getSelectedItem().toString());
            points = dao.tongTienTheoNam(ySel - 4, ySel);
        }

        List<BarEntry> barEntries = new ArrayList<>();
        List<Entry> lineEntries = new ArrayList<>();
        final List<String> labels = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            ChartPoint p = points.get(i);
            labels.add(p.label);
            barEntries.add(new BarEntry(i, p.value));
            lineEntries.add(new Entry(i, p.value));
        }

        int color = primaryColor();
        IndexAxisValueFormatter idxFmt = new IndexAxisValueFormatter(labels.toArray(new String[0]));

        BarDataSet barDs = new BarDataSet(barEntries, getString(R.string.thong_ke_title));
        barDs.setColor(color);
        barDs.setValueTextSize(9f);
        BarData barData = new BarData(barDs);
        barData.setBarWidth(0.6f);
        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(idxFmt);
        barChart.getXAxis().setLabelCount(Math.min(12, labels.size()));
        barChart.invalidate();

        LineDataSet lineDs = new LineDataSet(lineEntries, getString(R.string.chart_line_caption));
        lineDs.setColor(color);
        lineDs.setCircleColor(color);
        lineDs.setLineWidth(2f);
        lineDs.setValueTextSize(9f);
        lineDs.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        LineData lineData = new LineData(lineDs);
        lineChart.setData(lineData);
        lineChart.getXAxis().setValueFormatter(idxFmt);
        lineChart.getXAxis().setLabelCount(Math.min(12, labels.size()), false);
        lineChart.invalidate();
    }
}
