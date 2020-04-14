package ch.xwr.seicentobilling.business.helper;

import java.util.Collection;
import java.util.Iterator;

import javax.persistence.PersistenceException;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Field;
import com.vaadin.ui.Notification;
import com.xdev.ui.XdevFieldGroup;

public class SeicentoCrud {

	public static void validateField(final XdevFieldGroup<?> fieldGroup) {
		final Collection<Field<?>> c2 = fieldGroup.getFields();
		for (final Iterator<Field<?>> iterator = c2.iterator(); iterator.hasNext();) {
			final Field<?> object = iterator.next();
			final String name = (String) fieldGroup.getPropertyId(object);
			try {
				object.validate();
			} catch (final InvalidValueException e) {
				Notification.show("Ungültiger Wert in Feld " + name, "Message" + e.getMessage(), Notification.Type.ERROR_MESSAGE);
			} catch (final Exception e) {
				Notification.show("Fehler beim Speichern " + object.getCaption(), e.getMessage(), Notification.Type.ERROR_MESSAGE);
			}
		}
	}

	public static boolean doSave(final XdevFieldGroup<?> fieldGroup) {

		try {
			fieldGroup.save();

			Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);

			return true;
		} catch (final PersistenceException cx) {
			final String msg = getPerExceptionError(cx);
			Notification.show("Fehler beim Speichern", msg, Notification.Type.ERROR_MESSAGE);
			cx.printStackTrace();
		} catch (final Exception e) {
			Notification.show("Fehler beim Speichern", e.getMessage(), Notification.Type.ERROR_MESSAGE);
			e.printStackTrace();
		}

		return false;
	}

	public static String getPerExceptionError(final PersistenceException cx ) {
		String msg = cx.getMessage();
		if (cx.getCause() != null) {
			msg = cx.getCause().getMessage();
			if (cx.getCause().getCause() != null) {
				msg = cx.getCause().getCause().getMessage();
			}
		}
		return msg;
	}
}
