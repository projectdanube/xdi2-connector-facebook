<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>xdi2-connector-facebook</title>
<link rel="stylesheet" target="_blank" href="style.css" TYPE="text/css" MEDIA="screen">
</head>
<body style="background-image: url('images/back.png'); background-repeat: repeat-y; margin-left: 60px;">

	<div class="header">
	<img src="images/facebook-logo.png" align="middle">&nbsp;&nbsp;&nbsp;
	<img src="images/arrow.png" align="middle">&nbsp;&nbsp;&nbsp;
	<img src="images/logo64.png" align="middle">&nbsp;&nbsp;&nbsp;<span id="appname">xdi2-connector-facebook</span>
	</div>

	<% if (request.getAttribute("error") != null) { %>

		<p><font color="red"><%= request.getAttribute("error") != null ? request.getAttribute("error") : "" %></font></p>

	<% } %>

	<% if (request.getAttribute("feedback") != null) { %>

		<p><font color="#5e1bda"><%= request.getAttribute("feedback") != null ? request.getAttribute("feedback") : "" %></font></p>

	<% } %>

	<p class="subheader">Obtain Facebook API Access Token</p>

	<p>This step will initiate an OAuth "code flow" (also known as "server-side flow") to the Facebook API, in order to obtain an access token.</p>
	<p>The token is then stored in your XDI graph, where it is picked up and used by the XDI2 server to handle requests to your Facebook XDI context.</p>

	<table>
	<tr>
	
	<td><img src="images/oauth2-logo.png" align="middle" style="float:left;padding-right:10px;"></td>

	<td>
	<form action="connect" method="get" style="float:left;padding-right:10px;">

		<input type="hidden" name="startoauth" value="1">
		<input type="submit" value="Request Access Token!">

	</form>
	</td>

	<td>
	<form action="connect" method="get">

		<input type="hidden" name="revokeoauth" value="1">
		<input type="submit" value="Revoke Access Token!">

	</form>
	</td>
	
	</tr>
	</table>

	<p class="subheader">Send a Message to my XDI Endpoint</p>

	<p>Certain parts of your graph will only be accessible if you have a Facebook API access token.</p>

	<form action="connect" method="post">

		<textarea name="input" style="width: 100%" rows="12"><%= request.getAttribute("input") != null ? request.getAttribute("input") : "" %></textarea><br>

		<% String resultFormat = (String) request.getAttribute("resultFormat"); if (resultFormat == null) resultFormat = ""; %>
		<% String writeContexts = (String) request.getAttribute("writeContexts"); if (writeContexts == null) writeContexts = ""; %>
		<% String writeOrdered = (String) request.getAttribute("writeOrdered"); if (writeOrdered == null) writeOrdered = ""; %>
		<% String writePretty = (String) request.getAttribute("writePretty"); if (writePretty == null) writePretty = ""; %>
		<% String endpoint = (String) request.getAttribute("endpoint"); if (endpoint == null) endpoint = ""; %>

		Send to endpoint: 
		<input type="text" name="endpoint" size="80" value="<%= endpoint %>">

		Result Format:
		<select name="resultFormat">
		<option value="XDI/JSON" <%= resultFormat.equals("XDI/JSON") ? "selected" : "" %>>XDI/JSON</option>
		<option value="XDI DISPLAY" <%= resultFormat.equals("XDI DISPLAY") ? "selected" : "" %>>XDI DISPLAY</option>
		</select>
		&nbsp;

		<input name="writeContexts" type="checkbox" <%= writeContexts.equals("on") ? "checked" : "" %>>contexts=1

		<input name="writeOrdered" type="checkbox" <%= writeOrdered.equals("on") ? "checked" : "" %>>ordered=1

		<input name="writePretty" type="checkbox" <%= writePretty.equals("on") ? "checked" : "" %>>pretty=1

		<input type="submit" value="Go!">

	</form>

	<% if (request.getAttribute("stats") != null) { %>
		<p>
		<%= request.getAttribute("stats") %>

		<% if (request.getAttribute("output") != null) { %>
			Copy&amp;Paste: <textarea style="width: 100px; height: 1.2em; overflow: hidden"><%= request.getAttribute("output") != null ? request.getAttribute("output") : "" %></textarea>
		<% } %>
		</p>
	<% } %>

	<% if (request.getAttribute("output") != null) { %>
		<div class="result"><pre><%= request.getAttribute("output") != null ? request.getAttribute("output") : "" %></pre></div><br>
	<% } %>

</body>
</html>
