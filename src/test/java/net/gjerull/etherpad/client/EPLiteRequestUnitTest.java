package net.gjerull.etherpad.client;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

public class EPLiteRequestUnitTest {
	
	@Test
	public void PostSendRequest() throws Exception{
		
		final URLConnection mockUrlCon = Mockito.mock(URLConnection.class);

		ByteArrayInputStream is = new ByteArrayInputStream(
		        "<myList></myList>".getBytes("UTF-8"));
		ByteArrayOutputStream out = new ByteArrayOutputStream(20);
		
		Mockito.doReturn(is).when(mockUrlCon).getInputStream();
		Mockito.doReturn(out).when(mockUrlCon).getOutputStream();

		//make getLastModified() return first 10, then 11
		Mockito.when(mockUrlCon.getLastModified()).thenReturn((Long)10L, (Long)11L);

		URLStreamHandler stubUrlHandler = new URLStreamHandler() {
		    @Override
		     protected URLConnection openConnection(URL u) throws IOException {
		        return mockUrlCon;
		     }
        
		};
		URL url = new URL("foo", "bar", 99, "/foobar", stubUrlHandler);
		
		POSTRequest req = new POSTRequest(url, "gggg");
		
		String response = req.send();
		
		assertEquals(response, "<myList></myList>");
		
	}
	
	@Test
	public void GetSendRequest() throws Exception{
		
		final URLConnection mockUrlCon = Mockito.mock(URLConnection.class);

		ByteArrayInputStream is = new ByteArrayInputStream(
		        "<myList></myList>".getBytes("UTF-8"));
		ByteArrayOutputStream out = new ByteArrayOutputStream(20);
		
		Mockito.doReturn(is).when(mockUrlCon).getInputStream();
		Mockito.doReturn(out).when(mockUrlCon).getOutputStream();

		//make getLastModified() return first 10, then 11
		Mockito.when(mockUrlCon.getLastModified()).thenReturn((Long)10L, (Long)11L);

		URLStreamHandler stubUrlHandler = new URLStreamHandler() {
		    @Override
		     protected URLConnection openConnection(URL u) throws IOException {
		        return mockUrlCon;
		     }
        
		};
		URL url = new URL("foo", "bar", 99, "/foobar", stubUrlHandler);
		
		GETRequest req = new GETRequest(url);
		
		String response = req.send();
		
		assertEquals(response, "<myList></myList>");
		
	}
	
	

}
