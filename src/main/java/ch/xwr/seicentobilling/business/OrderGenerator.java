package ch.xwr.seicentobilling.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import ch.xwr.seicentobilling.business.model.billing.BillDto;
import ch.xwr.seicentobilling.business.model.billing.BillLine;
import ch.xwr.seicentobilling.business.model.billing.GuiGeneratorFields;
import ch.xwr.seicentobilling.dal.OrderDAO;
import ch.xwr.seicentobilling.dal.OrderLineDAO;
import ch.xwr.seicentobilling.dal.ProjectLineDAO;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.OrderLine;
import ch.xwr.seicentobilling.entities.Periode;
import ch.xwr.seicentobilling.entities.ProjectLine;

public class OrderGenerator {
	private final HashMap<Long, BillDto> _billMap = new HashMap<>();
	private OrderCalculator _calc = null;
	private GuiGeneratorFields _guifld = null;
	/** Logger initialized */
	private static final org.apache.log4j.Logger _logger = org.apache.log4j.Logger.getLogger(OrderGenerator.class);


	public List<BillDto> proposeDraft(final Periode inp) {
		final ProjectLineDAO dao = new ProjectLineDAO();
		final List<ProjectLine> list = dao.findByPeriode(inp);

		for (final Iterator<ProjectLine> iterator = list.iterator(); iterator.hasNext();) {
			final ProjectLine pln = iterator.next();
			final int cusnbr = pln.getProject().getCustomer().getCusNumber();
			final long proId = pln.getProject().getProId();
			BillDto bill = null;
			if (! this._billMap.containsKey(proId)) {
				bill = new BillDto();
				bill.setCustomerNbr(cusnbr);
				bill.setCustomer(pln.getProject().getCustomer());
				bill.setProject(pln.getProject());
				bill.setCostaccount(pln.getPeriode().getCostAccount());
				bill.setPeriode(inp);
				this._billMap.put(proId, bill);
			} else {
				bill = this._billMap.get(proId);
			}

			cumulateLine(bill, pln);
		}

		final List<BillDto> rlist = calculateTotalHeader2List();
		return rlist;
	}

	private List<BillDto> calculateTotalHeader2List() {
		final List<BillDto> rlist = new ArrayList<>(this._billMap.values());

		for (final BillDto billDto : rlist) {
			final Double amt1 = getAmountFromLine(billDto.getExpenseHours());
			final Double amt2 = getAmountFromLine(billDto.getJourneyHours());
			final Double amt3 = getAmountFromLine(billDto.getProjectHours());

			billDto.setTotalAmount(amt1 + amt2 + amt3);
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
		if (pln.getPrlWorkType().equals(LovState.WorkType.journey)) {  //Reisezeit
			checkListEntry(bill.getJourneyHours(), pln);
		} else if (pln.getPrlWorkType().equals(LovState.WorkType.expense)) {  //Spesen
			checkListEntry(bill.getExpenseHours(), pln);
		} else {
			checkListEntry(bill.getProjectHours(), pln);
		}
	}

	private void checkListEntry(final List<BillLine> list, final ProjectLine pln) {
		BillLine line = null;
		if (! list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				final BillLine tmp = list.get(i);
				if (tmp.getRate().equals(pln.getPrlRate()) &&
						tmp.getCostaccount().getCsaId().equals(pln.getPeriode().getCostAccount().getCsaId())) {
					line = list.get(i);
					line.setHours(line.getHours() + pln.getPrlHours());
				}
			}
		}

		if (line == null) {
			list.add(getEmptyLine(pln));  //either empty or missing criteria
		}

	}

	private BillLine getEmptyLine(final ProjectLine pln) {
		final BillLine line = new BillLine();
		line.setRate(pln.getPrlRate());
		line.setWorkType(pln.getPrlWorkType());
		line.setCostaccount(pln.getPeriode().getCostAccount());
		line.setHours(pln.getPrlHours());

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
		createPosition(hdr, billDto, billDto.getProjectHours(), 0);
		createPosition(hdr, billDto, billDto.getJourneyHours(), 2);
		createPosition(hdr, billDto, billDto.getExpenseHours(), 1);
	}

	private void createPosition(final Order hdr, final BillDto billDto, final List<BillLine> list, final int flag) {
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

				switch (flag) {
				case 0:
					pos.setItem(this._guifld.getItemProject());
					pos.setOdlText(disolveLineText(billDto, tmp, this._guifld.getLineTextProject()));
					break;
				case 1:
					pos.setItem(this._guifld.getItemExpense());
					pos.setOdlText(disolveLineText(billDto, tmp, this._guifld.getLineTextExpense()));
					break;
				case 2:
					pos.setItem(this._guifld.getItemJourney());
					pos.setOdlText(disolveLineText(billDto, tmp, this._guifld.getLineTextJourney()));
					break;
				}

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

	private String disolveLineText(final BillDto billDto, final BillLine tmp, String text) {
		String monthText = LovState.Month.fromId(billDto.getPeriode().getPerMonth().getValue()).name();
		monthText = monthText.substring(0, 1).toUpperCase() + monthText.substring(1);

        text = text.replace("{csaName}", tmp.getCostaccount().getCsaName().trim());
        text = text.replace("{csaCode}", tmp.getCostaccount().getCsaCode().trim());
        text = text.replace("{perYear}", billDto.getPeriode().getPerYear().toString());
        text = text.replace("{perMonth}", "" + billDto.getPeriode().getPerMonth().getValue());
        text = text.replace("{perMonthText}", monthText);


		return text;
	}

}
