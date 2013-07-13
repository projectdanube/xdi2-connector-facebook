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
import xdi2.core.xri3.XDI3Segment;

public class FacebookMapping {

	public static final XDI3Segment XRI_S_FACEBOOK_CONTEXT = XDI3Segment.create("(https://facebook.com/)");

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
	public String facebookUserIdXriToFacebookUserId(XDI3Segment facebookUserIdXri) {

		if (facebookUserIdXri == null) throw new NullPointerException();

		// convert

		String facebookUserId = facebookUserIdXri.getLastSubSegment().getLiteral();

		// done

		if (log.isDebugEnabled()) log.debug("Converted " + facebookUserIdXri + " to " + facebookUserId);

		return facebookUserId;
	}

	/**
	 * Converts a native Facebook user ID to a Facebook user ID XRI.
	 * Example: 588183713 --> [!]!588183713
	 */
	public XDI3Segment facebookUserIdToFacebookUserIdXri(String facebookUserId) {

		if (facebookUserId == null) throw new NullPointerException();

		// convert

		XDI3Segment facebookUserIdXri = XDI3Segment.create("[!]!" + facebookUserId);

		// done

		if (log.isDebugEnabled()) log.debug("Converted " + facebookUserId + " to " + facebookUserIdXri);

		return facebookUserIdXri;
	}

	/**
	 * Converts a Facebook data XRI to a native Facebook object identifier.
	 * Example: +(user)<+(first_name)> --> user
	 */
	public String facebookDataXriToFacebookObjectIdentifier(XDI3Segment facebookDataXri) {

		if (facebookDataXri == null) throw new NullPointerException();

		// convert

		String facebookObjectIdentifier = Dictionary.instanceXriToNativeIdentifier(XdiAbstractContext.getBaseArcXri(facebookDataXri.getSubSegment(0)));

		// done

		if (log.isDebugEnabled()) log.debug("Converted " + facebookDataXri + " to " + facebookObjectIdentifier);

		return facebookObjectIdentifier;
	}

	/**
	 * Converts a Facebook data XRI to a native Facebook field identifier.
	 * Example: +(user)<+(first_name)> --> first_name
	 */
	public String facebookDataXriToFacebookFieldIdentifier(XDI3Segment facebookDataXri) {

		if (facebookDataXri == null) throw new NullPointerException();

		// convert

		String facebookFieldIdentifier = Dictionary.instanceXriToNativeIdentifier(XdiAbstractContext.getBaseArcXri(facebookDataXri.getSubSegment(1)));

		// done

		if (log.isDebugEnabled()) log.debug("Converted " + facebookDataXri + " to " + facebookFieldIdentifier);

		return facebookFieldIdentifier;
	}

	/**
	 * Maps and converts a Facebook data XRI to an XDI data XRI.
	 * Example: +(user)<+(first_name)> --> +first<+name>
	 */
	public XDI3Segment facebookDataXriToXdiDataXri(XDI3Segment facebookDataXri) {

		if (facebookDataXri == null) throw new NullPointerException();

		// convert

		StringBuffer buffer1 = new StringBuffer();

		for (int i=0; i<facebookDataXri.getNumSubSegments(); i++) {

			buffer1.append(Dictionary.instanceXriToDictionaryXri(facebookDataXri.getSubSegment(i)));
		}

		// map

		XDI3Segment facebookDataDictionaryXri = XDI3Segment.create("" + XRI_S_FACEBOOK_CONTEXT + buffer1.toString());
		ContextNode facebookDataDictionaryContextNode = this.mappingGraph.getDeepContextNode(facebookDataDictionaryXri);
		if (facebookDataDictionaryContextNode == null) return null;

		ContextNode xdiDataDictionaryContextNode = Equivalence.getReferenceContextNode(facebookDataDictionaryContextNode);
		XDI3Segment xdiDataDictionaryXri = xdiDataDictionaryContextNode.getXri();

		// convert

		StringBuilder buffer2 = new StringBuilder();

		for (int i=0; i<xdiDataDictionaryXri.getNumSubSegments(); i++) {

			buffer2.append(Dictionary.dictionaryXriToInstanceXri(xdiDataDictionaryXri.getSubSegment(i)));
		}

		XDI3Segment xdiDataXri = XDI3Segment.create(buffer2.toString());

		// done

		if (log.isDebugEnabled()) log.debug("Mapped and converted " + facebookDataXri + " to " + xdiDataXri);

		return xdiDataXri;
	}

	/**
	 * Maps and converts an XDI data XRI to a Facebook data XRI.
	 * Example: +first<+name> --> +(user)<+(first_name)> 
	 */
	public XDI3Segment xdiDataXriToFacebookDataXri(XDI3Segment xdiDataXri) {

		if (xdiDataXri == null) throw new NullPointerException();

		// convert

		StringBuffer buffer1 = new StringBuffer();

		for (int i=0; i<xdiDataXri.getNumSubSegments(); i++) {

			buffer1.append(Dictionary.instanceXriToDictionaryXri(xdiDataXri.getSubSegment(i)));
		}

		// map

		XDI3Segment xdiDataDictionaryXri = XDI3Segment.create(buffer1.toString());
		ContextNode xdiDataDictionaryContextNode = this.mappingGraph.getDeepContextNode(xdiDataDictionaryXri);
		if (xdiDataDictionaryContextNode == null) return null;

		ContextNode facebookDataDictionaryContextNode = Equivalence.getIncomingReferenceContextNodes(xdiDataDictionaryContextNode).next();
		XDI3Segment facebookDataDictionaryXri = facebookDataDictionaryContextNode.getXri();

		// convert

		StringBuilder buffer2 = new StringBuilder();

		for (int i=1; i<facebookDataDictionaryXri.getNumSubSegments(); i++) {

			buffer2.append(Dictionary.dictionaryXriToInstanceXri(facebookDataDictionaryXri.getSubSegment(i)));
		}

		XDI3Segment facebookDataXri = XDI3Segment.create(buffer2.toString());

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
