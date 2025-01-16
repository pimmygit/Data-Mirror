package log.exception;

public class NonExistantHandleException extends RuntimeException 
{
    public NonExistantHandleException(String info)
    {
	super(info);
    }
}
