/* Author: David McCoy dave.mccoy@cospandesign.com
 *
 *     This file is part of DLCM.
 *
 *  DLCM is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DLCM is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with DLCM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

/* Member.java
 *
 * This is a virtual wrapper for a data member for an LCM file, Java reflections
 * require a lot of control data, and this class is made to encapsulate those
 * functions and present a data member to the user
 *
 */
package dlcm;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Array;

/**
 *
 * @author David McCoy
 *
 * Lowest level in the dynamic structures that make up a DLCM structure, Member
 * is a replacemnt for any statically generated member in lcm structures
 *
 */
public class Member {

	//Constants
    public static final int LCM_CONST = 0;
    public static final int LCM_VAR = 1;


    public String Name;
    public String Type;
    public Object Data;
    public Object ConstantData;
    public boolean canPublish = true;
    public boolean canSubscribe = true;
    private Structure mParent;

    //Names
    public String mShortName;
    public String mPackage;
    public String mLCMTypeName;

    //Flags
    public boolean Constant;
    public boolean ReferenceToType;

    //Dimensions
    public int Dimensions = 0;
    public String[] DimensionsVariableSize;
    public Integer[] DimensionsConstantSize;
    public Boolean[] ConstDimensions;

    //Constructor
    /**
     * This constructor is used primarily for junit test
     */
    public Member(){
        mShortName = "";
        mPackage = "";
        mLCMTypeName = "";
    }
    /**
     * Constructor used for generating a Member at runtime
     *
     * @param parent Structure that contains Member
     */
    public Member(Structure parent){
        this();
        mParent = parent;
    }
    /**
     * Initializes the member, and instantiates an object regardless of type
     * Primary cause of most exceptions will be due to java.lang.reflect exceptions
     *
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public void initialize() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException{
            //Constant
            if (isConstant()){
                    Data = initializeConstant(Type, ConstantData);
                    return;
            }
            //Array
            if (isArray()){
                    initializeArray();
                    return;
            }
            //Not an Array, or Constant
            Data = initializeType(Type);
    }
    private Object initializeType(String type) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException{

            Object obj = null;

            if (type.matches("int8_t")) obj = new Byte((byte) 0);
            else if (type.matches("int16_t")) obj = new Short((short) 0);
            else if (type.matches("int32_t")) obj = new Integer((int) 0);
            else if (type.matches("int64_t")) obj = new Long ((long) 0);
            else if (type.matches("byte")) obj = new Byte((byte) 0);
            else if (type.matches("float")) obj = new Float ((Float) 0.0f);
            else if (type.matches("double")) obj = new Double ((Double) 0.0d);
            else if (type.matches("string")) obj = new String ((String) "");
            else if (type.matches("boolean")) obj = new Boolean ((boolean) false);
            else if (mParent.mDLCM.StructureExists(type)){
                    obj =   mParent.mDLCM.CloneStructure(type);
                            //mParent.MemberMap.get(type).clone();
                    ((Structure)obj).initialize(mParent.mDLCM);
            }
            else {
                    throw new IOException ("Coule not find matching type");
            }
            return obj;
    }
    private void initializeArray () throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException{
        int[] dimensionsArray = new int[Dimensions];

        //InitializeArrayData(0, Data);

        for (int i = 0; i < Dimensions; i++){
                dimensionsArray[i] = (int)getDimensionsSize(i);
        }
        Class<?> c = null;
        if (!this.isPrimitive()){
            c = Structure.class;
        }
        else {
            c = getClassFromString (Type);
        }
        //initialize the entire single/multi-dimensional array in one go
        Data = java.lang.reflect.Array.newInstance(c, dimensionsArray);

        //Top of the recurrsion array initializer
        initializeArrayData(0, Data);

    }
    private Object initializeArrayData(int depth, Object depthData) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException{


        if (depth == Dimensions){
            //Reached the Bottom of recursion
            return initializeType(Type);
        }

        //Need to continue with recurssion
        int size = (int) getDimensionsSize(depth);

        for (int i = 0; i < size; i++){
                //go to the location in the data array
                Object innerData = java.lang.reflect.Array.get(depthData, (int)i);
                java.lang.reflect.Array.set(depthData, i, initializeArrayData(depth + 1, innerData));

        }
        //set the recursive call above to an array of data... or array of array of data... or an array of an array of...
        return depthData;
    }
    private static Object initializeConstant(String type, Object ConstantObject){
            Object obj = null;
            if (type.matches("int8_t")) obj = new Byte((Byte) ConstantObject);
            else if (type.matches("int16_t")) obj = new Short((Short) ConstantObject);
            else if (type.matches("int32_t")) obj = new Integer((Integer) ConstantObject);
            else if (type.matches("int64_t")) obj = new Long ((Long) ConstantObject);
            else if (type.matches("byte")) obj = new Byte((Byte) ConstantObject);
            else if (type.matches("float")) obj = new Float ((Float) ConstantObject);
            else if (type.matches("double")) obj = new Double ((Double) ConstantObject);
            else if (type.matches("boolean")) obj = new Boolean ((Boolean) ConstantObject);
            return obj;
    }

    //Getters/Setters
    /**
     *
     * @param parent
     */
    public void setParent(Structure parent){
            mParent = parent;
    }
    /**
     *
     * @param string
     */
    public void setName(String string){
            Name = new String (string);
    }
    /**
     *
     * @param string
     */
    public void setType(String string){
            Type = new String (string);
    }
    /**
     *
     * @return
     */
    public Structure getParent(){
            return mParent;
    }
    /**
     *  gets the data that this Member represents, its up to the user to cast it
     *
     * @return the data (Object)
     */
    public Object getData(){
        //if (Dimensions == 0){
        if (this.isConstant()){
            return this.ConstantData;
        }
        return Data;
        //}

        //Need to create an array with the dimensions
        //if (Data instanceof Byte){

        //}
        //return null;
    }

    //Flags
    /**
     * Returns true if the data is a primitive type
     *
     * @return true if data is a primitive type
     */
    public boolean isPrimitive(){
            if (Type.matches("int8_t")) return true;
            if (Type.matches("int16_t")) return true;
            if (Type.matches("int32_t")) return true;
            if (Type.matches("int64_t")) return true;
            if (Type.matches("float")) return true;
            if (Type.matches("double")) return true;
            if (Type.matches("byte")) return true;
            if (Type.matches("boolean")) return true;
            return false;
    }
    /**
     * Returns true if the data is a string
     *
     * @return true if the data is a string
     */
    public boolean isString(){
            if (Type.matches("string")) return true;
            return false;
    }
    /**
     * Returns true if the data is an array
     *
     * @return true if the data is an array
     */
    public boolean isArray(){
            return (Dimensions > 0);
    }
    /**
     * Return true if the data is a reference to another Structure or Member
     *
     * @return true if the data is a reference to another Structure or Member
     */
    public boolean isDataReferenceToType(){
            return ReferenceToType;
    }
    /**
     * Returns true if the data is a LCM constant
     *
     * @return true if the data is a LCM constant
     */
    public boolean isConstant(){
            return Constant;
    }
    /**
     * Check if the specific dimension of the Data is a constant
     *
     * @param d the dimension of the single/multi-dimensional array
     * @return
     */
    public boolean isConstantDimension(int d){
            return ConstDimensions[d];
    }

    //Array/Dimensions Functions
    /**
     * if the Dimension of the array is a variable, get the name of the Dimesion
     *
     * @param index index of the dimesion of the single/multi-dimensional array
     * @return
     */
    public String getVariableDimensionName(int index){
            return DimensionsVariableSize[index];
    }
    /**
     * returns the enumeration of a constant
     *  LCM_CONST = constant
     *  LCM_VAR = variable
     * @param index
     * @return
     */
    public int getEnumeratorConstantDimensions(int index){
            if (ConstDimensions[index]){
                    return LCM_CONST;
            }
            return LCM_VAR;
    }
    /**
     * returns the size of the constant dimension
     *
     * @param index index of single/mult-dimensional array
     * @return numeric constant dimension size
     */
    public int getDimensionSizeConst(int index){
            return DimensionsConstantSize[index];
    }
    /**
     * general get dimension size function
     * @param index of the single/multi-dimensional array
     * @return size of the array
     * @throws IOException Index of dimension is out of range
     */
    public long getDimensionsSize(int index) throws IOException{
            //outside of range
            if (Dimensions < index){
                    throw new IOException ("Index out of range");
            }

            //Constant Size
            if (ConstDimensions[index]){
                    return DimensionsConstantSize[index];
            }

            //Variable Size
            Member member = mParent.MemberMap.get(DimensionsVariableSize[index]);

            Long size = null;

            if (member.Data instanceof Byte){
                size = new Long ((Byte)member.Data);
            }
            else if (member.Data instanceof Short){
                size = new Long ((Short)member.Data);
            }
            else if (member.Data instanceof Integer){
                size = new Long ((Integer)member.Data);
            }
            else if (member.Data instanceof Long){
                size = new Long ((Long)member.Data);
            }

            //Already checked that it is a valid array type in the Structure parser
            /*if (size == 0){
                return new Long(1);
            }

             */
            return size;
    }

    //Overrides

    @Override
    public boolean equals(Object obj)
    {
        if(obj == this) return true;
        if(obj.getClass().equals(Member.class))
        {
            Member member = (Member)obj;
            return((Name == null ? this.Name == null : Name.equals(this.Name) &&
               (Type == null ? this.Type == null : Type.equals(this.Type) &&
                Constant == this.Constant &&
                Dimensions == this.Dimensions)));
            // TODO: Add dimensions parameters here
        }
        return false;
    }

    @Override
    public int hashCode() {
       final int PRIME = 31;
       int result = 1;
       result = PRIME * result + Name.hashCode();
       result = PRIME * result + Type.hashCode();
       result = PRIME * result + (Constant ? 1 : 0);
       result = PRIME * result + Dimensions;
       // TODO: Add dimensions parameters here
       return result;
    }
        
    @Override
    protected Object clone() {
            Member m = new Member();
            if (Name != null) m.Name = new String(Name);
            if (Type != null) m.Type = new String(Type);

            m.Constant = Constant;
            if (m.Constant){
                    m.ConstantData = initializeConstant(Type, ConstantData);
            }
            m.Dimensions = Dimensions;
            if (ConstDimensions != null){
                m.ConstDimensions = ConstDimensions.clone();
            }
            if (DimensionsConstantSize != null){
                m.DimensionsConstantSize = DimensionsConstantSize.clone();
            }
            if (DimensionsVariableSize != null){
                m.DimensionsVariableSize = DimensionsVariableSize.clone();
            }
            return m;
    }

    //Helper Functions
    /**
     * gets the Class of the data by the name that is in Type
     *
     * @return Class of the Data
     * @throws ClassNotFoundException if the Object not found, this can be thrown when a user removes a referencing Structure
     */
    public Class<?> getDataClass() throws ClassNotFoundException{
            return getClassFromString (Type);
    }
    private static Class<?> getClassFromString (String type) throws ClassNotFoundException{
            String classString = new String ("");

            /*
            for (int i = 0; i < dimensions; i++){
                    classString += "[";
            }

             *
             */
            if (type.matches("int8_t")) classString += Byte.class.getName();
            else if (type.matches("int16_t")) classString += Short.class.getName();
            else if (type.matches("int32_t")) classString += Integer.class.getName();
            else if (type.matches("int64_t")) classString += Long.class.getName();
            else if (type.matches("byte")) classString += Byte.class.getName();
            else if (type.matches("float")) classString += Float.class.getName();
            else if (type.matches("double")) classString += Double.class.getName();
            else if (type.matches("string")) classString += String.class.getName();
            else if (type.matches("boolean")) classString += Boolean.class.getName();
            else classString += "L" + Member.class.getName();

            return Class.forName(classString);
    }

    //encode Functions
    /**
     * Encodes data for LCM output
     *
     * @param outs byte encoding
     * @throws IOException
     */
    public void encode(DataOutput outs) throws IOException{
            if (isConstant()){
                    return;
            }
            encodeRecursive(0, Data, outs);
    }
    private void encodeRecursive (int index, Object data, DataOutput outs) throws IOException{

            //is the index at lowest level?
            if (index == Dimensions){
                    if (isPrimitive()){
                            encodePrimitiveType(data, Type, outs);
                    }
                    else if (isString()){
                            encodeString((String)data, outs);
                    }
                    else{
                            ((Structure)data).encode(outs);
                    }
                    return;
            }

            //not at the bottom
            int size = (int) getDimensionsSize(index);
            for (int i = 0; i < size; i++){
                    //go to the new location in the data array
                    Object innerData = java.lang.reflect.Array.get(data, i);
                    encodeRecursive (index + 1, innerData, outs);
            }
    }
    private void encodePrimitiveType (Object dataOut, String typeOut, DataOutput outs) throws IOException{
            if (typeOut.matches("int8_t")) outs.writeByte ((Byte) dataOut);
            else if (typeOut.matches("int16_t")) outs.writeShort ((Short) dataOut);
            else if (typeOut.matches("int32_t")) outs.writeInt((Integer) dataOut);
            else if (typeOut.matches("int64_t")) outs.writeLong((Long) dataOut);
            else if (typeOut.matches("byte")) outs.writeByte((Byte) dataOut);
            else if (typeOut.matches("float")) outs.writeFloat((Float) dataOut);
            else if (typeOut.matches("double")) outs.writeDouble((Double) dataOut);
            else if (typeOut.matches("boolean")) outs.writeByte((Byte) (((Boolean) dataOut) ? ((byte) 1) : ((byte) 0)));

    }
    private void encodeString (String outString, DataOutput outs) throws IOException{
            byte[] strbuf = null;
            strbuf = outString.getBytes();
            outs.writeInt(strbuf.length + 1);
            outs.write(strbuf, 0, strbuf.length);
            outs.writeByte(0);
    }

    //decode Functions
    /**
     * Decodes data from reading data from the network
     *
     * @param inps input byte stream to decode
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void decode(DataInput inps) throws IOException, ClassNotFoundException{
            if (isConstant()){
                    return;
            }
            Data = decodeRecursive(0, Data, inps);
    }
    private Object decodeRecursive (int index, Object data, DataInput inps) throws IOException, ClassNotFoundException{
            //we are at the bottom level
            if (index == Dimensions){
                    if (isPrimitive()){
                            data = decodePrimitive(Type, inps);
                    }
                    else if (isString()){
                            data = decodeString(inps);
                    }
                    else {
                            ((Structure) data).decode(inps);
                    }
            }
            else{
                    int size = (int) getDimensionsSize(index);
                    //if the data sizes don't match up, we need to fix that

                    Class<?> c = getClassFromString(Type);
                    data = java.lang.reflect.Array.newInstance(c, size);

                    for (int i = 0; i < size; i++){
                            //go to the location in the data array
                            Object innerData = java.lang.reflect.Array.get(data, (int)i);
                            java.lang.reflect.Array.set(data, i, decodeRecursive(index + 1, innerData, inps));
                            //java.lang.reflect.Array.set(data, i, innerData);
                    }
            }
            return data;
    }
    private static Object decodePrimitive (String typeIn, DataInput inps) throws IOException{
            Object dataIn = null;

            if (typeIn.matches("int8_t")) dataIn = inps.readByte();
            else if (typeIn.matches("int16_t")) dataIn = inps.readShort();
            else if (typeIn.matches("int32_t")) dataIn = inps.readInt();
            else if (typeIn.matches("int64_t")) dataIn = inps.readLong();
            else if (typeIn.matches("float")) dataIn = inps.readFloat();
            else if (typeIn.matches("double")) dataIn = inps.readDouble();
            else if (typeIn.matches("boolean")) dataIn = new Boolean (inps.readByte() != 0);

            return dataIn;
    }
    private static String decodeString (DataInput inps) throws IOException{
            String outString = null;

            byte[] strbuf = null;
            strbuf = new byte[inps.readInt() - 1];
            inps.readFully(strbuf);
            inps.readByte();

            outString = new String (strbuf, "UTF-8");
            return outString;
    }

}
