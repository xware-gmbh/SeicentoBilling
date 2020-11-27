
package ch.xwr.seicentobilling;

import com.rapidclipse.framework.server.RapServletService;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinSession;


/**
 * Servlet error handler, prints stacktraces by default. 
 */
public class LogExtension implements RapServletService.Extension
{
	@Override
	public void sessionCreated(
		final RapServletService service,
		final VaadinSession session,
		final VaadinRequest request)
	{
		session.setErrorHandler(event -> {
			
			event.getThrowable().printStackTrace();
			
		});
	}
}
