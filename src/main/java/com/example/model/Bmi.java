package com.example.model;

import java.time.LocalDateTime;

//読み書きするBMIのデータを表すレコードです。
public record Bmi(double mHeight, double kgWeight, double bmi, LocalDateTime createdDate) {
}