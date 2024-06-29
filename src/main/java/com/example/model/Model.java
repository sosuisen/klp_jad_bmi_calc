package com.example.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.model.HealthRecords.BmiEntry;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.JsonbException;

// BMIの計算、JSONファイルの書き込み、読み込みを実行するモデルです。
public class Model {
	private static final String filePath = "c:\\pleiades\\2024-03\\health-records.json";

	private static final Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().withFormatting(true));

	private HealthRecords getHealthRecords() throws IOException, JsonbException {
		try {
			return jsonb.fromJson(Files.readString(Path.of(filePath)), HealthRecords.class);
		} catch (NoSuchFileException e) {
			// ファイルが存在しない場合に発生します。
			// 保存時に自動的に作成されるため、問題のない例外です。
			System.out.println("File not found: " + filePath);
		}
		
		return new HealthRecords(new ArrayList<BmiEntry>());
	}

	public List<BmiEntry> getBmiEntries() throws IOException, JsonbException {
		return getHealthRecords().bmiEntries();
	}

	public synchronized void save(BmiEntry bmiEntry) throws IOException, JsonbException {
		var healthRecords = getHealthRecords();
		healthRecords.bmiEntries().add(bmiEntry);
		Files.writeString(Path.of(filePath), jsonb.toJson(healthRecords));
	}

	public double calc(double mHeight, double kgWeight) throws IOException, JsonbException {
		var bmiValue = kgWeight / (mHeight * mHeight);
		var bmi = new BmiEntry(LocalDateTime.now(), mHeight, kgWeight, bmiValue);
		save(bmi);
		return bmiValue;
	}
}
