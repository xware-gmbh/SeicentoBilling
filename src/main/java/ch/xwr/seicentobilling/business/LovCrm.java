package ch.xwr.seicentobilling.business;

public class LovCrm {
	public static enum ActivityType {
		phone, mail, personal, offer, bill, document, misc
	}

	public static enum LabelType {
		system, user
	}

	public static enum AddressType {
		privat, business
	}

	public static enum LinkType {
		phone, mail, web
	}

	public static enum Salutation {
		herr, frau
	}

	public static enum BillTarget {
		postal, pdf, misc
	}

	public static enum BillReport {
		billonly, working, project
	}

	public static enum ContactRelation {
		eltern, kind, arbeitgeber, arbeitnehmer, ehepartner, undefiniert
	}

	public static enum Department {
		main, billing, sales, misc, mobile
	}

}
