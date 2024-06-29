<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.example.BmiServlet.DisplayEntry" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<%
// プロジェクトのJavaのビルドパスに src/main/resources を追加しておく必要があります。
var locale = request.getLocale();
var mes = ResourceBundle.getBundle("messages", locale);
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title><%= mes.getString("title") %></title>
</head>
<body>
    <h1><%= mes.getString("title") %></h1>
    <%
        var current = (DisplayEntry) request.getAttribute("currentEntry");
	%>
    <form action="./" method="post">
        <table>
            <tr>
                <td><%= mes.getString("height") %></td>
                <td><input size="7" type="text" name="height" value="<%= current.height() %>"></td>
                <td><%= mes.getString("height_unit") %></td>
            </tr>
            <tr>
                <td><%= mes.getString("weight") %></td>
                <td><input size="7" type="text" name="weight" value="<%= current.weight() %>"></td>
                <td><%= mes.getString("weight_unit") %></td>
            </tr>
			<tr>
			    <td>BMI</td>
                <td><%= current.bmi() %></td>
                <td><%= current.category() %></td>
           	</tr>	
		</table>
        <button><%=mes.getString("calc")%></button>
    </form>
    <p style="color: red;">
        <%= mes.getString((String) request.getAttribute("error")) %>
    </p>
    <p>
        
    </p>
    <h2><%= mes.getString("history") %></h2>

    <table border="1">
    	<thead>
    	    <tr>
    	    	<th><%= mes.getString("date") %></th>
    	    	<th><%= mes.getString("height") %>(<%= mes.getString("height_unit") %>)</th>
    	    	<th><%= mes.getString("weight") %>(<%= mes.getString("weight_unit") %>)</th>
    	    	<th>BMI</th>
    	    </tr>
    	</thead>
        <%
            var entries = (List<DisplayEntry>) request.getAttribute("displayEntries");
            if (entries != null) {
                for (var entry : entries) {
        %>
        <tr>
            <td><%= entry.createdDate() %></td>
            <td><%= entry.height() %></td>
            <td><%= entry.weight() %></td>
            <td><%= entry.bmi() %></td>
            <td><%= entry.category() %></td>
        </tr>
        <%
                }
            }
        %>
    </table>
</body>
</html>
