package com.example;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.model.Model;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// コントローラを担当するサーブレットです。
public class BmiServlet extends HttpServlet {
	private static final Model model = new Model();

	private final double METER_FEET = 3.2808;
	private final double KG_POUNDS = 2.2046;

	// BMIに関する値をJSPへ渡すためだけに用いる表示用recordです。
	// JSP側でimportするためpublic staticにしています。
	public static record DisplayEntry(String height, String weight, String bmi, String createdDate) {
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// Modelから過去の結果を取得します。
		var bmiEntries = model.getBmiEntries();

		// Modelから受け取ったデータをViewで表示しやすいよう加工するのは、Controllerの役割です。
		// mをcmに変換して、日時から秒数を削除して、順序を新しい順にします。
		List<DisplayEntry> displayEntries = bmiEntries.stream()
				.map(entry -> new DisplayEntry(String.format("%.1f", entry.mHeight() * METER_FEET),
						String.format("%.1f", entry.kgWeight() * KG_POUNDS),
						String.format("%.1f", entry.bmi()),
						entry.createdDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))))
				.toList().reversed();

		// データをViewに渡すため、リクエストスコープへセットします。
		request.setAttribute("displayEntries", displayEntries);

		// doPostから呼ばれたときは既にcurrentEntryがセットされているので、nullのときだけセットします。
		if (request.getAttribute("currentEntry") == null)
			request.setAttribute("currentEntry", new DisplayEntry("", "", "", ""));

		request.getRequestDispatcher("/WEB-INF/bmi.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		// Webブラウザから送られてきたデータを取得します。
		var feetHeight = Double.valueOf(request.getParameter("feetHeight"));
		var poundsWeight = Double.valueOf(request.getParameter("poundsWeight"));

		// 単位変換	
		var mHeight = feetHeight / METER_FEET;
		var kgWeight = poundsWeight / KG_POUNDS;

		// ModelのBMI計算機能を呼び出します。
		var bmi = model.calc(mHeight, kgWeight);

		// 入力と計算結果を表示するため、リクエストスコープにセットします。
		var currentEntry = new DisplayEntry(String.valueOf(feetHeight), String.valueOf(poundsWeight), String.format("%.1f", bmi), "");
		request.setAttribute("currentEntry", currentEntry);

		// この後の処理はdoGetメソッドと同じなので、doGetに任せます。
		doGet(request, response);
	}
}
