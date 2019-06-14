package ch.xwr.seicentobilling.business;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.xdev.persistence.PersistenceUtils;

import ch.xwr.seicentobilling.dal.ExpenseDAO;
import ch.xwr.seicentobilling.entities.Expense;
import ch.xwr.seicentobilling.entities.Periode;

public class ExpenseHandler {
	/** Logger initialized */
	private static final Logger LOG = LoggerFactory.getLogger(ExpenseHandler.class);


	public void copyExpensePeriode(final Periode from, final Periode toP) throws Exception {
		if (from == null || toP == null) {
			throw new Exception("Ungültige Periode(n) für das Kopieren!");
		}
		if (LovState.BookingType.gebucht.equals(toP.getPerBookedExpense())) {
			throw new Exception("Die Zielperiode ist bereits verbucht!");
		}
		if (targetCount(toP) > 0) {
			throw new Exception("Die Zielperiode enthält bereits Daten!");
		}
		if (targetCount(from) < 1) {
			throw new Exception("Die Ausgangsperiode enthält keine Daten!");
		}

		LOG.info("Start kopiere Spesen von Periode: " + from.getPerName());
		startLoop(from, toP);
	}

	private int targetCount(final Periode toP) {
		final ExpenseDAO dao = new ExpenseDAO();
		final List<Expense> lst = dao.findByPeriode(toP);

		return lst.size();
	}

	private void startLoop(final Periode from, final Periode toP) {
		final ExpenseDAO dao = new ExpenseDAO();

		final List<Expense> lst = dao.findByPeriode(from);
		for (final Iterator<Expense> iterator = lst.iterator(); iterator.hasNext();) {
			final Expense expense = iterator.next();

			copySingleRecord(dao, expense, toP);
		}
	}

	private void copySingleRecord(final ExpenseDAO dao, final Expense expense, final Periode toP) {
		PersistenceUtils.getEntityManager(Expense.class).detach(expense);

		expense.setExpId(new Long(0));
		expense.setExpBooked(null);
		expense.setPeriode(toP);
		expense.setExpDate(calcNewDate(expense.getExpDate(), toP));

		final Expense newBean = dao.merge(expense);
		dao.save(newBean);

		//create Objectroot
		final RowObjectManager man = new RowObjectManager();
		man.updateObject(newBean.getExpId(), newBean.getClass().getSimpleName());

	}

	private Date calcNewDate(final Date oldDate, final Periode toP) {
		final Calendar cal = Calendar.getInstance(); // Gets the current date
		cal.setTime(oldDate);

		int oldDay = cal.get(Calendar.DAY_OF_MONTH);
		if (oldDay > 30) {
			oldDay = 30;
		}
		if (oldDay > 28 && toP.getPerMonth().ordinal() == 2) {
			oldDay = 28;
		}

		final int newMonth = toP.getPerMonth().ordinal() - 1;
		final int newYear = toP.getPerYear();

		cal.set(newYear, newMonth, oldDay);
		if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
			if (oldDay < 28) {
				cal.add(Calendar.DATE, 2);
			} else {
				cal.add(Calendar.DATE, -1);
			}
		}
		if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			if (oldDay < 28) {
				cal.add(Calendar.DATE, 1);
			} else {
				cal.add(Calendar.DATE, -2);
			}
		}
		return cal.getTime();

	}

}
