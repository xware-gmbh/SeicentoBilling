
package ch.xwr.seicentobilling.ui;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.persistence.PersistenceException;

import com.flowingcode.vaadin.addons.ironicons.ImageIcons;
import com.rapidclipse.framework.server.data.converter.ConverterBuilder;
import com.rapidclipse.framework.server.data.format.NumberFormatBuilder;
import com.rapidclipse.framework.server.data.renderer.CaptionRenderer;
import com.rapidclipse.framework.server.resources.CaptionUtils;
import com.rapidclipse.framework.server.resources.StringResourceUtils;
import com.rapidclipse.framework.server.ui.ItemLabelGeneratorFactory;
import com.rapidclipse.framework.server.ui.StartsWithIgnoreCaseItemFilter;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.FormItem;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.ItemDoubleClickEvent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.DataProvider;

import ch.xwr.seicentobilling.business.ConfirmDialog;
import ch.xwr.seicentobilling.business.LovState;
import ch.xwr.seicentobilling.business.LovState.State;
import ch.xwr.seicentobilling.business.RowObjectManager;
import ch.xwr.seicentobilling.business.helper.SeicentoCrud;
import ch.xwr.seicentobilling.dal.EntityDAO;
import ch.xwr.seicentobilling.dal.RowImageDAO;
import ch.xwr.seicentobilling.dal.RowObjectDAO;
import ch.xwr.seicentobilling.dal.RowParameterDAO;
import ch.xwr.seicentobilling.dal.RowTextDAO;
import ch.xwr.seicentobilling.entities.Entity;
import ch.xwr.seicentobilling.entities.RowImage;
import ch.xwr.seicentobilling.entities.RowObject;
import ch.xwr.seicentobilling.entities.RowParameter;
import ch.xwr.seicentobilling.entities.RowText;


public class RowObjectView extends VerticalLayout
{
	RowObject selectedRowObj;

	/**
	 *
	 */
	public RowObjectView()
	{
		super();
		this.initUI();
		this.txtObjState.setItems(LovState.State.values());

		this.verticalLayoutFile.setVisible(false);
		this.verticalLayoutParam.setVisible(false);

		final Map<Tab, Component> tabsToPages = new HashMap<>();
		tabsToPages.put(this.tab, this.verticalLayoutText);
		tabsToPages.put(this.tab2, this.verticalLayoutFile);
		tabsToPages.put(this.tab3, this.verticalLayoutParam);
		
		this.tabs.addSelectedChangeListener(event -> {
			tabsToPages.values().forEach(page -> page.setVisible(false));
			final Component selectedPage = tabsToPages.get(this.tabs.getSelectedTab());
			selectedPage.setVisible(true);
		});
		
		this.tableRowImage
			.addComponentColumn(
				item -> new FunctionUpDownloadRowFile().createDownLoadButton(this.tableRowImage, item))
			.setHeader("Beleg");
	}
	
	/**
	 *
	 */
	public RowObjectView(final long rowid, final String entName)
	{
		this();

		this.loadUI(rowid, entName);
	}
	
	public static Dialog getPopupWindow()
	{
		final Dialog win = new Dialog();
		// win.setWidth("1140");
		// win.setHeight("760");
		win.setSizeFull();
		win.setModal(true);
		win.setResizable(true);
		final Button cancelButton = new Button("", e -> {
			win.close();
		});
		cancelButton.setIcon(VaadinIcon.CLOSE.create());
		cancelButton.getStyle().set("float", "right");
		win.add(cancelButton);
		return win;
	}
	
	private void loadUI(final long rowid, final String entName)
	{
		final RowObjectDAO objDao = new RowObjectDAO();
		this.selectedRowObj = objDao.getObjectBase(entName, rowid);

		if(this.selectedRowObj == null)
		{
			Notification.show("Kein Record gefunden!", 5000, Notification.Position.BOTTOM_END);
		}
		else
		{
			this.binder.setBean(this.selectedRowObj);
			// 2nd ObjText
			this.setTableRowText(this.selectedRowObj);
			this.setTableRowImage(this.selectedRowObj);
			this.setTableRowParam(this.selectedRowObj);
		}
		
	}

	private void setTableRowParam(final RowObject obj)
	{
		if(obj == null)
		{
			this.tableRowParam.setItems(new ArrayList<RowParameter>());
		}
		else
		{
			final RowParameterDAO rtp = new RowParameterDAO();
			this.tableRowParam.setDataProvider(DataProvider.ofCollection(rtp.findByObject(obj)));
		}
		this.tableRowParam.getDataProvider().refreshAll();

	}
	
	private void setTableRowText(final RowObject obj)
	{
		if(obj == null)
		{
			this.tableText.setItems(new ArrayList<RowText>());
		}
		else
		{
			final RowTextDAO rtd = new RowTextDAO();
			this.tableText.setDataProvider(DataProvider.ofCollection(rtd.findByObject(obj)));
		}
		this.tableText.getDataProvider().refreshAll();
	}
	
	private void setTableRowImage(final RowObject obj)
	{
		if(obj == null)
		{
			this.tableRowImage.setItems(new ArrayList<RowImage>());
		}
		else
		{
			final RowImageDAO rti = new RowImageDAO();
			this.tableRowImage.setDataProvider(DataProvider.ofCollection(rti.findByObject(obj)));
		}
		this.tableRowImage.getDataProvider().refreshAll();

	}

	private void popupRowText()
	{
		final Dialog win = RowTextPopup.getPopupWindow();

		win.addDetachListener(new ComponentEventListener<DetachEvent>()
		{
			
			@Override
			public void onComponentEvent(final DetachEvent event)
			{
				RowObjectView.this.setTableRowText(RowObjectView.this.binder.getBean());
				
			}
			
		});

		win.open();
		
	}
	
	private void popupRowImage()
	{
		final Dialog win = RowFilePopup.getPopupWindow();

		win.addDetachListener(new ComponentEventListener<DetachEvent>()
		{

			@Override
			public void onComponentEvent(final DetachEvent event)
			{
				RowObjectView.this.setTableRowImage(RowObjectView.this.binder.getBean());

			}

		});
		win.open();

	}

	private void popupRowParam()
	{
		final Dialog win = RowParamPopup.getPopupWindow();

		win.addDetachListener(new ComponentEventListener<DetachEvent>()
		{

			@Override
			public void onComponentEvent(final DetachEvent event)
			{
				RowObjectView.this.setTableRowParam(RowObjectView.this.binder.getBean());

			}

		});
		win.open();

	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdNewText}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNewText_onClick(final ClickEvent<Button> event)
	{
		final Long beanId = null;
		final Long objId  = this.binder.getBean().getObjId();

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		this.popupRowText();
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdUpdateText}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdUpdateText_onClick(final ClickEvent<Button> event)
	{
		if(!this.tableText.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			return;
		}

		final Long beanId = this.tableText.getSelectionModel().getFirstSelectedItem().get().getTxtId();
		final Long objId  = null;

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		this.popupRowText();
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdDeleteText}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDeleteText_onClick(final ClickEvent<Button> event)
	{
		if(this.tableText.getSelectedItems() == null)
		{
			com.vaadin.flow.component.notification.Notification.show("Es wurde keine Zeile selektiert in der Tabelle",
				20, Notification.Position.BOTTOM_START);
			return;
		}

		ConfirmDialog.show("Datensatz löschen", "Wirklich löschen?", okEvent -> {
			
			try
			{
				
				final RowText bean = this.tableText.getSelectionModel().getFirstSelectedItem().get();
				
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getTxtId(), bean.getClass().getSimpleName());
				
				final RowTextDAO dao = new RowTextDAO();
				dao.remove(bean);
				dao.flush();
				
				this.setTableRowText(this.binder.getBean());
				
				Notification.show("Datensatz wurde gelöscht!",
					20, Notification.Position.BOTTOM_START);
				
			}
			catch(final PersistenceException cx)
			{
				final String msg = SeicentoCrud.getPerExceptionError(cx);
				Notification.show(msg, 20, Notification.Position.BOTTOM_START);
				cx.printStackTrace();
			}
		});
		
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdNewFile}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNewFile_onClick(final ClickEvent<Button> event)
	{
		final Long beanId = null;
		final Long objId  = this.binder.getBean().getObjId();

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		this.popupRowImage();
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdDeleteFile}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDeleteFile_onClick(final ClickEvent<Button> event)
	{
		if(this.tableRowImage.getSelectedItems() == null)
		{
			com.vaadin.flow.component.notification.Notification.show("Es wurde keine Zeile selektiert in der Tabelle",
				20, Notification.Position.BOTTOM_START);
			return;
		}

		ConfirmDialog.show("Datensatz löschen", "Wirklich löschen?", okEvent -> {
			try
			{
				
				final RowImage bean = this.tableRowImage.getSelectionModel().getFirstSelectedItem().get();
				
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getRimId(), bean.getClass().getSimpleName());
				
				final RowImageDAO dao = new RowImageDAO();
				dao.remove(bean);
				dao.flush();
				
				this.setTableRowImage(this.binder.getBean());
				
				Notification.show("Datensatz wurde gelöscht!",
					20, Notification.Position.BOTTOM_START);
				
			}
			catch(final PersistenceException cx)
			{
				final String msg = SeicentoCrud.getPerExceptionError(cx);
				Notification.show(msg, 20, Notification.Position.BOTTOM_START);
				cx.printStackTrace();
			}
		});
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdUpdateFile}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdUpdateFile_onClick(final ClickEvent<Button> event)
	{

		if(!this.tableRowImage.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			return;
		}

		final Long beanId = this.tableRowImage.getSelectionModel().getFirstSelectedItem().get().getRimId();
		final Long objId  = null;

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		this.popupRowImage();
		
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdNewParam}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdNewParam_onClick(final ClickEvent<Button> event)
	{
		final Long beanId = null;
		final Long objId  = this.binder.getBean().getObjId();
		
		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);
		
		this.popupRowParam();
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdDeleteParam}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDeleteParam_onClick(final ClickEvent<Button> event)
	{
		if(this.tableRowParam.getSelectedItems() == null)
		{
			com.vaadin.flow.component.notification.Notification.show("Es wurde keine Zeile selektiert in der Tabelle",
				20, Notification.Position.BOTTOM_START);
			return;
		}

		ConfirmDialog.show("Datensatz löschen", "Wirklich löschen?", okEvent -> {
			
			try
			{
				
				final RowParameter bean = this.tableRowParam.getSelectionModel().getFirstSelectedItem().get();
				
				// Delete Record
				final RowObjectManager man = new RowObjectManager();
				man.deleteObject(bean.getPrmId(), bean.getClass().getSimpleName());
				
				final RowParameterDAO dao = new RowParameterDAO();
				dao.remove(bean);
				dao.flush();
				
				this.setTableRowParam(this.binder.getBean());
				
				Notification.show("Datensatz wurde gelöscht!",
					20, Notification.Position.BOTTOM_START);
				
			}
			catch(final PersistenceException cx)
			{
				final String msg = SeicentoCrud.getPerExceptionError(cx);
				Notification.show(msg, 20, Notification.Position.BOTTOM_START);
				cx.printStackTrace();
			}
		});
	}
	
	/**
	 * Event handler delegate method for the {@link Button} {@link #cmdUpdateParam}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdUpdateParam_onClick(final ClickEvent<Button> event)
	{
		if(!this.tableRowParam.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			return;
		}

		final Long beanId = this.tableRowParam.getSelectionModel().getFirstSelectedItem().get().getPrmId();
		final Long objId  = null;

		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);

		this.popupRowParam();
	}
	
	/**
	 * Event handler delegate method for the {@link Grid} {@link #tableRowParam}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableRowParam_onItemDoubleClick(final ItemDoubleClickEvent<RowParameter> event)
	{

		final RowParameter bean   = event.getItem();
		final Long         beanId = bean.getPrmId(); // this.tableRowParam.getSelectedItem().getBean().getPrmId();
		final Long         objId  = null;
		
		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);
		
		this.popupRowParam();
	}

	/**
	 * Event handler delegate method for the {@link Grid} {@link #tableText}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableText_onItemDoubleClick(final ItemDoubleClickEvent<RowText> event)
	{
		final RowText bean   = event.getItem();
		final Long    beanId = bean.getTxtId(); // this.tableRowParam.getSelectedItem().getBean().getPrmId();
		final Long    objId  = null;
		
		UI.getCurrent().getSession().setAttribute("beanId", beanId);
		UI.getCurrent().getSession().setAttribute("objId", objId);
		
		this.popupRowText();
	}
	
	/**
	 * Event handler delegate method for the {@link Grid} {@link #tableRowImage}.
	 *
	 * @see ComponentEventListener#onComponentEvent(ComponentEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void tableRowImage_onItemClick(final ItemClickEvent<RowImage> event)
	{
		if(!this.tableRowImage.getSelectionModel().getFirstSelectedItem().isPresent())
		{
			return;
		}
		
		final RowImage rof      = this.tableRowImage.getSelectionModel().getFirstSelectedItem().get();
		boolean        editable = true;
		if(rof.getRimNumber() == 100)
		{
			// Archive
			editable = false;
		}
		
		this.cmdDeleteFile.setEnabled(editable);
		this.cmdUpdateFile.setEnabled(editable);
	}
	
	/* WARNING: Do NOT edit!<br>The content of this method is always regenerated by the UI designer. */
	// <generated-code name="initUI">
	private void initUI()
	{
		this.splitLayout         = new SplitLayout();
		this.formLayout          = new FormLayout();
		this.formItem            = new FormItem();
		this.lblObjId            = new Label();
		this.txtObjId            = new TextField();
		this.formItem12          = new FormItem();
		this.lblObjAdded         = new Label();
		this.dateObjAdded        = new DatePicker();
		this.formItem13          = new FormItem();
		this.lblObjDeleted       = new Label();
		this.dateObjDeleted      = new DatePicker();
		this.formItem7           = new FormItem();
		this.lblObjChanged       = new Label();
		this.dateObjChanged      = new DatePicker();
		this.formItem3           = new FormItem();
		this.lblEntity           = new Label();
		this.cmbEntity           = new ComboBox<>();
		this.formItem4           = new FormItem();
		this.lblObjAddedBy       = new Label();
		this.txtObjAddedBy       = new TextField();
		this.formItem5           = new FormItem();
		this.lblObjDeletedBy     = new Label();
		this.txtObjDeletedBy     = new TextField();
		this.formItem9           = new FormItem();
		this.lblObjChangedBy     = new Label();
		this.txtObjChangedBy     = new TextField();
		this.formItem6           = new FormItem();
		this.lblObjRowId         = new Label();
		this.txtObjRowId         = new TextField();
		this.formItem8           = new FormItem();
		this.lblObjChngcnt       = new Label();
		this.txtObjChngcnt       = new TextField();
		this.formItem10          = new FormItem();
		this.lblObjState         = new Label();
		this.txtObjState         = new ComboBox<>();
		this.verticalLayout      = new VerticalLayout();
		this.tabs                = new Tabs();
		this.tab                 = new Tab();
		this.tab2                = new Tab();
		this.tab3                = new Tab();
		this.verticalLayoutText  = new VerticalLayout();
		this.horizontalLayout    = new HorizontalLayout();
		this.cmdNewText          = new Button();
		this.cmdDeleteText       = new Button();
		this.cmdUpdateText       = new Button();
		this.tableText           = new Grid<>(RowText.class, false);
		this.verticalLayoutFile  = new VerticalLayout();
		this.horizontalLayout2   = new HorizontalLayout();
		this.cmdNewFile          = new Button();
		this.cmdDeleteFile       = new Button();
		this.cmdUpdateFile       = new Button();
		this.tableRowImage       = new Grid<>(RowImage.class, false);
		this.verticalLayoutParam = new VerticalLayout();
		this.horizontalLayout3   = new HorizontalLayout();
		this.cmdNewParam         = new Button();
		this.cmdDeleteParam      = new Button();
		this.cmdUpdateParam      = new Button();
		this.tableRowParam       = new Grid<>(RowParameter.class, false);
		this.binder              = new Binder<>();
		
		this.splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		this.formLayout.setResponsiveSteps(
			new FormLayout.ResponsiveStep("0px", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("500px", 3, FormLayout.ResponsiveStep.LabelsPosition.TOP),
			new FormLayout.ResponsiveStep("1000px", 4, FormLayout.ResponsiveStep.LabelsPosition.TOP));
		this.lblObjId.setText(StringResourceUtils.optLocalizeString("{$lblObjId.value}", this));
		this.txtObjId.setReadOnly(true);
		this.lblObjAdded.setText(StringResourceUtils.optLocalizeString("{$lblObjAdded.value}", this));
		this.dateObjAdded.setReadOnly(true);
		this.lblObjDeleted.setText(StringResourceUtils.optLocalizeString("{$lblObjDeleted.value}", this));
		this.lblObjChanged.setText(StringResourceUtils.optLocalizeString("{$lblObjChanged.value}", this));
		this.lblEntity.setText(StringResourceUtils.optLocalizeString("{$lblEntity.value}", this));
		this.cmbEntity.setDataProvider(StartsWithIgnoreCaseItemFilter.New(this.cmbEntity::getItemLabelGenerator),
			DataProvider.ofCollection(new EntityDAO().findAll()));
		this.cmbEntity.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(Entity::getEntName));
		this.lblObjAddedBy.setText(StringResourceUtils.optLocalizeString("{$lblObjAddedBy.value}", this));
		this.lblObjDeletedBy.setText(StringResourceUtils.optLocalizeString("{$lblObjDeletedBy.value}", this));
		this.lblObjChangedBy.setText(StringResourceUtils.optLocalizeString("{$lblObjChangedBy.value}", this));
		this.lblObjRowId.setText(StringResourceUtils.optLocalizeString("{$lblObjRowId.value}", this));
		this.lblObjChngcnt.setText(StringResourceUtils.optLocalizeString("{$lblObjChngcnt.value}", this));
		this.lblObjState.setText(StringResourceUtils.optLocalizeString("{$lblObjState.value}", this));
		this.txtObjState.setItemLabelGenerator(ItemLabelGeneratorFactory.NonNull(CaptionUtils::resolveCaption));
		this.verticalLayout.setPadding(false);
		this.tab.setLabel("Text");
		this.tab2.setLabel("Dateien");
		this.tab3.setLabel("Parameter");
		this.verticalLayoutText.setPadding(false);
		this.cmdNewText.setText("Neu");
		this.cmdNewText.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDeleteText.setText(StringResourceUtils.optLocalizeString("{$cmdDeleteText.caption}", this));
		this.cmdDeleteText.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdUpdateText.setText(StringResourceUtils.optLocalizeString("{$cmdUpdateText.caption}", this));
		this.cmdUpdateText.setIcon(ImageIcons.EDIT.create());
		this.tableText.addColumn(RowText::getTxtNumber).setKey("txtNumber").setHeader("Nbr").setResizable(true)
			.setSortable(true);
		this.tableText.addColumn(RowText::getTxtFreetext).setKey("txtFreetext").setHeader("Text").setResizable(true)
			.setSortable(true);
		this.tableText.addColumn(new CaptionRenderer<>(RowText::getLanguage)).setKey("language").setHeader("Sprache")
			.setResizable(true).setSortable(false);
		this.tableText.addColumn(new CaptionRenderer<>(RowText::getTxtState)).setKey("txtState").setHeader("Status")
			.setResizable(true).setSortable(true);
		this.tableText.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.verticalLayoutFile.setPadding(false);
		this.cmdNewFile.setText(StringResourceUtils.optLocalizeString("{$cmdNewFile.caption}", this));
		this.cmdNewFile.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDeleteFile.setText(StringResourceUtils.optLocalizeString("{$cmdDeleteFile.caption}", this));
		this.cmdDeleteFile.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdUpdateFile.setText(StringResourceUtils.optLocalizeString("{$cmdUpdateFile.caption}", this));
		this.cmdUpdateFile.setIcon(ImageIcons.EDIT.create());
		this.tableRowImage.addColumn(RowImage::getRimNumber).setKey("rimNumber").setHeader("Nbr").setResizable(true)
			.setSortable(true);
		this.tableRowImage.addColumn(RowImage::getRimName).setKey("rimName").setHeader("Name").setResizable(true)
			.setSortable(true);
		this.tableRowImage.addColumn(RowImage::getRimSize).setKey("rimSize").setHeader("Grösse").setResizable(true)
			.setSortable(true);
		this.tableRowImage.addColumn(RowImage::getRimType).setKey("rimType").setHeader("Typ").setResizable(true)
			.setSortable(true);
		this.tableRowImage.addColumn(RowImage::getRimMimetype).setKey("rimMimetype").setHeader("Mime Typ")
			.setResizable(true).setSortable(true);
		this.tableRowImage.addColumn(new CaptionRenderer<>(RowImage::getRimState)).setKey("rimState")
			.setHeader("Status")
			.setResizable(true).setSortable(true);
		this.tableRowImage.setSelectionMode(Grid.SelectionMode.SINGLE);
		this.verticalLayoutParam.setPadding(false);
		this.cmdNewParam.setText(StringResourceUtils.optLocalizeString("{$cmdNewFile.caption}", this));
		this.cmdNewParam.setIcon(VaadinIcon.PLUS_CIRCLE.create());
		this.cmdDeleteParam.setText(StringResourceUtils.optLocalizeString("{$cmdDeleteFile.caption}", this));
		this.cmdDeleteParam.setIcon(VaadinIcon.MINUS_CIRCLE.create());
		this.cmdUpdateParam.setText(StringResourceUtils.optLocalizeString("{$cmdUpdateFile.caption}", this));
		this.cmdUpdateParam.setIcon(ImageIcons.EDIT.create());
		this.tableRowParam.addColumn(RowParameter::getPrmValue).setKey("prmValue").setHeader("Wert").setSortable(true);
		this.tableRowParam.addColumn(RowParameter::getPrmGroup).setKey("prmGroup").setHeader("Gruppe")
			.setResizable(true)
			.setSortable(true);
		this.tableRowParam.addColumn(RowParameter::getPrmSubGroup).setKey("prmSubGroup").setHeader("Untergruppe")
			.setResizable(true).setSortable(true);
		this.tableRowParam.addColumn(RowParameter::getPrmKey).setKey("prmKey").setHeader("Schlüssel").setResizable(true)
			.setSortable(true);
		this.tableRowParam.addColumn(new CaptionRenderer<>(RowParameter::getPrmValueType)).setKey("prmValueType")
			.setHeader("Type").setResizable(true).setSortable(true).setVisible(false);
		this.tableRowParam.addColumn(new CaptionRenderer<>(RowParameter::getPrmState)).setKey("prmState")
			.setHeader("Status").setResizable(true).setSortable(true).setVisible(false);
		this.tableRowParam
			.addColumn(v -> Optional.ofNullable(v).map(RowParameter::getRowObject).map(RowObject::getEntity)
				.map(Entity::getEntName).orElse(null))
			.setKey("rowObject.entity.entName").setHeader("Tabelle").setResizable(true).setSortable(true)
			.setVisible(false);
		this.tableRowParam.setSelectionMode(Grid.SelectionMode.SINGLE);
		
		this.binder.forField(this.txtObjId).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToLong().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind(RowObject::getObjId, RowObject::setObjId).setReadOnly(true);
		this.binder.forField(this.dateObjAdded).withNullRepresentation(LocalDate.of(2020, Month.NOVEMBER, 18))
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build())
			.bind(RowObject::getObjAdded, RowObject::setObjAdded).setReadOnly(true);
		this.binder.forField(this.dateObjDeleted).withNullRepresentation(LocalDate.of(2020, Month.NOVEMBER, 18))
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build())
			.bind(RowObject::getObjDeleted, RowObject::setObjDeleted).setReadOnly(true);
		this.binder.forField(this.cmbEntity).bind(RowObject::getEntity, RowObject::setEntity).setReadOnly(true);
		this.binder.forField(this.txtObjAddedBy).withNullRepresentation("")
			.bind(RowObject::getObjAddedBy, RowObject::setObjAddedBy).setReadOnly(true);
		this.binder.forField(this.txtObjDeletedBy).withNullRepresentation("")
			.bind(RowObject::getObjDeletedBy, RowObject::setObjDeletedBy).setReadOnly(true);
		this.binder.forField(this.txtObjRowId).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToLong().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind(RowObject::getObjRowId, RowObject::setObjRowId).setReadOnly(true);
		this.binder.forField(this.dateObjChanged).withNullRepresentation(LocalDate.of(2020, Month.NOVEMBER, 18))
			.withConverter(ConverterBuilder.LocalDateToUtilDate().systemDefaultZoneId().build())
			.bind(RowObject::getObjChanged, RowObject::setObjChanged).setReadOnly(true);
		this.binder.forField(this.txtObjChngcnt).withNullRepresentation("")
			.withConverter(ConverterBuilder.StringToLong().numberFormatBuilder(NumberFormatBuilder.Integer()).build())
			.bind(RowObject::getObjChngcnt, RowObject::setObjChngcnt).setReadOnly(true);
		this.binder.forField(this.txtObjChangedBy).withNullRepresentation("")
			.bind(RowObject::getObjChangedBy, RowObject::setObjChangedBy).setReadOnly(true);
		this.binder.forField(this.txtObjState).bind(RowObject::getObjState, RowObject::setObjState).setReadOnly(true);
		
		this.lblObjId.setSizeUndefined();
		this.lblObjId.getElement().setAttribute("slot", "label");
		this.txtObjId.setWidthFull();
		this.txtObjId.setHeight(null);
		this.formItem.add(this.lblObjId, this.txtObjId);
		this.lblObjAdded.setSizeUndefined();
		this.lblObjAdded.getElement().setAttribute("slot", "label");
		this.dateObjAdded.setWidthFull();
		this.dateObjAdded.setHeight(null);
		this.formItem12.add(this.lblObjAdded, this.dateObjAdded);
		this.lblObjDeleted.setSizeUndefined();
		this.lblObjDeleted.getElement().setAttribute("slot", "label");
		this.dateObjDeleted.setWidthFull();
		this.dateObjDeleted.setHeight(null);
		this.formItem13.add(this.lblObjDeleted, this.dateObjDeleted);
		this.lblObjChanged.setSizeUndefined();
		this.lblObjChanged.getElement().setAttribute("slot", "label");
		this.dateObjChanged.setSizeUndefined();
		this.formItem7.add(this.lblObjChanged, this.dateObjChanged);
		this.lblEntity.setSizeUndefined();
		this.lblEntity.getElement().setAttribute("slot", "label");
		this.cmbEntity.setSizeUndefined();
		this.formItem3.add(this.lblEntity, this.cmbEntity);
		this.lblObjAddedBy.setSizeUndefined();
		this.lblObjAddedBy.getElement().setAttribute("slot", "label");
		this.txtObjAddedBy.setWidthFull();
		this.txtObjAddedBy.setHeight(null);
		this.formItem4.add(this.lblObjAddedBy, this.txtObjAddedBy);
		this.lblObjDeletedBy.setSizeUndefined();
		this.lblObjDeletedBy.getElement().setAttribute("slot", "label");
		this.txtObjDeletedBy.setWidthFull();
		this.txtObjDeletedBy.setHeight(null);
		this.formItem5.add(this.lblObjDeletedBy, this.txtObjDeletedBy);
		this.lblObjChangedBy.setSizeUndefined();
		this.lblObjChangedBy.getElement().setAttribute("slot", "label");
		this.txtObjChangedBy.setSizeUndefined();
		this.formItem9.add(this.lblObjChangedBy, this.txtObjChangedBy);
		this.lblObjRowId.setSizeUndefined();
		this.lblObjRowId.getElement().setAttribute("slot", "label");
		this.txtObjRowId.setWidthFull();
		this.txtObjRowId.setHeight(null);
		this.formItem6.add(this.lblObjRowId, this.txtObjRowId);
		this.lblObjChngcnt.setSizeUndefined();
		this.lblObjChngcnt.getElement().setAttribute("slot", "label");
		this.txtObjChngcnt.setWidthFull();
		this.txtObjChngcnt.setHeight(null);
		this.formItem8.add(this.lblObjChngcnt, this.txtObjChngcnt);
		this.lblObjState.setSizeUndefined();
		this.lblObjState.getElement().setAttribute("slot", "label");
		this.txtObjState.setSizeUndefined();
		this.formItem10.add(this.lblObjState, this.txtObjState);
		this.formLayout.add(this.formItem, this.formItem12, this.formItem13, this.formItem7, this.formItem3,
			this.formItem4,
			this.formItem5, this.formItem9, this.formItem6, this.formItem8, this.formItem10);
		this.tabs.add(this.tab, this.tab2, this.tab3);
		this.cmdNewText.setSizeUndefined();
		this.cmdDeleteText.setSizeUndefined();
		this.cmdUpdateText.setSizeUndefined();
		this.horizontalLayout.add(this.cmdNewText, this.cmdDeleteText, this.cmdUpdateText);
		this.horizontalLayout.setWidthFull();
		this.horizontalLayout.setHeight("40px");
		this.tableText.setSizeFull();
		this.verticalLayoutText.add(this.horizontalLayout, this.tableText);
		this.verticalLayoutText.setFlexGrow(1.0, this.tableText);
		this.cmdNewFile.setSizeUndefined();
		this.cmdDeleteFile.setSizeUndefined();
		this.cmdUpdateFile.setSizeUndefined();
		this.horizontalLayout2.add(this.cmdNewFile, this.cmdDeleteFile, this.cmdUpdateFile);
		this.horizontalLayout2.setWidthFull();
		this.horizontalLayout2.setHeight("40px");
		this.tableRowImage.setSizeFull();
		this.verticalLayoutFile.add(this.horizontalLayout2, this.tableRowImage);
		this.verticalLayoutFile.setFlexGrow(1.0, this.tableRowImage);
		this.cmdNewParam.setSizeUndefined();
		this.cmdDeleteParam.setSizeUndefined();
		this.cmdUpdateParam.setSizeUndefined();
		this.horizontalLayout3.add(this.cmdNewParam, this.cmdDeleteParam, this.cmdUpdateParam);
		this.horizontalLayout3.setWidthFull();
		this.horizontalLayout3.setHeight("40px");
		this.tableRowParam.setSizeFull();
		this.verticalLayoutParam.add(this.horizontalLayout3, this.tableRowParam);
		this.verticalLayoutParam.setFlexGrow(1.0, this.tableRowParam);
		this.tabs.setWidthFull();
		this.tabs.setHeight(null);
		this.verticalLayoutText.setSizeFull();
		this.verticalLayoutFile.setSizeFull();
		this.verticalLayoutParam.setSizeFull();
		this.verticalLayout.add(this.tabs, this.verticalLayoutText, this.verticalLayoutFile, this.verticalLayoutParam);
		this.splitLayout.addToPrimary(this.formLayout);
		this.splitLayout.addToSecondary(this.verticalLayout);
		this.splitLayout.setSplitterPosition(50.0);
		this.splitLayout.setSizeFull();
		this.add(this.splitLayout);
		this.setFlexGrow(1.0, this.splitLayout);
		this.setSizeFull();
		
		this.tabs.setSelectedIndex(0);
		
		this.cmdNewText.addClickListener(this::cmdNewText_onClick);
		this.cmdDeleteText.addClickListener(this::cmdDeleteText_onClick);
		this.cmdUpdateText.addClickListener(this::cmdUpdateText_onClick);
		this.tableText.addItemDoubleClickListener(this::tableText_onItemDoubleClick);
		this.cmdNewFile.addClickListener(this::cmdNewFile_onClick);
		this.cmdDeleteFile.addClickListener(this::cmdDeleteFile_onClick);
		this.cmdUpdateFile.addClickListener(this::cmdUpdateFile_onClick);
		this.tableRowImage.addItemClickListener(this::tableRowImage_onItemClick);
		this.cmdNewParam.addClickListener(this::cmdNewParam_onClick);
		this.cmdDeleteParam.addClickListener(this::cmdDeleteParam_onClick);
		this.cmdUpdateParam.addClickListener(this::cmdUpdateParam_onClick);
		this.tableRowParam.addItemDoubleClickListener(this::tableRowParam_onItemDoubleClick);
	} // </generated-code>
	
	// <generated-code name="variables">
	private ComboBox<Entity>   cmbEntity;
	private Tab                tab, tab2, tab3;
	private VerticalLayout     verticalLayout, verticalLayoutText, verticalLayoutFile, verticalLayoutParam;
	private HorizontalLayout   horizontalLayout, horizontalLayout2, horizontalLayout3;
	private Label              lblObjId, lblObjAdded, lblObjDeleted, lblObjChanged, lblEntity, lblObjAddedBy,
		lblObjDeletedBy, lblObjChangedBy, lblObjRowId, lblObjChngcnt, lblObjState;
	private Binder<RowObject>  binder;
	private Tabs               tabs;
	private Grid<RowParameter> tableRowParam;
	private Grid<RowText>      tableText;
	private FormItem           formItem, formItem12, formItem13, formItem7, formItem3, formItem4, formItem5, formItem9,
		formItem6, formItem8, formItem10;
	private FormLayout         formLayout;
	private Button             cmdNewText, cmdDeleteText, cmdUpdateText, cmdNewFile, cmdDeleteFile, cmdUpdateFile,
		cmdNewParam, cmdDeleteParam, cmdUpdateParam;
	private ComboBox<State>    txtObjState;
	private SplitLayout        splitLayout;
	private DatePicker         dateObjAdded, dateObjDeleted, dateObjChanged;
	private Grid<RowImage>     tableRowImage;
	private TextField          txtObjId, txtObjAddedBy, txtObjDeletedBy, txtObjChangedBy, txtObjRowId, txtObjChngcnt;
	// </generated-code>

}
