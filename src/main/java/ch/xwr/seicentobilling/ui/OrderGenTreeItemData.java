
package ch.xwr.seicentobilling.ui;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.xwr.seicentobilling.business.model.billing.BillDto;
import ch.xwr.seicentobilling.business.model.billing.BillLine;
import ch.xwr.seicentobilling.dal.OrderDAO;
import ch.xwr.seicentobilling.entities.Order;


public class OrderGenTreeItemData
{
	List<OrderGenTreeItem> orderData = new ArrayList<>();

	public OrderGenTreeItemData(final List<BillDto> lst)
	{
		this.orderData = this.convertToParentItem(lst);
	}
	
	public List<OrderGenTreeItem> getOrderData()
	{
		return this.orderData;
	}

	public void setOrderData(final List<OrderGenTreeItem> orderData)
	{
		this.orderData = orderData;
	}
	
	public List<OrderGenTreeItem> getRootOrderItems()
	{
		return this.orderData;
	}

	public List<OrderGenTreeItem> getChildOrderItems(final OrderGenTreeItem parent)
	{
		return parent.getChildItems();
	}
	
	private List<OrderGenTreeItem> convertToParentItem(final List<BillDto> lst)
	{
		final List<OrderGenTreeItem> pList = new ArrayList<>();

		for(final BillDto billDto : lst)
		{
			Boolean cbo = true;
			if(billDto.getProject().getInternal() == null || billDto.getProject().getInternal())
			{
				cbo = false;
			}
			final OrderGenTreeItem       p          =
				new OrderGenTreeItem(billDto.getCustomer().getFullname(), billDto.getProject().getProName(),
					this.getAmtString(billDto.getTotalAmount(), true), this.getLastBillDate(billDto), cbo, billDto);
			final List<OrderGenTreeItem> childItems = new ArrayList<>();
			final OrderGenTreeItem       sp         = this.getDetailGridLine("Spesen", billDto.getExpenseHours());
			if(sp != null)
			{
				childItems.add(sp);
			}
			final OrderGenTreeItem re = this.getDetailGridLine("Reisezeit", billDto.getJourneyHours());
			if(re != null)
			{
				childItems.add(re);
			}
			final OrderGenTreeItem di = this.getDetailGridLine("Dienstleistung", billDto.getProjectHours());
			if(di != null)
			{
				childItems.add(di);
			}
			p.setChildItems(childItems);
			
			pList.add(p);
			
		}
		return pList;
	}
	
	private String getLastBillDate(final BillDto billDto)
	{
		final String     pattern = "dd.MM.yyyy";
		final DateFormat df      = new SimpleDateFormat(pattern);

		final OrderDAO    dao = new OrderDAO();
		final List<Order> lst = dao.findByCustomer(billDto.getCustomer());
		for(final Iterator<Order> iterator = lst.iterator(); iterator.hasNext();)
		{
			final Order order = iterator.next();
			if(order.getProject() != null)
			{
				if(order.getProject().getProId().equals(billDto.getProject().getProId()))
				{
					return df.format(order.getOrdBillDate());
				}
			}
		}

		return "";
	}

	private String getAmtString(final Double amount, final boolean currency)
	{
		final DecimalFormat decimalFormat  = new DecimalFormat("#,##0.00");
		final String        numberAsString = "            " + decimalFormat.format(amount);
		final int           ilen           = numberAsString.length();
		final String        retval         = numberAsString.substring(ilen - 11);
		if(currency)
		{
			return "CHF" + retval;
		}
		return retval;
	}

	private OrderGenTreeItem getDetailGridLine(final String text, final List<BillLine> list)
	{
		Double amt = new Double(0);
		if(list != null && !list.isEmpty())
		{
			for(int i = 0; i < list.size(); i++)
			{
				final BillLine tmp = list.get(i);
				amt = amt + tmp.getAmount();
			}
		}

		if(amt > 0)
		{
			final String           samt = this.getAmtString(amt, false);
			final OrderGenTreeItem ogt  = new OrderGenTreeItem("", text, samt, "", false, null);
			
			return ogt;
		}

		return null;
	}
}
