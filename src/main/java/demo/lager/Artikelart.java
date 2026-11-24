package demo.lager;

public class Artikelart {
  private String aid;
  private String aname;
  private double gewicht;

  public Artikelart() {
  }

  public Artikelart(String aid, String aname, double gewicht) {
    this.aid = aid;
    this.aname = aname;
    this.gewicht = gewicht;
  }

  public String getAid() { return aid; }
  public void setAid(String aid) { this.aid = aid; }
  public String getAname() { return aname; }
  public void setAname(String aname) { this.aname = aname; }
  public double getGewicht() { return gewicht; }
  public void setGewicht(double gewicht) { this.gewicht = gewicht; }
}
