package com.example.model;

import java.time.LocalDateTime;
import java.util.ArrayList;

//読み書きするJSONデータを表すレコードです。
public record HealthRecords(ArrayList<BmiEntry> bmiEntries) {
	public record BmiEntry(double mHeight, double kgWeight, double bmi, LocalDateTime createdDate) {
	}
}
