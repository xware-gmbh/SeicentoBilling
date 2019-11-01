package ch.xwr.seicentobilling.business;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.vaadin.external.org.slf4j.Logger;
import com.vaadin.external.org.slf4j.LoggerFactory;
import com.vaadin.ui.Field;
import com.xdev.persistence.PersistenceUtils;
import com.xdev.ui.XdevFieldGroup;

import ch.xwr.seicentobilling.dal.OrderDAO;
import ch.xwr.seicentobilling.dal.OrderLineDAO;
import ch.xwr.seicentobilling.dal.VatLineDAO;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.OrderLine;
import ch.xwr.seicentobilling.entities.Vat;
import ch.xwr.seicentobilling.entities.VatLine;

public class OrderCalculator {
	/** Logger initialized */
	private static final Logger _logger = LoggerFactory.getLogger(OrderCalculator.class);


	public OrderLine calculateLine(final OrderLine inp) {
		if (0 == inp.getOdlQuantity()|| null == inp.getOdlPrice() || 0 == inp.getOdlPrice().doubleValue()) {
			inp.setOdlAmountBrut(new Double(0));
			inp.setOdlAmountNet(new Double(0));
			inp.setOdlVatAmount(new Double(0));

			return inp;
		}

		final Double val1 = inp.getOdlQuantity() * inp.getOdlPrice().doubleValue();
		final Double vat1 = getVatAmount(inp.getOrderhdr().getOrdBillDate(), val1, inp.getVat());
		Double val2 = val1 + vat1;		//excl.
		if (null !=inp.getVat() && inp.getVat().getVatInclude() == true) {
			val2 = val1;
		}
		val2 = swissCommercialRound(new BigDecimal(val2));

		inp.setOdlAmountBrut(val1);
		inp.setOdlVatAmount(vat1);
		inp.setOdlAmountNet(val2);

		return inp;
	}

	public boolean isOrderValid(final Order bean) {
		final OrderDAO dao1 = new OrderDAO();
		final Order bean1 = dao1.find(bean.getOrdId());

		final OrderLineDAO dao = new OrderLineDAO();
		final List<OrderLine> olst = dao.findByOrder(bean);

		Double amt1 = new Double(0);
		Double amt2	= new Double(0);
		//Double vat = new Double(0);
		for (final OrderLine orderLine : olst) {
			amt1 = amt1 + orderLine.getOdlAmountBrut();
			amt2 = amt2 + orderLine.getOdlAmountNet();
			//vat = vat + orderLine.getOdlVatAmount();
		}

		if (Double.compare(amt1, bean1.getOrdAmountBrut()) != 0) {
			return false;
		}
		if (Double.compare(amt2, bean1.getOrdAmountNet()) != 0) {
			return false;
		}
//		if (Double.compare(vat, bean1.getOrdAmountVat()) != 0) {
//			return false;
//		}

		return true;
	}

    private Double getVatAmount(final Date date, final Double val1, final Vat vat) {
    	if (vat == null  || val1 == null) {
			return new Double(0);
		}

    	final Date refDate = getRawDate(date);

    	//TFS-240
    	final VatLineDAO dao = new VatLineDAO();
    	final List<VatLine> lst = dao.findByVatAndDate(vat, refDate);
    	if (lst == null || lst.isEmpty()) {
    		System.out.println("No VatLine found");
    		_logger.warn("no VatLine found for vat: " + vat.getVatName() + " and Date: " + date);
			return new Double(0);
    	}

    	final VatLine vl = lst.get(0);
    	final double rate = vl.getVanRate().doubleValue();
    	double base = 100.;

		if (vat.getVatInclude() == true) {
			base = base + rate;
		}
    	final Double vat1 = val1 / base * rate;

		return swissCommercialRound(new BigDecimal(vat1));
	}

    //remove time
    private Date getRawDate(final Date date) {
    	try {
    		final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    		final Date rawdate = sdf.parse(sdf.format(date));
    		return rawdate;
    	} catch (final Exception e) {

    	}

    	return date;

    }

//	private Double computePercentage(final Double input, final double d1) {
//    	//Double output = input.divide(new Double(d1), RoundingMode.HALF_DOWN);
//    	final Double output = input/d1;
//        return swissCommercialRound(new BigDecimal(output));
//    }

    private Double swissCommercialRound(final BigDecimal input)
    {
    	final double ans = java.lang.Math.round((input.doubleValue() / 0.05)) * 0.05;
    	final BigDecimal value = new BigDecimal(ans).setScale(2, RoundingMode.HALF_DOWN);

        return value.doubleValue();
    }

	public Integer getNewOrderNumber(final boolean commit, final Integer nbr) {
		final NumberRangeHandler handler = new NumberRangeHandler();
		return handler.getNewOrderNumber(commit, nbr);
	}

	public Order calculateHeader(final Order bean) {
		final OrderLineDAO dao = new OrderLineDAO();
		final List<OrderLine> olst = dao.findByOrder(bean);

		Double amt1 = new Double(0);
		Double amt2	= new Double(0);
		Double vat = new Double(0);
		for (final OrderLine orderLine : olst) {
			if (orderLine.getOdlAmountBrut() != null) {
				amt1 = amt1 + orderLine.getOdlAmountBrut();
			}
			if (orderLine.getOdlAmountNet() != null) {
				amt2 = amt2 + orderLine.getOdlAmountNet();
			}
			if (orderLine.getOdlVatAmount() != null) {
				vat = vat + orderLine.getOdlVatAmount();
			}
		}

		bean.setOrdAmountBrut(amt1);
		bean.setOrdAmountNet(amt2);
		//bean.setOrd(vat);
        //C# result = Convert.ToDecimal(ordAmountNet - ordAmountBrut);


		return bean;
	}

	public int getNextLineNumber(final Order bean) {
		final OrderLineDAO dao = new OrderLineDAO();
		final List<OrderLine> olst = dao.findByOrder(bean);

		int ipos = 10;

		if (olst.size() > 0) {
			final OrderLine odl = olst.get(olst.size() - 1);
			ipos = odl.getOdlNumber() + 10;
		}

		return ipos;
	}

	public void commitFields(final XdevFieldGroup<?> fieldGroup) {
		final Collection<Field<?>> fld = fieldGroup.getFields();
		for (final Iterator<Field<?>> iterator = fld.iterator(); iterator.hasNext();) {
			final Field<?> field = iterator.next();

//			final Object obj2= fieldGroup.getPropertyId(object);
//			System.out.println(object.toString() + " " + obj2 + " / " + object.getValue());

			if (field.getValue() != null) {
				field.commit();  //writes fieldvalue to bean
			}
		}
	}

	public Order copyOrder(final Order bean) {
		final OrderDAO dao = new OrderDAO();
		final Integer newOrdNbr = getNewOrderNumber(false, 0);
		final Order source = dao.find(bean.getOrdId());

		if (dao.isAttached(bean)) {
			PersistenceUtils.getEntityManager(Order.class).detach(bean);
		}

		bean.setOrdId(new Long(0));
		bean.setOrdNumber(newOrdNbr);
		bean.setOrdBillDate(new Date());
		bean.setOrdCreated(new Date());
		bean.setOrdCreatedBy(Seicento.getUserName());
		bean.setOrdOrderDate(new Date());
		bean.setOrdPayDate(null);
		bean.setOrdBookedOn(null);

		final Order newBean = new OrderDAO().merge(bean);
		dao.save(newBean);
		getNewOrderNumber(true, newOrdNbr);

		final RowObjectManager man = new RowObjectManager();
		man.updateObject(newBean.getOrdId(), newBean.getClass().getSimpleName());

		copyOrderLines(source, newBean);

		return newBean;
	}

	private void copyOrderLines(final Order source, final Order target) {
		final OrderLineDAO dao = new OrderLineDAO();
		final List<OrderLine> olst = dao.findByOrder(source);

		for (final OrderLine orderLine : olst) {

			if (dao.isAttached(orderLine)) {
				PersistenceUtils.getEntityManager(Order.class).detach(orderLine);
			}

			orderLine.setOrderhdr(target);
			orderLine.setOdlId(new Long(0));

			final OrderLine newBean = dao.merge(orderLine);
			dao.save(newBean);

			final RowObjectManager man = new RowObjectManager();
			man.updateObject(newBean.getOdlId(), newBean.getClass().getSimpleName());

		}

	}

}
