package net.gjerull.etherpad.client;

import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import com.pholser.junit.quickcheck.Property;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

@RunWith(JUnitQuickcheck.class)
public class EPLiteConnectionPropertiesTest {
	private static final String API_VERSION = "1.2.12";
	private static final String ENCODING = "UTF-8";

	@Property
	public void domain_with_trailing_slash_when_construction_an_api_path(String exampleMethod) throws Exception {
		EPLiteConnection connection = new EPLiteConnection("http://example.com/", "apikey", API_VERSION, ENCODING);
		String apiMethodPath = connection.apiPath(exampleMethod);
		assertEquals("/api/1.2.12/" + exampleMethod, apiMethodPath);
	}
	
	@Property
	public void domain_without_trailing_slash_when_construction_an_api_path(String exampleMethod) throws Exception {
		EPLiteConnection connection = new EPLiteConnection("http://example.com", "apikey", API_VERSION, ENCODING);
		String apiMethodPath = connection.apiPath(exampleMethod);
		assertEquals("/api/1.2.12/" + exampleMethod, apiMethodPath);
	}
	
}