package gov.nysenate.inventory.model;

/**
 *
 * @author HEITNER
 */
public class Employee {
    private int nuxrefem;
    private String nafirst = null;
    private String nalast = null;
    private String namidinit = null;
    private String nasuffix = null;
    private String naemail = null;
    private transient String naemployee = null;

    public Employee () {
    }

    public Employee (int nuxrefem, String nafirst, String nalast, String namidinit, String nasuffix) {
        this.nuxrefem = nuxrefem;
        this.nafirst = nafirst;
        this.nalast = nalast;
        this.namidinit = namidinit;
        this.nasuffix = nasuffix;
    }

    public String getFullName() {
        String fullName = nafirst;
        fullName += namidinit != null ? " " + namidinit : "";
        fullName += " " + nalast;
        fullName += nasuffix != null ? " " + nasuffix : "";
        return fullName;
    }

    public int getNuxrefem() {
        return nuxrefem;
    }

    public void setNuxrefem(int nuxrefem) {
        this.nuxrefem = nuxrefem;
    }

    public String getNafirst() {
        return nafirst;
    }

    public void setNafirst(String nafirst) {
        this.nafirst = nafirst;
    }

    public String getNalast() {
        return nalast;
    }

    public void setNalast(String nalast) {
        this.nalast = nalast;
    }

    public String getNamidinit() {
        return namidinit;
    }

    public void setNamidinit(String namidinit) {
        this.namidinit = namidinit;
    }

    public String getNasuffix() {
        return nasuffix;
    }

    public void setNasuffix(String nasuffix) {
        this.nasuffix = nasuffix;
    }

    public String getNaemail() {
        return naemail;
    }

    public void setNaemail(String naemail) {
        this.naemail = naemail;
    }

    public String getNaemployee() {
        return naemployee;
    }

    public void setNaemployee(String naemployee) {
        this.naemployee = naemployee;
    }
}
