package xdi2.connector.facebook.util;

import xdi2.connector.facebook.mapping.FacebookMapping;
import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.Literal;
import xdi2.core.constants.XDIPolicyConstants;
import xdi2.core.xri3.XDI3Segment;

public class GraphUtil {

	private GraphUtil() { }

	public static String retrieveAccessToken(Graph graph, XDI3Segment userXri) {

		XDI3Segment contextNodeXri = XDI3Segment.create("" + FacebookMapping.XRI_S_FACEBOOK_CONTEXT + userXri + XDIPolicyConstants.XRI_S_OAUTH_TOKEN);

		Literal literal = graph.getDeepLiteral(contextNodeXri);

		return literal == null ? null : literal.getLiteralData();
	}

	public static void storeAccessToken(Graph graph, XDI3Segment userXri, String accessToken) {

		XDI3Segment contextNodeXri = XDI3Segment.create("" + FacebookMapping.XRI_S_FACEBOOK_CONTEXT + userXri + XDIPolicyConstants.XRI_S_OAUTH_TOKEN);

		graph.setDeepLiteral(contextNodeXri, accessToken);
	}

	public static void removeAccessToken(Graph graph, XDI3Segment userXri) {

		XDI3Segment contextNodeXri = XDI3Segment.create("" + FacebookMapping.XRI_S_FACEBOOK_CONTEXT + userXri + XDIPolicyConstants.XRI_S_OAUTH_TOKEN);

		ContextNode contextNode = graph.getDeepContextNode(contextNodeXri);
		if (contextNode == null) return;

		contextNode.delete();
	}
}
