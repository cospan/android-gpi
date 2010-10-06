/* LCM type definition class file
 * This file was automatically generated by lcm-gen
 * DO NOT MODIFY BY HAND!!!!
 */

package lcmtypes;
 
import java.io.*;
import java.util.*;
 
public final class control_t implements lcm.lcm.LCMEncodable
{
    public String control_string;
    public String from;
    public long board_id;
    public int status;
    public int flags;
 
    public control_t()
    {
    }
 
    public static final long LCM_FINGERPRINT;
    public static final long LCM_FINGERPRINT_BASE = 0x1772abd23dcf534cL;
 
    static {
        LCM_FINGERPRINT = _hashRecursive(new ArrayList<Class>());
    }
 
    public static long _hashRecursive(ArrayList<Class> classes)
    {
        if (classes.contains(lcmtypes.control_t.class))
            return 0L;
 
        classes.add(lcmtypes.control_t.class);
        long hash = LCM_FINGERPRINT_BASE
            ;
        classes.remove(classes.size() - 1);
        return (hash<<1) + ((hash>>63)&1);
    }
 
    public void encode(DataOutput outs) throws IOException
    {
        outs.writeLong(LCM_FINGERPRINT);
        _encodeRecursive(outs);
    }
 
    public void _encodeRecursive(DataOutput outs) throws IOException
    {
        byte[] __strbuf = null;
        __strbuf = this.control_string.getBytes("UTF-8"); outs.writeInt(__strbuf.length+1); outs.write(__strbuf, 0, __strbuf.length); outs.writeByte(0); 
 
        __strbuf = this.from.getBytes("UTF-8"); outs.writeInt(__strbuf.length+1); outs.write(__strbuf, 0, __strbuf.length); outs.writeByte(0); 
 
        outs.writeLong(this.board_id); 
 
        outs.writeInt(this.status); 
 
        outs.writeInt(this.flags); 
 
    }
 
    public control_t(byte[] data) throws IOException
    {
        this(new DataInputStream(new ByteArrayInputStream(data)));
    }
 
    public control_t(DataInput ins) throws IOException
    {
        if (ins.readLong() != LCM_FINGERPRINT)
            throw new IOException("LCM Decode error: bad fingerprint");
 
        _decodeRecursive(ins);
    }
 
    public static lcmtypes.control_t _decodeRecursiveFactory(DataInput ins) throws IOException
    {
        lcmtypes.control_t o = new lcmtypes.control_t();
        o._decodeRecursive(ins);
        return o;
    }
 
    public void _decodeRecursive(DataInput ins) throws IOException
    {
        byte[] __strbuf = null;
        __strbuf = new byte[ins.readInt()-1]; ins.readFully(__strbuf); ins.readByte(); this.control_string = new String(__strbuf, "UTF-8");
 
        __strbuf = new byte[ins.readInt()-1]; ins.readFully(__strbuf); ins.readByte(); this.from = new String(__strbuf, "UTF-8");
 
        this.board_id = ins.readLong();
 
        this.status = ins.readInt();
 
        this.flags = ins.readInt();
 
    }
 
    public lcmtypes.control_t copy()
    {
        lcmtypes.control_t outobj = new lcmtypes.control_t();
        outobj.control_string = this.control_string;
 
        outobj.from = this.from;
 
        outobj.board_id = this.board_id;
 
        outobj.status = this.status;
 
        outobj.flags = this.flags;
 
        return outobj;
    }
 
}

