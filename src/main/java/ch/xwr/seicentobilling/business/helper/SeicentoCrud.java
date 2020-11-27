
package ch.xwr.seicentobilling.business.helper;

import javax.persistence.PersistenceException;

import com.rapidclipse.framework.server.jpa.dal.JpaDataAccessObject;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.binder.Binder;


public class SeicentoCrud
{
	public static void validateField(final Binder<?> fieldGroup)
	{

		try
		{
			fieldGroup.validate();
		}
		catch(final Exception e)
		{
			Notification.show("Fehler beim Speichern", 5000, Notification.Position.BOTTOM_START);
		}
		
	}

	/*
	 * public static void validateField(final XdevFieldGroup<?> fieldGroup) {
	 * final Collection<Field<?>> c2 = fieldGroup.getFields();
	 * for (final Iterator<Field<?>> iterator = c2.iterator(); iterator.hasNext();) {
	 * final Field<?> object = iterator.next();
	 * final String name = (String) fieldGroup.getPropertyId(object);
	 * try {
	 * object.validate();
	 * } catch (final InvalidValueException e) {
	 * Notification.show("Ungültiger Wert in Feld " + name, "Message" + e.getMessage(),
	 * Notification.Type.ERROR_MESSAGE);
	 * } catch (final Exception e) {
	 * Notification.show("Fehler beim Speichern " + object.getCaption(), e.getMessage(),
	 * Notification.Type.ERROR_MESSAGE);
	 * }
	 * }
	 * }
	 *
	 * public static boolean doSave(final XdevFieldGroup<?> fieldGroup) {
	 *
	 * try {
	 * fieldGroup.save();
	 *
	 * Notification.show("Save clicked", "Daten wurden gespeichert", Notification.Type.TRAY_NOTIFICATION);
	 *
	 * return true;
	 * } catch (final PersistenceException cx) {
	 * final String msg = getPerExceptionError(cx);
	 * Notification.show("Fehler beim Speichern", msg, Notification.Type.ERROR_MESSAGE);
	 * cx.printStackTrace();
	 * } catch (final Exception e) {
	 * Notification.show("Fehler beim Speichern", e.getMessage(), Notification.Type.ERROR_MESSAGE);
	 * e.printStackTrace();
	 * }
	 *
	 * return false;
	 * }
	 */
	public static boolean doSave(final Binder<?> fieldGroup, final JpaDataAccessObject dao)
	{
		
		try
		{
			if(fieldGroup.isValid())
			{
				dao.save(fieldGroup.getBean());
				// fieldGroup.save();
				Notification.show("Daten wurden gespeichert", 5000, Notification.Position.BOTTOM_END);

				return true;
			}
		}
		catch(final PersistenceException cx)
		{
			final String msg = SeicentoCrud.getPerExceptionError(cx);
			Notification.show(msg, 5000, Notification.Position.BOTTOM_START);
			cx.printStackTrace();
		}
		catch(final Exception e)
		{
			Notification.show(e.getMessage(), 5000, Notification.Position.BOTTOM_START);
			e.printStackTrace();
		}
		
		return false;
	}

	public static String getPerExceptionError(final PersistenceException cx)
	{
		String msg = cx.getMessage();
		if(cx.getCause() != null)
		{
			msg = cx.getCause().getMessage();
			if(cx.getCause().getCause() != null)
			{
				msg = cx.getCause().getCause().getMessage();
			}
		}
		return msg;
	}
}
