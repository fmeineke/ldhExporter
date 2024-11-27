package imise;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LDHExport {
	static XslPipeline xp; 

	final static Logger log = LoggerFactory.getLogger(LDHExport.class);

	public static void copy(String name, String def) {
		String value = System.getenv(name);
		if (value != null) {
			System.setProperty(name, value);
			value = System.getProperty(name);
		} else {
			if (System.getProperty(name) == null && def != null) System.setProperty(name, def);
		}
	}
	static Server server;
	public static void stop() throws Exception {
		server.stop();
	}
	public static void main(String[] args) throws Exception {
		xp = new XslPipeline();
		copy("LDH_EXP_PORT","8083");
		copy("LDH_EXP","http://localhost" + (System.getProperty("LDH_EXP_PORT")==null?"":":"+System.getProperty("LDH_EXP_PORT")));
		copy("LDH_SOURCE","https://ldh.zks.uni-leipzig.de");
		copy("CSH_URL","https://csh.nfdi4health.de");
		copy("CLIENT_ID","ldh-test");		
		copy("CLIENT_SECRET",null);
		copy("CSH_TOKEN_URL","https://sso.studyhub.nfdi4health.de/realms/nfdi4health/protocol/openid-connect/token");


		server = new Server(Integer.parseInt(System.getProperty("LDH_EXP_PORT")));
//		{
//			final HttpConfiguration httpConfiguration = new HttpConfiguration();
//			httpConfiguration.setSecureScheme("https");
//			int httpsPort = 8084;
//			int httpPort = 8083;
//			httpConfiguration.setSecurePort(httpsPort);
//
//			final ServerConnector http = new ServerConnector(server,
//			    new HttpConnectionFactory(httpConfiguration));
//			http.setPort(httpPort);
//			server.addConnector(http);
//			
//
//			String keyStorePassword = null;
//			final SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
//			sslContextFactory.setKeyStorePassword(keyStorePassword);
//			final HttpConfiguration httpsConfiguration = new HttpConfiguration(httpConfiguration);
//			httpsConfiguration.addCustomizer(new SecureRequestCustomizer());
//			final ServerConnector httpsConnector = new ServerConnector(server,
//			    new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
//			    new HttpConnectionFactory(httpsConfiguration));
//			httpsConnector.setPort(httpsPort);
//			server.addConnector(httpsConnector);
//		}
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.addServlet(ServletLdhExport.class, "/export/*");
		context.addServlet(ServletCshStats.class, "/stats");
		context.addServlet(ServletCshUser.class, "/user");
		context.addServlet(ServletCshPublish.class, "/publish/*");
		context.addServlet(ServletValidate.class, "/validate/*");
		context.addServlet(ServletCshDelete.class, "/delete/*");
		context.addServlet(ServletLdhImportCTG.class, "/ctg/*");
		context.addServlet(ServletIndex.class, "/");

		server.setHandler(context);
		server.start();
		//        server.join();
	}
}
