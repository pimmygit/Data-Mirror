package utils;

import log.LogManager;
import log.LogHandle;

import java.nio.channels.FileChannel;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Pimmy
 * Date: 28-Sep-2005
 * Time: 14:21:17
 * To change this template use File | Settings | File Templates.
 */
public class FileManipulator
{
    private String _source;
    private String _destination;

    // Var used for determining the size of directory
    private long totalSize = 0;

    public FileManipulator()
    {

    }

    public FileManipulator(String src, String dst)
    {
        if (copyFile(src, dst))
        {
            System.out.println("File ["+src+"] successfully copied to ["+dst+"].\n");
        }
        else
        {
            System.out.println("ERROR: Failed to copy ["+src+"] to ["+dst+"].\n");
        }
    }

    public boolean copyFile(String src, String dst)
    {
        LogManager.write("File Manipulator     - Copying file ["+src+"] to ["+dst+"].", LogHandle.ALL,LogManager.DEBUG);

        _source = src;
        _destination = dst;

        FileChannel in = null;
        FileChannel out = null;
        try
        {
            in = new FileInputStream(_source).getChannel();
            out = new FileOutputStream(_destination).getChannel();
            in.transferTo( 0, in.size(), out);

            // Another way of copying files
            //in = new FileInputStream(src).getChannel();
            //out = new FileOutputStream(dst).getChannel();
            //long size = in.size();
            //MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);
            //out.write(buf);
        }
        catch (IOException e)
        {
            return false;
        }
        
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
                if (out != null)
                {
                    out.close();
                }
            }
            catch (IOException e)
            {
                return false;
            }
        }
        return true;
    }

    // Returns the free space in bytes at the pointed location
    public int getFreeSpace(File f_path)
    {
        int freeSpace = 0;


        return freeSpace;
    }

    // Calculates the space taken by this file
    // If a directory is to be checked, the function will go trough
    // every file and will recurse into the subfolders.
    public long getFileSize(File f_path)
    {
        if (f_path.isDirectory())
        {
            innerListFiles(f_path);
        }
        else if(f_path.isFile())
        {
            totalSize = f_path.length();
        }

        return totalSize;
    }

    private void innerListFiles(File file)
    {
        File[] found = file.listFiles();
        for (int i = 0; i < found.length; i++)
        {
            if (found[i].isDirectory())
            {
                innerListFiles(found[i]);
            }
            else
            {
                totalSize = totalSize + file.length();
            }
        }
    }

    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("\n");
            System.out.println("* JAVA File Copier - copyFile *\n");
            System.out.println("*******************************\n");
            System.out.println("* Usage: copyFile <src> <dst> *\n");
            System.out.println("*        -------------------- *\n");
            System.out.println("* <src> - Full path of source.*\n");
            System.out.println("* <dst> - Full path of dest.  *\n");
            System.out.println("*******************************\n");
            System.exit(-1);
        }
        else
        {
            new FileManipulator(args[1], args[2]);
        }
    }
}
