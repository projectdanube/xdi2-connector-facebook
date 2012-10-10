package xdi2.connector.facebook.mapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.exceptions.Xdi2RuntimeException;
import xdi2.core.features.dictionary.Dictionary;
import xdi2.core.features.multiplicity.Multiplicity;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.io.XDIReaderRegistry;
import xdi2.core.xri3.impl.XRI3Segment;
import xdi2.core.xri3.impl.XRI3SubSegment;

public class FacebookMapping {

	public static final XRI3Segment XRI_S_FACEBOOK_CONTEXT = new XRI3Segment("+(https://facebook.com/)");

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
	 * Converts a Facebook data XRI to a native Facebook object identifier.
	 * Example: +(user)$!(+(first_name)) --> user
	 */
	public String facebookDataXriToFacebookObjectIdentifier(XRI3Segment facebookDataXri) {

		if (facebookDataXri == null) throw new NullPointerException();

		// convert

		String facebookObjectIdentifier = Dictionary.instanceXriToNativeIdentifier(Multiplicity.baseArcXri((XRI3SubSegment) facebookDataXri.getSubSegment(0)));

		// done

		if (log.isDebugEnabled()) log.debug("Converted " + facebookDataXri + " to " + facebookObjectIdentifier);

		return facebookObjectIdentifier;
	}

	/**
	 * Converts a Facebook data XRI to a native Facebook field identifier.
	 * Example: +(user)$!(+(first_name)) --> first_name
	 */
	public String facebookDataXriToFacebookFieldIdentifier(XRI3Segment facebookDataXri) {

		if (facebookDataXri == null) throw new NullPointerException();

		// convert

		String facebookFieldIdentifier = Dictionary.instanceXriToNativeIdentifier(Multiplicity.baseArcXri((XRI3SubSegment) facebookDataXri.getSubSegment(1)));

		// done

		if (log.isDebugEnabled()) log.debug("Converted " + facebookDataXri + " to " + facebookFieldIdentifier);

		return facebookFieldIdentifier;
	}

	/**
	 * Maps and converts a Facebook data XRI to an XDI data XRI.
	 * Example: +(user)$!(+(first_name)) --> +first$!(+name)
	 */
	public XRI3Segment facebookDataXriToXdiDataXri(XRI3Segment facebookDataXri) {

		if (facebookDataXri == null) throw new NullPointerException();

		// map

		XRI3SubSegment facebookObjectXri = Dictionary.nativeIdentifierToInstanceXri(this.facebookDataXriToFacebookObjectIdentifier(facebookDataXri));
		XRI3SubSegment facebookFieldXri = Dictionary.nativeIdentifierToInstanceXri(this.facebookDataXriToFacebookFieldIdentifier(facebookDataXri));

		XRI3Segment facebookDataDictionaryXri = new XRI3Segment("" + XRI_S_FACEBOOK_CONTEXT + Dictionary.instanceXriToDictionaryXri(facebookObjectXri) + Dictionary.instanceXriToDictionaryXri(facebookFieldXri));
		ContextNode facebookDataDictionaryContextNode = this.mappingGraph.findContextNode(facebookDataDictionaryXri, false);
		if (facebookDataDictionaryContextNode == null) return null;

		ContextNode xdiDataDictionaryContextNode = Dictionary.getCanonicalContextNode(facebookDataDictionaryContextNode);
		XRI3Segment xdiDataDictionaryXri = xdiDataDictionaryContextNode.getXri();

		// convert

		StringBuilder buffer = new StringBuilder();

		for (int i=0; i<xdiDataDictionaryXri.getNumSubSegments(); i++) {

			if (i + 1 < xdiDataDictionaryXri.getNumSubSegments()) {

				buffer.append(Multiplicity.entitySingletonArcXri(Dictionary.dictionaryXriToInstanceXri((XRI3SubSegment) xdiDataDictionaryXri.getSubSegment(i))));
			} else {

				buffer.append(Multiplicity.attributeSingletonArcXri(Dictionary.dictionaryXriToInstanceXri((XRI3SubSegment) xdiDataDictionaryXri.getSubSegment(i))));
			}
		}

		XRI3Segment xdiDataXri = new XRI3Segment(buffer.toString());

		// done

		if (log.isDebugEnabled()) log.debug("Mapped and converted " + facebookDataXri + " to " + xdiDataXri);

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
}
