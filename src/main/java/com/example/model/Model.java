package com.example.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.example.model.HealthRecords.BmiEntry;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

// BMIの計算、JSONファイルの書き込み、読み込みを実行するモデルです。
public class Model {
	private String filePath = "c:\\pleiades\\2024-03\\health-records.json";
	
	JsonbConfig config = new JsonbConfig().withFormatting(true);
	Jsonb jsonb = JsonbBuilder.create(config);

	private HealthRecords getHealthRecords() {
		try {
			return jsonb.fromJson(Files.readString(Path.of(filePath)), HealthRecords.class);
		} catch (NoSuchFileException e) {
			System.out.println("File not found: " + filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new HealthRecords(new ArrayList<BmiEntry>());
	}
	
	public ArrayList<BmiEntry> getBmiEntries() {
	    return getHealthRecords().bmiEntries();	
	}
	
	synchronized public void save(BmiEntry bmiEntry) {
		try {
			var healthRecords = getHealthRecords();
			healthRecords.bmiEntries().add(bmiEntry);
            Files.writeString(Path.of(filePath), jsonb.toJson(healthRecords));
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public double calc(double mHeight, double kgWeight) {
		var bmiValue = kgWeight / (mHeight * mHeight);
		var bmi = new BmiEntry(mHeight, kgWeight, bmiValue, LocalDateTime.now());
		save(bmi);
		return bmiValue;
	}
}
