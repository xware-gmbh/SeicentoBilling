
package ch.xwr.seicentobilling.ui;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;


public class SeicentoNotification
{
	public static void showError(final String message)
	{
		final Div content = new Div();
		content.addClassName("my-style");
		content.setText("This component is styled using global styles");
		
		final Notification notification = new Notification(content);
		notification.setDuration(5000);
		notification.setPosition(Notification.Position.BOTTOM_END);
		
		// @formatter:off
		final String styles = ".my-style { "
		        + "  color: red;"
		        + " }";
		final StreamRegistration resource = UI.getCurrent().getSession()
	        .getResourceRegistry()
	        .registerResource(new StreamResource("error.css", () -> {
	            final byte[] bytes = styles.getBytes(StandardCharsets.UTF_8);
	            return new ByteArrayInputStream(bytes);
	        }));
		UI.getCurrent().getPage().addStyleSheet(
	        "base://" + resource.getResourceUri().toString());
		notification.open();
	}

	public static void showInfo(final String message)
	{
		final Notification notification = new Notification(message,5000,Notification.Position.BOTTOM_END);
		notification.open();
	}

	public static void showWarn(final String message)
	{
		final Notification notification = new Notification(message,5000,Notification.Position.BOTTOM_END);
		notification.open();
	}

	public static void showWarn(final String title, final String message)
	{
		final Notification notification = new Notification(title+":"+message,5000,Notification.Position.BOTTOM_END);
		notification.open();

	}

	public static void showErro(final String title, final String message)
	{
		SeicentoNotification.showError(title+":"+message);

	}
}
