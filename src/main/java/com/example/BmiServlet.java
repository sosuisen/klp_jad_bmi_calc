package com.example;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

import jakarta.json.bind.JsonbException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// コントローラを担当するサーブレットです。
public class BmiServlet extends HttpServlet {
	private static final double METER_FEET = 3.2808;
	private static final double KG_POUNDS = 2.2046;
	private static final Logger logger = Logger.getLogger(BmiServlet.class.getName());
	private static final Model model = new Model();	

	// BMIに関する値をJSPへ渡すためだけに用いる表示用recordです。
	public record DisplayEntry(String createdDate, String height, String weight, String bmi) {		
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String language = request.getLocale().getLanguage(); // ja, en など
		logger.info("Language: " + language);
		
		try {
			// Modelから過去の結果を取得します。
			var bmiEntries = model.getBmiEntries();

			// Modelから受け取ったデータをViewで表示しやすいよう加工するのは、Controllerの役割です。
			// 単位を変換して、日時から秒数を削除して、順序を新しい順にします。
			List<DisplayEntry> displayEntries = bmiEntries.stream()
					.map(entry -> new DisplayEntry(
							entry.createdDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
							String.format("%.1f", language.equals("en") ? entry.mHeight() * METER_FEET : entry.mHeight() * 100 ),
							String.format("%.1f", language.equals("en") ? entry.kgWeight() * KG_POUNDS : entry.kgWeight() ),
							String.format("%.1f", entry.bmi())))
					.toList().reversed();

			// データをViewに渡すため、リクエストスコープへセットします。
			request.setAttribute("displayEntries", displayEntries);
		} catch (IOException e) {
			// ファイルが壊れている場合などに発生。大きな問題です。
			logger.severe("IOException: " + e.getMessage());
			request.setAttribute("error", "io_error");
		} catch (JsonbException e) {
			// JSONの変換に失敗した場合に発生。
			// JSONファイルを修正するか削除する必要があるので大きな問題です。
			logger.severe("JsonbException: " + e.getMessage());
			request.setAttribute("error", "json_error");
		} catch (Exception e) {
			// その他の例外が発生した場合
			logger.severe("Exception: " + e.getMessage());
			request.setAttribute("error", "unknown_error");
		} finally {
			// nullの場合は値を設定しておきます。
			// doPost経由でdoGetが呼ばれたときは、nullでないことがあります。
			if (request.getAttribute("error") == null) {
				request.setAttribute("error", "no_error");
			}
			if (request.getAttribute("currentEntry") == null) {
				request.setAttribute("currentEntry", new DisplayEntry("", "", "", ""));
			}
		}
		request.getRequestDispatcher("/WEB-INF/bmi.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String language = request.getLocale().getLanguage(); // ja, en など

		try {
			var height = Double.parseDouble(request.getParameter("height"));
			var weight = Double.parseDouble(request.getParameter("weight"));

			// 単位変換	
			var mHeight = language.equals("en") ? height / METER_FEET : height / 100;
			var kgWeight = language.equals("en") ? weight / KG_POUNDS : weight;

			// ModelのBMI計算機能を呼び出します。
			var bmi = model.calc(mHeight, kgWeight);
			
			// 入力と計算結果を表示するため、リクエストスコープにセットします。
			var currentEntry = new DisplayEntry("", String.valueOf(height), String.valueOf(weight),
					String.format("%.1f", bmi));
			request.setAttribute("currentEntry", currentEntry);
		} catch (NumberFormatException e) {
			// 入力が数値でない場合に発生
			// ユーザに再入力してもらえば良いだけなので、大きな問題ではありません。
			logger.warning("NumberFormatException: " + e.getMessage());
			request.setAttribute("error", "number_format_error");
		} catch (JsonbException e) {
			logger.severe("JsonbException: " + e.getMessage());
			request.setAttribute("error", "json_error");
		} catch (IOException e) {
			// ファイルが壊れていたり、読み取り専用になっている場合などに発生
			// 大きな問題です。
			logger.severe("IOException: " + e.getMessage());
			request.setAttribute("error", "io_error");
		} catch (Exception e) {
			logger.severe("Exception: " + e.getMessage());
			request.setAttribute("error", "unknown_error");
		} 
		// この後の処理はdoGetメソッドと同じなので、doGetに任せます。
		doGet(request, response);
	}
}
