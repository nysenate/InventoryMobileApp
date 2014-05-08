package gov.nysenate.inventory.exception;

public class InvalidFocusException  extends Exception
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    //Parameterless Constructor
    public InvalidFocusException() {}

    //Constructor that accepts a message
    public InvalidFocusException(String message)
    {
       super(message);
    }
}
