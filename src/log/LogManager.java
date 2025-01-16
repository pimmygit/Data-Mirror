package log;
import log.exception.*;

import java.util.*;
import java.io.*;

public class LogManager
{
	/* Log levels */
	public static int DEBUG=0;
	public static int MINOR=1;
	public static int MAJOR=2;
	public static int CRITICAL=3;

	/* Log sink types */
	public static int STDOUT=0;
	public static int FILE=1;


	private static int _log_level=DEBUG;
	private static int _sink_type=FILE;

	private static final String[] _days={"Sun","Mon","Tue","Wed","Thr","Fri","Sat"};

	private static Hashtable _handles;



	public static synchronized void setLogLevel(int level)
	{
		if(level<DEBUG||level>CRITICAL)
		{
			throw new IllegalArgumentException("LogLevel out of range");
		}
		else
		{
			_log_level=level;
		}
	}

	public static synchronized void setSinkType(int type)
	{
		if(type!=STDOUT&&type!=FILE)
		{
			throw new IllegalArgumentException("Unknown Sink Type");
		}
		else
		{
			_sink_type=type;
		}
	}


	public static synchronized void setLog(String handle,String file) throws IOException
	{
		setLog(handle,new File(file));
	}


	public static synchronized void setLog(String handle,File file) throws IOException
	{
		if(_handles==null)
		{
			_handles=new Hashtable();
		}

		if(_handles.get(handle)!=null)
		{
			throw new AlreadyInUseHandleException("Handle "+handle+" already in use.");
		}
		else
		{
			PrintWriter writer=new PrintWriter(new BufferedWriter(new FileWriter(file.getPath(),true)));
			_handles.put(handle,writer);
		}
	}



	public static void write(String info,String handle,int level)
	{
		if(level>=_log_level)
		{

			if(_sink_type==STDOUT)
			{
				String event=formatEvent(info);
				System.out.println(event);
			}
			else
			{
				Object o=_handles.get(handle);
				if(o==null)
				{
					throw new NonExistantHandleException("Handle "+handle+" does not exist");
				}
				else
				{
					PrintWriter writer=(PrintWriter)o;
					_write(writer,info);
				}
			}
		}
	}



	private static void _write(PrintWriter writer,String info)
	{
		synchronized(writer)
		{
			String event=formatEvent(info);
			writer.println(event);
			writer.flush();
		}
	}

	protected static final String formatEvent(String info)
	{
		GregorianCalendar cal=new GregorianCalendar();
		cal.setTime(new Date(System.currentTimeMillis()));
		String[] tmp=new String[5];
		String hour=tmp[0]=Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
		String minute=tmp[1]=Integer.toString(cal.get(Calendar.MINUTE));
		String second=tmp[2]=Integer.toString(cal.get(Calendar.SECOND));
		String day=tmp[3]=Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
		String month=tmp[4]=Integer.toString(cal.get(Calendar.MONTH) + 1);
		String year=Integer.toString(cal.get(Calendar.YEAR));
		StringBuffer timestamp=new StringBuffer();

		for(int i=0;i<5;i++)
		{
			if(tmp[i].length()==1)
			{
				tmp[i]="0"+tmp[i];
			}
		}
		StringBuffer buffer=new StringBuffer();
		buffer.append("[");
		buffer.append(tmp[0]);
		buffer.append(":");
		buffer.append(tmp[1]);
		buffer.append(":");
		buffer.append(tmp[2]);
		buffer.append("  ");
		buffer.append(_days[cal.get(Calendar.DAY_OF_WEEK)-1]);
		buffer.append(' ');
		buffer.append(tmp[3]);
		buffer.append("/");
		buffer.append(tmp[4]);
		buffer.append("/");
		buffer.append(year);
		buffer.append("]");
		buffer.append(" ");
		buffer.append(info);
		return buffer.toString();
	}

	protected static String formatEvent1(String info)
	{
		GregorianCalendar cal=new GregorianCalendar();
		cal.setTime(new Date(System.currentTimeMillis()));
		String[] tmp=new String[5];
		String hour=tmp[0]=Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
		String minute=tmp[1]=Integer.toString(cal.get(Calendar.MINUTE));
		String second=tmp[2]=Integer.toString(cal.get(Calendar.SECOND));
		String day=tmp[3]=Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
		String month=tmp[4]=Integer.toString(cal.get(Calendar.MONTH) + 1);
		String year=Integer.toString(cal.get(Calendar.YEAR));
		StringBuffer timestamp=new StringBuffer();

		for(int i=0;i<5;i++)
		{
			if(tmp[i].length()==1)
			{
				tmp[i]="0"+tmp[i];
			}
		}
		StringBuffer buffer=new StringBuffer();
		buffer.append("[");
		buffer.append(tmp[0]);
		buffer.append(":");
		buffer.append(tmp[1]);
		buffer.append(":");
		buffer.append(tmp[2]);
		buffer.append(" ");
		buffer.append(tmp[3]);
		buffer.append("/");
		buffer.append(tmp[4]);
		buffer.append("/");
		buffer.append(year);
		buffer.append("]");
		buffer.append(" ");
		buffer.append(info);
		return buffer.toString();
	}

}







