
package ch.xwr.seicentobilling.ui;

public class ProjectLineOverviewItem
{
	String datum;
	String datumStyle   = "";
	String stunden;
	String stundenStyle = "";
	
	public String getDatum()
	{
		return this.datum;
	}
	
	public void setDatum(final String datum)
	{
		this.datum = datum;
	}
	
	public String getStunden()
	{
		return this.stunden;
	}
	
	public void setStunden(final String stunden)
	{
		this.stunden = stunden;
	}

	public String getDatumStyle()
	{
		return this.datumStyle;
	}

	public void setDatumStyle(final String datumStyle)
	{
		this.datumStyle = datumStyle;
	}

	public String getStundenStyle()
	{
		return this.stundenStyle;
	}

	public void setStundenStyle(final String stundenStyle)
	{
		this.stundenStyle = stundenStyle;
	}
	
}
