package log.exception;
    
public class AlreadyInUseHandleException extends RuntimeException 
{
    public AlreadyInUseHandleException(String info)
    {
	super(info);
    }
}
