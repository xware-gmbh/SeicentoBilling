
package ch.xwr.seicentobilling.ui;

import javax.servlet.annotation.WebServlet;

import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.UIClassSelectionEvent;
import com.vaadin.server.UIProvider;
import com.vaadin.ui.UI;
import com.xdev.communication.ClientInfo;
import com.xdev.communication.XdevServlet;

import ch.xwr.seicentobilling.ui.desktop.DesktopUI;
import ch.xwr.seicentobilling.ui.phone.PhoneUI;

@WebServlet(value = "/*", asyncSupported = true)
public class Servlet extends XdevServlet {
	public Servlet() {
		super();
		// System.out.println("************** Servlet constructor");
	}

	@Override
	protected void initSession(final SessionInitEvent event) {
		super.initSession(event);

		event.getSession().addUIProvider(new ServletUIProvider());

		// final String value = System.getenv("APP_STAGE");
		// System.out.println("************** initSession: " + value);
	}

//    @Override
//    public void init(final javax.servlet.ServletConfig servletConfig) throws ServletException {
//		URL.setURLStreamHandlerFactory(new TelHandlerFactory());
//        super.init(servletConfig);
//    }

	/**
	 * UIProvider which provides different UIs depending on the caller's device.
	 */
	private static class ServletUIProvider extends UIProvider {
		@Override
		public Class<? extends UI> getUIClass(final UIClassSelectionEvent event) {
			final ClientInfo client = ClientInfo.getCurrent();
			if (client != null) {
				if (client.isMobile()) {
					return PhoneUI.class;
				}
				if (client.isTablet()) {
					//return TabletUI.class;
					return DesktopUI.class;
				}
			}
			return DesktopUI.class;
		}
	}
}