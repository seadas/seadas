package gov.nasa.gsfc.seadas.processing.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Created by aabduraz on 2/19/18.
 */
public class FileCompare {

    public static void main(String args[])
    {
        if(args.length != 2)    throw (new RuntimeException("Usage : java FileCompare fileName1 fileName2"));
        String fileName1=args[0];
        String fileName2=args[1];
        String hashValue="";
        String hashValue1="";
        //First Method
        try
        {
            if (CompareFilesbyByte(fileName1,fileName2)==true)
                System.out.println("No Difference encountered  using CompareFilesbyByte method");
            else System.out.println("Both Files are not equal using CompareFilesbyByte method");
        }catch (IOException e)
        {
            System.out.println("Error");
        }
        System.out.println();

        //IInd Method
        try
        {
            hashValue =MD5HashFile(fileName1);
            hashValue1 =MD5HashFile(fileName2);
            if (hashValue.equals(hashValue1)) System.out.println("No Difference encountered using method MD5Hash"); else System.out.println("Both Files are not equal  using MD5Hash");
        }
        catch (Exception e)
        {
            System.out.println("Error");
        }
    }

    //reading bytes and compare
    public static boolean CompareFilesbyByte(String file1, String file2) throws IOException
    {
        File f1=new File(file1);
        File f2=new File(file2);
        FileInputStream fis1 = new FileInputStream (f1);
        FileInputStream fis2 = new FileInputStream (f2);
        if (f1.length()==f2.length())
        {
            int n=0;
            byte[] b1;
            byte[] b2;
            while ((n = fis1.available()) > 0) {
                if (n>80) n=80;
                b1 = new byte[n];
                b2 = new byte[n];
                fis1.read(b1);
                fis2.read(b2);
                if (Arrays.equals(b1,b2)==false)
                {
                    System.out.println(file1 + " :\n\n " + new String(b1));
                    System.out.println();
                    System.out.println(file2 + " : \n\n" + new String(b2));
                    return false;
                }
            }
        }
        else return false;  // length is not matched.
        return true;
    }

    public static String MD5HashFile(String filename) throws Exception {
        byte[] buf = ChecksumFile(filename);
        String res = "";
        for (int i = 0; i < buf.length; i++) {
            res+= Integer.toString((buf[i] & 0xff) + 0x100, 16).substring(1);
        }
        return res;
    }


    public static byte[]  ChecksumFile(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);
        byte[] buf = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int n;
        do {
            n= fis.read(buf);
            if (n > 0) {
                complete.update(buf, 0, n);
            }
        } while (n != -1);
        fis.close();
        return complete.digest();
    }

}