
package ch.xwr.seicentobilling.ui;

import java.util.ArrayList;
import java.util.List;

import ch.xwr.seicentobilling.business.model.billing.BillDto;


public class OrderGenTreeItem
{
	private String                 cusName;
	private String                 proName;
	private String                 totalAmount;
	private String                 ldate;
	private Boolean                cbo;
	private BillDto                object;
	private List<OrderGenTreeItem> childItems = new ArrayList<OrderGenTreeItem>();
	
	public OrderGenTreeItem(
		final String cusName,
		final String proName,
		final String totalAmount,
		final String ldate,
		final Boolean cbo,
		final BillDto object)
	{
		this.setCusName(cusName);
		this.setProName(proName);
		this.setTotalAmount(totalAmount);
		this.setLdate(ldate);
		this.setCbo(cbo);
		this.setObject(object);
	}

	public String getCusName()
	{
		return this.cusName;
	}

	public void setCusName(final String cusName)
	{
		this.cusName = cusName;
	}

	public String getProName()
	{
		return this.proName;
	}

	public void setProName(final String proName)
	{
		this.proName = proName;
	}

	public String getTotalAmount()
	{
		return this.totalAmount;
	}
	
	public void setTotalAmount(final String totalAmount)
	{
		this.totalAmount = totalAmount;
	}
	
	public String getLdate()
	{
		return this.ldate;
	}

	public void setLdate(final String ldate)
	{
		this.ldate = ldate;
	}
	
	public BillDto getObject()
	{
		return this.object;
	}
	
	public void setObject(final BillDto object)
	{
		this.object = object;
	}
	
	public Boolean getCbo()
	{
		return this.cbo;
	}
	
	public void setCbo(final Boolean cbo)
	{
		this.cbo = cbo;
	}
	
	public List<OrderGenTreeItem> getChildItems()
	{
		return this.childItems;
	}
	
	public void setChildItems(final List<OrderGenTreeItem> childItems)
	{
		this.childItems = childItems;
	}
	
}
