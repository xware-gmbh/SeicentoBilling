package ch.xwr.seicentobilling.business;

public class LovState {
	public static enum State {
		inactive, active, locked
	}

	public static enum Unit {
		piece, hour, unit, kg, meter, litre
	}

	public static enum ProState {
		grün, gelb, rot
	}

	public static enum ProModel {
		undefined, fix, variable
	}

	public static enum ExpUnit {
		kilometer, währung, stück
	}

	public static enum ExpType {
		standard, essen, reise
	}

	public static enum WorkType {
		analysis, consulting, development, project, journey, expense
	}

	public static enum ValueType {
		string, integer, decimal, bool, date
	}

	public static enum BookingType {
		offen, gebucht
	}

	public static enum Theme {
		dark, facebook, light
	}

	/**
	 * Monate (sollte eigentlich bei 1 starten)
	 **/
	public static enum Month {
		empty(0), januar(1), februar(2), märz(3), april(4), mai(5), juni(6), juli(7), august(8), september(9), oktober(
				10), november(11), dezember(12);

		private final int value;

		private Month(final int value) {
			this.value = value;
		}

		public int getValue() {
			return this.value;
		}

		public static Month fromId(final int id) {
			for (final Month type : Month.values()) {
				if (type.getValue() == id) {
					return type;
				}
			}
			return null;
		}
	}

	public static enum AccountType {
		natürlich, juristisch
	}


	/**
	 * Spesenkonti (Text) not used anymore - own DAO
	 **/
//	public static enum Accounts {
//		spesen("a.Spesen"), weiterbildung("Test"), büroaufwand("x"), reisespesen("y"), divers("d"),
//			repräsentation("r"), werbung("w"), miete("m"), itunterhalt("itc");
//
//		private final String text;
//
//		Accounts(final String text) {
//			this.text = text;
//		}
//
//		public String getText() {
//			return this.text;
//		}
//
//		public static Accounts fromString(final String text) {
//			for (final Accounts b : Accounts.values()) {
//				if (b.text.equalsIgnoreCase(text)) {
//					return b;
//				}
//			}
//			return null;
//		}
//	}

	public static enum itmPriceLevel {
		item, project
	}

}
