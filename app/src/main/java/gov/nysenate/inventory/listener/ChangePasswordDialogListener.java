package gov.nysenate.inventory.listener;

public interface ChangePasswordDialogListener
{
    public void onChangePasswordOKButtonClicked(boolean oldPasswordRequired, String oldPassword, String newPassword, String confirmPassword);
    
    public void onChangePasswordCancelButtonClicked();
}
