package net.gjerull.etherpad.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import etm.core.configuration.BasicEtmConfigurator;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.renderer.SimpleTextRenderer;

/**
 * Integration test for simple App.
 */
public class EPLiteClientIntegrationTest {

	private EPLiteClient client;
	private static ClientAndServer mockServer;
	private EtmMonitor monitor;

	/**
	 * Useless testing as it depends on a specific API key
	 *
	 * TODO: Find a way to make it configurable
	 */
	@Before
	public void setUp() throws Exception {
		this.client = new EPLiteClient("http://localhost:9001",
				"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58");

		((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger("org.mockserver.mock"))
		.setLevel(ch.qos.logback.classic.Level.OFF);

		BasicEtmConfigurator.configure();
		monitor = EtmManager.getEtmMonitor();
		monitor.start();
	}

	@After
	public void tearDown() {
		mockServer.reset();
		monitor.render(new SimpleTextRenderer());
		monitor.stop();
	}

	@BeforeClass
	public static void setUpClass() {
		mockServer = startClientAndServer(9001);
	}

	@AfterClass
	public static void cleanClass() {
		mockServer.stop();
	}

	@Test
	public void validate_token() throws Exception {
		mockServer
		.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/checkToken")
				.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\"}"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		client.checkToken();
	}

	@Test
	public void create_and_delete_group() throws Exception {
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroup"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.uGIQRLEntil3YMPj\"}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteGroup")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		Map response = client.createGroup();

		assertTrue(response.containsKey("groupID"));
		String groupId = (String) response.get("groupID");
		assertTrue("Unexpected groupID " + groupId, groupId != null && groupId.startsWith("g."));

		client.deleteGroup(groupId);
	}

	@Test
	public void create_group_if_not_exists_for_and_list_all_groups() throws Exception {
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupIfNotExistsFor"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.6h2V5bBb38lBDco7\"}}"));

		mockServer
		.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listAllGroups")
				.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\"}"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupIDs\":[\"g.6h2V5bBb38lBDco7\"]}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteGroup")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		String groupMapper = "groupname";

		Map response = client.createGroupIfNotExistsFor(groupMapper);

		assertTrue(response.containsKey("groupID"));
		String groupId = (String) response.get("groupID");
		try {
			Map listResponse = client.listAllGroups();
			assertTrue(listResponse.containsKey("groupIDs"));
			int firstNumGroups = ((List) listResponse.get("groupIDs")).size();

			client.createGroupIfNotExistsFor(groupMapper);

			listResponse = client.listAllGroups();
			int secondNumGroups = ((List) listResponse.get("groupIDs")).size();

			assertEquals(firstNumGroups, secondNumGroups);
		} finally {
			client.deleteGroup(groupId);
		}
	}

	@Test
	public void create_group_pads_and_list_them() throws Exception {
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroup"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.uGIQRLEntil3YMPj\"}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupPad"))
		.respond(HttpResponse.response().withStatusCode(200).withBody(
				"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"g.hfQ7DU0MkSYNuYy5$integration-test-1\"}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setPublicStatus")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getPublicStatus").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"g.hfQ7DU0MkSYNuYy5$integration-test-1\"}"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"publicStatus\":true}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setPassword")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/isPasswordProtected").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"g.hfQ7DU0MkSYNuYy5$integration-test-1\"}"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"isPasswordProtected\":true}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"g.hfQ7DU0MkSYNuYy5$integration-test-2\"}"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"Initial text\\n\"}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listPads").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"groupID\":\"g.hfQ7DU0MkSYNuYy5\"}"))
		.respond(HttpResponse.response().withStatusCode(200).withBody(
				"{\"code\":0,\"message\":\"ok\",\"data\":{\"padIDs\":[\"g.hfQ7DU0MkSYNuYy5$integration-test-1\",\"g.hfQ7DU0MkSYNuYy5$integration-test-2\"]}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteGroup")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		Map response = client.createGroup();
		String groupId = (String) response.get("groupID");
		String padName1 = "integration-test-1";
		String padName2 = "integration-test-2";
		try {
			Map padResponse = client.createGroupPad(groupId, padName1);
			assertTrue(padResponse.containsKey("padID"));
			String padId1 = (String) padResponse.get("padID");

			client.setPublicStatus(padId1, true);
			boolean publicStatus = (boolean) client.getPublicStatus(padId1).get("publicStatus");
			assertTrue(publicStatus);

			client.setPassword(padId1, "integration");
			boolean passwordProtected = (boolean) client.isPasswordProtected(padId1).get("isPasswordProtected");
			assertTrue(passwordProtected);

			padResponse = client.createGroupPad(groupId, padName2, "Initial text");
			assertTrue(padResponse.containsKey("padID"));

			String padId = (String) padResponse.get("padID");
			String initialText = (String) client.getText(padId).get("text");
			assertEquals("Initial text\n", initialText);

			Map padListResponse = client.listPads(groupId);

			assertTrue(padListResponse.containsKey("padIDs"));
			List padIds = (List) padListResponse.get("padIDs");

			assertEquals(2, padIds.size());
		} finally {
			client.deleteGroup(groupId);
		}
	}

	@Test
	public void create_author() throws Exception {

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthor"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.qGh5EutnGTyacAJV\"}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/createAuthor").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"name\":\"integration-author\"}"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.5GoZWP87e5g4uRdi\"}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"authorID\":\"a.5GoZWP87e5g4uRdi\"}"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author\"}"));

		Map authorResponse = client.createAuthor();
		String authorId = (String) authorResponse.get("authorID");
		assertTrue(authorId != null && !authorId.isEmpty());

		authorResponse = client.createAuthor("integration-author");
		authorId = (String) authorResponse.get("authorID");

		String authorName = client.getAuthorName(authorId);
		assertEquals("integration-author", authorName);
	}

	@Test
	public void create_author_with_author_mapper() throws Exception {

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.siYORA4fX3Ppd7uU\"}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"authorID\":\"a.siYORA4fX3Ppd7uU\"}"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author-1\"}"));

		String authorMapper = "username";

		Map authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-1");
		String firstAuthorId = (String) authorResponse.get("authorID");
		assertTrue(firstAuthorId != null && !firstAuthorId.isEmpty());

		String firstAuthorName = client.getAuthorName(firstAuthorId);

		mockServer.reset();

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"authorID\":\"a.siYORA4fX3Ppd7uU\"}"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author-2\"}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.siYORA4fX3Ppd7uU\"}}"));

		authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-2");
		String secondAuthorId = (String) authorResponse.get("authorID");
		assertEquals(firstAuthorId, secondAuthorId);

		String secondAuthorName = client.getAuthorName(secondAuthorId);

		assertNotEquals(firstAuthorName, secondAuthorName);

		authorResponse = client.createAuthorIfNotExistsFor(authorMapper);
		String thirdAuthorId = (String) authorResponse.get("authorID");
		assertEquals(secondAuthorId, thirdAuthorId);
		String thirdAuthorName = client.getAuthorName(thirdAuthorId);

		assertEquals(secondAuthorName, thirdAuthorName);
	}

	@Test
	public void create_and_delete_session() throws Exception {

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupIfNotExistsFor"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.Mhrfd2ojfVexSjq5\"}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.siYORA4fX3Ppd7uU\"}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createSession"))
		.respond(HttpResponse.response().withStatusCode(200).withBody(
				"{\"code\":0,\"message\":\"ok\",\"data\":{\"sessionID\":\"s.dbb345445216dbd7dd74848107919ace\"}}"));

		String authorMapper = "username";
		String groupMapper = "groupname";

		Map groupResponse = client.createGroupIfNotExistsFor(groupMapper);
		String groupId = (String) groupResponse.get("groupID");
		Map authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-1");
		String authorId = (String) authorResponse.get("authorID");

		int sessionDuration = 8;
		Map sessionResponse = client.createSession(groupId, authorId, sessionDuration);
		String firstSessionId = (String) sessionResponse.get("sessionID");

		mockServer.reset();

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createSession"))
		.respond(HttpResponse.response().withStatusCode(200).withBody(
				"{\"code\":0,\"message\":\"ok\",\"data\":{\"sessionID\":\"s.26246bb1255840be181974b5a91b89ca\"}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getSessionInfo").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"sessionID\":\"s.26246bb1255840be181974b5a91b89ca\"}"))
		.respond(HttpResponse.response().withStatusCode(200).withBody(
				"{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.Mhrfd2ojfVexSjq5\",\"authorID\":\"a.siYORA4fX3Ppd7uU\",\"validUntil\":1574041116}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listSessionsOfGroup").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"groupID\":\"g.Mhrfd2ojfVexSjq5\"}"))
		.respond(HttpResponse.response().withStatusCode(200).withBody(
				"{\"code\":0,\"message\":\"ok\",\"data\":{\"s.dbb345445216dbd7dd74848107919ace\":{\"groupID\":\"g.Mhrfd2ojfVexSjq5\",\"authorID\":\"a.siYORA4fX3Ppd7uU\",\"validUntil\":1542533916},\"s.26246bb1255840be181974b5a91b89ca\":{\"groupID\":\"g.Mhrfd2ojfVexSjq5\",\"authorID\":\"a.siYORA4fX3Ppd7uU\",\"validUntil\":1574041116}}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listSessionsOfAuthor").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"authorID\":\"a.siYORA4fX3Ppd7uU\"}"))
		.respond(HttpResponse.response().withStatusCode(200).withBody(
				"{\"code\":0,\"message\":\"ok\",\"data\":{\"s.dbb345445216dbd7dd74848107919ace\":{\"groupID\":\"g.Mhrfd2ojfVexSjq5\",\"authorID\":\"a.siYORA4fX3Ppd7uU\",\"validUntil\":1542533916},\"s.26246bb1255840be181974b5a91b89ca\":{\"groupID\":\"g.Mhrfd2ojfVexSjq5\",\"authorID\":\"a.siYORA4fX3Ppd7uU\",\"validUntil\":1574041116}}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteSession")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		Calendar oneYearFromNow = Calendar.getInstance();
		oneYearFromNow.add(Calendar.YEAR, 1);
		Date sessionValidUntil = oneYearFromNow.getTime();
		sessionResponse = client.createSession(groupId, authorId, sessionValidUntil);
		String secondSessionId = (String) sessionResponse.get("sessionID");
		try {
			assertNotEquals(firstSessionId, secondSessionId);

			Map sessionInfo = client.getSessionInfo(secondSessionId);
			assertEquals(groupId, sessionInfo.get("groupID"));
			assertEquals(authorId, sessionInfo.get("authorID"));
			// assertEquals(sessionValidUntil.getTime() / 1000L, (long)
			// sessionInfo.get("validUntil"));

			Map sessionsOfGroup = client.listSessionsOfGroup(groupId);
			sessionInfo = (Map) sessionsOfGroup.get(firstSessionId);
			assertEquals(groupId, sessionInfo.get("groupID"));
			sessionInfo = (Map) sessionsOfGroup.get(secondSessionId);
			assertEquals(groupId, sessionInfo.get("groupID"));

			Map sessionsOfAuthor = client.listSessionsOfAuthor(authorId);
			sessionInfo = (Map) sessionsOfAuthor.get(firstSessionId);
			assertEquals(authorId, sessionInfo.get("authorID"));
			sessionInfo = (Map) sessionsOfAuthor.get(secondSessionId);
			assertEquals(authorId, sessionInfo.get("authorID"));
		} finally {
			client.deleteSession(firstSessionId);
			client.deleteSession(secondSessionId);
		}

	}

	@Test
	public void create_pad_set_and_get_content() {

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setText")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"text\\n\"}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setHTML")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getHTML").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
		.respond(HttpResponse.response().withStatusCode(200).withBody(
				"{\"code\":0,\"message\":\"ok\",\"data\":{\"html\":\"<!DOCTYPE HTML><html><body>text<br><br></body></html>\"}}"));

		String padID = "integration-test-pad";
		client.createPad(padID);
		try {
			client.setText(padID, "text");
			String text = (String) client.getText(padID).get("text");
			assertEquals("text\n", text);

			client.setHTML(padID, "<!DOCTYPE HTML><html><body><p>text</p></body></html>");
			String html = (String) client.getHTML(padID).get("html");
			assertTrue(html, html.contains("text<br><br>"));

			mockServer.reset();

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getHTML").withBody(
					"{\"rev\":\"2\",\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
			.respond(HttpResponse.response().withStatusCode(200).withBody(
					"{\"code\":0,\"message\":\"ok\",\"data\":{\"html\":\"<!DOCTYPE HTML><html><body><br></body></html>\"}}"));

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText").withBody(
					"{\"rev\":\"2\",\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
			.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"\\n\"}}"));

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getRevisionsCount").withBody(
					"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
			.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"revisions\":3}}"));

			mockServer
			.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getRevisionChangeset").withBody(
					"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
			.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"Z:1>5|1+5$text\\n\"}"));

			mockServer
			.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getRevisionChangeset").withBody(
					"{\"rev\":\"2\",\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
			.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"Z:5<4|1-5|1+1$\\n\"}"));

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/createDiffHTML").withBody(
					"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\",\"startRev\":\"1\",\"endRev\":\"2\"}"))
			.respond(HttpResponse.response().withStatusCode(200).withBody(
					"{\"code\":0,\"message\":\"ok\",\"data\":{\"html\":\"<style>\\n.removed {text-decoration: line-through; -ms-filter:'progid:DXImageTransform.Microsoft.Alpha(Opacity=80)'; filter: alpha(opacity=80); opacity: 0.8; }\\n</style><span class=\\\"removed\\\">text</span><br><br>\",\"authors\":[\"\"]}}"));

			html = (String) client.getHTML(padID, 2).get("html");
			assertEquals("<!DOCTYPE HTML><html><body><br></body></html>", html);
			text = (String) client.getText(padID, 2).get("text");
			assertEquals("\n", text);

			long revisionCount = (long) client.getRevisionsCount(padID).get("revisions");
			assertEquals(3L, revisionCount);

			String revisionChangeset = client.getRevisionChangeset(padID);
			assertTrue(revisionChangeset, revisionChangeset.contains("text"));

			revisionChangeset = client.getRevisionChangeset(padID, 2);
			// assertTrue(revisionChangeset, revisionChangeset.contains("|1-j|1+1$\n"));

			String diffHTML = (String) client.createDiffHTML(padID, 1, 2).get("html");
			assertTrue(diffHTML, diffHTML.contains("<span class=\"removed\">text</span>"));

			mockServer.reset();

			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/appendText"))
			.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText").withBody(
					"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
			.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"text\\n\\n\"}}"));

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAttributePool").withBody(
					"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
			.respond(HttpResponse.response().withStatusCode(200).withBody(
					"{\"code\":0,\"message\":\"ok\",\"data\":{\"pool\":{\"numToAttrib\":{\"0\":[\"author\",\"\"],\"1\":[\"removed\",\"true\"]},\"attribToNum\":{\"author,\":0,\"removed,true\":1},\"nextNum\":2}}}"));

			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/saveRevision"))
			.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			mockServer.when(
					HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getSavedRevisionsCount").withBody(
							"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
			.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"savedRevisions\":2}}"));

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listSavedRevisions").withBody(
					"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
			.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"savedRevisions\":[2,4]}}"));

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/padUsersCount").withBody(
					"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
			.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"padUsersCount\":0}}"));

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/padUsers").withBody(
					"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
			.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"padUsers\":[]}}"));

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getReadOnlyID").withBody(
					"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
			.respond(HttpResponse.response().withStatusCode(200).withBody(
					"{\"code\":0,\"message\":\"ok\",\"data\":{\"readOnlyID\":\"r.542beee7ee842f7806877aea71654680\"}}"));

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getPadID").withBody(
					"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"roID\":\"r.542beee7ee842f7806877aea71654680\"}"))
			.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-test-pad\"}}"));

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listAuthorsOfPad").withBody(
					"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
			.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorIDs\":[]}}"));

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getLastEdited").withBody(
					"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
			.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"lastEdited\":1542546051540}}"));

			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/sendClientsMessage"))
			.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{}}"));

			client.appendText(padID, "");
			text = (String) client.getText(padID).get("text");
			assertEquals("text\n\n", text);

			Map attributePool = (Map) client.getAttributePool(padID).get("pool");
			assertTrue(attributePool.containsKey("attribToNum"));
			assertTrue(attributePool.containsKey("nextNum"));
			assertTrue(attributePool.containsKey("numToAttrib"));

			client.saveRevision(padID);
			client.saveRevision(padID, 2);

			long savedRevisionCount = (long) client.getSavedRevisionsCount(padID).get("savedRevisions");
			assertEquals(2L, savedRevisionCount);

			List savedRevisions = (List) client.listSavedRevisions(padID).get("savedRevisions");
			assertEquals(2, savedRevisions.size());
			assertEquals(2L, savedRevisions.get(0));
			assertEquals(4L, savedRevisions.get(1));

			long padUsersCount = (long) client.padUsersCount(padID).get("padUsersCount");
			assertEquals(0, padUsersCount);

			List padUsers = (List) client.padUsers(padID).get("padUsers");
			assertEquals(0, padUsers.size());

			String readOnlyId = (String) client.getReadOnlyID(padID).get("readOnlyID");
			String padIdFromROId = (String) client.getPadID(readOnlyId).get("padID");
			assertEquals(padID, padIdFromROId);

			List authorsOfPad = (List) client.listAuthorsOfPad(padID).get("authorIDs");
			assertEquals(0, authorsOfPad.size());

			long lastEditedTimeStamp = (long) client.getLastEdited(padID).get("lastEdited");
			Calendar lastEdited = Calendar.getInstance();
			lastEdited.setTimeInMillis(lastEditedTimeStamp);
			Calendar now = Calendar.getInstance();
			assertTrue(lastEdited.before(now));

			client.sendClientsMessage(padID, "test message");
		} finally {

			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad"))
			.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			client.deletePad(padID);
		}
	}

	@Test
	public void create_pad_move_and_copy() throws Exception {

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/copyPad"))
		.respond(HttpResponse.response().withStatusCode(200).withBody(
				"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-test-pad-copy\"}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad-copy\"}"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"should be kept\\n\"}}"));

		String padID = "integration-test-pad";
		String copyPadId = "integration-test-pad-copy";
		String movePadId = "integration-move-pad-move";
		String keep = "should be kept";
		String change = "should be changed";
		client.createPad(padID, keep);

		client.copyPad(padID, copyPadId);
		String copyPadText = (String) client.getText(copyPadId).get("text");

		mockServer.reset();

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/copyPad"))
		.respond(HttpResponse.response().withStatusCode(200).withBody(
				"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-move-pad-move\"}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-move-pad-move\"}"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"should be kept\\n\"}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setText")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		client.movePad(padID, movePadId);
		String movePadText = (String) client.getText(movePadId).get("text");

		client.setText(movePadId, change);

		mockServer.reset();

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/copyPad"))
		.respond(HttpResponse.response().withStatusCode(200).withBody(
				"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-test-pad-copy\"}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad-copy\"}"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"should be changed\\n\"}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/movePad")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		client.copyPad(movePadId, copyPadId, true);
		String copyPadTextForce = (String) client.getText(copyPadId).get("text");
		client.movePad(movePadId, copyPadId, true);
		String movePadTextForce = (String) client.getText(copyPadId).get("text");

		client.deletePad(copyPadId);
		client.deletePad(padID);

		assertEquals(keep + "\n", copyPadText);
		assertEquals(keep + "\n", movePadText);

		assertEquals(change + "\n", copyPadTextForce);
		assertEquals(change + "\n", movePadTextForce);
	}

	@Test
	public void create_pads_and_list_them() throws InterruptedException {

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listAllPads").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\"}"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"padIDs\":[\"T1fqSCq0Pd\",\"g.6cVFEsO1daQUqjZ1$integration-test-1\",\"g.6cVFEsO1daQUqjZ1$integration-test-2\",\"g.BzVCMhcyV0Nnqpkl$integration-test-1\",\"g.BzVCMhcyV0Nnqpkl$integration-test-2\",\"g.DQVyBmDeQvhLEuki$integration-test-1\",\"g.DQVyBmDeQvhLEuki$integration-test-2\",\"g.ZBtNJ1u21OBCE1WJ$integration-test-1\",\"g.ZBtNJ1u21OBCE1WJ$integration-test-2\",\"g.hfQ7DU0MkSYNuYy5$integration-test-1\",\"g.hfQ7DU0MkSYNuYy5$integration-test-2\",\"g.i0YUlhSQE3cZeQXe$integration-test-1\",\"g.i0YUlhSQE3cZeQXe$integration-test-2\",\"g.iErJImsEwfwFV5D8$integration-test-1\",\"g.iErJImsEwfwFV5D8$integration-test-2\",\"g.kYOg9iwlsTSCC32U$integration-test-1\",\"g.kYOg9iwlsTSCC32U$integration-test-2\",\"g.uSkUS1SyDGkQsVku$integration-test-1\",\"g.uSkUS1SyDGkQsVku$integration-test-2\",\"ggQJKZF0Ue\",\"integration-move-pad-move\",\"integration-test-pad\",\"integration-test-pad-1\",\"integration-test-pad-2\",\"integration-test-pad-copy\",\"xGAcig4Fv2\"]}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		String pad1 = "integration-test-pad-1";
		String pad2 = "integration-test-pad-2";
		client.createPad(pad1);
		client.createPad(pad2);
		Thread.sleep(100);
		List padIDs = (List) client.listAllPads().get("padIDs");
		client.deletePad(pad1);
		client.deletePad(pad2);

		assertTrue(String.format("Size was %d", padIDs.size()), padIDs.size() >= 2);
		assertTrue(padIDs.contains(pad1));
		assertTrue(padIDs.contains(pad2));
	}

	@Test
	public void create_pad_and_chat_about_it() {

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.qoQdFoqnyBqSQyjk\"}}"));

		String padID = "integration-test-pad-1";
		String user1 = "user1";
		String user2 = "user2";
		Map response = client.createAuthorIfNotExistsFor(user1, "integration-author-1");
		String author1Id = (String) response.get("authorID");

		mockServer.reset();

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.MKXerRkHJa6UAEOf\"}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/appendChatMessage")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHead").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad-1\"}"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"chatHead\":2}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHistory").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad-1\"}"))
		.respond(HttpResponse.response().withStatusCode(200)
				.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"messages\":[{\"text\":\"hi from user1\",\"userId\":\"a.qoQdFoqnyBqSQyjk\",\"time\":1542557899346,\"userName\":\"integration-author-1\"},{\"text\":\"hi from user2\",\"userId\":\"a.MKXerRkHJa6UAEOf\",\"time\":1542557899,\"userName\":\"integration-author-2\"},{\"text\":\"text\",\"userId\":\"a.qoQdFoqnyBqSQyjk\",\"time\":1542557899,\"userName\":\"integration-author-1\"}]}}"));

		response = client.createAuthorIfNotExistsFor(user2, "integration-author-2");
		String author2Id = (String) response.get("authorID");

		client.createPad(padID);
		try {
			client.appendChatMessage(padID, "hi from user1", author1Id);
			client.appendChatMessage(padID, "hi from user2", author2Id, System.currentTimeMillis() / 1000L);
			client.appendChatMessage(padID, "text", author1Id, System.currentTimeMillis() / 1000L);
			response = client.getChatHead(padID);
			long chatHead = (long) response.get("chatHead");
			assertEquals(2, chatHead);

			response = client.getChatHistory(padID);
			List chatHistory = (List) response.get("messages");
			assertEquals(3, chatHistory.size());
			assertEquals("text", ((Map) chatHistory.get(2)).get("text"));

			mockServer.reset();

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHistory").withBody(
					"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"start\":\"0\",\"padID\":\"integration-test-pad-1\",\"end\":\"1\"}"))
			.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"messages\":[{\"text\":\"hi from user1\",\"userId\":\"a.qoQdFoqnyBqSQyjk\",\"time\":1542557899346,\"userName\":\"integration-author-1\"},{\"text\":\"hi from user2\",\"userId\":\"a.MKXerRkHJa6UAEOf\",\"time\":1542557899,\"userName\":\"integration-author-2\"}]}}"));

			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad")).respond(
					HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			response = client.getChatHistory(padID, 0, 1);
			chatHistory = (List) response.get("messages");
			assertEquals(2, chatHistory.size());
			assertEquals("hi from user2", ((Map) chatHistory.get(1)).get("text"));
		} finally {
			client.deletePad(padID);
		}

	}
}
