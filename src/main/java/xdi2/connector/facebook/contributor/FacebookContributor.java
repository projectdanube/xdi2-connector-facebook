package xdi2.connector.facebook.contributor;

import java.io.IOException;

import org.json.JSONArray;
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
import xdi2.core.features.nodetypes.XdiAbstractAttribute;
import xdi2.core.features.nodetypes.XdiAttributeSingleton;
import xdi2.core.features.nodetypes.XdiEntityCollection;
import xdi2.core.features.nodetypes.XdiEntityInstanceOrdered;
import xdi2.core.syntax.XDIAddress;
import xdi2.messaging.GetOperation;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.MessageResult;
import xdi2.messaging.context.ExecutionContext;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.Prototype;
import xdi2.messaging.target.contributor.AbstractContributor;
import xdi2.messaging.target.contributor.ContributorMount;
import xdi2.messaging.target.contributor.ContributorResult;
import xdi2.messaging.target.impl.graph.GraphMessagingTarget;
import xdi2.messaging.target.interceptor.InterceptorResult;
import xdi2.messaging.target.interceptor.MessageEnvelopeInterceptor;

@ContributorMount(contributorXDIAddresses={"(https://facebook.com/)"})
public class FacebookContributor extends AbstractContributor implements MessageEnvelopeInterceptor, Prototype<FacebookContributor> {

	private static final Logger log = LoggerFactory.getLogger(FacebookContributor.class);

	private FacebookApi facebookApi;
	private FacebookMapping facebookMapping;
	private Graph tokenGraph;

	public FacebookContributor() {

		super();

		this.facebookApi = null;
		this.facebookMapping = null;
		this.tokenGraph = null;

		this.getContributors().addContributor(new FacebookUserContributor());
	}

	/*
	 * Prototype
	 */

	@Override
	public FacebookContributor instanceFor(PrototypingContext prototypingContext) throws Xdi2MessagingException {

		// create new contributor

		FacebookContributor contributor = new FacebookContributor();

		// set the graph

		contributor.setTokenGraph(this.getTokenGraph());

		// set api and mapping

		contributor.setFacebookApi(this.getFacebookApi());
		contributor.setFacebookMapping(this.getFacebookMapping());

		// done

		return contributor;
	}

	/*
	 * Init and shutdown
	 */

	@Override
	public void init(MessagingTarget messagingTarget) throws Exception {

		super.init(messagingTarget);

		if (this.getTokenGraph() == null && messagingTarget instanceof GraphMessagingTarget) this.setTokenGraph(((GraphMessagingTarget) messagingTarget).getGraph()); 
		if (this.getTokenGraph() == null) throw new Xdi2MessagingException("No token graph.", null, null);
	}

	/*
	 * MessageEnvelopeInterceptor
	 */

	@Override
	public InterceptorResult before(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		FacebookContributorExecutionContext.resetUsers(executionContext);

		return InterceptorResult.DEFAULT;
	}

	@Override
	public InterceptorResult after(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

		return InterceptorResult.DEFAULT;
	}

	@Override
	public void exception(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext, Exception ex) {

	}

	/*
	 * Sub-Contributors
	 */

	@ContributorMount(contributorXDIAddresses={"[=]{*!:uuid:0000}"})
	private class FacebookUserContributor extends AbstractContributor {

		private FacebookUserContributor() {

			super();

			//this.getContributors().addContributor(new FacebookUserFriendsContributor());
			this.getContributors().addContributor(new FacebookUserFieldContributor());
		}

		@Override
		public ContributorResult executeGetOnAddress(XDIAddress[] contributorAddresses, XDIAddress contributorsXri, XDIAddress relativeTargetAddress, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

			XDIAddress facebookContextXri = contributorAddresses[contributorAddresses.length - 2];
			XDIAddress userIdXri = contributorAddresses[contributorAddresses.length - 1];

			log.debug("facebookContextXri: " + facebookContextXri + ", userIdXri: " + userIdXri);

			if (userIdXri.equals("[=]{*!:uuid:0000}")) return ContributorResult.DEFAULT;

			// retrieve the Facebook user ID

			/*			String facebookUserId = null;

			try {

				String accessToken = GraphUtil.retrieveAccessToken(FacebookContributor.this.getTokenGraph(), userXri);
				if (accessToken == null) {

					log.warn("No access token for user XRI: " + userXri);
					return false;
				}

				JSONObject user = FacebookContributor.this.retrieveUser(executionContext, accessToken);
				if (user == null) throw new Exception("No user.");

				facebookUserId = user.getString("id");
			} catch (Exception ex) {

				throw new Xdi2MessagingException("Cannot load user data: " + ex.getMessage(), ex, null);
			}

			// add the Facebook user ID to the response

			if (facebookUserId != null) {

				XDIAddress facebookUserXri = XDIAddress.create("[=]!" + facebookUserId);

				ContextNode facebookUserContextNode = messageResult.getGraph().setDeepContextNode(XDIAddress.create("" + facebookContextXri + facebookUserXri));
				ContextNode userContextNode = messageResult.getGraph().setDeepContextNode(contributorsXri);

				Equivalence.addIdentityContextNode(userContextNode, facebookUserContextNode);
			}*/

			// done

			return ContributorResult.DEFAULT;
		}
	}

	@ContributorMount(contributorXDIAddresses={"#(user)[#(friend)]"})
	private class FacebookUserFriendsContributor extends AbstractContributor {

		private FacebookUserFriendsContributor() {

			super();
		}

		@Override
		public ContributorResult executeGetOnAddress(XDIAddress[] contributorAddresses, XDIAddress contributorsXri, XDIAddress relativeTargetAddress, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

			XDIAddress facebookContextXri = contributorAddresses[contributorAddresses.length - 3];
			XDIAddress facebookUserIdXri = contributorAddresses[contributorAddresses.length - 2];

			log.debug("facebookContextXri: " + facebookContextXri + ", facebookUserIdXri: " + facebookUserIdXri);

			if (facebookUserIdXri.equals("[=]{*!:uuid:0000}")) return ContributorResult.DEFAULT;

			// retrieve the Facebook friends

			JSONArray facebookFriends = null;

			try {

				String accessToken = GraphUtil.retrieveFacebookAccessToken(FacebookContributor.this.getTokenGraph(), facebookUserIdXri);
				if (accessToken == null) {

					log.warn("No access token for user ID: " + facebookUserIdXri);
					return new ContributorResult(true, false, true);
				}

				JSONObject user = FacebookContributor.this.retrieveUser(executionContext, accessToken);
				if (user == null) throw new Exception("No user.");
				if (! user.has("friends")) return new ContributorResult(true, false, true);

				facebookFriends = user.getJSONObject("friends").getJSONArray("data");
			} catch (Exception ex) {

				throw new Xdi2MessagingException("Cannot load user data: " + ex.getMessage(), ex, null);
			}

			// add the Facebook friends to the response

			if (facebookFriends != null) {

				XdiEntityCollection friendXdiEntityCollection = XdiEntityCollection.fromContextNode(messageResult.getGraph().setDeepContextNode(contributorsXri));

				for (int i=0; i<facebookFriends.length(); i++) {

					JSONObject facebookFriend;
					String facebookFriendName;
					String facebookFriendId; 

					try {

						facebookFriend = facebookFriends.getJSONObject(i);
						facebookFriendId = facebookFriend.getString("id");
						facebookFriendName = facebookFriend.getString("name");
					} catch (JSONException ex) {

						throw new Xdi2MessagingException("Cannot load user data: " + ex.getMessage(), ex, null);
					}

					XDIAddress facebookFriendXri = XDIAddress.create("[=]!" + facebookFriendId);
					ContextNode facebookFriendContextNode = messageResult.getGraph().setDeepContextNode(XDIAddress.create("" + facebookContextXri + facebookFriendXri));
					facebookFriendContextNode.setDeepContextNode(XDIAddress.create("<#name>&")).setLiteralString(facebookFriendName);

					XdiEntityInstanceOrdered friendXdiEntityInstanceOrdered = friendXdiEntityCollection.setXdiInstanceOrdered(false, false);

					Equivalence.setIdentityContextNode(friendXdiEntityInstanceOrdered.getContextNode(), facebookFriendContextNode);
				}
			}

			// done

			return new ContributorResult(true, false, true);
		}
	}

	@ContributorMount(contributorXDIAddresses={"#(user){<#field>}"})
	private class FacebookUserFieldContributor extends AbstractContributor {

		private FacebookUserFieldContributor() {

			super();
		}

		@Override
		public ContributorResult executeGetOnAddress(XDIAddress[] contributorAddresses, XDIAddress contributorsXri, XDIAddress relativeTargetAddress, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

			XDIAddress facebookContextXri = contributorAddresses[contributorAddresses.length - 3];
			XDIAddress facebookUserIdXri = contributorAddresses[contributorAddresses.length - 2];
			XDIAddress facebookDataXri = contributorAddresses[contributorAddresses.length - 1];

			log.debug("facebookContextXri: " + facebookContextXri + ", userIdXri: " + facebookUserIdXri + ", facebookDataXri: " + facebookDataXri);

			if (facebookUserIdXri.equals("[=]{*!:uuid:0000}")) return ContributorResult.DEFAULT;
			if (facebookDataXri.equals("{<#field>}")) return ContributorResult.DEFAULT;

			// parse identifiers

			String facebookUserId = FacebookContributor.this.facebookMapping.facebookUserIdXriToFacebookUserId(facebookUserIdXri);
			String facebookObjectIdentifier = FacebookContributor.this.facebookMapping.facebookDataXriToFacebookObjectIdentifier(facebookDataXri);
			String facebookFieldIdentifier = FacebookContributor.this.facebookMapping.facebookDataXriToFacebookFieldIdentifier(facebookDataXri);
			if (facebookUserId == null) return new ContributorResult(true, false, true);
			if (facebookObjectIdentifier == null) return new ContributorResult(true, false, true);
			if (facebookFieldIdentifier == null) return new ContributorResult(true, false, true);

			log.debug("facebookUserId: " + facebookUserId + ", facebookObjectIdentifier: " + facebookObjectIdentifier + ", facebookFieldIdentifier: " + facebookFieldIdentifier);

			// retrieve the Facebook field

			String facebookField = null;

			try {

				String accessToken = GraphUtil.retrieveFacebookAccessToken(FacebookContributor.this.getTokenGraph(), facebookUserIdXri);
				if (accessToken == null) {

					log.warn("No access token for user ID: " + facebookUserIdXri);
					return new ContributorResult(true, false, true);
				}

				JSONObject user = FacebookContributor.this.retrieveUser(executionContext, facebookUserId, accessToken);
				if (user == null) throw new Exception("No user.");
				if (! user.has(facebookFieldIdentifier)) return new ContributorResult(true, false, true);

				facebookField = user.getString(facebookFieldIdentifier);
			} catch (Exception ex) {

				throw new Xdi2MessagingException("Cannot load user data: " + ex.getMessage(), ex, null);
			}

			// add the Facebook field to the response

			if (facebookField != null) {

				XdiAttributeSingleton xdiAttributeSingleton = (XdiAttributeSingleton) XdiAbstractAttribute.fromContextNode(messageResult.getGraph().setDeepContextNode(contributorsXri));
				xdiAttributeSingleton.setLiteralDataString(facebookField);
			}

			// done

			return new ContributorResult(true, false, true);
		}
	}

	/*
	 * Helper methods
	 */

	private JSONObject retrieveUser(ExecutionContext executionContext, String facebookUserId, String accessToken) throws IOException, JSONException {

		JSONObject user = FacebookContributorExecutionContext.getUser(executionContext, accessToken);

		if (user == null) {

			user = this.facebookApi.retrieveUser(facebookUserId, accessToken, null);
			JSONObject userFriends = this.facebookApi.retrieveUser(accessToken, "friends");
			user.put("friends", userFriends.getJSONObject("friends"));

			FacebookContributorExecutionContext.putUser(executionContext, accessToken, user);
		}

		return user;
	}

	private JSONObject retrieveUser(ExecutionContext executionContext, String accessToken) throws IOException, JSONException {

		JSONObject user = FacebookContributorExecutionContext.getUser(executionContext, accessToken);

		if (user == null) {

			user = this.facebookApi.retrieveUser(accessToken, null);
			JSONObject userFriends = this.facebookApi.retrieveUser(accessToken, "friends");
			user.put("friends", userFriends.getJSONObject("friends"));

			FacebookContributorExecutionContext.putUser(executionContext, accessToken, user);
		}

		return user;
	}

	/*
	 * Getters and setters
	 */

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

	public Graph getTokenGraph() {

		return this.tokenGraph;
	}

	public void setTokenGraph(Graph tokenGraph) {

		this.tokenGraph = tokenGraph;
	}
}
