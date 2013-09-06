package gov.nysenate.inventory.android;

public class Commodity
{
   String decommodityf = "";
   String cdtype = "";
   String nusenate = "";
   String cdcategory = "";
   String cdlocat = "";
   String cdlocatto = "";
   String decomments = "";
   String nuxrefco = "";
   String cdissunit = "";
   String cdcommodity = "";
   String nucnt = "";
     
    boolean selected = false;
    
    final int DECOMMODITYF = -101;
    final int CDTYPE = -102;
    final int NUSENATE = -103;
    final int CDCATEGORY = -104;
    final int SELECTED = -105;   
    final int CDLOCAT = -106;   
    final int CDLOCATTO = -107;   
    final int DECOMMENTS = -109; 
    final int NUXREFCO = -110; 
    final int CDISSUNIT = -111;
    final int CDCOMMODITY = -112;
    final int NUCNT = -113;
    
    public Commodity() {
    }

    public String getDecommodityf() {
        return decommodityf;
    }

    public void setDecommodityf(String decommodityf) {
        this.decommodityf = decommodityf;
    }

    public String getCdcategory() {
        return cdcategory;
    }

    public void setCdcategory(String cdcategory) {
        this.cdcategory = cdcategory;
    }

    public String getCdtype() {
        return cdtype;
    }

    public void setCdtype(String cdtype) {
        this.cdtype = cdtype;
    }

    public String getCDissunit() {
        return cdissunit;
    }

    public void setCDissunit(String cdissunit) {
        this.cdissunit = cdissunit;
    }
    
    public String getNuxrefco() {
        return nuxrefco;
    }

    public void setNuxrefco(String nuxrefco) {
        this.nuxrefco = nuxrefco;
    }    

    public String getCdcommodty() {
        return cdcommodity;
    }

    public void setCdcommodity(String cdcommodity) {
        this.cdcommodity = cdcommodity;
    }
    
    public String getNucnt() {
        return nucnt;
    }

    public void setNucnt(String nucnt) {
        this.nucnt = nucnt;
    }        
    
   public String getDecomments() {
        return decomments;
    }
    
    public void setDecomments(String decomments) {
        this.decomments = decomments;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean getSelected() {
        return this.selected;
    }

    @Override
    public String toString() {
        return decommodityf;
    }
}
