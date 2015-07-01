package xdi2.connector.facebook.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestHandler;

import xdi2.client.XDIClient;
import xdi2.client.exceptions.Xdi2ClientException;
import xdi2.client.impl.http.XDIHttpClient;
import xdi2.connector.facebook.api.FacebookApi;
import xdi2.connector.facebook.mapping.FacebookMapping;
import xdi2.connector.facebook.util.GraphUtil;
import xdi2.core.Graph;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.io.XDIReader;
import xdi2.core.io.XDIReaderRegistry;
import xdi2.core.io.XDIWriter;
import xdi2.core.io.XDIWriterRegistry;
import xdi2.core.io.writers.XDIDisplayWriter;
import xdi2.core.syntax.XDIAddress;
import xdi2.messaging.MessageEnvelope;
import xdi2.messaging.response.MessagingResponse;

public class ClientServlet extends HttpServlet implements HttpRequestHandler {

	private static final long serialVersionUID = 3793048689633131588L;

	private static final Logger log = LoggerFactory.getLogger(ClientServlet.class);

	private static MemoryGraphFactory graphFactory;
	static String sampleInput;
	static String sampleEndpoint;

	private Graph graph;
	private FacebookApi facebookApi;
	private FacebookMapping facebookMapping;

	static {

		graphFactory = MemoryGraphFactory.getInstance();
		graphFactory.setSortmode(MemoryGraphFactory.SORTMODE_ORDER);

		InputStream inputStream = ClientServlet.class.getResourceAsStream("message.xdi");
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		int i;

		try {

			while ((i = inputStream.read()) != -1) outputStream.write(i);
			sampleInput = new String(outputStream.toByteArray());
		} catch (Exception ex) {

		} finally {

			try {

				inputStream.close();
				outputStream.close();
			} catch (Exception ex) {

			}
		}

		sampleEndpoint = "/xdi/graph"; 
	}

	public ClientServlet() {

		this.graph = null;
		this.facebookApi = null;
		this.facebookMapping = null;
	}

	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		this.service(request, response);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		log.debug("Incoming GET request to " + request.getRequestURL() + ". Content-Type: " + request.getContentType() + ", Content-Length: " + request.getContentLength());

		// start OAuth?

		if ("Request Access Token!".equals(request.getParameter("submit"))) {

			XDIAddress userXri = XDIAddress.create(request.getParameter("userXri"));
			request.getSession().setAttribute("userXri", userXri);

			try {

				response.sendRedirect(this.getFacebookApi().startOAuth(request, null, userXri));
				return;
			} catch (Exception ex) {

				request.setAttribute("error", ex.getMessage());
			}
		}

		// revoke OAuth?

		if ("Revoke Access Token!".equals(request.getParameter("submit"))) {

			XDIAddress userXri = XDIAddress.create(request.getParameter("userXri"));
			request.getSession().setAttribute("userXri", userXri);

			try {

				XDIAddress facebookUserIdXri = GraphUtil.retrieveFacebookUserIdXri(this.getGraph(), userXri);
				if (facebookUserIdXri == null) throw new Exception("No user ID in graph.");

				System.err.println(facebookUserIdXri);
				String facebookAccessToken = GraphUtil.retrieveFacebookAccessToken(this.getGraph(), facebookUserIdXri);
				if (facebookAccessToken == null) throw new Exception("No access token in graph.");

				this.getFacebookApi().revokeAccessToken(facebookAccessToken);

				GraphUtil.removeFacebookAccessToken(this.getGraph(), facebookUserIdXri);
				GraphUtil.removeFacebookUserIdXri(this.getGraph(), userXri);

				request.setAttribute("feedback", "OAuth access token successfully revoked and removed from graph.");
			} catch (Exception ex) {

				request.setAttribute("error", ex.getMessage());
			}
		}

		// error from OAuth?

		if (request.getParameter("error") != null) {

			String errorDescription = request.getParameter("error_description");
			if (errorDescription == null) errorDescription = request.getParameter("error_reason");
			if (errorDescription == null) errorDescription = request.getParameter("error");

			request.setAttribute("error", "OAuth error: " + errorDescription);
		}

		// callback from OAuth?

		if (request.getParameter("code") != null) {

			XDIAddress userXri = (XDIAddress) request.getSession().getAttribute("userXri");

			try {

				this.getFacebookApi().checkState(request.getParameterMap(), userXri);

				String facebookAccessToken = this.getFacebookApi().exchangeCodeForAccessToken(request.getRequestURL().toString(), request.getParameterMap());
				if (facebookAccessToken == null) throw new Exception("No access token received.");

				String facebookUserId = this.getFacebookApi().retrieveUserId(facebookAccessToken);
				XDIAddress facebookUserIdXri = this.getFacebookMapping().facebookUserIdToFacebookUserIdXri(facebookUserId);

				GraphUtil.storeFacebookUserIdXri(this.getGraph(), userXri, facebookUserIdXri);
				GraphUtil.storeFacebookAccessToken(this.getGraph(), facebookUserIdXri, facebookAccessToken);

				request.setAttribute("feedback", "Access token successfully received and stored in graph.");
			} catch (Exception ex) {

				log.error(ex.getMessage(), ex);

				request.setAttribute("error", ex.getMessage());
			}
		}

		// display results

		request.setAttribute("resultFormat", XDIDisplayWriter.FORMAT_NAME);
		request.setAttribute("writeImplied", null);
		request.setAttribute("writeOrdered", "on");
		request.setAttribute("writePretty", null);
		request.setAttribute("input", sampleInput);
		request.setAttribute("endpoint", request.getRequestURL().substring(0, request.getRequestURL().lastIndexOf("/")) + sampleEndpoint);

		request.getRequestDispatcher("/Client.jsp").forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String resultFormat = request.getParameter("resultFormat");
		String writeImplied = request.getParameter("writeImplied");
		String writeOrdered = request.getParameter("writeOrdered");
		String writePretty = request.getParameter("writePretty");
		String input = request.getParameter("input");
		String endpoint = request.getParameter("endpoint");
		String output = "";
		String stats = "-1";
		String error = null;

		Properties xdiResultWriterParameters = new Properties();

		xdiResultWriterParameters.setProperty(XDIWriterRegistry.PARAMETER_IMPLIED, "on".equals(writeImplied) ? "1" : "0");
		xdiResultWriterParameters.setProperty(XDIWriterRegistry.PARAMETER_ORDERED, "on".equals(writeOrdered) ? "1" : "0");
		xdiResultWriterParameters.setProperty(XDIWriterRegistry.PARAMETER_PRETTY, "on".equals(writePretty) ? "1" : "0");

		XDIReader xdiReader = XDIReaderRegistry.getAuto();
		XDIWriter xdiResultWriter = XDIWriterRegistry.forFormat(resultFormat, xdiResultWriterParameters);

		MessageEnvelope messageEnvelope = null;
		MessagingResponse messagingResponse = null;

		long start = System.currentTimeMillis();

		try {

			// parse the message envelope

			messageEnvelope = new MessageEnvelope();

			xdiReader.read(messageEnvelope.getGraph(), new StringReader(input));

			// send the message envelope and read result

			XDIClient client = new XDIHttpClient(endpoint);

			messagingResponse = client.send(messageEnvelope);

			// output the message result

			StringWriter writer = new StringWriter();

			xdiResultWriter.write(messagingResponse.getGraph(), writer);

			output = StringEscapeUtils.escapeHtml(writer.getBuffer().toString());
		} catch (Exception ex) {

			if (ex instanceof Xdi2ClientException) {

				messagingResponse = ((Xdi2ClientException) ex).getMessagingResponse();

				// output the message result

				if (messagingResponse != null) {

					StringWriter writer2 = new StringWriter();
					xdiResultWriter.write(messagingResponse.getGraph(), writer2);
					output = StringEscapeUtils.escapeHtml(writer2.getBuffer().toString());
				}
			}

			log.error(ex.getMessage(), ex);
			error = ex.getMessage();
			if (error == null) error = ex.getClass().getName();
		}

		long stop = System.currentTimeMillis();

		stats = "";
		stats += Long.toString(stop - start) + " ms time. ";
		if (messageEnvelope != null) stats += Long.toString(messageEnvelope.getMessageCount()) + " message(s). ";
		if (messageEnvelope != null) stats += Long.toString(messageEnvelope.getOperationCount()) + " operation(s). ";
		if (messagingResponse != null) stats += Long.toString(messagingResponse.getGraph().getRootContextNode().getAllStatementCount()) + " result statement(s). ";

		// display results

		request.setAttribute("resultFormat", resultFormat);
		request.setAttribute("writeImplied", writeImplied);
		request.setAttribute("writeOrdered", writeOrdered);
		request.setAttribute("writePretty", writePretty);
		request.setAttribute("input", input);
		request.setAttribute("endpoint", endpoint);
		request.setAttribute("output", output);
		request.setAttribute("stats", stats);
		request.setAttribute("error", error);

		request.getRequestDispatcher("/Client.jsp").forward(request, response);
	}

	public Graph getGraph() {

		return this.graph;
	}

	public void setGraph(Graph graph) {

		this.graph = graph;
	}

	public FacebookApi getFacebookApi() {

		return this.facebookApi;
	}

	public void setFacebookApi(FacebookApi facebookApi) {

		this.facebookApi = facebookApi;
	}

	public FacebookMapping getFacebookMapping() {

		return this.facebookMapping;
	}

	public void setFacebookMapping(FacebookMapping facebookMapping) {

		this.facebookMapping = facebookMapping;
	}
}
