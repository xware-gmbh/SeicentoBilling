
package ch.xwr.seicentobilling.business;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import ch.xwr.seicentobilling.business.helper.VatHelper;
import ch.xwr.seicentobilling.dal.OrderDAO;
import ch.xwr.seicentobilling.dal.OrderLineDAO;
import ch.xwr.seicentobilling.entities.Order;
import ch.xwr.seicentobilling.entities.OrderLine;
import ch.xwr.seicentobilling.entities.Vat;


public class OrderCalculator
{
	/** Logger initialized */
	private static final org.apache.log4j.Logger _logger = org.apache.log4j.Logger.getLogger(OrderCalculator.class);
	
	public OrderLine calculateLine(final OrderLine inp)
	{
		if(0 == inp.getOdlQuantity() || null == inp.getOdlPrice() || 0 == inp.getOdlPrice().doubleValue())
		{
			inp.setOdlAmountBrut(new Double(0));
			inp.setOdlAmountNet(new Double(0));
			inp.setOdlVatAmount(new Double(0));
			
			return inp;
		}
		
		final Double val1 = inp.getOdlQuantity() * inp.getOdlPrice().doubleValue();
		final Double vat1 = this.getVatAmount(inp.getOrderhdr().getOrdBillDate(), val1, inp.getVat());
		Double       val2 = val1 + vat1;                                                              // excl.
		if(null != inp.getVat() && inp.getVat().getVatInclude() == true)
		{
			val2 = val1;
		}
		val2 = this.swissCommercialRound(new BigDecimal(val2));
		
		inp.setOdlAmountBrut(val1);
		inp.setOdlVatAmount(vat1);
		inp.setOdlAmountNet(val2);
		
		return inp;
	}
	
	public boolean isOrderValid(final Order bean)
	{
		final OrderDAO dao1  = new OrderDAO();
		final Order    bean1 = dao1.find(bean.getOrdId());
		
		final OrderLineDAO    dao  = new OrderLineDAO();
		final List<OrderLine> olst = dao.findByOrder(bean);
		
		Double amt1 = new Double(0);
		Double amt2 = new Double(0);
		// Double vat = new Double(0);
		for(final OrderLine orderLine : olst)
		{
			amt1 = amt1 + orderLine.getOdlAmountBrut();
			amt2 = amt2 + orderLine.getOdlAmountNet();
			// vat = vat + orderLine.getOdlVatAmount();
		}
		amt1 = new BigDecimal(amt1).setScale(2, RoundingMode.HALF_UP).doubleValue();
		amt2 = new BigDecimal(amt2).setScale(2, RoundingMode.HALF_UP).doubleValue();
		
		amt1 = new BigDecimal(amt1).setScale(2, RoundingMode.HALF_UP).doubleValue();
		amt2 = new BigDecimal(amt2).setScale(2, RoundingMode.HALF_UP).doubleValue();
		
		if(Double.compare(amt1, bean1.getOrdAmountBrut()) != 0)
		{
			return false;
		}
		if(Double.compare(amt2, bean1.getOrdAmountNet()) != 0)
		{
			return false;
		}
		// if (Double.compare(vat, bean1.getOrdAmountVat()) != 0) {
		// return false;
		// }
		
		return true;
	}
	
	private Double getVatAmount(final Date date, final Double val1, final Vat vat)
	{
		if(vat == null || val1 == null)
		{
			return new Double(0);
		}
		
		final VatHelper vhl  = new VatHelper();
		final Double    vat1 = vhl.getVatAmount(date, val1, vat);
		return this.swissCommercialRound(new BigDecimal(vat1));
		
	}
	
	// private Double computePercentage(final Double input, final double d1) {
	// //Double output = input.divide(new Double(d1), RoundingMode.HALF_DOWN);
	// final Double output = input/d1;
	// return swissCommercialRound(new BigDecimal(output));
	// }
	
	private Double swissCommercialRound(final BigDecimal input)
	{
		final double     ans   = java.lang.Math.round((input.doubleValue() / 0.05)) * 0.05;
		final BigDecimal value = new BigDecimal(ans).setScale(2, RoundingMode.HALF_DOWN);
		
		return value.doubleValue();
	}
	
	public Integer getNewOrderNumber(final boolean commit, final Integer nbr)
	{
		final NumberRangeHandler handler = new NumberRangeHandler();
		return handler.getNewOrderNumber(commit, nbr);
	}
	
	public Order calculateHeader(final Order bean)
	{
		final OrderLineDAO    dao  = new OrderLineDAO();
		final List<OrderLine> olst = dao.findByOrder(bean);
		
		Double amt1 = new Double(0);
		Double amt2 = new Double(0);
		Double vat  = new Double(0);
		for(final OrderLine orderLine : olst)
		{
			if(orderLine.getOdlAmountBrut() != null)
			{
				amt1 = amt1 + orderLine.getOdlAmountBrut();
			}
			if(orderLine.getOdlAmountNet() != null)
			{
				amt2 = amt2 + orderLine.getOdlAmountNet();
			}
			if(orderLine.getOdlVatAmount() != null)
			{
				vat = vat + orderLine.getOdlVatAmount();
			}
		}
		
		bean.setOrdAmountBrut(amt1);
		bean.setOrdAmountNet(amt2);
		// bean.setOrd(vat);
		// C# result = Convert.ToDecimal(ordAmountNet - ordAmountBrut);
		
		return bean;
	}
	
	public int getNextLineNumber(final Order bean)
	{
		final OrderLineDAO    dao  = new OrderLineDAO();
		final List<OrderLine> olst = dao.findByOrder(bean);
		
		int ipos = 10;
		
		if(olst.size() > 0)
		{
			final OrderLine odl = olst.get(olst.size() - 1);
			ipos = odl.getOdlNumber() + 10;
		}
		
		return ipos;
	}
	
	// public void commitFields(final XdevFieldGroup<?> fieldGroup) {
	// final Collection<Field<?>> fld = fieldGroup.getFields();
	// for (final Iterator<Field<?>> iterator = fld.iterator(); iterator.hasNext();) {
	// final Field<?> field = iterator.next();
	//
	//// final Object obj2= fieldGroup.getPropertyId(object);
	//// System.out.println(object.toString() + " " + obj2 + " / " + object.getValue());
	//
	// if (field.getValue() != null) {
	// field.commit(); //writes fieldvalue to bean
	// }
	// }
	// }
	
	public Order copyOrder(final Order bean)
	{
		OrderCalculator._logger.debug("start copyOrder: " + bean.getOrdNumber());
		final OrderDAO dao       = new OrderDAO();
		final Integer  newOrdNbr = this.getNewOrderNumber(false, 0);
		final Order    source    = dao.find(bean.getOrdId());
		
		// if (dao.isAttached(bean)) {
		// PersistenceUtils.getEntityManager(Order.class).detach(bean);
		// }
		
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
		this.getNewOrderNumber(true, newOrdNbr);
		
		final RowObjectManager man = new RowObjectManager();
		man.updateObject(newBean.getOrdId(), newBean.getClass().getSimpleName());
		
		this.copyOrderLines(source, newBean);
		
		return newBean;
	}
	
	private void copyOrderLines(final Order source, final Order target)
	{
		final OrderLineDAO    dao  = new OrderLineDAO();
		final List<OrderLine> olst = dao.findByOrder(source);
		
		for(final OrderLine orderLine : olst)
		{
			
			// if (dao.isAttached(orderLine)) {
			// PersistenceUtils.getEntityManager(Order.class).detach(orderLine);
			// }
			
			orderLine.setOrderhdr(target);
			orderLine.setOdlId(new Long(0));
			
			final OrderLine newBean = dao.merge(orderLine);
			dao.save(newBean);
			
			final RowObjectManager man = new RowObjectManager();
			man.updateObject(newBean.getOdlId(), newBean.getClass().getSimpleName());
			
		}
		
	}
	
}
