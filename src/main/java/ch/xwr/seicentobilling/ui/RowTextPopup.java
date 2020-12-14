
package ch.xwr.seicentobilling.ui;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flowingcode.vaadin.addons.ironicons.IronIcons;
import com.rapidclipse.framework.server.data.converter.ConverterBuilder;
import com.rapidclipse.framework.server.data.format.NumberFormatBuilder;
import com.rapidclipse.framework.server.resources.CaptionUtils;
import com.rapidclipse.framework.server.resources.StringResourceUtils;
import com.rapidclipse.framework.server.ui.ItemLabelGeneratorFactory;
import com.rapidclipse.framework.server.ui.StartsWithIgnoreCaseItemFilter;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.DataProvider;

import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.State;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.LanguageDAO;
import ch.xwr.seicentobilling.dal.RowObjectDAO;
import ch.xwr.seicentobilling.dal.RowTextDAO;
import ch.xwr.seicentobilling.entities.Entity;
import ch.xwr.seicentobilling.entities.Language;
import ch.xwr.seicentobilling.entities.RowObject;
import ch.xwr.seicentobilling.entities.RowText;


public class RowTextPopup extends VerticalLayout
{
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(RowTextPopup.class);

	/**
	 *
	 */
	public RowTextPopup()
	{
		super();
		this.initUI();
		
		// this.setHeight(Seicento.calculateThemeHeight(Float.parseFloat(this.getHeight()), Lumo.DARK));

		// State
		this.comboBoxState.setItems(LovState.State.values());

		// get Parameter
		final Long beanId = (Long)UI.getCurrent().getSession().getAttribute("beanId");
		final Long objId  = (Long)UI.getCurrent().getSession().getAttribute("objId");
		RowText    bean   = null;
		RowObject  obj    = null;

		if(beanId == null)
		{
			// new
			final RowObjectDAO objDao = new RowObjectDAO();
			obj = objDao.find(objId);

			final List<Language> llst = new LanguageDAO().findAll();
			Language             lng  = null;
			if(llst.size() > 0)
			{
				lng = llst.get(0);
			}

			bean = new RowText();
			bean.setTxtState(LovState.State.active);
			bean.setTxtNumber(0);
			bean.setTxtFreetext("");
			bean.setLanguage(lng);
			bean.setRowObject(obj);

		}
		else
		{
			final RowTextDAO dao = new RowTextDAO();
			bean = dao.find(beanId.longValue());
		}

		this.binder.setBean(bean);

		this.comboBoxObject.setEnabled(false);
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
		win.add(cancelButton, new RowTextPopup());
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
		if(SeicentoCrud.doSave(this.binder, new RowTextDAO()))
		{
			try
			{

				final RowObjectManager man = new RowObjectManager();
				man.updateObject(this.binder.getBean().getTxtId(),
					this.binder.getBean().getClass().getSimpleName());
				
				UI.getCurrent().getSession().setAttribute(String.class, "cmdSave");
				UI.getCurrent().getSession().setAttribute("beanId",
					this.binder.getBean().getTxtId());
				
				((Dialog)this.getParent().get()).close();
				// Notification.show("Daten wurden gespeichert", 5000, Notification.Position.BOTTOM_END);
			}
			catch(final Exception e)
			{
				RowTextPopup.LOG.error("could not save ObjRoot", e);
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
		this.formItem4         = new FormItem();
		this.lblVat2           = new Label();
		this.comboBoxObject    = new ComboBox<>();
		this.formItem          = new FormItem();
		this.label2            = new Label();
		this.textField         = new TextField();
		this.formItem2         = new FormItem();
		this.label3            = new Label();
		this.textArea          = new TextArea();
		this.formItem3         = new FormItem();
		this.label4            = new Label();
		this.comboBox2         = new ComboBox<>();
		this.formItem5         = new FormItem();
		this.label5            = new Label();
		this.comboBoxState     = new ComboBox<>();
		this.horizontalLayout2 = new HorizontalLayout();
		this.cmdSave           = new Button();
		this.cmdReset          = new Button();
		this.binder            = new BeanValidationBinder<>(RowText.class);

		this.verticalLayout.setPadding(false);
		this.formLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("500px", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE),
			new FormLayout.ResponsiveStep("1000px", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE));
		this.lblVat2.setText(StringResourceUtils.optLocalizeString("{$label.value}", this));
		this.comboBoxObject.setEnabled(false);
		this.comboBoxObject.setDataProvider(
			StartsWithIgnoreCaseItemFilter.New(this.comboBoxObject::getItemLabelGenerator),
			DataProvider.ofCollection(new RowObjectDAO().findAll()));
		this.comboBoxObject.setItemLabelGenerator(ItemLabelGeneratorFactory
			.NonNull(v -> Optional.ofNullable(v).map(RowObject::getEntity).map(Entity::getEntName).orElse(null)));
		this.label2.setText(StringResourceUtils.optLocalizeString("{$label2.value}", this));
		this.label3.setText(StringResourceUtils.optLocalizeString("{$label3.value}", this));
		this.label4.setText(StringResourceUtils.optLocalizeString("{$label4.value}", this));
		this.comboBox2.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.comboBox2::getItemLabelGenerator),
			DataProvider.ofCollection(new LanguageDAO().findAll()));
		this.comboBox2.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Language::getLngName));
		this.label5.setText(StringResourceUtils.optLocalizeString("{$label5.value}", this));
		this.comboBoxState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.horizontalLayout2.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		this.horizontalLayout2.setAlignItems(FlexComponent.Alignment.CENTER);
		this.cmdSave.setText("Speichern");
		this.cmdSave.setIcon(IronIcons.SAVE.create());
		this.cmdReset.setText("Schliessen");
		this.cmdReset.setIcon(IronIcons.CANCEL.create());

		this.binder.forField(this.comboBoxObject).bind("rowObject");
		this.binder.forField(this.textField).withNullRepresentation("")
			.withConverter(
				ConverterBuilder.StringToInteger().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind("txtNumber");
		this.binder.forField(this.textArea).withNullRepresentation("").bind("txtFreetext");
		this.binder.forField(this.comboBox2).bind("language");
		this.binder.forField(this.comboBoxState).bind("txtState");

		this.lblVat2.setSizeUndefined();
		this.lblVat2.getElement().setAttribute("slot", "label");
		this.comboBoxObject.setWidth("400px");
		this.comboBoxObject.setHeight(null);
		this.formItem4.add(this.lblVat2, this.comboBoxObject);
		this.label2.setSizeUndefined();
		this.label2.getElement().setAttribute("slot", "label");
		this.textField.setWidth("250px");
		this.textField.setHeight(null);
		this.formItem.add(this.label2, this.textField);
		this.label3.setSizeUndefined();
		this.label3.getElement().setAttribute("slot", "label");
		this.textArea.setWidth("400px");
		this.textArea.setHeight(null);
		this.formItem2.add(this.label3, this.textArea);
		this.label4.setSizeUndefined();
		this.label4.getElement().setAttribute("slot", "label");
		this.comboBox2.setWidth("250px");
		this.comboBox2.setHeight(null);
		this.formItem3.add(this.label4, this.comboBox2);
		this.label5.setSizeUndefined();
		this.label5.getElement().setAttribute("slot", "label");
		this.comboBoxState.setWidth("250px");
		this.comboBoxState.setHeight(null);
		this.formItem5.add(this.label5, this.comboBoxState);
		this.formLayout.add(this.formItem4, this.formItem, this.formItem2, this.formItem3, this.formItem5);
		this.cmdSave.setWidth("125px");
		this.cmdSave.setHeight(null);
		this.cmdReset.setWidth("135px");
		this.cmdReset.setHeight(null);
		this.horizontalLayout2.add(this.cmdSave, this.cmdReset);
		this.horizontalLayout2.setVerticalComponentAlignment(FlexComponent.Alignment.STRETCH, this.cmdSave);
		this.horizontalLayout2.setVerticalComponentAlignment(FlexComponent.Alignment.STRETCH, this.cmdReset);
		this.horizontalLayout.setWidthFull();
		this.horizontalLayout.setHeight("30px");
		this.formLayout.setWidthFull();
		this.formLayout.setHeight("250px");
		this.horizontalLayout2.setWidthFull();
		this.horizontalLayout2.setHeight("12%");
		this.verticalLayout.add(this.horizontalLayout, this.formLayout, this.horizontalLayout2);
		this.verticalLayout.setWidthFull();
		this.verticalLayout.setHeight("450px");
		this.add(this.verticalLayout);
		this.setWidth("700px");
		this.setHeight("400px");

		this.cmdSave.addClickListener(this::cmdSave_onClick);
		this.cmdReset.addClickListener(this::cmdReset_onClick);
	} // </generated-code>
	
	// <generated-code name="variables">
	private FormLayout                    formLayout;
	private ComboBox<Language>            comboBox2;
	private Button                        cmdSave, cmdReset;
	private ComboBox<State>               comboBoxState;
	private TextArea                      textArea;
	private VerticalLayout                verticalLayout;
	private HorizontalLayout              horizontalLayout, horizontalLayout2;
	private Label                         lblVat2, label2, label3, label4, label5;
	private ComboBox<RowObject>           comboBoxObject;
	private BeanValidationBinder<RowText> binder;
	private TextField                     textField;
	private FormItem                      formItem4, formItem, formItem2, formItem3, formItem5;
	// </generated-code>
	
}
