package net.gjerull.etherpad.client;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Connection object for talking to and parsing
 *  responses from the Etherpad Lite Server.
 */
public class EPLiteConnection {
	/** Code OK. */
	public static final int CODE_OK = 0;
	/** Code Invalid Parameters. */
	public static final int CODE_INVALID_PARAMETERS = 1;
	/** Code Internal Error. */
	public static final int CODE_INTERNAL_ERROR = 2;
	/** Code Invalid Method. */
	public static final int CODE_INVALID_METHOD = 3;
	/** Code Invalid Api Key. */
	public static final int CODE_INVALID_API_KEY = 4;

	/**
	 * The url of the API.
	 */
	final URI uri;

	/**
	 * The API key.
	 */
	private final String apiKey;

	/**
	 * The Etherpad Lite API version.
	 */
	private final String apiVersion;

	/**
	 * The character encoding of your application.
	 */
	private final String encoding;

	/** Getter.
	 * @return Uri
	 * */
	public final URI getUri() {
		return uri;
	}

	/** Getter.
	 * @return String
	 * */
	public final String getApiKey() {
		return apiKey;
	}

	/** Getter.
	 * @return String
	 * */
	public final String getApiVersion() {
		return apiVersion;
	}

	/** Getter.
	 * @return String
	 * */
	public final String getEncoding() {
		return encoding;
	}

	/**
	 * Initializes a new
	 * net.gjerull.etherpad.client.EPLiteConnection object.
	 *
	 * @param url an absolute url, including protocol, to the EPL api
	 * @param apiKey the API Key
	 * @param apiVersion the API version
	 * @param encoding encode type
	 */
	public EPLiteConnection(String url, final String apiKey,
			final String apiVersion, final String encoding) {
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 1);
		}
		this.uri = URI.create(url);
		this.apiKey = apiKey;
		this.apiVersion = apiVersion;
		this.encoding = encoding;
	}

	/**
	 * GETs from the HTTP JSON API.
	 *
	 * @param apiMethod the name of the API method to call
	 * @return Object
	 */
	public final Object getObject(final String apiMethod) {
		return this.getObject(apiMethod,
				new HashMap<String, Object>());
	}

	/**
	 * GETs from the HTTP JSON API.
	 *
	 * @param apiMethod the name of the API method to call
	 * @return Map
	 */
	public final Map get(final String apiMethod) {
		Map response = (Map) this.getObject(apiMethod);
		return (response != null) ? response : new HashMap();
	}

	/**
	 * GETs from the HTTP JSON API.
	 *
	 * @param apiMethod the name of the API method to call
	 * @param apiArgs a HashMap of url/form parameters.
	 *  apikey will be set automatically
	 * @return Object
	 */
	public final Object getObject(final String apiMethod,
			Map<String, Object> apiArgs) {
		String path = this.apiPath(apiMethod);
		String query = this.queryString(apiArgs, false);
		URL url = apiUrl(path, query);
		Request request = new GETRequest(url);
		return this.call(request);
	}

	/**
	 * GETs from the HTTP JSON API.
	 *
	 * @param apiMethod the name of the API method to call
	 * @param apiArgs a HashMap of url/form parameters.
	 *  apikey will be set automatically
	 * @return Map
	 */
	public final Map get(final String apiMethod,
			final Map<String, Object> apiArgs) {
		Map response = (Map) this.getObject(apiMethod, apiArgs);
		return (response != null) ? response : new HashMap();
	}

	/**
	 * POSTs to the HTTP JSON API.
	 *
	 * @param apiMethod the name of the API method to call
	 * @return Object
	 */
	public final Object postObject(final String apiMethod) {
		return this.postObject(apiMethod,
				new HashMap<String, Object>());
	}

	/**
	 * POSTs to the HTTP JSON API.
	 *
	 * @param apiMethod the name of the API method to call
	 * @return Map
	 */
	public final Map post(final String apiMethod) {
		Map response = (Map) this.postObject(apiMethod);
		return (response != null) ? response : new HashMap();
	}

	/**
	 * POSTs to the HTTP JSON API.
	 *
	 * @param apiMethod the name of the API method to call
	 * @param apiArgs a HashMap of url/form parameters.
	 *  apikey will be set automatically
	 * @return Object
	 */
	public final Object postObject(final String apiMethod,
			Map<String, Object> apiArgs) {
		String path = this.apiPath(apiMethod);
		String query = this.queryString(apiArgs, true);
		URL url = apiUrl(path, null);
		Request request = new POSTRequest(url, query);
		return this.call(request);
	}

	/**
	 * POSTs to the HTTP JSON API.
	 *
	 * @param apiMethod the name of the API method to call
	 * @param apiArgs a HashMap of url/form parameters.
	 *  apikey will be set automatically
	 * @return Map
	 */
	public final Map post(final String apiMethod,
			final Map<String, Object> apiArgs) {
		Map response = (Map) this.postObject(apiMethod, apiArgs);
		return (response != null) ? response : new HashMap();
	}

	/**
	 * Handle error condition and returns the parsed content
	 *
	 * @param jsonString a valid JSON string
	 * @return Object
	 */
	protected final Object handleResponse(final String jsonString) {
		try {
			JSONParser parser = new JSONParser();
			Map response = (Map) parser.parse(jsonString);
			// Act on the response code
			if (response.get("code") != null)  {
				int code = ((Long) response.get("code")).intValue();
				switch ( code ) {
				// Valid code, parse the response
				case CODE_OK:
					return response.get("data");
					// Invalid code, throw an exception with the message
				case CODE_INVALID_PARAMETERS:
				case CODE_INTERNAL_ERROR:
				case CODE_INVALID_METHOD:
				case CODE_INVALID_API_KEY:
					throw new EPLiteException(
							(String)response.get("message"));
				default:
					throw new EPLiteException(
							"An unknown error has"
									+ " occurred while"
									+ " handling the response: "
									+ jsonString);
				}
				// No response code, something's really wrong
			} else {
				throw new EPLiteException("An unexpected"
						+ " response from the server: " + jsonString);
			}
		} catch (ParseException e) {
			throw new EPLiteException("Unable to parse"
					+ " JSON response (" + jsonString + ")", e);
		}
	}

	/**
	 * Returns the URL for the api path and query.
	 *
	 * @param path the api path
	 * @param query the query string (may be null)
	 * @return URL
	 */
	protected final URL apiUrl(final String path, final String query) {
		try {
			return new URL(new URI(this.uri.getScheme(), null,
					this.uri.getHost(), this.uri.getPort(),
					path, query, null).toString());
		} catch (MalformedURLException | URISyntaxException e) {
			throw new EPLiteException("Error in the URL to"
					+ " the Etherpad Lite instance (" + e.getClass()
					+ "): " + e.getMessage());
		}
	}

	/**
	 * Returns a URI path for the API method
	 *
	 * @param apiMethod the api method
	 * @return String
	 */
	protected final String apiPath(final String apiMethod) {
		return this.uri.getPath() + "/api/"
				+ this.apiVersion + "/" + apiMethod;
	}

	/**
	 * Returns a query string made from HashMap keys and values
	 *
	 * @param apiArgs the api arguments in a HashMap
	 * @return String
	 */
	protected final String queryString(
			final Map<String, Object> apiArgs,
			boolean urlEncode) {
		StringBuilder strArgs = new StringBuilder();
		apiArgs.put("apikey", this.apiKey);
		Iterator i = apiArgs.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry entry = (Map.Entry)i.next();
			Object key = entry.getKey();
			Object value = entry.getValue();
			if (urlEncode) {
				try {
					if (key instanceof String) {
						URLEncoder.encode((String) key, this.encoding);
					}
					if (value instanceof String) {
						value = URLEncoder.encode(
								(String) value, this.encoding);
					}
				} catch (UnsupportedEncodingException e) {
					throw new EPLiteException(String.format(
							"Unable to URLEncode using"
									+ " encoding '%s'",
									this.encoding), e);
				}
			}
			strArgs.append(key).append("=").append(value);
			if (i.hasNext()) {
				strArgs.append("&");
			}
		}
		return strArgs.toString();
	}

	/**
	 * Calls the HTTP JSON API.
	 *
	 * @param request the request object to send
	 * @return HashMap
	 */
	private final Object call(final Request request) {
		trustServerAndCertificate();

		try {
			String response = request.send();
			return this.handleResponse(response);
		}
		catch (EPLiteException e) {
			throw e;
		}
		catch (Exception e) {
			throw new EPLiteException("Unable to connect"
					+ " to Etherpad Lite instance (" + e.getClass()
					+ "): " + e.getMessage());
		}
	}

	/**
	 * Creates a trust manager to trust all
	 *  certificates if you open a ssl connection
	 */
	private final void trustServerAndCertificate() {
		// Create a trust manager that does not validate
		//certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					@Override
					public java.security.cert.X509Certificate[]
							getAcceptedIssuers() {
						return null;
					}

					@Override
					public void checkClientTrusted(
							java.security.cert.X509Certificate[] certs,
							String authType) {
					}

					@Override
					public void checkServerTrusted(
							java.security.cert.X509Certificate[] certs,
							String authType) {
					}
				}
		};

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts,
					new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(
					sc.getSocketFactory());
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			throw new EPLiteException("Unable to create SSL context", e);
		}

		HostnameVerifier hv = new HostnameVerifier() {
			//@Override
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}
}
