package ch.xwr.seicentobilling.ui.desktop.project;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import com.google.common.collect.Lists;
import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.xdev.ui.XdevButton;
import com.xdev.ui.XdevGridLayout;
import com.xdev.ui.XdevHorizontalLayout;
import com.xdev.ui.XdevLabel;
import com.xdev.ui.XdevPanel;
import com.xdev.ui.XdevPopupDateField;
import com.xdev.ui.XdevTextField;
import com.xdev.ui.XdevView;
import com.xdev.util.ConverterBuilder;

import ch.xwr.seicentobilling.business.Seicento;
import ch.xwr.seicentobilling.entities.Project;

public class ProjectEffortDefault extends XdevView {
	private Project proDao = null;

	/**
	 *
	 */
	public ProjectEffortDefault() {
		super();
		this.initUI();

		this.setHeight(Seicento.calculateThemeHeight(this.getHeight(),UI.getCurrent().getTheme()));

		// get Parameter
		this.proDao = (Project) UI.getCurrent().getSession().getAttribute("ProjectDao");

		if (this.proDao != null) {
			this.datePraStartDate.setValue(this.proDao.getProStartDate());
			this.datePraStartDate.setReadOnly(true);

			this.datePraEndDate.setValue(this.proDao.getProEndDate());
			this.txtPraIntensityPercent.setConvertedValue(this.proDao.getProIntensityPercent());
			this.txtPraHours.setConvertedValue(this.proDao.getProHours());
		}

		//setBeanGui(bean);


	}

	public static Window getPopupWindow() {
		final Window win = new Window();
		//win.setWidth("920");
		//win.setHeight("610");
		win.center();
		win.setModal(true);
		win.setContent(new ProjectEffortDefault());

		return win;
	}

	private void calculateTargetHours() {
		final Integer dayhours = (Integer) this.txtHoursWorkDay.getConvertedValue();

		if (this.datePraStartDate.isValid() && this.datePraEndDate.isValid()) {
			final long days = getBusinessDaysDifference(this.datePraStartDate.getValue(), this.datePraEndDate.getValue());
			int ihours = (int) (days * dayhours.intValue());

			int ipercent= 0;
			try {
				ipercent = Integer.parseInt(this.txtPraIntensityPercent.getValue());
			} catch (final Exception e) {
				//ignore
			}

			if (ipercent > 0) {
				ihours = ihours * ipercent / 100;
			}
			if (ihours < 1) {
				ihours = 1;
			}

			this.txtPraHours.setValue("" + ihours);
			//System.out.println("set hours: " + ihours);
		}
	}

	private void calculateTargetIntensity() {
		final Integer dayhours = (Integer) this.txtHoursWorkDay.getConvertedValue();

		if (this.datePraStartDate.isValid() && this.datePraEndDate.isValid()) {
			final long days = getBusinessDaysDifference(this.datePraStartDate.getValue(), this.datePraEndDate.getValue());
			final Integer ihours = (Integer) this.txtPraHours.getConvertedValue();

			final int ihoursFull = (int) (days * dayhours.intValue());

			int ipercent= 0;
			try {
				ipercent = 100 * ihours.intValue() / ihoursFull;
			} catch (final Exception e) {
				//ignore
			}


			this.txtPraIntensityPercent.setValue("" + ipercent);
			//System.out.println("set hours: " + ihours);
		}

	}

	private void calculateTargetDate() {
		final Integer dayhours = (Integer) this.txtHoursWorkDay.getConvertedValue();

		if (this.datePraStartDate.isValid()) {
			final Integer ipercent = (Integer) this.txtPraIntensityPercent.getConvertedValue();
			final Integer ihours = (Integer) this.txtPraHours.getConvertedValue();
			final int day1 = ihours / dayhours * 100 / ipercent.intValue();

			final Date toDate = addDays(this.datePraStartDate.getValue(), day1);
			this.datePraEndDate.setValue(toDate);
		}

	}

    private Date addDays(final Date date, final int days)
    {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); //minus number would decrement the days
        return cal.getTime();
    }

	private long getBusinessDaysDifference(final Date dFrom, final Date dTo) {

		final LocalDate startDate = convertToLocalDateViaInstant(dFrom);
		final LocalDate endDate = convertToLocalDateViaInstant(dTo);

	    final EnumSet<DayOfWeek> weekend = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
	    final List<LocalDate> list = Lists.newArrayList();

	    LocalDate start = startDate;
	    while (start.isBefore(endDate)) {
	        list.add(start);
	        start = start.plus(1, ChronoUnit.DAYS);
	    }

	    final long numberOfDays = list.stream().filter(d -> !weekend.contains(d.getDayOfWeek())).count();

		final int daysOff = (int) this.txtDaysOff.getConvertedValue();

	    return numberOfDays - daysOff;
	}

	public LocalDate convertToLocalDateViaInstant(final Date dateToConvert) {
		final java.util.Date utilDate = new java.util.Date(dateToConvert.getTime());
	    return utilDate.toInstant()
	      .atZone(ZoneId.systemDefault())
	      .toLocalDate();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdCancel}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdCancel_buttonClick(final Button.ClickEvent event) {
		//this.fieldGroup.discard();
		((Window) this.getParent()).close();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdDone}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdDone_buttonClick(final Button.ClickEvent event) {
		if (this.proDao != null) {
			this.proDao.setProEndDate(this.datePraEndDate.getValue());
			this.proDao.setProIntensityPercent((Integer) this.txtPraIntensityPercent.getConvertedValue());
			this.proDao.setProHours((Integer) this.txtPraHours.getConvertedValue());

			UI.getCurrent().getSession().setAttribute("ProjectDao", this.proDao);
		}

		((Window) this.getParent()).close();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdPraHours}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdPraHours_buttonClick(final Button.ClickEvent event) {
		calculateTargetHours();
	}

	/**
	 * Event handler delegate method for the {@link XdevButton}
	 * {@link #cmdPraPercent}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdPraPercent_buttonClick(final Button.ClickEvent event) {
		calculateTargetIntensity();
	}


	/**
	 * Event handler delegate method for the {@link XdevButton} {@link #cmdEndDate}.
	 *
	 * @see Button.ClickListener#buttonClick(Button.ClickEvent)
	 * @eventHandlerDelegate Do NOT delete, used by UI designer!
	 */
	private void cmdEndDate_buttonClick(final Button.ClickEvent event) {
		calculateTargetDate();
	}


	/*
	 * WARNING: Do NOT edit!<br>The content of this method is always regenerated by
	 * the UI designer.
	 */
	// <generated-code name="initUI">
	private void initUI() {
		this.panel = new XdevPanel();
		this.gridLayout = new XdevGridLayout();
		this.lblPraStartDate = new XdevLabel();
		this.datePraStartDate = new XdevPopupDateField();
		this.lblPraEndDate = new XdevLabel();
		this.hlEndDate = new XdevHorizontalLayout();
		this.datePraEndDate = new XdevPopupDateField();
		this.cmdEndDate = new XdevButton();
		this.lblHoursDay = new XdevLabel();
		this.txtHoursWorkDay = new XdevTextField();
		this.lblDaysOff = new XdevLabel();
		this.txtDaysOff = new XdevTextField();
		this.lblPraHours = new XdevLabel();
		this.hlHours = new XdevHorizontalLayout();
		this.txtPraHours = new XdevTextField();
		this.cmdPraHours = new XdevButton();
		this.lblPraIntensityPercent = new XdevLabel();
		this.hlPraIntensity = new XdevHorizontalLayout();
		this.txtPraIntensityPercent = new XdevTextField();
		this.cmdPraPercent = new XdevButton();
		this.horizontalLayout = new XdevHorizontalLayout();
		this.cmdDone = new XdevButton();
		this.cmdCancel = new XdevButton();

		this.panel.setCaption("Vorschlag berechnen");
		this.lblPraStartDate.setValue("Projektstart");
		this.datePraStartDate.setRequired(true);
		this.lblPraEndDate.setValue("Projektende");
		this.hlEndDate.setMargin(new MarginInfo(false));
		this.cmdEndDate.setIcon(FontAwesome.ANGLE_DOUBLE_LEFT);
		this.lblHoursDay.setValue("Arbeitstag Std");
		this.txtHoursWorkDay.setConverter(ConverterBuilder.stringToInteger().build());
		this.txtHoursWorkDay.setValue("8");
		this.txtHoursWorkDay.addValidator(new IntegerRangeValidator("", 1, 24));
		this.lblDaysOff.setValue("Tage abziehen");
		this.txtDaysOff.setConverter(ConverterBuilder.stringToInteger().build());
		this.txtDaysOff.setValue("0");
		this.lblPraHours.setValue("Stundensoll");
		this.hlHours.setMargin(new MarginInfo(false));
		this.txtPraHours.setConverter(ConverterBuilder.stringToInteger().build());
		this.txtPraHours.setValue("0");
		this.cmdPraHours.setIcon(FontAwesome.ANGLE_DOUBLE_LEFT);
		this.lblPraIntensityPercent.setValue("Intensität");
		this.hlPraIntensity.setMargin(new MarginInfo(false));
		this.txtPraIntensityPercent.setConverter(ConverterBuilder.stringToInteger().build());
		this.txtPraIntensityPercent.setValue("100");
		this.cmdPraPercent.setIcon(FontAwesome.ANGLE_DOUBLE_LEFT);
		this.horizontalLayout.setMargin(new MarginInfo(true, false, false, false));
		this.cmdDone.setIcon(FontAwesome.CHECK_CIRCLE);
		this.cmdDone.setCaption("Übernehmen");
		this.cmdDone.setClickShortcut(ShortcutAction.KeyCode.ENTER);
		this.cmdCancel.setIcon(FontAwesome.CLOSE);
		this.cmdCancel.setCaption("Abbrechen");
		this.cmdCancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE);

		this.datePraEndDate.setSizeUndefined();
		this.hlEndDate.addComponent(this.datePraEndDate);
		this.hlEndDate.setExpandRatio(this.datePraEndDate, 100.0F);
		this.cmdEndDate.setSizeUndefined();
		this.hlEndDate.addComponent(this.cmdEndDate);
		this.hlEndDate.setComponentAlignment(this.cmdEndDate, Alignment.MIDDLE_CENTER);
		this.txtPraHours.setSizeUndefined();
		this.hlHours.addComponent(this.txtPraHours);
		this.hlHours.setExpandRatio(this.txtPraHours, 100.0F);
		this.cmdPraHours.setSizeUndefined();
		this.hlHours.addComponent(this.cmdPraHours);
		this.hlHours.setComponentAlignment(this.cmdPraHours, Alignment.MIDDLE_CENTER);
		this.txtPraIntensityPercent.setSizeUndefined();
		this.hlPraIntensity.addComponent(this.txtPraIntensityPercent);
		this.hlPraIntensity.setExpandRatio(this.txtPraIntensityPercent, 100.0F);
		this.cmdPraPercent.setSizeUndefined();
		this.hlPraIntensity.addComponent(this.cmdPraPercent);
		this.hlPraIntensity.setComponentAlignment(this.cmdPraPercent, Alignment.MIDDLE_CENTER);
		this.cmdDone.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdDone);
		this.horizontalLayout.setComponentAlignment(this.cmdDone, Alignment.MIDDLE_LEFT);
		this.cmdCancel.setSizeUndefined();
		this.horizontalLayout.addComponent(this.cmdCancel);
		this.horizontalLayout.setComponentAlignment(this.cmdCancel, Alignment.MIDDLE_LEFT);
		this.gridLayout.setColumns(4);
		this.gridLayout.setRows(5);
		this.lblPraStartDate.setSizeUndefined();
		this.gridLayout.addComponent(this.lblPraStartDate, 0, 0);
		this.datePraStartDate.setSizeUndefined();
		this.gridLayout.addComponent(this.datePraStartDate, 1, 0);
		this.lblPraEndDate.setSizeUndefined();
		this.gridLayout.addComponent(this.lblPraEndDate, 2, 0);
		this.hlEndDate.setSizeUndefined();
		this.gridLayout.addComponent(this.hlEndDate, 3, 0);
		this.lblHoursDay.setSizeUndefined();
		this.gridLayout.addComponent(this.lblHoursDay, 0, 1);
		this.txtHoursWorkDay.setSizeUndefined();
		this.gridLayout.addComponent(this.txtHoursWorkDay, 1, 1);
		this.lblDaysOff.setSizeUndefined();
		this.gridLayout.addComponent(this.lblDaysOff, 2, 1);
		this.txtDaysOff.setSizeUndefined();
		this.gridLayout.addComponent(this.txtDaysOff, 3, 1);
		this.lblPraHours.setSizeUndefined();
		this.gridLayout.addComponent(this.lblPraHours, 0, 2);
		this.hlHours.setSizeUndefined();
		this.gridLayout.addComponent(this.hlHours, 1, 2);
		this.lblPraIntensityPercent.setSizeUndefined();
		this.gridLayout.addComponent(this.lblPraIntensityPercent, 2, 2);
		this.hlPraIntensity.setSizeUndefined();
		this.gridLayout.addComponent(this.hlPraIntensity, 3, 2);
		this.horizontalLayout.setSizeUndefined();
		this.gridLayout.addComponent(this.horizontalLayout, 0, 3, 1, 3);
		this.gridLayout.setComponentAlignment(this.horizontalLayout, Alignment.TOP_RIGHT);
		this.gridLayout.setColumnExpandRatio(1, 100.0F);
		this.gridLayout.setColumnExpandRatio(3, 100.0F);
		final CustomComponent gridLayout_vSpacer = new CustomComponent();
		gridLayout_vSpacer.setSizeFull();
		this.gridLayout.addComponent(gridLayout_vSpacer, 0, 4, 3, 4);
		this.gridLayout.setRowExpandRatio(4, 1.0F);
		this.gridLayout.setSizeFull();
		this.panel.setContent(this.gridLayout);
		this.panel.setSizeFull();
		this.setContent(this.panel);
		this.setWidth(840, Unit.PIXELS);
		this.setHeight(340, Unit.PIXELS);

		this.cmdEndDate.addClickListener(event -> this.cmdEndDate_buttonClick(event));
		this.cmdPraHours.addClickListener(event -> this.cmdPraHours_buttonClick(event));
		this.cmdPraPercent.addClickListener(event -> this.cmdPraPercent_buttonClick(event));
		this.cmdDone.addClickListener(event -> this.cmdDone_buttonClick(event));
		this.cmdCancel.addClickListener(event -> this.cmdCancel_buttonClick(event));
	} // </generated-code>

	// <generated-code name="variables">
	private XdevLabel lblPraStartDate, lblPraEndDate, lblHoursDay, lblDaysOff, lblPraHours, lblPraIntensityPercent;
	private XdevButton cmdEndDate, cmdPraHours, cmdPraPercent, cmdDone, cmdCancel;
	private XdevHorizontalLayout hlEndDate, hlHours, hlPraIntensity, horizontalLayout;
	private XdevPopupDateField datePraStartDate, datePraEndDate;
	private XdevPanel panel;
	private XdevGridLayout gridLayout;
	private XdevTextField txtHoursWorkDay, txtDaysOff, txtPraHours, txtPraIntensityPercent;
	// </generated-code>

}
