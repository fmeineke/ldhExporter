package imise;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ServletCshUser extends HttpServlet {
	private static final long serialVersionUID = 4311758397612594122L;
	protected final static String JSON = "application/json; charset=utf-8";
	final static String HTML = "text/html; charset=utf-8";
	final static String XML = "application/xml; charset=utf-8";


	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {			
			JsonAPI api = new JsonAPI(System.getProperty("CSH_URL"));		
			api.getAccessToken(System.getProperty("CLIENT_ID"),System.getProperty("CLIENT_SECRET"),System.getProperty("CSH_TOKEN_URL"));
			JsonNode json = api.getResource("api/resources/user/");
			response.setContentType(JSON);
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().print(json.toString());
		} catch (HttpException e) {
			response.sendError(e.getCode(), e.getMessage());
		} catch (IOException | InterruptedException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
}