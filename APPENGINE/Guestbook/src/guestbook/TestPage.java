package guestbook;

import java.io.IOException;
import javax.servlet.http.*;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.List;

import org.scribe.oauth.*;

import org.apache.commons.codec.*;

import guestbook.MeetupUser;
import guestbook.PMF;


public class TestPage extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		Properties prop = new Properties();
		prop.setProperty("consumer.key","12345");
		prop.setProperty("consumer.secret","67890");
		prop.setProperty("request.token.verb","POST");
		prop.setProperty("request.token.url","http://www.meetup.com/oauth/request/");
		prop.setProperty("access.token.verb","POST");
		prop.setProperty("access.token.url","http://www.meetup.com/oauth/access/");
		prop.setProperty("callback.url","http://localhost:8080/test");

		//Create scribe object
		Scribe scribe = new Scribe(prop);

		PersistenceManager pm = PMF.get().getPersistenceManager();

		if (req.getQueryString() != null) {  //If access key is obtained
			resp.setContentType("text/plain");
			resp.getWriter().println(req.getQueryString());
			
			String token = getArg("oauth_token",req.getQueryString());
			String verify = getArg("oauth_verifier",req.getQueryString());
			Token requestToken = null;
			//resp.getWriter().println(token);
			//resp.getWriter().println(verify);

			Query query = pm.newQuery(MeetupUser.class);
			query.setFilter("reqToken == reqTokenParam");
			query.declareParameters("String reqTokenParam");
			try {
				List<MeetupUser> users = (List<MeetupUser>) query.execute(token);
				if (users.iterator().hasNext()) {
					requestToken = new Token(users.get(0).getReqToken(),users.get(0).getReqTokenSecret());

					Token accessToken = scribe.getAccessToken(requestToken, verify);

					resp.getWriter().println(accessToken.toString());
				}
				
			} finally {
				query.closeAll();
			}
			
			

		} else {
			//Get request Token
			Token requestToken = scribe.getRequestToken();

			MeetupUser newUser = new MeetupUser();
			newUser.setReqToken(requestToken.getToken());		
			newUser.setReqTokenSecret(requestToken.getSecret());
			try {
				pm.makePersistent(newUser);
			} finally {
				pm.close();
			}
		
			resp.sendRedirect("http://www.meetup.com/authorize/?oauth_token="+requestToken.getToken());
		
		}

	}

	//Parses a given query string and returns the value of reqVar, if it exists
	public String getArg(String reqVar, String query) {
		StringTokenizer st = new StringTokenizer(query,"&");
		while (st.hasMoreTokens()) {
			String temp = st.nextToken();
			if (temp.startsWith(reqVar)) {
				return temp.substring(reqVar.length() + 1);
			}	
		}
		return null;
	}
}
