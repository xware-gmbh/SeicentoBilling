
package ch.xwr.seicentobilling.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flowingcode.vaadin.addons.ironicons.IronIcons;
import com.rapidclipse.framework.server.data.converter.ConverterBuilder;
import com.rapidclipse.framework.server.data.format.NumberFormatBuilder;
import com.rapidclipse.framework.server.resources.CaptionUtils;
import com.rapidclipse.framework.server.resources.StringResourceUtils;
import com.rapidclipse.framework.server.ui.ItemLabelGeneratorFactory;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.State;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.UploadReceiver;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.RowImageDAO;
import ch.xwr.seicentobilling.dal.RowObjectDAO;
import ch.xwr.seicentobilling.entities.RowImage;
import ch.xwr.seicentobilling.entities.RowObject;


public class RowFilePopup extends VerticalLayout
{
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(RowFilePopup.class);
	
	/**
	 *
	 */
	public RowFilePopup()
	{
		super();
		this.initUI();

		// this.setHeight(Seicento.calculateThemeHeight(Float.parseFloat(this.getHeight()), Lumo.DARK));
		
		// State
		this.comboBoxState.setItems(LovState.State.values());
		
		// get Parameter
		final Long beanId = (Long)UI.getCurrent().getSession().getAttribute("beanId");
		final Long objId  = (Long)UI.getCurrent().getSession().getAttribute("objId");
		RowImage   bean   = null;
		RowObject  obj    = null;
		
		if(beanId == null)
		{
			// new
			final RowObjectDAO objDao = new RowObjectDAO();
			obj = objDao.find(objId);
			
			bean = new RowImage();
			bean.setRimState(LovState.State.active);
			bean.setRowObject(obj);
			
		}
		else
		{
			final RowImageDAO dao = new RowImageDAO();
			bean = dao.find(beanId.longValue());
		}
		
		this.setBeanGui(bean);
		this.setupUploader(bean);
	}
	
	private void setupUploader(final RowImage bean)
	{
		// uploader
		final UploadReceiver rec = new UploadReceiver(bean);
		// upload.setImmediate(true);
		// upload.setButtonCaption("Upload File");
		
		this.upload.setReceiver(rec);
		
		this.upload.addSucceededListener(event -> {
			
			// This method gets called when the upload finished successfully
			System.out.println("________________ UPLOAD SUCCEEDED y");
			
			rec.uploadSucceeded(event);
			RowFilePopup.this.setBeanGui(rec.getBean());

		});
	}

	private void setBeanGui(final RowImage bean)
	{
		// set Bean + Fields
		this.binder.setBean(bean);

		// this.comboBoxObject.setEnabled(false);
		this.textFieldSize.setEnabled(false);
		this.textFieldName.setEnabled(false);
		this.textFieldMime.setEnabled(false);
	}

	public static Dialog getPopupWindow()
	{
		final Dialog win = new Dialog();
		win.setModal(true);
		win.setResizable(true);
		final Button cancelButton = new Button("", e -> {
			win.close();
		});
		cancelButton.setIcon(VaadinIcon.CLOSE.create());
		cancelButton.getStyle().set("float", "right");
		win.add(cancelButton, new RowFilePopup());
		return win;
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdSave}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdSave_onClick(final ClickEvent<Button> event)
	{
		if(SeicentoCrud.doSave(this.binder, new RowImageDAO()))
		{
			try
			{
				
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.binder.getBean().getRimId(),
					this.binder.getBean().getClass().getSimpleName());

				UI.getCurrent().getSession().setAttribute(String.class, "cmdSave");
				UI.getCurrent().getSession().setAttribute("beanId",
					this.binder.getBean().getRimId());

				((Dialog)this.getParent().get()).close();
				// Notification.show("Daten wurden gespeichert", 5000, Notification.Position.BOTTOM_END);
			}
			catch(final Exception e)
			{
				RowFilePopup.LOG.error("could not save ObjRoot", e);
			}
		}
		
	}

	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdReset}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdReset_onClick(final ClickEvent<Button> event)
	{
		UI.getCurrent().getSession().setAttribute(String.class, "cmdCancel");
		this.binder.removeBean();
		((Dialog)this.getParent().get()).close();
	}
	
	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.verticalLayout    = new VerticalLayout();
		this.horizontalLayout  = new HorizontalLayout();
		this.formLayout        = new FormLayout();
		this.formItem          = new FormItem();
		this.labelNbr          = new Label();
		this.textField         = new TextField();
		this.formItem2         = new FormItem();
		this.labelFile         = new Label();
		this.textFieldName     = new TextField();
		this.upload            = new Upload();
		this.formItem3         = new FormItem();
		this.lblMimeType       = new Label();
		this.textFieldMime     = new TextField();
		this.formItem5         = new FormItem();
		this.labelType         = new Label();
		this.textField4        = new TextField();
		this.formItem4         = new FormItem();
		this.labelSize         = new Label();
		this.textFieldSize     = new TextField();
		this.formItem6         = new FormItem();
		this.comboBoxState     = new ComboBox<>();
		this.labelState        = new Label();
		this.horizontalLayout2 = new HorizontalLayout();
		this.cmdSave           = new Button();
		this.cmdReset          = new Button();
		this.binder            = new BeanValidationBinder<>(RowImage.class);
		
		this.verticalLayout.setPadding(false);
		this.formLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("500px", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE),
			new FormLayout.ResponsiveStep("1000px", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.labelNbr.setText(StringResourceUtils.optLocalizeString("{$labelNbr.value}", this));
		this.labelFile.setText(StringResourceUtils.optLocalizeString("{$labelFile.value}", this));
		this.lblMimeType.setText(StringResourceUtils.optLocalizeString("{$lblMimeType.value}", this));
		this.labelType.setText(StringResourceUtils.optLocalizeString("{$labelType.value}", this));
		this.labelSize.setText(StringResourceUtils.optLocalizeString("{$labelSize.value}", this));
		this.comboBoxState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.labelState.setText(StringResourceUtils.optLocalizeString("{$labelState.value}", this));
		this.horizontalLayout2.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		this.horizontalLayout2.setAlignItems(FlexComponent.Alignment.CENTER);
		this.cmdSave.setText("Speichern");
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdReset.setText("Schliessen");
		this.cmdReset.setIcon(IronIcons.CANCEL.create());
		
		this.binder.forField(this.textField).withNullRepresentation("")
			.withConverter(
				ConverterBuilder.StringToInteger().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind("rimNumber");
		this.binder.forField(this.textFieldName).withNullRepresentation("").bind("rimName");
		this.binder.forField(this.textFieldMime).withNullRepresentation("").bind("rimMimetype");
		this.binder.forField(this.textField4).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToShort().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind("rimType");
		this.binder.forField(this.textFieldSize).withNullRepresentation("").bind("rimSize");
		this.binder.forField(this.comboBoxState).bind("rimState");
		
		this.labelNbr.setSizeUndefined();
		this.labelNbr.getElement().setAttribute("slot", "label");
		this.textField.setWidth("50%");
		this.textField.setHeight(null);
		this.formItem.add(this.labelNbr, this.textField);
		this.labelFile.setSizeUndefined();
		this.labelFile.getElement().setAttribute("slot", "label");
		this.textFieldName.setWidth("80%");
		this.textFieldName.setHeight(null);
		this.upload.setSizeUndefined();
		this.formItem2.add(this.labelFile, this.textFieldName, this.upload);
		this.lblMimeType.setSizeUndefined();
		this.lblMimeType.getElement().setAttribute("slot", "label");
		this.textFieldMime.setWidth("80%");
		this.textFieldMime.setHeight(null);
		this.formItem3.add(this.lblMimeType, this.textFieldMime);
		this.labelType.setSizeUndefined();
		this.labelType.getElement().setAttribute("slot", "label");
		this.textField4.setWidth("20%");
		this.textField4.setHeight(null);
		this.formItem5.add(this.labelType, this.textField4);
		this.labelSize.setSizeUndefined();
		this.labelSize.getElement().setAttribute("slot", "label");
		this.textFieldSize.setWidth("80%");
		this.textFieldSize.setHeight(null);
		this.formItem4.add(this.labelSize, this.textFieldSize);
		this.comboBoxState.setWidth("80%");
		this.comboBoxState.setHeight(null);
		this.labelState.setSizeUndefined();
		this.labelState.getElement().setAttribute("slot", "label");
		this.formItem6.add(this.comboBoxState, this.labelState);
		this.formLayout.add(this.formItem, this.formItem2, this.formItem3, this.formItem5, this.formItem4,
			this.formItem6);
		this.cmdSave.setWidth("125px");
		this.cmdSave.setHeight(null);
		this.cmdReset.setWidth("135px");
		this.cmdReset.setHeight(null);
		this.horizontalLayout2.add(this.cmdSave, this.cmdReset);
		this.horizontalLayout2.setVerticalComponentAlignment(FlexComponent.Alignment.STRETCH, this.cmdSave);
		this.horizontalLayout2.setVerticalComponentAlignment(FlexComponent.Alignment.STRETCH, this.cmdReset);
		this.horizontalLayout.setWidthFull();
		this.horizontalLayout.setHeight("30px");
		this.formLayout.setSizeFull();
		this.horizontalLayout2.setWidthFull();
		this.horizontalLayout2.setHeight("12%");
		this.verticalLayout.add(this.horizontalLayout, this.formLayout, this.horizontalLayout2);
		this.verticalLayout.setSizeFull();
		this.add(this.verticalLayout);
		this.setWidth("700px");
		this.setHeight("440px");
		
		this.cmdSave.addClickListener(this::cmdSave_onClick);
		this.cmdReset.addClickListener(this::cmdReset_onClick);
	} // </generated-code>

	// <generated-code name="variables">
	private FormLayout                     formLayout;
	private Button                         cmdSave, cmdReset;
	private ComboBox<State>                comboBoxState;
	private BeanValidationBinder<RowImage> binder;
	private Upload                         upload;
	private VerticalLayout                 verticalLayout;
	private HorizontalLayout               horizontalLayout, horizontalLayout2;
	private Label                          labelNbr, labelFile, lblMimeType, labelType, labelSize, labelState;
	private TextField                      textField, textFieldName, textFieldMime, textField4, textFieldSize;
	private FormItem                       formItem, formItem2, formItem3, formItem5, formItem4, formItem6;
	// </generated-code>

}
