package xdi2.connector.facebook.mapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.features.dictionary.Dictionary;
import xdi2.core.features.multiplicity.Multiplicity;
import xdi2.core.xri3.impl.XRI3Segment;
import xdi2.core.xri3.impl.XRI3SubSegment;

public class FacebookMapping {

	public static final XRI3Segment XRI_S_FACEBOOK_CONTEXT = new XRI3Segment("(https://facebook.com)");

	private static final Logger log = LoggerFactory.getLogger(FacebookMapping.class);

	private Graph mappingGraph;

	/**
	 * Converts a Facebook data XRI to a native Facebook user field identifier.
	 * Example: $!(first_name) --> first_name
	 */
	public String facebookDataXriToFacebookUserFieldIdentifier(XRI3Segment facebookDataXri) {

		// convert

		String facebookUserFieldIdentifier = subSegmentXRefValue(facebookDataXri, 0);

		// done

		if (log.isDebugEnabled()) log.debug("Converted " + facebookDataXri + " to " + facebookUserFieldIdentifier);

		return facebookUserFieldIdentifier;
	}

	/**
	 * Maps and converts a Facebook data XRI to an XDI data XRI.
	 * Example: $!(first_name) --> +first$!(+name)
	 */
	public XRI3Segment facebookDataXriToXdiDataXri(XRI3Segment facebookUserFieldXri) {

		// map

		XRI3Segment facebookDataDictionaryXri = new XRI3Segment("" + XRI_S_FACEBOOK_CONTEXT + "+(+(" + subSegmentXRefValue(facebookUserFieldXri, 0) + "))");
		ContextNode facebookDataDictionaryContextNode = this.mappingGraph.findContextNode(facebookDataDictionaryXri, false);

		ContextNode xdiDataDictionaryContextNode = Dictionary.getCanonicalContextNode(facebookDataDictionaryContextNode);
		XRI3Segment xdiDataDictionaryXri = xdiDataDictionaryContextNode.getXri();

		// convert

		StringBuilder buffer = new StringBuilder();

		for (int i=0; i<xdiDataDictionaryXri.getNumSubSegments(); i++) {

			if (i + 1 < xdiDataDictionaryXri.getNumSubSegments()) {

				buffer.append(Multiplicity.entitySingletonArcXri((XRI3SubSegment) xdiDataDictionaryXri.getSubSegment(i)).toString());
			} else {

				buffer.append(Multiplicity.attributeSingletonArcXri((XRI3SubSegment) xdiDataDictionaryXri.getSubSegment(i)).toString());
			}
		}

		XRI3Segment xdiDataXri = new XRI3Segment(buffer.toString());

		// done

		if (log.isDebugEnabled()) log.debug("Mapped and converted " + facebookUserFieldXri + " to " + xdiDataXri);

		return xdiDataXri;
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

	/*
	 * Helper methods
	 */
	
	private static String subSegmentXRefValue(XRI3Segment xri, int i) {
		
		if (! xri.getSubSegment(i).hasXRef()) return null;
		if (! xri.getSubSegment(i).getXRef().hasXRIReference()) return null;

		return xri.getFirstSubSegment().getXRef().getXRIReference().toString();
	}
}
