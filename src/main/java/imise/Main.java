package imise;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpServer;

public class Main {
	static String source,service; 
	static int port=8321;
	final static Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws Exception {

		String portStr = System.getenv("LDH_PORT");
		if (portStr != null) port = Integer.parseInt(portStr); else port = 8321;

		source = System.getenv("LDH_SOURCE");
		log.info("default source (LDH_SOURCE) is " + (source==null?"not yet defined (no LDH_SOURCE, use url param,)":source));

		service = System.getenv("LDH_EXP");
		if (service == null) service="http://localhost:" + port;

		HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/export", new ExportHandler());

		server.start();	
		log.info("service (LDH_EXP) running at " + service);
	}
}
