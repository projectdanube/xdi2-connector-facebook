package xdi2.tests.connector.facebook.mapping;

import junit.framework.TestCase;
import xdi2.connector.facebook.mapping.FacebookMapping;
import xdi2.core.Graph;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.xri3.XDI3Segment;

public class FacebookMappingTest extends TestCase {

	private Graph mappingGraph;
	private FacebookMapping facebookMapping;

	@Override
	protected void setUp() throws Exception {

		this.mappingGraph = MemoryGraphFactory.getInstance().loadGraph(FacebookMapping.class.getResourceAsStream("mapping.xdi"));
		this.facebookMapping = new FacebookMapping();
		this.facebookMapping.setMappingGraph(this.mappingGraph);
	}

	@Override
	protected void tearDown() throws Exception {

		this.mappingGraph.close();
	}

	public void testMapping() throws Exception {

		XDI3Segment facebookDataXri = XDI3Segment.create("+(user)<+(first_name)>");
		XDI3Segment xdiDataXri = XDI3Segment.create("+first<+name>");

		assertEquals("user", this.facebookMapping.facebookDataXriToFacebookObjectIdentifier(facebookDataXri));
		assertEquals("first_name", this.facebookMapping.facebookDataXriToFacebookFieldIdentifier(facebookDataXri));
	
		assertEquals(xdiDataXri, this.facebookMapping.facebookDataXriToXdiDataXri(facebookDataXri));
		assertEquals(facebookDataXri, this.facebookMapping.xdiDataXriToFacebookDataXri(xdiDataXri));
	}
}
