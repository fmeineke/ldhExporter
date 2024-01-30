package imise;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServletTests
{
	JsonAPI api;
	@Before
	public void startJetty() throws Exception
	{
		String[] args = {};
		LDHExport.main(args);
		api = new JsonAPI("http://localhost:8083");
	}

	@After
	public void stopJetty() throws Exception
	{
		LDHExport.stop();
	}

	@Test
	public void testIndex() throws Exception
	{
		String s = api.getResourceAsString("");	
		System.out.println(s);
		assertTrue(s.startsWith("<html>\r\n"
				+ "<body>\r\n"
				+ "    <h1>SeekExporter</h1>\r\n"));
	}
	@Test
	public void testExport() throws Exception
	{
		String s = api.getResourceAsString("export/projects/2?format=csh");	
		System.out.println(s);
		assertTrue(s.startsWith("{\"resource\":{\"classification\":{\"type\":\"Study\"},\"titles\":[{\"text\":\"A multic"));
	}
	@Test
	public void testValidation() throws Exception
	{
		String s = api.getResourceAsString("validate/projects/2");	
		System.out.println(s);
		assertTrue(s.startsWith("{\"validation_error\":[],\"resource\":{\"resource\":{\"classification\":{\"type\":\"S"));
	}
	@Test
	public void testStats() throws Exception
	{
		String s = api.getResourceAsString("stats");	
		System.out.println(s);
		assertTrue(s.startsWith("{\"lastUpdate\":"));
	}
}