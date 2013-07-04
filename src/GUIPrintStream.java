/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import javax.swing.*;

/**
 *
 * @author kogayou
 */

public class GUIPrintStream extends PrintStream {
    
    private JTextArea jTextArea;
    private boolean toGUI;
    
    public GUIPrintStream(OutputStream out, JTextArea jTextArea) {
        super(out);
        this.jTextArea=jTextArea;
        this.toGUI=false;
    }
    
    public void setToGUI(boolean toGUI) {
        this.toGUI=toGUI;
    }
    
    public void write(byte[] buf,int off,int len) {
        final String message=new String(buf,off,len);
        if (jTextArea!=null&&toGUI) {
             jTextArea.append(message);
        } else {
            super.write(buf, off, len);
        }
     }
}
