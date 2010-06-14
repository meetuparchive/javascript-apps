<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="javax.jdo.PersistenceManager" %>
<%@ page import="javax.jdo.Query" %>
<%@ page import="java.util.Properties" %>
<%@ page import="java.util.List" %>

<%@ page import="guestbook.MeetupUser" %>
<%@ page import="guestbook.PMF" %>

<%@ page import="java.io.IOException" %>
<%@ page import="javax.servlet.http.*" %>

<%@ page import="org.scribe.oauth.*" %>
<%@ page import="org.scribe.http.*" %>

<%@ page import="org.json.*" %>

<%@ page import="javax.servlet.http.Cookie" %>
<html>
	<head>
		<link type="text/css" rel="stylesheet" href="/stylesheets/main.css" />
		<script src="saveKey.js"></script>
	</head>
	<body>
TEST
<a href="/test">LOGIN</a>
<%
		String key = "empty";
    		Cookie[] cookies = request.getCookies();
    		if (cookies != null) {
      			for (int i = 0; i < cookies.length; i++) {
        			if (cookies[i].getName().equals("meetup_access")) {
          				key = cookies[i].getValue();
        			}
      			}
    		}
		if (key.equals("empty")) {
			response.sendRedirect("/test");
		}	
		
		Properties prop = new Properties();
		prop.setProperty("consumer.key","12345");
		prop.setProperty("consumer.secret","67890");
		Scribe scribe = new Scribe(prop);
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(MeetupUser.class);
		query.setFilter("accToken == accTokenParam");
		query.declareParameters("String accTokenParam");
		try {
			List<MeetupUser> users = (List<MeetupUser>) query.execute(key);
			if (users.iterator().hasNext()) {
				Token accessToken = new Token(users.get(0).getAccToken(),users.get(0).getAccTokenSecret());
				Request APIrequest = new Request(Request.Verb.GET, "http://api.meetup.com/groups.json/?zip=11211&page=5&order=ctime&desc=true");
				scribe.signRequest(APIrequest,accessToken);
				Response APIresponse = APIrequest.send();
				JSONObject json = new JSONObject();
				try {
					json = new JSONObject(APIresponse.getBody());
					String[] names = JSONObject.getNames(json.getJSONArray("results").getJSONObject(0));
					for (int j = 0; j < json.getJSONArray("results").length(); j++) {
%>
<p>[<b><%=j %></b>]</p>
<%						
						for (int i = 0; i < names.length; i++) {
							String temp = json.getJSONArray("results").getJSONObject(j).getString(names[i]);
%>	
<p><%= (names[i]+": "+temp) %></p>					
<%
						}	
					}
				} catch (JSONException j) {
		
				}
			}
		}
		finally {
			query.closeAll();
		}
%>
	</body>
</html>
