package ch.xwr.seicentobilling.business.helper;

import java.util.List;

import ch.xwr.seicentobilling.dal.ExpenseDAO;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.entities.Expense;
import ch.xwr.seicentobilling.entities.Periode;

/*
 * #435 only needed for one call to migrate VAtTable
 */
public class MigrateVatR2120 {

	public void migrateVatTabelR2120() {
		System.out.println("Start Migrate Vat Table R21.2.0  -  flag: seicento/startup/migrateVat");
		processAllPeriods();
	}

	private void processAllPeriods() {
		final PeriodeDAO dao = new PeriodeDAO();
		final List<Periode> plst = dao.findAll();

		for (final Periode periode : plst) {
			System.out.println("Process Periode " + periode.getPerName());
			processExpensesPerPeriode(periode);
		}

	}

	private void processExpensesPerPeriode(final Periode per) {
		final ExpenseDAO dao = new ExpenseDAO();
		final List<Expense> elst = dao.findByPeriode(per);

		for (final Expense expense : elst) {
			calcVatOnExpense(expense);
		}

	}

	private void calcVatOnExpense(final Expense exp) {
		final VatHelper vhlp = new VatHelper();
		final Double amtV = vhlp.getVatAmount(exp.getExpDate(), new Double(exp.getExpAmount()), exp.getVat());

		exp.setExpAmountWOTax(exp.getExpAmount() - amtV);

		new ExpenseDAO().save(exp);
	}


}
