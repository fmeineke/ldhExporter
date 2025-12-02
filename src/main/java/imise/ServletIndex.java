package imise;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.text.StringSubstitutor;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ServletIndex extends HttpServlet {
	private static final long serialVersionUID = 4311758397612594122L;
	private static final String HTML = "text/html; charset=utf-8";
	private volatile String text;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType(HTML);
		response.setStatus(HttpServletResponse.SC_OK);

		if (text == null) {
			synchronized (this) {
				if (text == null) {
					InputStream in = getClass().getClassLoader().getResourceAsStream("index.html");
					if (in == null) {
						response.sendError(HttpServletResponse.SC_NOT_FOUND, "index.html not found");
						return;
					}
					text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
					text = StringSubstitutor.replace(text, System.getProperties());
				}
			}
		}
		response.getWriter().write(text);
	}
}
