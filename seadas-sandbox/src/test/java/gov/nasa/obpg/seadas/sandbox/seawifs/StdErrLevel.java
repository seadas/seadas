package gov.nasa.obpg.seadas.sandbox.seawifs;

import java.io.*;
import java.util.logging.*;
 
 /* 
  * Code (selected snippets) for redirecting of stderr stolen from: 
  *     http://blogs.oracle.com/nickstephen/entry/java_redirecting_system_out_and
  */
 class StdErrLevel extends Level {
     private StdErrLevel(String name, int value) {
         super(name, value);
     }
     public static Level STDERR =
         new StdErrLevel("STDERR", Level.INFO.intValue()+54);
 
     protected Object readResolve() throws ObjectStreamException {
         if (this.intValue() == STDERR.intValue())
             return STDERR;
         throw new InvalidObjectException("Unknown instance :" + this);
     }
 }
