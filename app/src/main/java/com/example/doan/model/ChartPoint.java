package com.example.doan.model;

/** Một điểm trên biểu đồ (nhãn trục X, giá trị). */
public class ChartPoint {
    public final String label;
    public final float value;

    public ChartPoint(String label, float value) {
        this.label = label;
        this.value = value;
    }
}
