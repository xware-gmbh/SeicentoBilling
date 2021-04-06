package ch.xwr.seicentobilling.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ch.xwr.seicentobilling.business.model.billing.BillDto;
import ch.xwr.seicentobilling.business.model.billing.BillLine;
import ch.xwr.seicentobilling.business.model.billing.GuiGeneratorFields;
import ch.xwr.seicentobilling.dal.OrderDAO;
import ch.xwr.seicentobilling.dal.OrderLineDAO;
import ch.xwr.seicentobilling.dal.PeriodeDAO;
import ch.xwr.seicentobilling.dal.ProjectDAO;
import ch.xwr.seicentobilling.dal.ProjectLineDAO;
import ch.xwr.seicentobilling.entities.CostAccount;
import ch.xwr.seicentobilling.entities.Item;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.OrderLine;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.Project;
import ch.xwr.seicentobilling.entities.ProjectAllocation;
import ch.xwr.seicentobilling.entities.ProjectLine;

public class OrderGenerator {
	private final HashMap<Long, BillDto> _billMap = new HashMap<>();
	private OrderCalculator _calc = null;
	private GuiGeneratorFields _guifld = null;
	/** Logger initialized */
	private static final org.apache.log4j.Logger _logger = org.apache.log4j.Logger.getLogger(OrderGenerator.class);


	public List<BillDto> proposeDraft(final Periode inp, final GuiGeneratorFields guifld) {
		this._guifld = guifld;
		final ProjectLineDAO dao = new ProjectLineDAO();
		final List<ProjectLine> list = dao.findByPeriode(inp);
		BillDto bill = null;

		//Loop 1 collect ProjectLines of current Periode
		for (final Iterator<ProjectLine> iterator = list.iterator(); iterator.hasNext();) {
			final ProjectLine pln = iterator.next();

			bill = getBillDto(pln, inp);
			cumulateLine(bill, pln);
		}

		//Loop 2 check additonal Ressources
		lookupProjectForStrategy(inp);

		final List<BillDto> rlist = calculateTotalHeader2List();
		return rlist;
	}

	private void lookupProjectForStrategy(final Periode inp) {
		final ProjectDAO proDao = new ProjectDAO();
		final List<Project> list = proDao.findByCostAccountActive(inp.getCostAccount());
		for (final Iterator<Project> iterator = list.iterator(); iterator.hasNext();) {
			final Project pro = iterator.next();

			if (pro.getProOrdergenerationStrategy() == LovState.ProOrderStrategy.zusammenziehen) {
				//this project should have more costaccounts for billing lookup
				final Set<ProjectAllocation> lst = pro.getProjectAllocations();
				for (final Iterator<ProjectAllocation> itr = lst.iterator(); itr.hasNext();) {
					final ProjectAllocation pra = itr.next();
					if (!pra.getCostAccount().getCsaId().equals(inp.getCostAccount().getCsaId())) { //prevent own double
						final Periode per = getValidPeriodForCst(inp, pra);
						//now we have the periode and the project
						if (per != null) {
							lookupProjectLinesForProject(per, pro);
						}
					}
				}

			}

		}

	}

	private Periode getValidPeriodForCst(final Periode inp, final ProjectAllocation pra) {
		final PeriodeDAO pd = new PeriodeDAO();
		final List<Periode> lsper = pd.findByCostAccountTop(pra.getCostAccount(), 5);
		for (final Iterator<Periode> itrP = lsper.iterator(); itrP.hasNext();) {
			final Periode per = itrP.next();

			if (per.getPerMonth().getValue() == inp.getPerMonth().getValue() && per.getPerYear().equals(inp.getPerYear())) {
				return per;
			}
		}

		return null;  //no Periode found
	}

	private void lookupProjectLinesForProject(final Periode per, final Project pro) {
		final ProjectLineDAO dao = new ProjectLineDAO();
		BillDto bill = null;

		final List<ProjectLine> lstPl = dao.findByPeriode(per);
		for (final Iterator<ProjectLine> itrPl = lstPl.iterator(); itrPl.hasNext();) {
			//here we have the project line of the periode from the ProjectAllocation
			final ProjectLine pln = itrPl.next();

			if (pln.getProject().getProId().equals(pro.getProId())) {
				//only take same projects
				bill = getBillDto(pln, per);
				cumulateLine(bill, pln);
			}
		}

	}

	private BillDto getBillDto(final ProjectLine pln, final Periode per) {
		final int cusnbr = pln.getProject().getCustomer().getCusNumber();
		final long proId = pln.getProject().getProId();
		//final long perId = per.getPerId();
		BillDto bill = null;

		if (! this._billMap.containsKey(proId)) {
			bill = new BillDto();
			bill.setCustomerNbr(cusnbr);
			bill.setCustomer(pln.getProject().getCustomer());
			bill.setProject(pln.getProject());
			bill.setCostaccount(per.getCostAccount());
			bill.setPeriode(per);
			this._billMap.put(proId, bill);
		} else {
			bill = this._billMap.get(proId);
		}
		return bill;
	}

	private List<BillDto> calculateTotalHeader2List() {
		final List<BillDto> rlist = new ArrayList<>(this._billMap.values());

		for (final BillDto billDto : rlist) {
			final Double amt = getAmountFromLine(billDto.getLines());
			billDto.setTotalAmount(amt);
		}

		return rlist;

	}

	private Double getAmountFromLine(final List<BillLine> list) {
		Double retVal = new Double(0);

		if (list!= null && ! list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				final BillLine tmp = list.get(i);
				retVal = retVal + tmp.getAmount();
			}
		}

		return retVal;
	}

	private void cumulateLine(final BillDto bill, final ProjectLine pln) {
		final Item itm = getItem(pln);
		final List<BillLine> list = bill.getLines();

		BillLine line = null;
		if (! list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				final BillLine tmp = list.get(i);
				if (tmp.getRate().equals(pln.getPrlRate()) &&
						tmp.getCostaccount().getCsaId().equals(pln.getPeriode().getCostAccount().getCsaId()) &&
						tmp.getItem().getItmId().equals(itm.getItmId())) {
					line = list.get(i);
					line.setHours(line.getHours() + pln.getPrlHours());
				}
			}
		}

		if (line == null) {
			final String text = getLineText(bill, pln);
			list.add(getEmptyLine(pln, itm, text));  //either empty or missing criteria
		}

	}

	private String getLineText(final BillDto billDto, final ProjectLine pln) {
		String text = null;

		if (pln.getPrlWorkType().equals(LovState.WorkType.journey)) {  //Reisezeit
			text = this._guifld.getLineTextJourney();
		} else if (pln.getPrlWorkType().equals(LovState.WorkType.expense)) {  //Spesen
			text = this._guifld.getLineTextExpense();
		} else {
			text = this._guifld.getLineTextProject();
		}

		if (text == null) {
			text = "no text!";
		}

		final String text2 = disolveLineText(billDto, pln.getPeriode().getCostAccount(), text);
		return text2;
	}

	private Item getItem(final ProjectLine pln) {
		Item itm = null;

		if (pln.getPrlWorkType().equals(LovState.WorkType.journey)) {  //Reisezeit
			itm = this._guifld.getItemJourney();
		} else if (pln.getPrlWorkType().equals(LovState.WorkType.expense)) {  //Spesen
			itm = this._guifld.getItemExpense();
		} else {
			itm = this._guifld.getItemProject();
		}

		return itm;
	}


	private BillLine getEmptyLine(final ProjectLine pln, final Item itm, final String text) {
		final BillLine line = new BillLine();
		line.setRate(pln.getPrlRate());
		line.setWorkType(pln.getPrlWorkType());
		line.setCostaccount(pln.getPeriode().getCostAccount());
		line.setHours(pln.getPrlHours());
		line.setItem(itm);
		line.setText(text);

		return line;
	}

	public Order createBill(final BillDto billDto, final GuiGeneratorFields guifld) {
		this._calc = new OrderCalculator();
		this._guifld = guifld;
		final Integer ordNbr = this._calc.getNewOrderNumber(false, 0);

		final OrderDAO dao = new OrderDAO();
		final Order hdr = getNewOrderWithDefaults(guifld, billDto);
		hdr.setOrdNumber(ordNbr);
		dao.save(hdr);

		//commit ordnbr
		this._calc.getNewOrderNumber(true, ordNbr); //commit ordnbr

		createPositions(hdr, billDto);
		this._calc.calculateHeader(hdr);

		//create objRoot
		final RowObjectManager man = new RowObjectManager();
		man.updateObject(hdr.getOrdId(), hdr.getClass().getSimpleName());

		_logger.debug("New Order Created " + hdr.getOrdId());

		return hdr;
	}

	private void createPositions(final Order hdr, final BillDto billDto) {
		createPosition(hdr, billDto, billDto.getLines());
	}

	private void createPosition(final Order hdr, final BillDto billDto, final List<BillLine> list) {
		final OrderLineDAO dao = new OrderLineDAO();

		if (list!= null && ! list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				final BillLine tmp = list.get(i);

				final OrderLine pos = new OrderLine();
				pos.setOrderhdr(hdr);
				pos.setCostAccount(tmp.getCostaccount());
				pos.setOdlPrice(tmp.getRate());
				pos.setOdlQuantity(tmp.getHours());
				pos.setOdlState(LovState.State.active);
				pos.setItem(tmp.getItem());
				pos.setOdlText(tmp.getText());

				pos.setVat(hdr.getProject().getVat());
				pos.setOdlNumber(this._calc.getNextLineNumber(hdr));

				final OrderLine newodl = this._calc.calculateLine(pos);
				dao.save(newodl);

				//create objRoot
				final RowObjectManager man = new RowObjectManager();
				man.updateObject(newodl.getOdlId(), newodl.getClass().getSimpleName());

			}

		}

	}


	private Order getNewOrderWithDefaults(final GuiGeneratorFields guifld, final BillDto billDto) {
		final Order dao = new Order();

		dao.setOrdState(LovState.State.active);
		dao.setOrdOrderDate(new Date());
		dao.setOrdAmountBrut(new Double(0.));
		dao.setOrdAmountNet(new Double(0.));

		dao.setOrdCreated(new Date());
		dao.setOrdCreatedBy(Seicento.getUserName());
		//guifld
		dao.setOrdBillDate(guifld.getBillDate());
		dao.setOrdText(disolveHeaderText(billDto, guifld.getBillText()));

		if (guifld.getCopyTextFromLastBill().booleanValue()) {
			final String lastText = lookupLastBill(billDto);
			if (!lastText.isEmpty()) {
				dao.setOrdText(lastText);
			}
		}
		//billDto
		dao.setCustomer(billDto.getCustomer());
		dao.setPaymentCondition(billDto.getCustomer().getPaymentCondition());
		dao.setProject(billDto.getProject());

		return dao;
	}

	private String lookupLastBill(final BillDto billDto) {
		final OrderDAO dao = new OrderDAO();
		final List<Order> list = dao.findByCustomer(billDto.getCustomer());

		int icount=0;
		for (final Iterator<Order> iterator = list.iterator(); iterator.hasNext();) {
			final Order order = iterator.next();
			if (order.getProject().getProId().equals(billDto.getProject().getProId())) {
				return order.getOrdText();
			}
			icount++;
			if (icount > 20) {
				return "";
			}
		}

		return "";
	}

	private String disolveHeaderText(final BillDto billDto, String text) {
		String contact = "";
		if (billDto.getProject().getProContact() != null) {
			contact = billDto.getProject().getProContact();
		}

		String monthText = LovState.Month.fromId(billDto.getPeriode().getPerMonth().getValue()).name();
		monthText = monthText.substring(0, 1).toUpperCase() + monthText.substring(1);

        text = text.replace("{proExtReference}", getText(billDto.getProject().getProExtReference()));
        text = text.replace("{proName}", billDto.getProject().getProName().trim());
        text = text.replace("{proContact}", contact.trim());
        //subject = subject.replace("#", "%23");
        text = text.replace("{csaName}", billDto.getCostaccount().getCsaName().trim());
        text = text.replace("{perYear}", billDto.getPeriode().getPerYear().toString());
        text = text.replace("{perMonth}", "" + billDto.getPeriode().getPerMonth().getValue());
        text = text.replace("{perMonthText}", monthText);

		return text;
	}

	private String getText(final String input) {
		if (input == null || input.isEmpty()) {
			return "";
		}

		return input.trim();
	}

	private String disolveLineText(final BillDto billDto, final CostAccount cst, String text) {
		String monthText = LovState.Month.fromId(billDto.getPeriode().getPerMonth().getValue()).name();
		monthText = monthText.substring(0, 1).toUpperCase() + monthText.substring(1);

        text = text.replace("{csaName}", cst.getCsaName().trim());
        text = text.replace("{csaCode}", cst.getCsaCode().trim());
        text = text.replace("{perYear}", billDto.getPeriode().getPerYear().toString());
        text = text.replace("{perMonth}", "" + billDto.getPeriode().getPerMonth().getValue());
        text = text.replace("{perMonthText}", monthText);


		return text;
	}

}

//final class _HashDto {
//	public long proId;
//	public long perId;
//
//	public _HashDto(final long proId, final long perId) {
//		this.proId = proId;
//		this.perId = 1; //perId;
//	}
//}