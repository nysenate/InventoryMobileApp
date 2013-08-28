package gov.nysenate.inventory.model;

import java.io.Serializable;

public abstract class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;
    protected int nuxrpd;
    protected Location origin;
    protected Location destination;

    // SignatureView signature;
    // byte[] sigBytes;

    public int getNuxrpd() {
        return nuxrpd;
    }

    public void setNuxrpd(int nuxrpd) {
        this.nuxrpd = nuxrpd;
    }

    public Location getOrigin() {
        return origin;
    }

    public void setOrigin(Location origin) {
        this.origin = origin;
    }

    public Location getDestination() {
        return destination;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }
}
