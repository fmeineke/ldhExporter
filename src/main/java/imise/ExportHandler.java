package imise;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ExportHandler implements HttpHandler {
	static XslPipeline xp;
	public ExportHandler() {
		xp = new XslPipeline();
	}
	static public Map<String, String> queryToMap(String query) {
		if(query == null) {
			return null;
		}
		Map<String, String> result = new HashMap<>();
		for (String param : query.split("&")) {
			String[] entry = param.split("=");
			if (entry.length > 1) {
				result.put(entry[0], entry[1]);
			}else{
				result.put(entry[0], "");
			}
		}
		return result;
	}
	static boolean isValidURL(String url)  {
		try {
			new URL(url).toURI();
			return true;
		} catch (MalformedURLException e) {
			return false;
		} catch (URISyntaxException e) {
			return false;
		}
	}	
	public void error(HttpExchange t,int code, String msg) throws UnsupportedEncodingException, IOException {
		t.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
		msg = "<html><h1>" + code + "</h1><p>" + msg + "</p></html>";
		t.sendResponseHeaders(code,0);
		OutputStream os = t.getResponseBody();
		os.write(msg.getBytes());
		os.close();
	}
	//		}
	@Override
	public void handle(HttpExchange t) throws IOException {
		try (OutputStream os = t.getResponseBody()){    
			String query = t.getRequestURI().getQuery();
			if (query == null) {
				t.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
				t.sendResponseHeaders(HttpURLConnection.HTTP_OK,0);
				//					try (OutputStream os = t.getResponseBody()) {
				//	getClass().getClassLoader().getResourceAsStream("index.html").transferTo(os);
				byte[] data = getClass().getClassLoader().getResourceAsStream("index.html").readAllBytes();
				String str = new String(data, StandardCharsets.UTF_8);
				str = str.replaceAll("\\$\\{LDH_EXP\\}", Main.service);
				str = str.replaceAll("\\$\\{LDH_SOURCE\\}", Main.source);
				os.write(str.getBytes());
			} else {
				Map<String, String> params = queryToMap(query);

				// mainly for debug - reload / recompile stylesheets
				String compile = params.get("compile");			
				if (compile != null) {
					xp = new XslPipeline();
				}
				String format = params.get("format");
				if (format == null)  format = "csh";


				// ok, this is too dangerous
//				String url = params.get("url");
				String url = Main.source;
//				if (url == null) url = Main.source;
				if (url == null) {
					throw new HttpException(HttpURLConnection.HTTP_BAD_REQUEST, "missing url");
				}
				if (!isValidURL(url)) {
					throw new HttpException(HttpURLConnection.HTTP_BAD_REQUEST, "invalid url");
				}
				String id = params.get("id");
				if (id == null) {
					throw new HttpException(HttpURLConnection.HTTP_BAD_REQUEST, "missing id");
				} 
				if (!id.matches("(investigations|studies)/[0-9]{1,10}")) {
					throw new HttpException(HttpURLConnection.HTTP_BAD_REQUEST, "invalid id");
				} 
				//				id = "investigations/" + id + ".json";
				id = id + ".json";
				JsonAPI sa = new JsonAPI(url);
				JsonNode json = sa.getResource(id);
				Source s = xp.prepareJson(json);	

				switch(format) {
				case "seek":
					t.sendResponseHeaders(HttpURLConnection.HTTP_OK,0);					
					t.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
					ObjectMapper objectMapper = new ObjectMapper();
					os.write(objectMapper.writeValueAsBytes(json));
					break;
				case "seekxml":
					t.sendResponseHeaders(HttpURLConnection.HTTP_OK,0);					
					t.getResponseHeaders().set("Content-Type", "application/xml; charset=utf-8");
					xp.pipeToSeekXml(s, os);
					break;
				case "xml":
					t.sendResponseHeaders(HttpURLConnection.HTTP_OK,0);					
					t.getResponseHeaders().set("Content-Type", "application/xml; charset=utf-8");
					xp.pipeToXml(s, os);
					break;
				case "cshxml":
					t.sendResponseHeaders(HttpURLConnection.HTTP_OK,0);					
					t.getResponseHeaders().set("Content-Type", "application/xml; charset=utf-8");
					xp.pipeToCshXml(s, os);
					break;
				case "csh":
					t.sendResponseHeaders(HttpURLConnection.HTTP_OK,0);					
					t.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
					xp.pipeToCsh(s, os);
					break;
				case "fhir":
					t.sendResponseHeaders(HttpURLConnection.HTTP_OK,0);					
					t.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
					xp.pipeToFhir(s, os);
					break;
				case "cshval":
					t.sendResponseHeaders(HttpURLConnection.HTTP_OK,0);			
					t.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
					ByteArrayOutputStream data = new ByteArrayOutputStream();
					xp.pipeToCsh(s, data);
					CshAPI.validate(new ByteArrayInputStream(data.toByteArray()),os);					
					break;
				default:
					Main.log.debug("unknown format" + format);
					throw new HttpException(HttpURLConnection.HTTP_NOT_IMPLEMENTED, "format not implemented");
				}
			}
		} catch (HttpException e) {
			error(t,e.code,e.getMessage());
		} catch (Exception e) {
			error(t,HttpURLConnection.HTTP_INTERNAL_ERROR, e.getMessage());
		}
	} 
}