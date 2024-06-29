<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.example.BmiServlet.DisplayEntry" %>	
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>BMI計算機</title>
</head>
<body>
	<h1>身長と体重を入力してください</h1>
	<% var current = (DisplayEntry)request.getAttribute("currentEntry"); %>
	<form action="./" method="post">
	    身長(feet)：<input type="text" name="feetHeight" value="<%= current.height() %>"><br>
		体重(pounds)：<input type="text" name="poundsWeight" value="<%= current.weight() %>"><br>
		<button>計算</button><br
	</form>
	BMI：<%= current.bmi() %>
	<h2>計算履歴</h2>
	<ul>
	<% var entries = (List<DisplayEntry>)request.getAttribute("displayEntries");
	   for (var entry : entries) { %>
		<li>[<%= entry.createdDate() %>] <%= entry.height() %>feet, <%= entry.weight() %> pound, BMI: <%= entry.bmi() %></li>
	<% } %>
	</ul>
</body>
</html>
