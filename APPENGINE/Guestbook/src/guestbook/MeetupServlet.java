package guestbook;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.Properties;
import java.util.List;

import guestbook.MeetupUser;
import guestbook.PMF;

import java.io.IOException;
import javax.servlet.http.*;

import org.scribe.oauth.*;
import org.scribe.http.*;

import org.json.*;

public class MeetupServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		resp.setContentType("text/plain");

		Properties prop = new Properties();
		prop.setProperty("consumer.key","12345");
		prop.setProperty("consumer.secret","67890");
		prop.setProperty("request.token.verb","POST");
		prop.setProperty("request.token.url","http://www.meetup.com/oauth/request/");
		prop.setProperty("access.token.verb","POST");
		prop.setProperty("access.token.url","http://www.meetup.com/oauth/access/");
		prop.setProperty("callback.url",req.getRequestURL().toString());

		//Create scribe object
		Scribe scribe = new Scribe(prop);

		PersistenceManager pm = PMF.get().getPersistenceManager();

		String key = req.getQueryString();

		Query query = pm.newQuery(MeetupUser.class);
		query.setFilter("accToken == accTokenParam");
		query.declareParameters("String accTokenParam");

		try {
			List<MeetupUser> users = (List<MeetupUser>) query.execute(key);
			if (users.iterator().hasNext()) {
				Token accessToken = new Token(users.get(0).getAccToken(),users.get(0).getAccTokenSecret());
				
				//resp.getWriter().println(accessToken.toString());
				Request request = new Request(Request.Verb.GET, "http://api.meetup.com/groups.json/?zip=11211&page=5&order=ctime&desc=true");
				scribe.signRequest(request,accessToken);
				Response response = request.send();
		
				JSONObject json = new JSONObject();

				try {
					json = new JSONObject(response.getBody());
					String[] names = JSONObject.getNames(json.getJSONArray("results").getJSONObject(0));

					for (int j = 0; j < json.getJSONArray("results").length(); j++) {
						resp.getWriter().println("["+j+"]:");
						for (int i = 0; i < names.length; i++) {
							String temp = json.getJSONArray("results").getJSONObject(j).getString(names[i]);
							resp.getWriter().println(names[i]+": "+temp);
						}	
					}



				} catch (JSONException j) {
		
				}
			}

		}
		finally {
			query.closeAll();

		}





	}

}