package net.gjerull.etherpad.client;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;

/**
 * A client for talking to Etherpad Lite's HTTP JSON API.<br />
 * <br />
 * Example:<br />
 * <br />
 * <code>
 * EPLiteClient api = new EPLiteClient("http://etherpad.mysite.com",
 * "FJ7jksalksdfj83jsdflkj");<br />
 * Map pad = api.getText("my_pad");<br />
 * String pad = pad.get("text").toString();
 * </code>
 */

/**

 * Esta clase define el cliente de EPLite.

 * @author: Alejandro González

 *
 */
public class EPLiteClient {
	/** Default API. */
	private static final String DEFAULT_API_VERSION = "1.2.13";
	/** Default encoding. */
	private static final String DEFAULT_ENCODING = "UTF-8";
	/** EPLiteConnection. */
	private final EPLiteConnection connection;
	/** monitor JETM. */
	private static final EtmMonitor etmMonitor = EtmManager.getEtmMonitor();

	/**
	 * Initializes a new net.gjerull.etherpad.client.EPLiteClient object.
	 * The default Etherpad Lite API version
	 * (in DEFAULT_API_VERSION) will be used.
	 *
	 * @param url an absolute url, including protocol, to the EPL api
	 * @param apiKey the API Key
	 */
	public EPLiteClient(final String url, final String apiKey) {
		this.connection = new EPLiteConnection(url, apiKey,
				DEFAULT_API_VERSION, DEFAULT_ENCODING);
	}

	/**
	 * Initializes a new net.gjerull.etherpad.client.EPLiteClient object.
	 * The specified Etherpad Lite API version will be used.
	 *
	 * @param url an absolute url, including protocol, to the EPL api
	 * @param apiKey the API Key
	 * @param apiVersion the API version
	 * @param encoding the encode type
	 */
	public EPLiteClient(final String url, final String apiKey,
			final String apiVersion, final String encoding) {
		this.connection = new EPLiteConnection(url, apiKey,
				apiVersion, encoding);
	}

	// Groups
	// Pads may belong to a group. These pads are not considered "public",
	//and won't be available through the Web UI without a session.

	/**
	 * Creates a new Group. The group id is returned
	 * in "groupID" in the Map.
	 *
	 * @return Map with groupID
	 */
	public final Map createGroup() {
		return this.connection.post("createGroup");
	}

	/**
	 * Creates a new Group for groupMapper if one doesn't already exist.
	 * Helps you map your application's groups to Etherpad Lite's groups.
	 * The group id is returned in "groupID" in the Map.
	 *
	 * @param groupMapper your group mapper string
	 * @return Map with groupID
	 */
	public final Map createGroupIfNotExistsFor(final String groupMapper) {
		Map<String, Object> args = new HashMap<>();
		args.put("groupMapper", groupMapper);
		return this.connection.post("createGroupIfNotExistsFor", args);
	}

	/**
	 * Delete group.
	 *
	 * @param groupID string
	 */
	public final void deleteGroup(final String groupID) {
		Map<String, Object> args = new HashMap<>();
		args.put("groupID", groupID);
		this.connection.post("deleteGroup", args);
	}

	/**
	 * List all the padIDs in a group. They will be in an array
	 *  inside "padIDs".
	 *
	 * @param groupID string
	 * @return Map
	 */
	public final Map listPads(final String groupID) {
		Map<String, Object> args = new HashMap<>();
		args.put("groupID", groupID);
		return this.connection.get("listPads", args);
	}

	/**
	 * Create a pad in this group.
	 *
	 * @param groupID the group the pad belongs to
	 * @param padName name of the pad
	 * @return Map
	 */
	public final Map createGroupPad(final String groupID,
			final String padName) {
		Map<String, Object> args = new HashMap<>();
		args.put("groupID", groupID);
		args.put("padName", padName);
		return this.connection.post("createGroupPad", args);
	}

	/**
	 * Create a pad in this group, with initial text.
	 *
	 * @param groupID the group the pad belongs to
	 * @param padName name of the pad
	 * @param text Initial text in the pad
	 * @return Map
	 */
	public final Map createGroupPad(final String groupID,
			final String padName,
			final String text) {
		Map<String, Object> args = new HashMap<>();
		args.put("groupID", groupID);
		args.put("padName", padName);
		args.put("text", text);
		return this.connection.post("createGroupPad", args);
	}

	/**
	 * Lists all existing groups. The group ids are returned in "groupIDs".
	 *
	 * @return Map with list of groupIDs
	 */
	public final Map listAllGroups() {
		return this.connection.get("listAllGroups");
	}

	// Authors
	// These authors are bound to the attributes the users choose
	//(color and name).
	// The author id is returned in "authorID".

	/**
	 * Create a new author.
	 *
	 * @return Map with authorID
	 */
	public final Map createAuthor() {
		return this.connection.get("createAuthor");
	}

	/**
	 * Create a new author with the given name. The author id is returned in
	 *  "authorID".
	 *
	 * @param name string
	 * @return Map with authorID
	 */
	public final Map createAuthor(final String name) {
		Map<String, Object> args = new HashMap<>();
		args.put("name", name);
		return this.connection.post("createAuthor", args);
	}

	/**
	 * Creates a new Author for authorMapper if one doesn't already exist.
	 * Helps you map your application's authors to Etherpad Lite's authors.
	 * The author id is returned in "authorID".
	 *
	 * @param authorMapper string
	 * @return Map with authorID
	 */
	public final Map createAuthorIfNotExistsFor(final String authorMapper) {
		Map<String, Object> args = new HashMap<>();
		args.put("authorMapper", authorMapper);
		return this.connection.post("createAuthorIfNotExistsFor", args);
	}

	/**
	 * Creates a new Author for authorMapper if one doesn't already exist.
	 * Helps you map your application's authors to Etherpad Lite's authors.
	 * The author id is returned in "authorID".
	 *
	 * @param authorMapper string
	 * @param name string
	 * @return Map with authorID
	 */
	public final Map createAuthorIfNotExistsFor(final String authorMapper,
			final String name) {
		Map<String, Object> args = new HashMap<>();
		args.put("authorMapper", authorMapper);
		args.put("name", name);
		return this.connection.post("createAuthorIfNotExistsFor", args);
	}

	/**
	 * List the ids of pads the author has edited. They will be in an array
	 *  inside "padIDs".
	 *
	 * @param authorId the authors's id string
	 * @return Map
	 */
	public final Map listPadsOfAuthor(final String authorId) {
		Map<String, Object> args = new HashMap<>();
		args.put("authorID", authorId);
		return this.connection.get("listPadsOfAuthor", args);
	}

	/**
	 * Returns the Author Name of the author.
	 *
	 * @param authorId the author's id string
	 * @return authorName
	 */
	public final String getAuthorName(final String authorId) {
		Map<String, Object> args = new HashMap<>();
		args.put("authorID", authorId);
		return (String) this.connection.getObject(
				"getAuthorName", args);
	}

	// Sessions
	// Sessions can be created between a group and an author. This allows an
	//author to access more than one group. The sessionID will be set as a
	// cookie to the client and is valid until a certain date. Only users
	//with a valid session for this group, can access group pads.
	//You can create a
	// session after you authenticated the user at
	//your web application, to giv
	//them access to the pads. You should save the sessionID of this session
	// and delete it after the user logged out.

	/**
	 * Create a new session for the given author in the given group,
	 * valid until the given UNIX time.
	 * <br />
	 * Example:<br />
	 * <br />
	 * <code>
	 * import java.util.Date;<br />
	 * ...<br />
	 * Date now = new Date();<br />
	 * long in1Hour = (now.getTime() + (60L * 60L * 1000L) / 1000L);<br />
	 * String sessID1 = api.createSession(groupID, authorID, in1Hour);
	 * </code>
	 *
	 * @param groupID string
	 * @param authorID string
	 * @param validUntil long UNIX timestamp <strong>in seconds</strong>
	 * @return Map with sessionID
	 */
	public final Map createSession(final String groupID,
			final String authorID, final long validUntil) {
		Map<String, Object> args = new HashMap<>();
		args.put("groupID", groupID);
		args.put("authorID", authorID);
		args.put("validUntil", String.valueOf(validUntil));
		return this.connection.post("createSession", args);
	}

	/**
	 * Create a new session for the given author in the given group
	 *  valid for the given number of hours.
	 * <br />
	 * Example:<br />
	 * <br />
	 * <code>
	 * // in 2 hours<br />
	 * String sessID1 = api.createSession(groupID, authorID, 2);
	 * </code>
	 *
	 * @param groupID string
	 * @param authorID string
	 * @param sessionDuration int duration of session in hours
	 * @return Map with sessionID
	 */
	public final Map createSession(final String groupID,
			final String authorID,
			final int sessionDuration) {
		long inNHours = ((new Date()).getTime()
				+ (sessionDuration * 60L * 60L * 1000L))
				/ 1000L;
		return this.createSession(groupID, authorID, inNHours);
	}

	/**
	 * Create a new session for the given author in the given group,
	 * valid until the given datetime.
	 * <br />
	 * Example:<br />
	 * <br />
	 * <code>
	 * import java.util.Date;<br />
	 * import java.text.DateFormat;<br />
	 * import java.text.SimpleDateFormat;<br />
	 * import java.util.TimeZone;<br />
	 * ...<br />
	 * DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");<br />
	 * dfm.setTimeZone(TimeZone.getTimeZone("GMT-5"));<br />
	 * Date longTime = dfm.parse("2056-01-15 20:15:00");<br />
	 * String sessID = api.createSession(groupID, authorID, longTime);
	 * </code>
	 *
	 * @param groupID string
	 * @param authorID string
	 * @param validUntil Date
	 * @return Map with sessionID
	 */
	public final Map createSession(final String groupID,
			final String authorID, final Date validUntil) {
		long seconds = validUntil.getTime() / 1000L;
		return this.createSession(groupID, authorID, seconds);
	}

	/**
	 * Delete a session.
	 *
	 * @param sessionID string
	 */
	public final void deleteSession(final String sessionID) {
		Map<String, Object> args = new HashMap<>();
		args.put("sessionID", sessionID);
		this.connection.post("deleteSession", args);
	}

	/**
	 * Returns information about a session: authorID, groupID
	 *  and validUntil.
	 *
	 * @param sessionID string
	 * @return Map
	 */
	public final Map getSessionInfo(final String sessionID) {
		Map<String, Object> args = new HashMap<>();
		args.put("sessionID", sessionID);
		return this.connection.get("getSessionInfo", args);
	}

	/**
	 * List all the sessions IDs in a group. Returned as a
	 *  Map of sessionIDs
	 * keys,
	 *  with values of Maps containing
	 * groupID, authorID, and validUntil.
	 *
	 * @param groupID string
	 * @return Map
	 */
	public final Map listSessionsOfGroup(final String groupID) {
		Map<String, Object> args = new HashMap<>();
		args.put("groupID", groupID);
		return this.connection.get("listSessionsOfGroup", args);
	}

	/**
	 * List all the sessions IDs belonging to an author.
	 *  Returned as a Map of
	 *  sessionIDs keys, with values of Maps containing
	 * groupID, authorID, and validUntil.
	 *
	 * @param authorID string
	 * @return Map
	 */
	public final Map listSessionsOfAuthor(final String authorID) {
		Map<String, Object> args = new HashMap<>();
		args.put("authorID", authorID);
		return this.connection.get("listSessionsOfAuthor", args);
	}

	// Pad content

	/**
	 * Returns a Map containing the latest revision of the pad's text.
	 * The text is stored under "text".
	 *
	 * @param padId the pad's id string
	 * @return a Map with the text content of pad
	 */
	public final Map getText(final String padId) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		return this.connection.get("getText", args);
	}

	/**
	 * Returns a Map containing the a specific revision of the pad's text.
	 * The text is stored under "text".
	 *
	 * @param padId the pad's id string
	 * @param rev the revision number
	 * @return a Map with the text content of pad in given revision
	 */
	public final Map getText(final String padId, final long rev) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		args.put("rev", rev);
		return this.connection.get("getText", args);
	}

	/**
	 * Creates a new revision with the given text.
	 *
	 * @param padId the pad's id string
	 * @param text the pad's new text
	 */
	public final void setText(final String padId, final String text) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		args.put("text", text);
		this.connection.post("setText", args);
	}

	/**
	 * Creates a new revision with the given text appended to the existing
	 *  text.
	 *
	 * API >= 1.2.13
	 *
	 * @param padId the pad's id string
	 * @param text the pad's new text
	 */
	public final void appendText(final String padId, final String text) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		args.put("text", text);
		this.connection.post("appendText", args);
	}

	/**
	 * Returns a Map containing the current revision of
	 *  the pad's text as HTML.
	 * The html is stored under "html".
	 *
	 * @param padId the pad's id string
	 * @return a Map with the HTML content of pad
	 */
	public final Map getHTML(final String padId) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		return this.connection.get("getHTML", args);
	}

	/**
	 * Returns a Map containing the a specific revision of the pad's
	 * text as HTML.
	 * The html is stored under "html".
	 *
	 * @param padId the pad's id string
	 * @param rev the revision number
	 * @return a Map with the HTML content of pad in given revision
	 */
	public final Map getHTML(final String padId, final long rev) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		args.put("rev", rev);
		return this.connection.get("getHTML", args);
	}

	/**
	 * Creates a new revision with the given html.
	 *
	 * @param padId the pad's id string
	 * @param html the pad's new html text
	 */
	public final void setHTML(final String padId, final String html) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		args.put("html", html);
		this.connection.post("setHTML", args);
	}

	/**
	 * Returns the attribute pool of a pad.
	 *
	 * API >= 1.2.8
	 *
	 * @param padId the pad's id string
	 * @return a Map with the attribute pool of a pad
	 */
	public final Map getAttributePool(final String padId) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		return this.connection.get("getAttributePool", args);
	}

	/**
	 * Get the changeset at the last revision.
	 *
	 * API >= 1.2.8
	 *
	 * @param padId the pad's id string
	 * @return the changeset at the last revision.
	 */
	public final String getRevisionChangeset(final String padId) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		return (String)
				this.connection.getObject(
						"getRevisionChangeset", args);
	}

	/**
	 * Get the changeset at a given revision.
	 *
	 * API >= 1.2.8
	 *
	 * @param padId the pad's id string
	 * @param rev the revision number
	 * @return the changeset at a given revision.
	 */
	public final String getRevisionChangeset(final String padId,
			final long rev) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		args.put("rev", rev);
		return (String) this.connection.getObject(
				"getRevisionChangeset", args);
	}

	/**
	 * Returns an object of diffs from 2 points in a pad.
	 *
	 * API >= 1.2.7
	 *
	 * @param padId the pad's id string
	 * @param startRev the start revision number
	 * @param endRev the end revision number
	 * @return a Map of diffs from 2 points in a pad
	 */
	public final Map createDiffHTML(final String padId, final long startRev,
			final long endRev) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		args.put("startRev", startRev);
		args.put("endRev", endRev);
		return this.connection.get("createDiffHTML", args);
	}

	// Chat

	/**
	 * Returns the complete chat history of pad.
	 *
	 * API >= 1.2.7
	 *
	 * @param padId the pad's id string
	 * @return the whole chat histroy
	 */
	public final Map getChatHistory(final String padId) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		return this.connection.get("getChatHistory", args);
	}

	/**
	 * Returns the chat history of pad with index between start and end.
	 *
	 * API >= 1.2.7
	 *
	 * @param padId the pad's id string
	 * @param start the start index
	 * @param end the end index
	 * @return a part of the chat history, between start and end
	 */
	public final Map getChatHistory(final String padId, final long start,
			final long end) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		args.put("start", start);
		args.put("end", end);
		return this.connection.get(
				"getChatHistory", args);
	}

	/**
	 * Returns the chatHead (last number of the last chat-message)
	 * of the pad.
	 *
	 * API >= 1.2.7
	 *
	 * @param padId the pad's id string
	 * @return the last number of the last chat-message
	 */
	public final Map getChatHead(final String padId) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		return this.connection.get("getChatHead", args);
	}

	/**
	 * Creates a chat message, saves it to the database and sends it to all
	 * connected clients of this pad, using the current time as timestamp.
	 *
	 * API >= 1.2.12
	 *
	 * @param padId the pad's id string
	 * @param text  the text of this chat entry
	 * @param authorId the author of this chat entry
	 */
	public final Map appendChatMessage(final String padId,
			final String text,
			final String authorId) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		args.put("text", text);
		args.put("authorID", authorId);
		return this.connection.post("appendChatMessage", args);
	}

	/**
	 * Creates a chat message, saves it to the database and sends it to all
	 * connected clients of this pad.
	 *
	 * API >= 1.2.12
	 *
	 * @param padId the pad's id string
	 * @param text  the text of this chat entry
	 * @param authorId the author of this chat entry
	 * @param time the timestamp of this chat entry
	 * @return Map
	 */
	public final Map appendChatMessage(final String padId,
			final String text,
			final String authorId,
			final long time) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		args.put("text", text);
		args.put("authorID", authorId);
		args.put("time", time);
		return this.connection.post("appendChatMessage", args);
	}

	// Pads
	// Group pads are normal pads, but with the name schema GROUPID$PADNAME.
	//A security manager controls access of them and its
	// forbidden for normal pads to include a $ in the name.

	/**
	 * Returns a list of all pads.
	 *
	 * @return a Map with list of pad id's
	 */
	public final Map listAllPads() {
		return this.connection.get("listAllPads");
	}

	/**
	 * Create a new pad.
	 *
	 * @param padId the pad's id string
	 */
	public final void createPad(final String padId) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		this.connection.post("createPad", args);
	}

	/**
	 * Create a new pad with the given initial text.
	 *
	 * @param padId the pad's id string
	 * @param text the initial text string
	 */
	public final void createPad(final String padId, final String text) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		args.put("text", text);
		this.connection.post("createPad", args);
	}

	/**
	 * Returns the number of revisions of this pad. The number is in
	 * "revisions".
	 *
	 * @param padId the pad's id string
	 * @return a Map with the number of revisions
	 */
	public final Map getRevisionsCount(final String padId) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		return this.connection.get("getRevisionsCount", args);
	}

	/**
	 * Returns the number of saved revisions of this pad.
	 *
	 * API >= 1.2.11
	 *
	 * @param padId the pad's id string
	 * @return a Map with number of saved revisions
	 */
	public final Map getSavedRevisionsCount(final String padId) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		return this.connection.get("getSavedRevisionsCount", args);
	}

	/**
	 * returns the list of saved revisions of this pad.
	 *
	 * API >= 1.2.11
	 *
	 * @param padId the pad's id string
	 * @return a Map with the list of saved revision numbers
	 */
	public final Map listSavedRevisions(final String padId) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		return this.connection.get("listSavedRevisions", args);
	}

	/**
	 * Saves the latest revision.
	 *
	 * API >= 1.2.11
	 *
	 * @param padId the pad's id string
	 */
	public final void saveRevision(final String padId) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		this.connection.post("saveRevision", args);
	}

	/**
	 * Saves the given revision.
	 *
	 * API >= 1.2.11
	 *
	 * @param padId the pad's id string
	 * @param rev the revision to be saved
	 */
	public final void saveRevision(final String padId, final long rev) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		args.put("rev", rev);
		this.connection.post("saveRevision", args);
	}

	/**
	 * Get the number of users currently editing a pad.
	 *
	 * @param padId the pad's id string
	 * @return a Map with the padUsersCount
	 */
	public final Map padUsersCount(final String padId) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		return this.connection.get("padUsersCount", args);
	}

	/**
	 * Returns the list of users that are currently editing this pad.
	 * A padUser has the values: "colorId", "name" and "timestamp".
	 *
	 * @param padId the pad's id string
	 * @return a Map with a List of pad user maps
	 */
	public final Map padUsers(final String padId) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		return this.connection.get("padUsers", args);
	}

	/**
	 * Deletes a pad.
	 *
	 * @param padId the pad's id string
	 */
	public final void deletePad(final String padId) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		this.connection.post("deletePad", args);
	}

	/**
	 * Copies a pad with full history and chat. If the destination
	 *  exists the copy will fail.
	 *
	 * API >= 1.2.8
	 *
	 * @param sourcePadId the id of the source pad
	 * @param destinationPadId the id of the destination pad
	 */
	public final void copyPad(final String sourcePadId,
			final String destinationPadId) {
		copyPad(sourcePadId, destinationPadId, false);
	}

	/**
	 * Copies a pad with full history and chat.
	 *
	 * API >= 1.2.8
	 *
	 * @param sourcePadId the id of the source pad
	 * @param destinationPadId the id of the destination pad
	 * @param force if force is true and the destination pad exists,
	 *  it will be overwritten.
	 */
	public final void copyPad(final String sourcePadId,
			final String destinationPadId,
			final boolean force) {
		Map<String, Object> args = new HashMap<>();
		args.put("sourceID", sourcePadId);
		args.put("destinationID", destinationPadId);
		args.put("force", force);
		this.connection.post("copyPad", args);
	}

	/**
	 * Moves a pad. If the destination exists the copy will fail.
	 *
	 * API >= 1.2.8
	 *
	 * @param sourcePadId the id of the source pad
	 * @param destinationPadId the id of the destination pad
	 */
	public final void movePad(final String sourcePadId,
			final String destinationPadId) {
		copyPad(sourcePadId, destinationPadId, false);
	}

	/**
	 * Moves a pad.
	 *
	 * API >= 1.2.8
	 *
	 * @param sourcePadId the id of the source pad
	 * @param destinationPadId the id of the destination pad
	 * @param force if force is true and the destination pad exists,
	 *  it will be overwritten.
	 */
	public final void movePad(final String sourcePadId,
			final String destinationPadId,
			final boolean force) {
		Map<String, Object> args = new HashMap<>();
		args.put("sourceID", sourcePadId);
		args.put("destinationID", destinationPadId);
		args.put("force", force);
		this.connection.post("movePad", args);
	}

	/**
	 * Get the pad's read-only id.
	 *
	 * @param padId the pad's id string
	 * @return a Map with the readOnlyID
	 */
	public final Map getReadOnlyID(final String padId) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		return this.connection.get("getReadOnlyID", args);
	}

	/**
	 * Get the pad's id from the read only id.
	 *
	 * API >= 1.2.10
	 *
	 * @param readOnlyPadId the pad's read only id string
	 * @return a Map with the padID
	 */
	public final Map getPadID(final String readOnlyPadId) {
		Map<String, Object> args = new HashMap<>();
		args.put("roID", readOnlyPadId);
		return this.connection.get("getPadID", args);
	}

	/**
	 * Sets the pad's public status.
	 * This is only applicable to group pads.
	 *
	 * @param padId the pad's id string
	 * @param publicStatus boolean
	 */
	public final void setPublicStatus(final String padId,
			final Boolean publicStatus) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		args.put("publicStatus", publicStatus);
		this.connection.post("setPublicStatus", args);
	}

	/**
	 * Gets the pad's public status.
	 *
	 * @param padId the pad's id string
	 * @return a Map with the Boolean publicStatus
	 */
	public final Map getPublicStatus(final String padId) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		return this.connection.get("getPublicStatus", args);
	}

	/**
	 * Sets the pad's password. This is only applicable to group pads.
	 *
	 * @param padId the pad's id string
	 * @param password string
	 */
	public final void setPassword(final String padId,
			final String password) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		args.put("password", password);
		this.connection.post("setPassword", args);
	}

	/**
	 * Checks whether the pad is password-protected or not.
	 *
	 * @param padId the pad's id string
	 * @return a Map with the Boolean passwordProtection
	 */
	public final Map isPasswordProtected(final String padId) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		return this.connection.get("isPasswordProtected", args);
	}

	/**
	 * List the ids of authors who have edited a pad.
	 *
	 * @param padId the pad's id string
	 * @return a Map with a List of author ids
	 */
	public final Map listAuthorsOfPad(final String padId) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		return this.connection.get("listAuthorsOfPad", args);
	}

	/**
	 * Get the pad's last edit date.
	 *
	 * @param padId the pad's id string
	 * @return a Map with lastEdited timestamp.
	 */
	public final Map getLastEdited(final String padId) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		return this.connection.get("getLastEdited", args);
	}

	/**
	 * Sends a custom message of type msg to the pad.
	 *
	 * @param padId the pad's id string
	 * @param msg the message to send
	 */
	public final void sendClientsMessage(final String padId,
			final String msg) {
		Map<String, Object> args = new HashMap<>();
		args.put("padID", padId);
		args.put("msg", msg);
		this.connection.post("sendClientsMessage", args);
	}

	/**
	 * Runs without error if current api token is valid.
	 *
	 * API >= 1.2
	 */
	public final void checkToken() {
		EtmPoint point = etmMonitor.createPoint("checkToken");

		try {
			this.connection.get("checkToken");
		} finally {
			point.collect();
		}
	}

	/**
	 * Returns true if the connection is using SSL/TLS, false if not.
	 *
	 * @return boolean
	 */
	public final boolean isSecure() {
		EtmPoint point = etmMonitor.createPoint("isSecure");

		try {
			return (this.connection.uri.getPort() == 443);
		} finally {
			point.collect();
		}

	}
}
