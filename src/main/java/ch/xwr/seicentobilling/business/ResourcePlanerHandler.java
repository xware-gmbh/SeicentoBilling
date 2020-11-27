package ch.xwr.seicentobilling.business;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ch.xwr.seicentobilling.dal.ResPlanningDAO;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.ResPlanning;

public class ResourcePlanerHandler {

	public void generatePlan(final Project bean) {
		removeAutoEntries(bean);

		//step2
		Date startDat = getStartDate(bean);
		final int durationMonth = getMonthDuration(bean, startDat);
		final Double ihours = getHourPerMonth(bean, durationMonth);

		final Calendar cal = Calendar.getInstance();
		cal.setTime(startDat);

		for (int i = 0; i < durationMonth; i++) {
			addRecord(bean, ihours, startDat);

			cal.add(Calendar.MONTH, 1);
			startDat = cal.getTime();
		}

	}

	private Double getHourPerMonth(final Project bean, final int durationMonth) {
		if (durationMonth < 1) {
			return new Double(0.);
		}

		//calc hours
		Double ihours1 = bean.getProHours() - bean.getProHoursEffective();
		if (ihours1 < 0) {
			ihours1 = Double.valueOf(0.);
		}
		final Double ihours2 = ihours1 / durationMonth;
		return ihours2;
	}

	private Date getStartDate(final Project bean) {
		Date startDat = bean.getProStartDate();
		final Date nowd = new Date();
		if (startDat.before(nowd)) {
			startDat = nowd;
		}
		return startDat;
	}

	private void addRecord(final Project project, final Double ihours2, final Date startDat) {
		final ResPlanningDAO dao = new ResPlanningDAO();
		final ResPlanning bean = new ResPlanning();

		bean.setCostAccount(project.getCostAccount());
		bean.setProject(project);
		bean.setRspHours(ihours2.doubleValue());
		bean.setRspMode((short) 0);
		bean.setRspPercent(getIntensity(project, ihours2));
		bean.setRspPlandate(startDat);
		bean.setRspState((short)1);
		//bean.setRspState(LovState.State.active);

		dao.save(bean);

	}

	private int getIntensity(final Project project, final Double ihours2) {
		try {
			//calculate intensity - assuming that 160 hours represent 100%
			final double iper = 100./160*ihours2;
			return (int) iper;
		} catch (final Exception e) {
			//ignore
		}
		return project.getProIntensityPercent();
	}

	private int getMonthDuration(final Project bean, final Date startDat) {
		Date endDat = bean.getProEndDate();
		if (endDat == null) {
			endDat = new Date();
		}

		if (startDat.after(endDat)) {
			return 0;
		}

		final Calendar cal = Calendar.getInstance();
		cal.setTime(startDat);
		final int iYearFrom = cal.get(Calendar.YEAR);
		final int iMonthFrom = cal.get(Calendar.MONTH);

		cal.setTime(endDat);
		final int iYearTo = cal.get(Calendar.YEAR);
		final int iMonthTo = cal.get(Calendar.MONTH);

		final int idiff = (iYearTo - iYearFrom) * 12 + (iMonthTo - iMonthFrom) + 1;
		return idiff;
	}

	private void removeAutoEntries(final Project bean) {
		final ResPlanningDAO dao = new ResPlanningDAO();

		//step 1 remove automatic entries
		final List<ResPlanning> lst = dao.findByProjectAndCostAccount(bean, bean.getCostAccount());
		for (final ResPlanning resPlanning : lst) {
			if (resPlanning.getRspMode() == 0) {		//automode
				dao.remove(resPlanning);
			}
		}

	}

}
