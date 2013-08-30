package xdi2.connector.facebook.util;

import xdi2.connector.facebook.mapping.FacebookMapping;
import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.Literal;
import xdi2.core.constants.XDIAuthenticationConstants;
import xdi2.core.constants.XDIDictionaryConstants;
import xdi2.core.features.equivalence.Equivalence;
import xdi2.core.xri3.XDI3Segment;

public class GraphUtil {

	private GraphUtil() { }

	public static String retrieveAccessToken(Graph graph, XDI3Segment userXri) {

		XDI3Segment contextNodeXri = XDI3Segment.create("" + FacebookMapping.XRI_S_FACEBOOK_CONTEXT + userXri + XDIAuthenticationConstants.XRI_S_OAUTH_TOKEN);

		Literal literal = graph.getDeepLiteral(contextNodeXri);

		return literal == null ? null : literal.getLiteralDataString();
	}

	public static void storeFacebookAccessToken(Graph graph, XDI3Segment facebookUserIdXri, String facebookAccessToken) {

		XDI3Segment contextNodeXri = XDI3Segment.create("" + FacebookMapping.XRI_S_FACEBOOK_CONTEXT + facebookUserIdXri + XDIAuthenticationConstants.XRI_S_OAUTH_TOKEN);

		graph.setDeepLiteral(contextNodeXri, facebookAccessToken);
	}

	public static void removeAccessToken(Graph graph, XDI3Segment facebookUserIdXri) {

		XDI3Segment contextNodeXri = XDI3Segment.create("" + FacebookMapping.XRI_S_FACEBOOK_CONTEXT + facebookUserIdXri + XDIAuthenticationConstants.XRI_S_OAUTH_TOKEN);

		ContextNode contextNode = graph.getDeepContextNode(contextNodeXri);
		if (contextNode == null) return;

		contextNode.delete();
	}

	public static void storeFacebookUserIdXri(Graph graph, XDI3Segment userXri, XDI3Segment facebookUserIdXri) {

		XDI3Segment contextNodeXri = XDI3Segment.create("" + FacebookMapping.XRI_S_FACEBOOK_CONTEXT + userXri);
		XDI3Segment targetContextNodeXri = XDI3Segment.create("" + FacebookMapping.XRI_S_FACEBOOK_CONTEXT + facebookUserIdXri);

		Equivalence.setReferenceContextNode(graph.setDeepContextNode(contextNodeXri), graph.setDeepContextNode(targetContextNodeXri));
	}

	public static void removeFacebookUserIdXri(Graph graph, XDI3Segment facebookUserIdXri) {

		XDI3Segment contextNodeXri = XDI3Segment.create("" + FacebookMapping.XRI_S_FACEBOOK_CONTEXT + facebookUserIdXri);

		ContextNode contextNode = graph.getDeepContextNode(contextNodeXri);
		if (contextNode == null) return;

		contextNode.delRelations(XDIDictionaryConstants.XRI_S_REF);
	}
}
