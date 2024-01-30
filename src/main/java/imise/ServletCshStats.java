package imise;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ServletCshStats extends HttpServlet {
	private static final long serialVersionUID = 4311758397612594122L;
	protected final static String JSON = "application/json; charset=utf-8";
	final static String HTML = "text/html; charset=utf-8";
	final static String XML = "application/xml; charset=utf-8";


	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			JsonAPI api = new JsonAPI(System.getProperty("CSH_URL"));		
			JsonNode json = api.getResource("api/stats/");
			response.setContentType(JSON);
			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().print(json.toString());
			return;
		} catch (HttpException e) {
			response.sendError(e.getCode(), e.getMessage());
		} catch (IOException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
}