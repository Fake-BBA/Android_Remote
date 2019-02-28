package com.kongqw.kqwrockerdemo;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import static java.sql.Types.NULL;

public class Connect {
    final String LOG_TAG = "Connect";   //日志类型
    DatagramSocket mSocket;             //套接字
    ReceiveThread mReceiveThread;       //接收线程
    SendThread mSendThread;             //发送线程
    protected Handler mUI_Handler;          //向UI线程发送消息
    InetAddress address;
    int port;
    Control mControl;   //控制飞控板
    protected Frsky mFrsky;
    short Senddata;
    //100
    Queue<Byte> mRecvQueue = new LinkedList<Byte>();

    protected final int MESSAGE_LEFT_ROCKER=0;
    protected final int MESSAGE_RIGHT_ROCKER=1;
    protected final int MESSAGE_CENTER_ROCKER=2;
    //接收主线程的消息
    protected Handler mHandlerRecvnew=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            Point point;
            switch (msg.what)
            {
                case MESSAGE_LEFT_ROCKER:
                    point=(Point)msg.obj;
                    mControl.channelBuffer[2]=(short)(1500-(short)point.y * 10);
                    mControl.channelBuffer[3]=(short)(1500+(short)point.x * 10);
                    //Log.d(LOG_TAG, "send success data is:" + point);
                    mSendThread = new SendThread(mControl.channelBuffer);
                    mSendThread.start();
                    break;
                case MESSAGE_RIGHT_ROCKER:
                    point=(Point)msg.obj;
                    mControl.channelBuffer[1]=(short)(1500-(short)point.y * 10);
                    mControl.channelBuffer[0]=(short)(1500+(short)point.x * 10);
                    //Log.d(LOG_TAG, "send success data is:" + point);
                    mSendThread = new SendThread(mControl.channelBuffer);
                    mSendThread.start();
                    break;
                case MESSAGE_CENTER_ROCKER:
                    point=(Point)msg.obj;
                    mControl.channelBuffer[6]=(short)(1500+(short)point.y * 10);
                    mControl.channelBuffer[7]=(short)(1500+(short)point.x * 10);
                    //Log.d(LOG_TAG, "send success data is:" + point);
                    mSendThread = new SendThread(mControl.channelBuffer);
                    mSendThread.start();
                    break;
                default:;
            }
        }
    };

    Connect()
    {
        mControl = new Control();
        mFrsky = new Frsky();
    }

    public void CopyHandler(Handler handler){
        this.mUI_Handler=handler;
    }

    public void ConnectRemote(String domain_name, int port) throws IOException{

        if (mSocket == null || mSocket.isClosed()) {
            try {
                address = InetAddress.getByName(domain_name);
                this.port=port;
                mSocket = new DatagramSocket(port);

                //mSocket.connect(address, port);
                Log.d(LOG_TAG, "connect "+domain_name);
                Log.d(LOG_TAG, "local port is : "+mSocket.getLocalPort());
                //开启接收线程
                mReceiveThread = new ReceiveThread();
                mReceiveThread.start();

            } catch (SocketException e) {
                Log.d(LOG_TAG, "connect fail");
                e.printStackTrace();
                Log.d(LOG_TAG, e.toString());
            }
        }
    }


    public class SendThread extends Thread{
        short sendBuffer[];

        SendThread(short sendBuffer[])
        {
            this.sendBuffer=sendBuffer;
        }
        @ Override
        public void run() {
            super.run();
            try {
                if (mSocket == null || mSocket.isClosed())
                    return;
                //发送
                //final String data = "Hello";
                //byte[]datas = data.getBytes();

               /* short data[]=new short[8];
                for(int i=0;i<8;i++) {
                    data[i]=1500;       //初始化各个频道的值
                }
                */
                int num=mControl.FrSkyFormat(mControl.channelBuffer,(short)8);
                final DatagramPacket packet = new DatagramPacket(mControl.transmitBuffer, num, address,port);
                mSocket.send(packet);
                //Log.d(LOG_TAG, "send success data is:" + data);

            } catch (UnknownHostException e) {
                Log.d(LOG_TAG, "Send Fail");
                e.printStackTrace();
            }
            catch (IOException e) {
                Log.d(LOG_TAG, "Send Fail");
                e.printStackTrace();
            }
        }
    }

    void Send(){

    }
    private class ReceiveThread extends Thread {
        int state=0;
        int pos=0;
        byte dataTemp[];
        byte recvData[];
        ReceiveThread()
        {
            dataTemp=new byte[2];
            recvData=new byte[1024];
        }
        @ Override
        public void run() {
            super.run();
            if (mSocket == null || mSocket.isClosed())
                return;
            while(!Thread.currentThread().isInterrupted())
            {
                try {
                    byte datas[] = new byte[512];
                    DatagramPacket packet = new DatagramPacket(datas, datas.length);
                    mSocket.receive(packet);
                    recvData=packet.getData();
                   // String receiveMsg = new String(packet.getData()).trim();
                    ;
                   // Log.d(LOG_TAG,"receive msg data len:"+packet.getLength());
                    for(int i=0;i<packet.getLength();i++)
                    {
                        if(mRecvQueue.offer(recvData[i]))
                        {
                            //Log.d(LOG_TAG,"offer success:"+recvData[i]);
                        }
                        else
                        {
                            //Log.d(LOG_TAG,"offer fail:"+recvData[i]);
                        }
                    }
                    byte temp;
                    //Log.d(LOG_TAG, "Recv Num:"+packet.getLength());
                    int n=0;
                    int location=0;
                    while(n++<packet.getLength())
                    {
                        //temp=recvData[n];
                        temp=mRecvQueue.poll();
                        //Log.d(LOG_TAG, "Recv:"+temp);
                        //if(NULL==temp)
                        {
                           //Log.d(LOG_TAG, "temp:empty");
                            //break;
                        }

                        switch(state)
                        {
                            case 0:
                                if(temp==0x5E)
                                {
                                    state=1;
                                    //Log.d(LOG_TAG, "is 0x5E");
                                }
                                break;
                            case 1:
                                pos=0;
                                if(temp==(byte)0x5E) break;
                                for(int i=0;i<mFrsky.mrecvStructList.size();i++)
                                {
                                    if(temp==mFrsky.mrecvStructList.get(i).ID) {
                                        state = 2;       //找到ID，读取数值
                                        location=i;
                                        //Log.d(LOG_TAG, "location is "+i+ "ID is "+ temp);
                                        break;
                                    }
                                    else
                                        state=0;
                                }
                                //Log.d(LOG_TAG, "Recv:"+temp);
                                break;
                            case 2:
                                dataTemp[pos++]=temp;
                                //Log.d(LOG_TAG, "Recv:"+temp);
                                if(pos==2) {
                                    //Log.d(LOG_TAG, "Recv:"+dataTemp[0]+dataTemp[1]);

                                    short combination=0;
                                    int ch1=dataTemp[1]&0xff;
                                    int ch2=dataTemp[0]&0xff;

                                    combination=(short)((ch1<<8)+(ch2));
                                    //combination =(short)(combination|dataTemp[1]);
                                    //combination =(short)(combination << 8);
                                    //combination = (short)(combination+dataTemp[0]);
                                    Message message = Message.obtain();
                                    message.what = mFrsky.mrecvStructList.get(location).ID;
                                    message.obj=(double)combination/10;
                                    mUI_Handler.sendMessage(message);
                                    state=0;
                                }
                                break;
                                default:
                                    state=0;
                        }

                    }
                } catch (IOException e) {
                    Log.d(LOG_TAG, "Recv Fail");
                    e.printStackTrace();
                }
            }
        }
    }
}
