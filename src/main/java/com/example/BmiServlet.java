package com.example;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.example.model.Model;

import jakarta.json.bind.JsonbException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// コントローラを担当するサーブレットです。
public class BmiServlet extends HttpServlet {
	private static final Model model = new Model();

	private static final double METER_FEET = 3.2808;
	private static final double KG_POUNDS = 2.2046;


	// BMIに関する値をJSPへ渡すためだけに用いる表示用recordです。
	// JSP側でimportするためpublic staticにしています。
	public static record DisplayEntry(String height, String weight, String bmi, String createdDate) {
	}
	
	private ResourceBundle getBundle(HttpServletRequest request) {
		Locale locale = request.getLocale();
		return ResourceBundle.getBundle("messages", locale);
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			// Modelから過去の結果を取得します。
			var bmiEntries = model.getBmiEntries();

			// Modelから受け取ったデータをViewで表示しやすいよう加工するのは、Controllerの役割です。
			// 単位を変換して、日時から秒数を削除して、順序を新しい順にします。
			List<DisplayEntry> displayEntries = bmiEntries.stream()
					.map(entry -> new DisplayEntry(String.format("%.1f", entry.mHeight() * METER_FEET),
							String.format("%.1f", entry.kgWeight() * KG_POUNDS),
							String.format("%.1f", entry.bmi()),
							entry.createdDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))))
					.toList().reversed();

			// データをViewに渡すため、リクエストスコープへセットします。
			request.setAttribute("displayEntries", displayEntries);
			// doPostでエラーがあった場合はnullではありません。
			if (request.getAttribute("error") == null) {
				request.setAttribute("error", "no_error");
			}
		} catch (IOException e) {
			e.printStackTrace();
			request.setAttribute("error", "io_error");
		} catch (JsonbException e) {
			e.printStackTrace();
			request.setAttribute("error", "json_error");
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("error", "unknown_error");
		}
		request.getRequestDispatcher("/WEB-INF/bmi.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			var feetHeight = Double.parseDouble(request.getParameter("feetHeight"));
			var poundsWeight = Double.parseDouble(request.getParameter("poundsWeight"));

			// 単位変換	
			var mHeight = feetHeight / METER_FEET;
			var kgWeight = poundsWeight / KG_POUNDS;

			// ModelのBMI計算機能を呼び出します。
			var bmi = model.calc(mHeight, kgWeight);
			
			// 入力と計算結果を表示するため、リクエストスコープにセットします。
			var currentEntry = new DisplayEntry(String.valueOf(feetHeight), String.valueOf(poundsWeight),
					String.format("%.1f", bmi), "");
			request.setAttribute("currentEntry", currentEntry);
		} catch (NumberFormatException e) {
			// 入力が数値でない場合に発生
			request.setAttribute("error", "number_format_error");
		} catch (JsonbException e) {
			// JSONの変換に失敗した場合に発生
			e.printStackTrace();
			request.setAttribute("error", "json_error");
		} catch (IOException e) {
			// ファイルが読み取り専用になっている場合などに発生
			e.printStackTrace();
			request.setAttribute("error", "io_error");
		} catch (Exception e) {
			e.printStackTrace();
			request.setAttribute("error", "unknown_error");
		} 
		// この後の処理はdoGetメソッドと同じなので、doGetに任せます。
		doGet(request, response);
	}
}
