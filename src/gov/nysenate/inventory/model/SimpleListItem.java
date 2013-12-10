package gov.nysenate.inventory.model;


public class SimpleListItem
{
  String natype = "";
  String navalue = "";

 public void setNatype (String natype){
   this.natype = natype;
 }

 public void setNavalue(String navalue) {
   this.navalue = navalue;
 }
 
 public String getNatype() {
   return natype;
 }
 
 public String getNavalue() {
   return navalue;
 }  
 
}