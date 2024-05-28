package imise;

import jakarta.servlet.http.HttpServlet;

public class ServletCshDelete extends HttpServlet {
	private static final long serialVersionUID = 4311758397612594122L;
	protected final static String JSON = "application/json; charset=utf-8";
	final static String HTML = "text/html; charset=utf-8";
	final static String XML = "application/xml; charset=utf-8";


//	@Override
//	protected void doGet(HttpServletRequest request, HttpServletResponse response)
//			throws ServletException, IOException {
//		try {			
//			JsonAPI api = new JsonAPI(System.getProperty("CSH_URL"));		
//			api.getAccessToken(System.getProperty("CLIENT_ID"),System.getProperty("CLIENT_SECRET"),System.getProperty("CSH_TOKEN_URL"));
//			
//			JsonNode json = api.deleteResource("api/resources/");
//			response.setContentType(JSON);
//			response.setStatus(HttpServletResponse.SC_OK);
//			response.getWriter().print(json.toString());
//		} catch (HttpException e) {
//			response.sendError(e.getCode(), e.getMessage());
//		} catch (IOException | InterruptedException e) {
//			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
//		}
//	}
}