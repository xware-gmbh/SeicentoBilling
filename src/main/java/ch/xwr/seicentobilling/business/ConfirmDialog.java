
package ch.xwr.seicentobilling.business;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;

import ch.xwr.seicentobilling.ui.ConfirmDialogPopup;
import ch.xwr.seicentobilling.ui.ConfirmDialogPopup.OkEvent;


public class ConfirmDialog extends Dialog
{
	
	public static void show(final String caption, final String customConfirmMsg, final OkEvent ee)
	{
		ConfirmDialogPopup.show(caption, customConfirmMsg, ee);

	}
	
	public static void show(final UI ui, final String caption, final String message)
	{
		ConfirmDialogPopup.show(caption, message, null);

	}

}
