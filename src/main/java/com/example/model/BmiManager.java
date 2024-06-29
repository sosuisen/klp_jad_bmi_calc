package com.example.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

// BMIの計算、JSONファイルの書き込み、読み込みを実行するモデルです。
public class BmiManager {
	private String filePath = "c:\\pleiades\\2024-03\\bmidata.json";
	
	JsonbConfig config = new JsonbConfig().withFormatting(true);
	Jsonb jsonb = JsonbBuilder.create(config);
	
	public ArrayList<Bmi> getBmiList() {
		try {
			return jsonb.fromJson(Files.readString(Path.of(filePath)),
					new ArrayList<Bmi>(){}.getClass().getGenericSuperclass());
		} catch (NoSuchFileException e) {
			System.out.println("File not found: " + filePath);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<Bmi>();
	}
	
	synchronized public void save(Bmi bmi) {
		try {
			var bmiList = getBmiList();
			bmiList.add(bmi);
			
            Files.writeString(Path.of(filePath), jsonb.toJson(bmiList));
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public double calc(double mHeight, double kgWeight) {
		var bmiValue = kgWeight / (mHeight * mHeight);
		var bmi = new Bmi(mHeight, kgWeight, bmiValue, LocalDateTime.now());
		save(bmi);
		return bmiValue;
	}
}
