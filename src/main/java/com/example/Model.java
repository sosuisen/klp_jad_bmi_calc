package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.example.Model.HealthRecords.BmiEntry;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.JsonbException;

// BMIの計算、JSONファイルの書き込み、読み込みを実行するモデルです。
public class Model {
    private static final Path FILE_PATH = Path.of("c:\\pleiades\\2024-03\\health-records.json");
	private static final Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().withFormatting(true));
	private static final Logger logger = Logger.getLogger(Model.class.getName());

	//読み書きするJSONデータを表すレコードです。
	public record HealthRecords(List<BmiEntry> bmiEntries) {
		public record BmiEntry(LocalDateTime createdDate, double mHeight, double kgWeight, double bmi) {
		}
	}

	private HealthRecords getHealthRecords() throws IOException, JsonbException {
		try {
			return jsonb.fromJson(Files.readString(FILE_PATH), HealthRecords.class);
		} catch (NoSuchFileException e) {
			// ファイルが存在しない場合に発生します。
			// 保存時に自動的に作成されるため、問題のない例外であるため、
			// エラーではなく情報(info)レベルのログのみ出力します。
			logger.info("File not found: " + FILE_PATH);
		}
		
		return new HealthRecords(new ArrayList<BmiEntry>());
	}

	public List<BmiEntry> getBmiEntries() throws IOException, JsonbException {
		return getHealthRecords().bmiEntries();
	}

	public synchronized void save(BmiEntry bmiEntry) throws IOException, JsonbException {
		var healthRecords = getHealthRecords();
		healthRecords.bmiEntries().add(bmiEntry);
		Files.writeString(FILE_PATH, jsonb.toJson(healthRecords));
	}

	public double calc(double mHeight, double kgWeight) throws IOException, JsonbException {
		var bmiValue = kgWeight / (mHeight * mHeight);
		var bmi = new BmiEntry(LocalDateTime.now(), mHeight, kgWeight, bmiValue);
		save(bmi);
		return bmiValue;
	}
	
	public String getBmiCategory(double bmi) {
		if (bmi < 18.5) {
			return "underweight";
		} else if (bmi < 25) {
			return "normal";
		} else {
			return "obesity";
		}
	}
	
}
