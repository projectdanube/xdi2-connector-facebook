package xdi2.connector.facebook.contributor;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.connector.facebook.api.FacebookApi;
import xdi2.connector.facebook.mapping.FacebookMapping;
import xdi2.connector.facebook.util.GraphUtil;
import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.features.equivalence.Equivalence;
import xdi2.core.features.nodetypes.XdiAttributeSingleton;
import xdi2.core.xri3.XDI3Segment;
import xdi2.messaging.GetOperation;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.MessageResult;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.ExecutionContext;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.Prototype;
import xdi2.messaging.target.contributor.AbstractContributor;
import xdi2.messaging.target.contributor.ContributorXri;
import xdi2.messaging.target.impl.graph.GraphMessagingTarget;
import xdi2.messaging.target.interceptor.MessageEnvelopeInterceptor;
import xdi2.messaging.target.interceptor.MessagingTargetInterceptor;

@ContributorXri(addresses={"(https://facebook.com)/"})
public class FacebookContributor extends AbstractContributor implements MessagingTargetInterceptor, MessageEnvelopeInterceptor, Prototype<FacebookContributor> {

	private static final Logger log = LoggerFactory.getLogger(FacebookContributor.class);

	private Graph tokenGraph;
	private FacebookApi facebookApi;
	private FacebookMapping facebookMapping;

	public FacebookContributor() {

		super();

		this.getContributors().addContributor(new FacebookEnabledContributor());
		this.getContributors().addContributor(new FacebookUserContributor());
	}

	/*
	 * Prototype
	 */

	@Override
	public FacebookContributor instanceFor(PrototypingContext prototypingContext) throws Xdi2MessagingException {

		// create new contributor

		FacebookContributor contributor = new FacebookContributor();

		// set api and mapping

		contributor.setFacebookApi(this.getFacebookApi());
		contributor.setFacebookMapping(this.getFacebookMapping());

		// done

		return contributor;
	}

	/*
	 * MessagingTargetInterceptor
	 */

	@Override
	public void init(MessagingTarget messagingTarget) throws Exception {

		// set the token graph

		if (this.tokenGraph == null && messagingTarget instanceof GraphMessagingTarget) {

			this.setTokenGraph(((GraphMessagingTarget) messagingTarget).getGraph());
		}
	}

	@Override
	public void shutdown(MessagingTarget messagingTarget) throws Exception {

	}

	/*
	 * MessageEnvelopeInterceptor
	 */

	@Override
	public boolean before(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		FacebookContributorExecutionContext.resetUsers(executionContext);

		return false;
	}

	@Override
	public boolean after(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		return false;
	}

	@Override
	public void exception(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext, Exception ex) {

	}

	/*
	 * Sub-Contributors
	 */

	@ContributorXri(addresses={"+|&enabled"})
	private class FacebookEnabledContributor extends AbstractContributor {

		@Override
		public boolean getContext(XDI3Segment[] contributorXris, XDI3Segment relativeContextNodeXri, XDI3Segment contextNodeXri, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

			XdiAttributeSingleton.fromContextNode(messageResult.getGraph().findContextNode(contextNodeXri, true)).getXdiValue(true).getContextNode().createLiteral("1");

			return true;
		}
	}

	@ContributorXri(addresses={"{{=@*!}}"})
	private class FacebookUserContributor extends AbstractContributor {

		private FacebookUserContributor() {

			super();

			this.getContributors().addContributor(new FacebookUserFieldContributor());
		}

		@Override
		public boolean getContext(XDI3Segment[] contributorXris, XDI3Segment relativeContextNodeXri, XDI3Segment contextNodeXri, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

			XDI3Segment facebookContextXri = contributorXris[contributorXris.length - 2];
			XDI3Segment userXri = contributorXris[contributorXris.length - 1];

			log.debug("facebookContextXri: " + facebookContextXri + ", userXri: " + userXri);

			if (relativeContextNodeXri != null) return false;

			// retrieve the Facebook user ID

			String facebookUserId = null;

			try {

				String accessToken = GraphUtil.retrieveAccessToken(FacebookContributor.this.getTokenGraph(), userXri);
				if (accessToken == null) throw new Exception("No access token.");

				JSONObject user = FacebookContributor.this.retrieveUser(executionContext, accessToken);
				if (user == null) throw new Exception("No user.");

				facebookUserId = user.getString("id");
			} catch (Exception ex) {

				throw new Xdi2MessagingException("Cannot load user data: " + ex.getMessage(), ex, null);
			}

			// add the Facebook user ID to the response

			if (facebookUserId != null) {

				XDI3Segment facebookUserXri = XDI3Segment.create("!" + facebookUserId);

				ContextNode facebookUserContextNode = messageResult.getGraph().findContextNode(XDI3Segment.create("" + facebookContextXri + facebookUserXri), true);
				ContextNode userContextNode = messageResult.getGraph().findContextNode(XDI3Segment.create("" + facebookContextXri + userXri), true);

				Equivalence.setReferenceContextNode(facebookUserContextNode, userContextNode);
			}

			// done

			return true;
		}
	}

	@ContributorXri(addresses={"+|(user){+}"})
	private class FacebookUserFieldContributor extends AbstractContributor {

		private FacebookUserFieldContributor() {

			super();
		}

		@Override
		public boolean getContext(XDI3Segment[] contributorXris, XDI3Segment relativeContextNodeXri, XDI3Segment contextNodeXri, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

			XDI3Segment facebookContextXri = contributorXris[contributorXris.length - 3];
			XDI3Segment userXri = contributorXris[contributorXris.length - 2];
			XDI3Segment facebookDataXri = contributorXris[contributorXris.length - 1];

			log.debug("facebookContextXri: " + facebookContextXri + ", userXri: " + userXri + ", facebookDataXri: " + facebookDataXri);

			// parse identifiers

			String facebookObjectIdentifier = FacebookContributor.this.facebookMapping.facebookDataXriToFacebookObjectIdentifier(facebookDataXri);
			String facebookFieldIdentifier = FacebookContributor.this.facebookMapping.facebookDataXriToFacebookFieldIdentifier(facebookDataXri);
			if (facebookObjectIdentifier == null) return false;
			if (facebookFieldIdentifier == null) return false;

			log.debug("facebookObjectIdentifier: " + facebookObjectIdentifier + ", facebookFieldIdentifier: " + facebookFieldIdentifier);
			
			// retrieve the Facebook value

			String facebookValue = null;

			try {

				String accessToken = GraphUtil.retrieveAccessToken(FacebookContributor.this.getTokenGraph(), userXri);
				if (accessToken == null) throw new Exception("No access token.");

				JSONObject user = FacebookContributor.this.retrieveUser(executionContext, accessToken);
				if (user == null) throw new Exception("No user.");
				if (! user.has(facebookFieldIdentifier)) return false;

				facebookValue = user.getString(facebookFieldIdentifier);
			} catch (Exception ex) {

				throw new Xdi2MessagingException("Cannot load user data: " + ex.getMessage(), ex, null);
			}

			// add the Facebook value to the response

			if (facebookValue != null) {

				ContextNode contextNode = messageResult.getGraph().findContextNode(contextNodeXri, true);
				contextNode.createLiteral(facebookValue);
			}

			// done

			return true;
		}
	}

	/*
	 * Helper methods
	 */

	private JSONObject retrieveUser(ExecutionContext executionContext, String accessToken) throws IOException, JSONException {

		JSONObject user = FacebookContributorExecutionContext.getUser(executionContext, accessToken);

		if (user == null) {

			user = this.facebookApi.getUser(accessToken);
			FacebookContributorExecutionContext.putUser(executionContext, accessToken, user);
		}

		return user;
	}

	/*
	 * Getters and setters
	 */

	public Graph getTokenGraph() {

		return this.tokenGraph;
	}

	public void setTokenGraph(Graph tokenGraph) {

		this.tokenGraph = tokenGraph;
	}

	public FacebookApi getFacebookApi() {

		return this.facebookApi;
	}

	public void setFacebookApi(FacebookApi facebookApi) {

		this.facebookApi = facebookApi;
	}

	public FacebookMapping getFacebookMapping() {

		return this.facebookMapping;
	}

	public void setFacebookMapping(FacebookMapping facebookMapping) {

		this.facebookMapping = facebookMapping;
	}
}
