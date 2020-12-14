
package ch.xwr.seicentobilling.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ch.xwr.seicentobilling.entities.Periode;


public class ProjectLineOverviewItemData
{
	List<ProjectLineOverviewItem> projectLineOverviewData = new ArrayList<>();

	public ProjectLineOverviewItemData(
		final Calendar cal,
		final int daysInMonth,
		final Periode per,
		final double[] hours)
	{
		for(int i = 1; i <= daysInMonth; i++)
		{
			cal.set(per.getPerYear(), per.getPerMonth().getValue() - 1, i);
			final SimpleDateFormat format1 = new SimpleDateFormat("EEE dd.MM.yyyy");
			final int              iday2   = cal.get(Calendar.DAY_OF_WEEK);

			final ProjectLineOverviewItem pi = new ProjectLineOverviewItem();
			pi.setDatum(format1.format(cal.getTime()));
			pi.setStunden("" + hours[i]);

			if(iday2 == Calendar.SATURDAY || iday2 == Calendar.SUNDAY)
			{
				pi.setDatumStyle("colored bold");

				if(hours[i] == 0.)
				{
					pi.setStunden("");
				}
			}
			else
			{
				if(hours[i] < 8.)
				{
					pi.setStundenStyle("failure");
				}
				else
				{
					pi.setStundenStyle("success");
				}
			}
			this.projectLineOverviewData.add(pi);
			
		}
	}

	public List<ProjectLineOverviewItem> getProjectLineOverviewData()
	{
		return this.projectLineOverviewData;
	}

	public List<ProjectLineOverviewItem> getGrid1RootItems()
	{
		return this.projectLineOverviewData.subList(0, 14);
	}

	public List<ProjectLineOverviewItem> getGrid2RootItems()
	{
		return this.projectLineOverviewData.subList(15, this.projectLineOverviewData.size());
	}
	
	public List<ProjectLineOverviewItem> getChildItems(final ProjectLineOverviewItem parent)
	{
		return new ArrayList<>();
	}

}
