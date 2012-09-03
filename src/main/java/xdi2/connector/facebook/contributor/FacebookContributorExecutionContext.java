package xdi2.connector.facebook.contributor;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import xdi2.messaging.target.ExecutionContext;

/**
 * Methods for storing state related to the FacebookContributor.
 */
public class FacebookContributorExecutionContext {

	private static final String EXECUTIONCONTEXT_KEY_USERS_PER_MESSAGEENVELOPE = FacebookContributor.class.getCanonicalName() + "#userspermessageenvelope";

	@SuppressWarnings("unchecked")
	public static Map<String, JSONObject> getUsers(ExecutionContext executionContext) {

		return (Map<String, JSONObject>) executionContext.getMessageEnvelopeAttribute(EXECUTIONCONTEXT_KEY_USERS_PER_MESSAGEENVELOPE);
	}

	public static JSONObject getUser(ExecutionContext executionContext, String key) {

		return getUsers(executionContext).get(key);
	}

	public static void putUser(ExecutionContext executionContext, String key, JSONObject value) {

		getUsers(executionContext).put(key, value);
	}

	public static void resetUsers(ExecutionContext executionContext) {

		executionContext.putMessageEnvelopeAttribute(EXECUTIONCONTEXT_KEY_USERS_PER_MESSAGEENVELOPE, new HashMap<String, JSONObject> ());
	}
}
