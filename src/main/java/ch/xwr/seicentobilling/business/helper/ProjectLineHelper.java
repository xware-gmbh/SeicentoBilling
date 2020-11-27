package ch.xwr.seicentobilling.business.helper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

public class ProjectLineHelper {

	/**
	 * return 5 minute rounded Time
	 * @param dateRaw
	 * @return
	 */
	public Date getStartStopTime(final Date dateRaw) {
		if (dateRaw == null) {
			return null;
		}
		final Calendar c1 = Calendar.getInstance();
		c1.setTime(dateRaw);
		final Calendar c2 = Calendar.getInstance();
		final Date d2 = new Date();  //now
		c2.setTime(d2);

		final int iminutes = c2.get(Calendar.MINUTE);
		final Calendar c3 = Calendar.getInstance();
		c3.set(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), c1.get(Calendar.DAY_OF_MONTH), c2.get(Calendar.HOUR_OF_DAY), iminutes);
		final int mod = iminutes % 5;
		c3.add(Calendar.MINUTE, iminutes == 0 ? -5 : -mod);
		c3.set(Calendar.SECOND, 0);
		c3.set(Calendar.MILLISECOND, 0);

		return c3.getTime();
	}

	/*
	 * get time bases of date of projectline date
	 */
	public Date getDateCorrect(final Date d1, final Date dateTm) {
		final Calendar c1 = Calendar.getInstance();
		c1.setTime(d1);
		final Calendar c2 = Calendar.getInstance();
		c2.setTime(dateTm);

		c2.set(Calendar.YEAR, c1.get(Calendar.YEAR));
		c2.set(Calendar.MONTH, c1.get(Calendar.MONTH));
		c2.set(Calendar.DAY_OF_MONTH, c1.get(Calendar.DAY_OF_MONTH));
		c2.set(Calendar.SECOND, 0);
		c2.set(Calendar.MILLISECOND, 0);

		return c2.getTime();
	}

	public double calcDurationFromTime(final Date fromHH, final Date toHH) {
		if (fromHH == null || toHH == null) {
			return 0;
		}

		final Instant instant1 = fromHH.toInstant();
		final Instant instant2 = toHH.toInstant();
		final long mins = ChronoUnit.MINUTES.between(instant1, instant2);
		double hours = ChronoUnit.HOURS.between(instant1, instant2);

		final int minutes = (int)(mins - (hours * 60));

		//convert to decimal
		double dec = 0.;
		if (minutes != 0) {
			dec =(100. / (60. / minutes) / 100.);
		}
		hours = hours + dec;
		return hours;
	}


}
