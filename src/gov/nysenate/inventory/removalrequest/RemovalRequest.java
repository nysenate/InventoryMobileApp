package gov.nysenate.inventory.removalrequest;

import gov.nysenate.inventory.model.InvItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RemovalRequest
{
    private int transactionNum;
    private List<InvItem> items;
    private RemovalReason reason;
    private final String employee;
    private Date date;
    private boolean approved; // TODO: need 2 approvals?
    private Date approvalDate;

    public RemovalRequest(String employee) {
        this.employee = employee;
        this.items = new ArrayList<InvItem>();
    }

    public int getTransactionNum() {
        return transactionNum;
    }

    public void setTransactionNum(int transactionNum) {
        this.transactionNum = transactionNum;
    }

    public List<InvItem> getItems() {
        return items;
    }

    public void addItem(InvItem item) {
        items.add(item);
    }

    public void deleteItem(InvItem item) {
        items.remove(item);
    }

    public RemovalReason getReason() {
        return reason;
    }

    public String getEmployee() {
        return employee;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public Date getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }
}
