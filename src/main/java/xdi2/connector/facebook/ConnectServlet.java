package xdi2.connector.facebook;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestHandler;

import xdi2.connector.facebook.api.FacebookApi;
import xdi2.connector.facebook.util.GraphUtil;
import xdi2.core.Graph;

public class ConnectServlet extends HttpServlet implements HttpRequestHandler {

	private static final long serialVersionUID = 4913215711424019239L;

	private static final Logger log = LoggerFactory.getLogger(ConnectServlet.class);

	private Graph graph;
	private FacebookApi facebookApi;

	public ConnectServlet() {

		this.graph = null;
		this.facebookApi = null;
	}

	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		this.service(request, response);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		log.debug("Incoming GET request to " + request.getRequestURL() + ". Content-Type: " + request.getContentType() + ", Content-Length: " + request.getContentLength());

		// redirect to Facebook?

		if (request.getParameter("startoauth") != null) {

			try {

				this.getFacebookApi().startOAuth(request, response);
				return;
			} catch (Exception ex) {

				request.setAttribute("error", ex.getMessage());
			}
		}

		// error from Facebook?

		if (request.getParameter("error") != null) {

			String errorDescription = request.getParameter("error_description");
			if (errorDescription == null) errorDescription = request.getParameter("error_reason");
			if (errorDescription == null) errorDescription = request.getParameter("error");

			request.setAttribute("error", errorDescription);
		}

		// callback from Facebook?

		if (request.getParameter("code") != null) {

			try {

				String accessToken = this.getFacebookApi().exchangeCodeForAccessToken(request);
				if (accessToken == null) throw new Exception("No Access Token received.");

				GraphUtil.storeAccessToken(this.getGraph(), accessToken);

				request.setAttribute("feedback", "Access Token successfully received and stored in graph.");
			} catch (Exception ex) {

				request.setAttribute("error", ex.getMessage());
			}
		}

		// display results

		request.setAttribute("writeContexts", null);
		request.setAttribute("writeOrdered", "on");
		request.setAttribute("input", MessageServlet.sampleInput);
		request.setAttribute("endpoint", request.getRequestURL().substring(0, request.getRequestURL().lastIndexOf("/")) + MessageServlet.sampleEndpoint);

		request.getRequestDispatcher("/FacebookConnector.jsp").forward(request, response);
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
}
