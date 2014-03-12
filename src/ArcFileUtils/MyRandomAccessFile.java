/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ArcFileUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author wiwat
 */
public class MyRandomAccessFile implements DataOutput, DataInput, AutoCloseable {

    public RandomAccessFile raf;

    public MyRandomAccessFile(String name, String mode) throws FileNotFoundException {
        raf = new RandomAccessFile(name, mode);
    }

    public MyRandomAccessFile(File name, String mode) throws FileNotFoundException {
        raf = new RandomAccessFile(name, mode);
    }

    public final void seek(long pos) throws IOException {
        raf.seek(pos);
    }

    public void setLength(long newLength) throws IOException {
        raf.setLength(newLength);
    }

    public long length() throws IOException {
        return raf.length();
    }

    public long getFilePointer() throws IOException {
        return raf.getFilePointer();
    }

    @Override
    public String readLine() throws IOException {
        String ans = "";
        int i = 0;
        byte v;
        int bs = 8192;
        int bs_max = 8186;
        byte[] b = new byte[bs];
        if ((v = (byte) raf.read()) != -1) {
            do {
                if (v >= 0) {
                    if (v == '\n') {
                        return ans + new String(b, 0, i, "utf-8");
                    }
                    b[i++] = v;
                } else {
                    b[i++] = v;
                    if (v >= -4) {
                        raf.read(b, i, 5);
                        i += 5;
                    } else if (v >= -8) {
                        raf.read(b, i, 4);
                        i += 4;
                    } else if (v >= -16) {
                        raf.read(b, i, 3);
                        i += 3;
                    } else if (v >= -32) {
                        raf.read(b, i, 2);
                        i += 2;
                    } else {
                        raf.read(b, i, 1);
                        i += 1;
                    }
                }
                if (i >= bs_max) {
                    ans += new String(b, 0, i, "utf-8");
                    i = 0;
                }
            } while ((v = (byte) raf.read()) != -1);
            return ans + new String(b, 0, i, "utf-8");
        }
        return "";
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return raf.skipBytes(n);
    }

    @Override
    public void close() throws IOException {
        raf.close();
    }

    @Override
    public void write(int b) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void write(byte[] b) throws IOException {
        raf.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        raf.write(b, off, len);
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        raf.writeBoolean(v);
    }

    @Override
    public void writeByte(int v) throws IOException {
        raf.writeByte(v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        raf.writeShort(v);
    }

    @Override
    public void writeChar(int v) throws IOException {
        raf.writeChar(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        raf.writeInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        raf.writeLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        raf.writeFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        raf.writeDouble(v);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        raf.write(s.getBytes());
    }

    @Override
    public void writeChars(String s) throws IOException {
        raf.write(s.getBytes());
    }

    @Override
    public void writeUTF(String s) throws IOException {
        raf.writeUTF(s);
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        raf.readFully(b);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        raf.readFully(b, off, len);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return raf.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return raf.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return raf.readUnsignedByte();
    }

    @Override
    public short readShort() throws IOException {
        return raf.readShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return raf.readUnsignedShort();
    }

    @Override
    public char readChar() throws IOException {
        return raf.readChar();
    }

    @Override
    public int readInt() throws IOException {
        return raf.readInt();
    }

    @Override
    public long readLong() throws IOException {
        return raf.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return raf.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return raf.readDouble();
    }

    @Override
    public String readUTF() throws IOException {
        return raf.readUTF();
    }

}
