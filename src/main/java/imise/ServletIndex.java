package imise;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.text.StringSubstitutor;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ServletIndex extends HttpServlet {
	private static final long serialVersionUID = 4311758397612594122L;
	protected final static String JSON = "application/json; charset=utf-8";
	final static String HTML = "text/html; charset=utf-8";
	final static String XML = "application/xml; charset=utf-8";
	String text;
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			response.setContentType(HTML);
			response.setStatus(HttpServletResponse.SC_OK);
			if (text == null) {
				text = new String(getClass().getClassLoader().getResourceAsStream("index.html").readAllBytes(), StandardCharsets.UTF_8);
				text = StringSubstitutor.replace(text, System.getProperties());			
			}
			response.getWriter().print(text);
		} catch (IOException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
	}
}