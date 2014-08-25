package xdi2.connector.facebook.mapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.exceptions.Xdi2RuntimeException;
import xdi2.core.features.dictionary.Dictionary;
import xdi2.core.features.equivalence.Equivalence;
import xdi2.core.features.nodetypes.XdiAbstractContext;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.io.XDIReaderRegistry;
import xdi2.core.syntax.XDIAddress;

public class FacebookMapping {

	public static final XDIAddress XDI_ADD_FACEBOOK_CONTEXT = XDIAddress.create("(https://facebook.com/)");

	private static final Logger log = LoggerFactory.getLogger(FacebookMapping.class);

	private static FacebookMapping instance;

	private Graph mappingGraph;

	public FacebookMapping() {

		this.mappingGraph = MemoryGraphFactory.getInstance().openGraph();

		try {

			XDIReaderRegistry.getAuto().read(this.mappingGraph, FacebookMapping.class.getResourceAsStream("mapping.xdi"));
		} catch (Exception ex) {

			throw new Xdi2RuntimeException(ex.getMessage(), ex);
		}
	}

	public static FacebookMapping getInstance() {

		if (instance == null) instance = new FacebookMapping();

		return instance;
	}

	/**
	 * Converts a Facebook user ID XRI to a native Facebook user ID.
	 * Example: [!]!588183713 --> 588183713
	 */
	public String facebookUserIdXriToFacebookUserId(XDIAddress facebookUserIdXri) {

		if (facebookUserIdXri == null) throw new NullPointerException();

		// convert

		String facebookUserId = facebookUserIdXri.getLastXDIArc().getLiteral();

		// done

		if (log.isDebugEnabled()) log.debug("Converted " + facebookUserIdXri + " to " + facebookUserId);

		return facebookUserId;
	}

	/**
	 * Converts a native Facebook user ID to a Facebook user ID XRI.
	 * Example: 588183713 --> [!]!588183713
	 */
	public XDIAddress facebookUserIdToFacebookUserIdXri(String facebookUserId) {

		if (facebookUserId == null) throw new NullPointerException();

		// convert

		XDIAddress facebookUserIdXri = XDIAddress.create("[!]!" + facebookUserId);

		// done

		if (log.isDebugEnabled()) log.debug("Converted " + facebookUserId + " to " + facebookUserIdXri);

		return facebookUserIdXri;
	}

	/**
	 * Converts a Facebook data XRI to a native Facebook object identifier.
	 * Example: +(user)<+(first_name)> --> user
	 */
	public String facebookDataXriToFacebookObjectIdentifier(XDIAddress facebookDataXri) {

		if (facebookDataXri == null) throw new NullPointerException();

		// convert

		String facebookObjectIdentifier = Dictionary.instanceXDIArcToNativeIdentifier(XdiAbstractContext.getBaseXDIArc(facebookDataXri.getXDIArc(0)));

		// done

		if (log.isDebugEnabled()) log.debug("Converted " + facebookDataXri + " to " + facebookObjectIdentifier);

		return facebookObjectIdentifier;
	}

	/**
	 * Converts a Facebook data XRI to a native Facebook field identifier.
	 * Example: +(user)<+(first_name)> --> first_name
	 */
	public String facebookDataXriToFacebookFieldIdentifier(XDIAddress facebookDataXri) {

		if (facebookDataXri == null) throw new NullPointerException();

		// convert

		String facebookFieldIdentifier = Dictionary.instanceXDIArcToNativeIdentifier(XdiAbstractContext.getBaseXDIArc(facebookDataXri.getXDIArc(1)));

		// done

		if (log.isDebugEnabled()) log.debug("Converted " + facebookDataXri + " to " + facebookFieldIdentifier);

		return facebookFieldIdentifier;
	}

	/**
	 * Maps and converts a Facebook data XRI to an XDI data XRI.
	 * Example: +(user)<+(first_name)> --> +first<+name>
	 */
	public XDIAddress facebookDataXriToXdiDataXri(XDIAddress facebookDataXri) {

		if (facebookDataXri == null) throw new NullPointerException();

		// convert

		StringBuffer buffer1 = new StringBuffer();

		for (int i=0; i<facebookDataXri.getNumXDIArcs(); i++) {

			buffer1.append(Dictionary.instanceXDIArcToDictionaryXDIArc(facebookDataXri.getXDIArc(i)));
		}

		// map

		XDIAddress facebookDataDictionaryXri = XDIAddress.create("" + XDI_ADD_FACEBOOK_CONTEXT + buffer1.toString());
		ContextNode facebookDataDictionaryContextNode = this.mappingGraph.getDeepContextNode(facebookDataDictionaryXri);
		if (facebookDataDictionaryContextNode == null) return null;

		ContextNode xdiDataDictionaryContextNode = Equivalence.getReferenceContextNode(facebookDataDictionaryContextNode);
		XDIAddress xdiDataDictionaryXri = xdiDataDictionaryContextNode.getXDIAddress();

		// convert

		StringBuilder buffer2 = new StringBuilder();

		for (int i=0; i<xdiDataDictionaryXri.getNumXDIArcs(); i++) {

			buffer2.append(Dictionary.dictionaryXDIArcToInstanceXDIArc(xdiDataDictionaryXri.getXDIArc(i)));
		}

		XDIAddress xdiDataXri = XDIAddress.create(buffer2.toString());

		// done

		if (log.isDebugEnabled()) log.debug("Mapped and converted " + facebookDataXri + " to " + xdiDataXri);

		return xdiDataXri;
	}

	/**
	 * Maps and converts an XDI data XRI to a Facebook data XRI.
	 * Example: +first<+name> --> +(user)<+(first_name)> 
	 */
	public XDIAddress xdiDataXriToFacebookDataXri(XDIAddress xdiDataXri) {

		if (xdiDataXri == null) throw new NullPointerException();

		// convert

		StringBuffer buffer1 = new StringBuffer();

		for (int i=0; i<xdiDataXri.getNumXDIArcs(); i++) {

			buffer1.append(Dictionary.instanceXDIArcToDictionaryXDIArc(xdiDataXri.getXDIArc(i)));
		}

		// map

		XDIAddress xdiDataDictionaryXri = XDIAddress.create(buffer1.toString());
		ContextNode xdiDataDictionaryContextNode = this.mappingGraph.getDeepContextNode(xdiDataDictionaryXri);
		if (xdiDataDictionaryContextNode == null) return null;

		ContextNode facebookDataDictionaryContextNode = Equivalence.getIncomingReferenceContextNodes(xdiDataDictionaryContextNode).next();
		XDIAddress facebookDataDictionaryXri = facebookDataDictionaryContextNode.getXDIAddress();

		// convert

		StringBuilder buffer2 = new StringBuilder();

		for (int i=1; i<facebookDataDictionaryXri.getNumXDIArcs(); i++) {

			buffer2.append(Dictionary.dictionaryXDIArcToInstanceXDIArc(facebookDataDictionaryXri.getXDIArc(i)));
		}

		XDIAddress facebookDataXri = XDIAddress.create(buffer2.toString());

		// done

		if (log.isDebugEnabled()) log.debug("Mapped and converted " + xdiDataXri + " to " + facebookDataXri);

		return facebookDataXri;
	}

	/*
	 * Getters and setters
	 */

	public Graph getMappingGraph() {

		return this.mappingGraph;
	}

	public void setMappingGraph(Graph mappingGraph) {

		this.mappingGraph = mappingGraph;
	}
}
