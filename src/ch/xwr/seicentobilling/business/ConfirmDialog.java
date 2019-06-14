package ch.xwr.seicentobilling.business;

import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseListener;

import ch.xwr.seicentobilling.ui.desktop.ConfirmDialogPopup;

public class ConfirmDialog {
    static final String DEFAULT_WINDOW_CAPTION = "Confirm";
    static final String DEFAULT_CANCEL_CAPTION = "Cancel";
    static final String DEFAULT_OK_CAPTION = "Ok";

//    public static final ContentMode CONTENT_TEXT_WITH_NEWLINES = ContentMode.TEXT; //-1;
//    public static final ContentMode CONTENT_TEXT = Label.CONTENT_TEXT;
//    public static final ContentMode CONTENT_PREFORMATTED = Label.CONTENT_PREFORMATTED;
//    public static final ContentMode CONTENT_HTML = Label.CONTENT_RAW;
//    public static final ContentMode CONTENT_DEFAULT = CONTENT_TEXT_WITH_NEWLINES;


    public static void show(final UI ui, final String title, final String text,  final CloseListener lst){

		final Window win = new Window();
		win.setWidth("480");
		win.setHeight("210");
		win.center();
		win.setModal(true);

		// UI.getCurrent().getSession().setAttribute(String.class,
		// bean.getClass().getSimpleName());
		win.setContent(new ConfirmDialogPopup(title, text, 0));

		win.addCloseListener(lst);
		ui.addWindow(win);
    }

}