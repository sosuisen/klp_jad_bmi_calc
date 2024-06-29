package com.example;

import java.io.IOException;
import java.util.List;

import com.example.model.BmiManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Servlet implementation class BmiServlet
 */
public class BmiServlet extends HttpServlet {
	private static final BmiManager model = new BmiManager();

	private final double METER_FEET = 3.2808;
	private final double KG_POUNDS = 2.2046;
	
	// BMIに関する値をJSPへ渡すためだけに用いる、Lombokで作成したDTOクラスです。
	@Getter
	@AllArgsConstructor
	public static class BmiDTO {
		private String height;
		private String weight;
		private String bmi;
		private String createdDate;
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Modelから過去の結果を取得します。
		var bmiList = model.getBmiList();

		// Modelから受け取ったデータをViewで表示しやすいよう加工するのは、Controllerの役割です。
		// mをcmに変換して、順序を新しい順にします。
		List<BmiDTO> history = bmiList.stream().map(bmi ->
			new BmiDTO(String.format("%.1f", bmi.mHeight() * METER_FEET),
                String.format("%.1f", bmi.kgWeight() * KG_POUNDS),
                String.format("%.1f", bmi.bmi()),
                bmi.createdDate().toString().substring(0, 10))
		).toList().reversed();
		
		// データをViewに渡すため、リクエストスコープへセットします。
		request.setAttribute("history", history);
		if(request.getAttribute("current") == null)
			request.setAttribute("current", new BmiDTO("", "", "", ""));
			
		request.getRequestDispatcher("/WEB-INF/bmi.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		
		// Webブラウザから送られてきたデータを取得します。
		var feetHeight = Double.valueOf(request.getParameter("feetHeight"));
		var poundsWeight = Double.valueOf(request.getParameter("poundsWeight"));
		
		var mHeight = feetHeight / METER_FEET;
		var kgWeight = poundsWeight / KG_POUNDS;

		// ModelのBMI計算機能を呼び出します。
		var bmi = model.calc(mHeight, kgWeight);
		
		// 入力と計算結果を表示するため、リクエストスコープにセットします。
		var current = new BmiDTO(String.valueOf(feetHeight), String.valueOf(poundsWeight), String.format("%.1f", bmi), "");
		request.setAttribute("current", current);
		
		// この後の処理はdoGetメソッドと同じなので、doGetに任せます。
		doGet(request, response);
	}
}
