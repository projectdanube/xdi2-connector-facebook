package xdi2.connector.facebook.util;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.Literal;
import xdi2.core.xri3.impl.XRI3Segment;
import xdi2.messaging.constants.XDIMessagingConstants;

public class GraphUtil {

	public static final XRI3Segment XRI_S_FACEBOOK_CONTEXT = new XRI3Segment("(https://facebook.com)");

	private GraphUtil() { }

	public static String retrieveAccessToken(Graph graph, XRI3Segment userXri) {

		XRI3Segment contextNodeXri = new XRI3Segment("" + XRI_S_FACEBOOK_CONTEXT + userXri + XDIMessagingConstants.XRI_S_OAUTH_TOKEN);

		ContextNode contextNode = graph.findContextNode(contextNodeXri, false);
		if (contextNode == null) return null;

		Literal literal = contextNode.getLiteral();
		if (literal == null) return null;

		return literal.getLiteralData();
	}

	public static void storeAccessToken(Graph graph, XRI3Segment userXri, String accessToken) {

		XRI3Segment contextNodeXri = new XRI3Segment("" + XRI_S_FACEBOOK_CONTEXT + userXri + XDIMessagingConstants.XRI_S_OAUTH_TOKEN);

		ContextNode contextNode = graph.findContextNode(contextNodeXri, true);

		if (contextNode.containsLiteral())
			contextNode.getLiteral().setLiteralData(accessToken);
		else
			contextNode.createLiteral(accessToken);
	}

	public static void removeAccessToken(Graph graph, XRI3Segment userXri) {

		XRI3Segment contextNodeXri = new XRI3Segment("" + XRI_S_FACEBOOK_CONTEXT + userXri + XDIMessagingConstants.XRI_S_OAUTH_TOKEN);

		ContextNode contextNode = graph.findContextNode(contextNodeXri, false);
		if (contextNode == null) return;

		contextNode.delete();
	}
}
