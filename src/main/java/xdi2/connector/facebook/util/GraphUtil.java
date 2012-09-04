package xdi2.connector.facebook.util;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.Literal;
import xdi2.core.xri3.impl.XRI3Segment;

public class GraphUtil {

	private static final XRI3Segment XRI_FACEBOOK_OAUTH_TOKEN = new XRI3Segment("(https://facebook.com)=!1111$($oauth)$!($token)");

	private GraphUtil() { }

	public static String retrieveAccessToken(Graph graph) {

		ContextNode contextNode = graph.findContextNode(XRI_FACEBOOK_OAUTH_TOKEN, false);
		if (contextNode == null) return null;

		Literal literal = contextNode.getLiteral();
		if (literal == null) return null;

		return literal.getLiteralData();
	}

	public static void storeAccessToken(Graph graph, String accessToken) {

		ContextNode contextNode = graph.findContextNode(XRI_FACEBOOK_OAUTH_TOKEN, true);

		if (contextNode.containsLiteral())
			contextNode.getLiteral().setLiteralData(accessToken);
		else
			contextNode.createLiteral(accessToken);
	}
}
