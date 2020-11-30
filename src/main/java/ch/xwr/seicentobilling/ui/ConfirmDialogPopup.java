
package ch.xwr.seicentobilling.ui;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;


public class ConfirmDialogPopup extends VerticalLayout
{
	
	public interface OkEvent
	{
		void execute(ComponentEvent<Button> clickEvent);
	}

	/**
	 *
	 */
	public ConfirmDialogPopup()
	{
		super();
		this.initUI();

	}

	private static final long serialVersionUID = 1L;
	
	public void addOkListener(final OkEvent ee)
	{
		this.cmdOk.addClickListener(e -> {
			ee.execute(e);
			((Dialog)this.getParent().get()).close();
		});
	}

	public static Dialog show(final String caption, final String customConfirmMsg, final OkEvent ee)
	{
		final Dialog win = new Dialog();
		win.setWidth("350");
		win.setHeight("175");
		win.setModal(true);
		win.setResizable(true);
		// final Button cancelButton = new Button("", e -> {
		// win.close();
		// });
		// cancelButton.setIcon(VaadinIcon.CLOSE.create());
		// cancelButton.getStyle().set("float", "right");
		// win.add(cancelButton, new ConfirmDialogPopup(caption, customConfirmMsg, ee));
		win.add(new ConfirmDialogPopup(caption, customConfirmMsg, ee));
		win.open();
		return win;
		
	}
	
	public ConfirmDialogPopup(final String caption, final String customConfirmMsg, final OkEvent ee)
	{
		this();
		if(ee != null)
		{
			this.addOkListener(ee);
		}
		this.caption.setText(caption);
		this.textMessage.setText(customConfirmMsg);
		
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdOk}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdOk_onClick(final ClickEvent<Button> event)
	{
		((Dialog)this.getParent().get()).close();
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdCancel}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdCancel_onClick(final ClickEvent<Button> event)
	{
		((Dialog)this.getParent().get()).close();
	}

	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.verticalLayout    = new VerticalLayout();
		this.div               = new Div();
		this.horizontalLayout  = new HorizontalLayout();
		this.caption           = new Label();
		this.horizontalLayout2 = new HorizontalLayout();
		this.horizontalLayout5 = new HorizontalLayout();
		this.image             = new Image();
		this.textMessage       = new Label();
		this.horizontalLayout4 = new HorizontalLayout();
		this.horizontalLayout3 = new HorizontalLayout();
		this.cmdOk             = new Button();
		this.cmdCancel         = new Button();

		this.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		this.setPadding(false);
		this.setAlignItems(FlexComponent.Alignment.START);
		this.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
		this.verticalLayout.setSpacing(false);
		this.verticalLayout.setPadding(false);
		this.verticalLayout.getStyle().set("background-color", "hsla(214, 40%, 16%, 0.94)");
		this.div.getStyle().set("background-color", "white");
		this.horizontalLayout.setSpacing(false);
		this.caption.setText("Confirm");
		this.caption.getStyle().set("color", "white");
		this.caption.getStyle().set("background-color", "hsl(214, 35%, 15%)");
		this.horizontalLayout2.setBoxSizing(BoxSizing.CONTENT_BOX);
		this.horizontalLayout2.setAlignItems(FlexComponent.Alignment.CENTER);
		this.horizontalLayout5.setAlignItems(FlexComponent.Alignment.START);
		this.horizontalLayout5.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
		this.image.setSrc("images/questionmark2.jpg");
		this.textMessage.setText("Are you Sure?");
		this.textMessage.getStyle().set("color", "white");
		this.horizontalLayout3.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		this.horizontalLayout3.setAlignItems(FlexComponent.Alignment.CENTER);
		this.cmdOk.setText("Ok");
		this.cmdCancel.setText("Abbrechen");

		this.caption.setWidthFull();
		this.caption.setHeight(null);
		this.horizontalLayout.add(this.caption);
		this.horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.STRETCH, this.caption);
		this.horizontalLayout.setFlexGrow(1.0, this.caption);
		this.horizontalLayout.setSizeFull();
		this.div.add(this.horizontalLayout);
		this.image.setWidth("30px");
		this.image.setHeight(null);
		this.textMessage.setSizeUndefined();
		this.horizontalLayout5.add(this.image, this.textMessage);
		this.cmdOk.setSizeUndefined();
		this.cmdCancel.setSizeUndefined();
		this.horizontalLayout3.add(this.cmdOk, this.cmdCancel);
		this.div.setWidthFull();
		this.div.setHeight("30px");
		this.horizontalLayout2.setWidthFull();
		this.horizontalLayout2.setHeight("50%");
		this.horizontalLayout5.setWidthFull();
		this.horizontalLayout5.setHeight("50px");
		this.horizontalLayout4.setWidthFull();
		this.horizontalLayout4.setHeight("60px");
		this.horizontalLayout3.setWidthFull();
		this.horizontalLayout3.setHeight("110px");
		this.verticalLayout.add(this.div, this.horizontalLayout2, this.horizontalLayout5, this.horizontalLayout4,
			this.horizontalLayout3);
		this.verticalLayout.setWidth("350px");
		this.verticalLayout.setHeight("175px");
		this.add(this.verticalLayout);
		this.setWidth(null);
		this.setHeightFull();

		this.cmdOk.addClickListener(this::cmdOk_onClick);
		this.cmdCancel.addClickListener(this::cmdCancel_onClick);
	} // </generated-code>
	
	// <generated-code name="variables">
	private Button           cmdOk, cmdCancel;
	private Image            image;
	private VerticalLayout   verticalLayout;
	private HorizontalLayout horizontalLayout, horizontalLayout2, horizontalLayout5, horizontalLayout4,
		horizontalLayout3;
	private Div              div;
	private Label            caption, textMessage;
	// </generated-code>
	
}
