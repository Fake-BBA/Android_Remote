package com.kongqw.kqwrockerdemo;

import java.util.ArrayList;
import java.util.List;

public class Control {
    final short CRC_POLYNOME = 0x1021;	//校验规则码
	final short  MAX_CHANNELS=8;		//协议最大支持到12
    final short transmitNum=32;         //
    public byte transmitBuffer[];				//格式化后的buff
    short channelBuffer[];	//每个频道的值


    Control()
    {
        transmitBuffer=new byte[transmitNum];
        channelBuffer=new short[MAX_CHANNELS];
    }

    private short crc16(short crc, byte value)
    {
        crc = (short)(crc ^ (short)value << 8);

        for (byte i = 0; i < 8; i++) {
            if ((crc & 0x8000)!=0)
                crc = (short)((crc << 1) ^ CRC_POLYNOME);
            else
                crc = (short)(crc << 1);
        }
        return crc;
    }

    public short FrSkyFormat(short sendData[], short channels)
    {
        short crc = 0;
        short dataNum = 0;

        transmitBuffer[dataNum++] = (byte)0xA8;	//head
        transmitBuffer[dataNum++] = (byte)0x01;	//head
        transmitBuffer[dataNum++] = (byte)channels;	//number channels of send

        for (int i = 0; i < channels; i++)
        {
            transmitBuffer[dataNum++] = (byte)(sendData[i] / 32);
            transmitBuffer[dataNum++] = (byte)(sendData[i] % 32 * 8);
        }

        short temp;
        for (int i = 0; i < dataNum; i++)		//计算校验和
        {
            temp = crc16(crc, transmitBuffer[i]);
            crc=temp;
        }
        transmitBuffer[dataNum++] = (byte)(crc >> 8);			//校验和赋值高位
        transmitBuffer[dataNum++] = (byte)(crc & 0xFF);		//校验和赋值低位

        return dataNum;
    }

    public void Send(short sendData[], short channels){
        short dataNum;
        dataNum=FrSkyFormat(sendData,channels);

    }
}

class Frsky{
    //protected RecvStruct IdList[];             //接收协议ID标记
    class RecvStruct{
        byte ID;
        String name;
        RecvStruct(byte id,String name){
            this.ID=id;
            this.name=name;
        }
    }
    List<RecvStruct> mrecvStructList;


    protected final byte ID_GPS_ALTIDUTE_BP=0x01;
    protected final byte ID_GPS_ALTIDUTE_AP=0x09;
    protected final byte ID_TEMPRATURE1=0x02;
    protected final byte ID_RPM=0x03;
    protected final byte ID_FUEL_LEVEL=0x04;
    protected final byte ID_TEMPRATURE2=0x05;
    protected final byte ID_VOLT=0x06;
    protected final byte ID_ALTITUDE_BP=0x10;		//高度
    protected final byte ID_ALTITUDE_AP= 0x21;
    protected final byte ID_GPS_SPEED_BP =0x11;
    protected final byte ID_GPS_SPEED_AP=0x19;
    protected final byte ID_LONGITUDE_BP =0x12;
    protected final byte ID_LONGITUDE_AP=0x1A;
    protected final byte ID_E_W =0x22;
    protected final byte ID_LATITUDE_BP=0x13;
    protected final byte ID_LATITUDE_AP=0x1B;
    protected final byte ID_N_S=0x23;
    protected final byte ID_COURSE_BP=0x14;
    protected final byte ID_COURSE_AP= 0x1C;
    protected final byte ID_DATE_MONTH =0x15;
    protected final byte ID_YEAR=0x16;
    protected final byte ID_HOUR_MINUTE=0x17;
    protected final byte ID_SECOND=0x18;
    protected final byte ID_ACC_X =0x24;
    protected final byte ID_ACC_Y =0x25;
    protected final byte ID_ACC_Z =0x26;
    protected final byte ID_VOLTAGE_AMP =0x39;
    protected final byte ID_VOLTAGE_AMP_BP=0x3A;
    protected final byte ID_VOLTAGE_AMP_AP=0x3B;
    protected final byte ID_CURRENT =0x28;
    // User defined data IDs
    protected final byte ID_GYRO_X =0x40;
    protected final byte ID_GYRO_Y =0x41;
    protected final byte ID_GYRO_Z =0x42;
    protected final byte ID_ATTITUDE_ROLL	=0x43;
    protected final byte ID_ATTITUDE_PITCH  =0x44;
    protected final byte ID_ATTITUCE_YAW	=0x45;
    protected final byte ID_BATTERY_VOLTAGE=0x46;
    protected final byte ID_VERT_SPEED  = 0x30; // opentx vario

    Frsky(){
        mrecvStructList=new ArrayList<RecvStruct>();
        //IdList=new RecvStruct[]{new RecvStruct((byte)3,"hello")};
        mrecvStructList.add(new RecvStruct((byte)0x43,"ID_ATTITUDE_ROLL"));
        mrecvStructList.add(new RecvStruct((byte)0x44,"ID_ATTITUDE_PITCH"));
        mrecvStructList.add(new RecvStruct((byte)0x45,"ID_ATTITUCE_YAW"));
        mrecvStructList.add(new RecvStruct((byte)0x46,"ID_BATTERY_VOLTAGE"));
    }

}
