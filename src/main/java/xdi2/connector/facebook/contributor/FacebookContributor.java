package xdi2.connector.facebook.contributor;

import org.json.JSONObject;

import xdi2.connector.facebook.api.FacebookApi;
import xdi2.connector.facebook.util.GraphUtil;
import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.xri3.impl.XRI3Segment;
import xdi2.messaging.GetOperation;
import xdi2.messaging.MessageResult;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.ExecutionContext;
import xdi2.messaging.target.contributor.AbstractContributor;

public class FacebookContributor extends AbstractContributor {

	private Graph graph;
	private FacebookApi facebookApi;

	public FacebookContributor() {

	}

	public void init() {

	}

	@Override
	public boolean get(XRI3Segment contextNodeXri, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		String contextNodeXriString = contextNodeXri.toString();
		String literalData = null;

		try {

			String accessToken = GraphUtil.retrieveAccessToken(this.getGraph());
			if (accessToken == null) throw new Exception("No access token.");

			JSONObject user = this.facebookApi.getUser(accessToken);
			if (user == null) throw new Exception("No user.");

			if (contextNodeXriString.endsWith("$!(+firstname)")) literalData = user.getString("first_name");
			else if (contextNodeXriString.endsWith("$!(+lastname)")) literalData = user.getString("last_name");
			else if (contextNodeXriString.endsWith("$!(+gender)")) literalData = user.getString("gender");
			else if (contextNodeXriString.endsWith("$!(+email)")) literalData = user.getString("email");
			else return false;
		} catch (Exception ex) {

			throw new Xdi2MessagingException("Cannot load user data: " + ex.getMessage(), ex, null);
		}

		if (literalData != null) {

			ContextNode contextNode = messageResult.getGraph().findContextNode(contextNodeXri, true);
			contextNode.createLiteral(literalData);
		}

		return true;
	}

	public Graph getGraph() {

		return this.graph;
	}

	public void setGraph(Graph graph) {

		this.graph = graph;
	}

	public FacebookApi getFacebookApi() {

		return this.facebookApi;
	}

	public void setFacebookApi(FacebookApi facebookApi) {

		this.facebookApi = facebookApi;
	}
}
