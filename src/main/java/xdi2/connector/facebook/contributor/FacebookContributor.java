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
import xdi2.core.constants.XDIConstants;
import xdi2.core.features.equivalence.Equivalence;
import xdi2.core.features.nodetypes.XdiAbstractAttribute;
import xdi2.core.features.nodetypes.XdiAttributeSingleton;
import xdi2.core.features.nodetypes.XdiEntityCollection;
import xdi2.core.features.nodetypes.XdiEntityMemberOrdered;
import xdi2.core.xri3.XDI3Segment;
import xdi2.core.xri3.XDI3Statement;
import xdi2.messaging.GetOperation;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.MessageResult;
import xdi2.messaging.SetOperation;
import xdi2.messaging.exceptions.Xdi2MessagingException;
import xdi2.messaging.target.ExecutionContext;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.Prototype;
import xdi2.messaging.target.contributor.AbstractContributor;
import xdi2.messaging.target.contributor.ContributorXri;
import xdi2.messaging.target.interceptor.MessageEnvelopeInterceptor;

@ContributorXri(addresses={"(https://facebook.com/)"})
public class FacebookContributor extends AbstractContributor implements MessageEnvelopeInterceptor, Prototype<FacebookContributor> {

	private static final Logger log = LoggerFactory.getLogger(FacebookContributor.class);

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

		// set the graph

		contributor.setGraph(this.getGraph());
		
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

		if (this.getGraph() == null) {

			throw new Xdi2MessagingException("No graph.", null, null);
		}
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

	@ContributorXri(addresses={"<+enabled>"})
	private class FacebookEnabledContributor extends AbstractContributor {

		@Override
		public boolean executeGetOnAddress(XDI3Segment[] contributorXris, XDI3Segment contributorsXri, XDI3Segment relativeTargetAddress, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

			if (FacebookContributor.this.isEnabled())
				messageResult.getGraph().setDeepContextNode(contributorsXri).setContextNode(XDIConstants.XRI_SS_LITERAL).setLiteral(Double.valueOf(1));
			else
				messageResult.getGraph().setDeepContextNode(contributorsXri).setContextNode(XDIConstants.XRI_SS_LITERAL).setLiteral(Double.valueOf(0));

			return false;
		}

		@Override
		public boolean executeSetOnLiteralStatement(XDI3Segment[] contributorXris, XDI3Segment contributorsXri, XDI3Statement relativeTargetStatement, SetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

			Object literalData = relativeTargetStatement.getLiteralData();

			if (Integer.valueOf(1).equals(literalData))
				FacebookContributor.this.setEnabled(true);
			else
				FacebookContributor.this.setEnabled(false);

			return false;
		}
	}

	@ContributorXri(addresses={"[!]{!}"})
	private class FacebookUserContributor extends AbstractContributor {

		private FacebookUserContributor() {

			super();

			//this.getContributors().addContributor(new FacebookUserFriendsContributor());
			this.getContributors().addContributor(new FacebookUserFieldContributor());
		}

		@Override
		public boolean executeGetOnAddress(XDI3Segment[] contributorXris, XDI3Segment contributorsXri, XDI3Segment relativeTargetAddress, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

			XDI3Segment facebookContextXri = contributorXris[contributorXris.length - 2];
			XDI3Segment userIdXri = contributorXris[contributorXris.length - 1];

			log.debug("facebookContextXri: " + facebookContextXri + ", userIdXri: " + userIdXri);

			if (userIdXri.equals("[!]{!}")) return false;

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

				XDI3Segment facebookUserXri = XDI3Segment.create("[!]!" + facebookUserId);

				ContextNode facebookUserContextNode = messageResult.getGraph().setDeepContextNode(XDI3Segment.create("" + facebookContextXri + facebookUserXri));
				ContextNode userContextNode = messageResult.getGraph().setDeepContextNode(contributorsXri);

				Equivalence.addIdentityContextNode(userContextNode, facebookUserContextNode);
			}*/

			// done

			return false;
		}
	}

	@ContributorXri(addresses={"+(user)[+(friend)]"})
	private class FacebookUserFriendsContributor extends AbstractContributor {

		private FacebookUserFriendsContributor() {

			super();
		}

		@Override
		public boolean executeGetOnAddress(XDI3Segment[] contributorXris, XDI3Segment contributorsXri, XDI3Segment relativeTargetAddress, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

			XDI3Segment facebookContextXri = contributorXris[contributorXris.length - 3];
			XDI3Segment userIdXri = contributorXris[contributorXris.length - 2];

			log.debug("facebookContextXri: " + facebookContextXri + ", userIdXri: " + userIdXri);

			if (userIdXri.equals("[!]{!}")) return false;

			// retrieve the Facebook friends

			JSONArray facebookFriends = null;

			try {

				String accessToken = GraphUtil.retrieveAccessToken(FacebookContributor.this.getGraph(), userIdXri);
				if (accessToken == null) {

					log.warn("No access token for user XRI: " + userIdXri);
					return false;
				}

				JSONObject user = FacebookContributor.this.retrieveUser(executionContext, accessToken);
				if (user == null) throw new Exception("No user.");
				if (! user.has("friends")) return false;

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

					XDI3Segment facebookFriendXri = XDI3Segment.create("[!]!" + facebookFriendId);
					ContextNode facebookFriendContextNode = messageResult.getGraph().setDeepContextNode(XDI3Segment.create("" + facebookContextXri + facebookFriendXri));
					facebookFriendContextNode.setDeepContextNode(XDI3Segment.create("<+name>&")).setLiteral(facebookFriendName);

					XdiEntityMemberOrdered friendXdiEntityMemberOrdered = friendXdiEntityCollection.setXdiMemberOrdered(-1);

					Equivalence.setIdentityContextNode(friendXdiEntityMemberOrdered.getContextNode(), facebookFriendContextNode);
				}
			}

			// done

			return false;
		}
	}

	@ContributorXri(addresses={"+(user){+}"})
	private class FacebookUserFieldContributor extends AbstractContributor {

		private FacebookUserFieldContributor() {

			super();
		}

		@Override
		public boolean executeGetOnAddress(XDI3Segment[] contributorXris, XDI3Segment contributorsXri, XDI3Segment relativeTargetAddress, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {

			XDI3Segment facebookContextXri = contributorXris[contributorXris.length - 3];
			XDI3Segment facebookUserIdXri = contributorXris[contributorXris.length - 2];
			XDI3Segment facebookDataXri = contributorXris[contributorXris.length - 1];

			log.debug("facebookContextXri: " + facebookContextXri + ", userIdXri: " + facebookUserIdXri + ", facebookDataXri: " + facebookDataXri);

			if (facebookUserIdXri.equals("[!]{!}")) return false;
			if (facebookDataXri.equals("{+}")) return false;

			// parse identifiers

			String facebookUserId = FacebookContributor.this.facebookMapping.facebookUserIdXriToFacebookUserId(facebookUserIdXri);
			String facebookObjectIdentifier = FacebookContributor.this.facebookMapping.facebookDataXriToFacebookObjectIdentifier(facebookDataXri);
			String facebookFieldIdentifier = FacebookContributor.this.facebookMapping.facebookDataXriToFacebookFieldIdentifier(facebookDataXri);
			if (facebookUserId == null) return false;
			if (facebookObjectIdentifier == null) return false;
			if (facebookFieldIdentifier == null) return false;

			log.debug("facebookUserId: " + facebookUserId + ", facebookObjectIdentifier: " + facebookObjectIdentifier + ", facebookFieldIdentifier: " + facebookFieldIdentifier);

			// retrieve the Facebook field

			String facebookField = null;

			try {

				String accessToken = GraphUtil.retrieveAccessToken(FacebookContributor.this.getGraph(), facebookUserIdXri);
				if (accessToken == null) {

					log.warn("No access token for user ID: " + facebookUserIdXri);
					return false;
				}

				JSONObject user = FacebookContributor.this.retrieveUser(executionContext, facebookUserId, accessToken);
				if (user == null) throw new Exception("No user.");
				if (! user.has(facebookFieldIdentifier)) return false;

				facebookField = user.getString(facebookFieldIdentifier);
			} catch (Exception ex) {

				throw new Xdi2MessagingException("Cannot load user data: " + ex.getMessage(), ex, null);
			}

			// add the Facebook field to the response

			if (facebookField != null) {

				XdiAttributeSingleton xdiAttributeSingleton = (XdiAttributeSingleton) XdiAbstractAttribute.fromContextNode(messageResult.getGraph().setDeepContextNode(contributorsXri));
				xdiAttributeSingleton.getXdiValue(true).getContextNode().setLiteral(facebookField);
			}

			// done

			return false;
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
}
