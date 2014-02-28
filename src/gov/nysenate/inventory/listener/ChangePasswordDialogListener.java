package gov.nysenate.inventory.listener;

public interface ChangePasswordDialogListener
{
    public void onContinueButtonClicked(String oldPassword, String newPassword, String confirmPassord);
    
    public void onCancelButtonClicked();
}
