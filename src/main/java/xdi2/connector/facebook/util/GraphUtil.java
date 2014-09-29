package xdi2.connector.facebook.util;

import xdi2.connector.facebook.mapping.FacebookMapping;
import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.Literal;
import xdi2.core.constants.XDIAuthenticationConstants;
import xdi2.core.constants.XDIConstants;
import xdi2.core.constants.XDIDictionaryConstants;
import xdi2.core.features.equivalence.Equivalence;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.util.XDIAddressUtil;

public class GraphUtil {

	private GraphUtil() { }

	public static String retrieveFacebookAccessToken(Graph graph, XDIAddress facebookUserIdXri) {

		XDIAddress contextNodeXri = XDIAddress.create("" + FacebookMapping.XDI_ADD_FACEBOOK_CONTEXT + facebookUserIdXri + XDIAuthenticationConstants.XDI_ADD_OAUTH_TOKEN + XDIConstants.XDI_ADD_VALUE);

		Literal literal = graph.getDeepLiteral(contextNodeXri);

		return literal == null ? null : literal.getLiteralDataString();
	}

	public static void storeFacebookAccessToken(Graph graph, XDIAddress facebookUserIdXri, String facebookAccessToken) {

		XDIAddress contextNodeXri = XDIAddress.create("" + FacebookMapping.XDI_ADD_FACEBOOK_CONTEXT + facebookUserIdXri + XDIAuthenticationConstants.XDI_ADD_OAUTH_TOKEN + XDIConstants.XDI_ADD_VALUE);

		graph.setDeepLiteral(contextNodeXri, facebookAccessToken);
	}

	public static void removeFacebookAccessToken(Graph graph, XDIAddress facebookUserIdXri) {

		XDIAddress contextNodeXri = XDIAddress.create("" + FacebookMapping.XDI_ADD_FACEBOOK_CONTEXT + facebookUserIdXri + XDIAuthenticationConstants.XDI_ADD_OAUTH_TOKEN + XDIConstants.XDI_ADD_VALUE);

		ContextNode contextNode = graph.getDeepContextNode(contextNodeXri);
		if (contextNode == null) return;

		contextNode.delete();
	}

	public static XDIAddress retrieveFacebookUserIdXri(Graph graph, XDIAddress userXri) {

		XDIAddress contextNodeXri = XDIAddress.create("" + FacebookMapping.XDI_ADD_FACEBOOK_CONTEXT + userXri);

		ContextNode contextNode = graph.getDeepContextNode(contextNodeXri);
		if (contextNode == null) return null;

		ContextNode targetContextNode = Equivalence.getReferenceContextNode(contextNode);
		if (targetContextNode == null) return null;

		return XDIAddressUtil.localXDIAddress(targetContextNode.getXDIAddress(), - FacebookMapping.XDI_ADD_FACEBOOK_CONTEXT.getNumXDIArcs());
	}

	public static void storeFacebookUserIdXri(Graph graph, XDIAddress userXri, XDIAddress facebookUserIdXri) {

		XDIAddress contextNodeXri = XDIAddress.create("" + FacebookMapping.XDI_ADD_FACEBOOK_CONTEXT + userXri);
		XDIAddress targetContextNodeXri = XDIAddress.create("" + FacebookMapping.XDI_ADD_FACEBOOK_CONTEXT + facebookUserIdXri);

		Equivalence.setReferenceContextNode(graph.setDeepContextNode(contextNodeXri), graph.setDeepContextNode(targetContextNodeXri));
	}

	public static void removeFacebookUserIdXri(Graph graph, XDIAddress userXri) {

		XDIAddress contextNodeXri = XDIAddress.create("" + FacebookMapping.XDI_ADD_FACEBOOK_CONTEXT + userXri);

		ContextNode contextNode = graph.getDeepContextNode(contextNodeXri);
		if (contextNode == null) return;

		contextNode.delRelations(XDIDictionaryConstants.XDI_ADD_REF);
	}
}
