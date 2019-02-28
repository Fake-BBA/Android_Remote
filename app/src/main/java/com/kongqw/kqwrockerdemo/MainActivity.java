package com.kongqw.kqwrockerdemo;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.util.Log;
import com.kongqw.rockerlibrary.view.RockerView;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    final String LOG_TAG = "MainActivity";
    private TextView mLogLeft;
    private TextView mLogRight;

    public TextView mPitch;
    public TextView mRoll;
    public TextView mN_S;
    public TextView mPower;

    public  Connect connect;    //

    private Handler mSendHandler;       //发送消息给子线程

    protected String SwitchDirection(double angle)
    {
        if((angle>336)||(angle<=21)) return "北"+angle;
        if((angle>21)&&(angle<=67)) return "东北"+angle;
        if((angle>67)&&(angle<=112)) return "东"+angle;
        if((angle>112)&&(angle<=156)) return "东南"+angle;
        if((angle>156)&&(angle<=202)) return "南"+angle;
        if((angle>202)&&(angle<=247)) return "西南"+angle;
        if((angle>247)&&(angle<=292)) return "西"+angle;
        if((angle>292)&&(angle<=336)) return "西北"+angle;
        return null;
    }
    //接收子线程消息
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
           // for(int i=0;i<connect.mFrsky.mrecvStructList.size();i++)
            {
                if(msg.what==connect.mFrsky.ID_ATTITUDE_PITCH)
                {
                    mPitch.setText(Double.toString((double) msg.obj));
                }
                else if(msg.what==connect.mFrsky.ID_ATTITUDE_ROLL)
                {
                    mRoll.setText(Double.toString((double) msg.obj));
                }
                else if(msg.what==connect.mFrsky.ID_ATTITUCE_YAW)
                {
                    String direction=SwitchDirection((double) msg.obj);
                    mN_S.setText(direction);
                }
                else if(msg.what==connect.mFrsky.ID_BATTERY_VOLTAGE)
                {
                    mPower.setText("Power:"+Double.toString((double) msg.obj));
                }
            }
            /*switch (msg.what) {
                case connect.mFrsky.ID_ATTITUDE_PITCH:
                    mPitch.setText(Double.toString((double) msg.obj));
                    break;
                case :
                default:
            }*/
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mLogLeft = (TextView) findViewById(R.id.log_left);
        mLogRight = (TextView) findViewById(R.id.log_right);

        mPitch = (TextView) findViewById(R.id.Pitch);
        mRoll = (TextView) findViewById(R.id.Roll);
        mN_S = (TextView) findViewById(R.id.N_S);
        mPower =(TextView) findViewById(R.id.Power);

        mPitch.setText("Pitch");
        mRoll.setText("Roll");
        mN_S.setText("N_S");
        mPower.setText("Power");


        try {
            connect = new Connect();
            connect.ConnectRemote("192.168.4.1", 1025);
            connect.CopyHandler(handler);
            mSendHandler=connect.mHandlerRecvnew;       //
        }
        catch(IOException e){
            e.printStackTrace();
        }

        RockerView rockerViewLeft = (RockerView) findViewById(R.id.rockerView_left);
        if (rockerViewLeft != null) {
            //rockerViewLeft.setCallBackMode(RockerView.CallBackMode.CALL_BACK_MODE_STATE_CHANGE);
            //rockerViewLeft.setOnShakeListener(RockerView.DirectionMode.DIRECTION_8, new RockerView.OnShakeListener() {
            rockerViewLeft.setOnCoordinateChangeListener(new RockerView.OnCoordinateChangeListener(){
                @Override
                public void onStart() {
                    mLogLeft.setText(null);
                }

                @Override
                public void Coordinate(Point point) {
                    mLogLeft.setText("摇动X:" +point.x+"   摇动Y:"+point.y);
                    Message message = Message.obtain();
                    message.what = connect.MESSAGE_LEFT_ROCKER;
                    message.obj=point;
                    mSendHandler.sendMessage(message);
                }

                @Override
                public void onFinish() {
                    mLogLeft.setText("摇动X:" +0+"   摇动Y:"+0);
                }
            });
        }

        RockerView rockerViewRight = (RockerView) findViewById(R.id.rockerView_right);
        if (rockerViewRight != null) {
            rockerViewRight.setOnCoordinateChangeListener(new RockerView.OnCoordinateChangeListener(){
                @Override
                public void onStart() {
                    mLogRight.setText(null);
                }

                @Override
                public void Coordinate(Point point) {
                    mLogRight.setText("摇动X:" +point.x+"   摇动Y:"+point.y);
                    Message message = Message.obtain();
                    message.what = connect.MESSAGE_RIGHT_ROCKER;
                    message.obj=point;
                    mSendHandler.sendMessage(message);
                }

                @Override
                public void onFinish() {
                    mLogRight.setText("摇动X:" +0+"   摇动Y:"+0);
                }
            });
        }


    RockerView rockerViewCenter = (RockerView) findViewById(R.id.rockerView_center);
        if (rockerViewCenter != null) {
        rockerViewCenter.setOnCoordinateChangeListener(new RockerView.OnCoordinateChangeListener(){
            @Override
            public void onStart() {
                mLogRight.setText(null);
            }

            @Override
            public void Coordinate(Point point) {
                //mLogRight.setText("摇动X:" +point.x+"   摇动Y:"+point.y);
                Message message = Message.obtain();
                message.what = connect.MESSAGE_CENTER_ROCKER;
                message.obj=point;
                mSendHandler.sendMessage(message);
            }

            @Override
            public void onFinish() {
                //mLogRight.setText("摇动X:" +0+"   摇动Y:"+0);
            }
        });
        }
    }
    private String getDirection(RockerView.Direction direction) {
        String message = null;
        switch (direction) {
            case DIRECTION_LEFT:
                message = "左";
                break;
            case DIRECTION_RIGHT:
                message = "右";
                break;
            case DIRECTION_UP:
                message = "上";
                break;
            case DIRECTION_DOWN:
                message = "下";
                break;
            case DIRECTION_UP_LEFT:
                message = "左上";
                break;
            case DIRECTION_UP_RIGHT:
                message = "右上";
                break;
            case DIRECTION_DOWN_LEFT:
                message = "左下";
                break;
            case DIRECTION_DOWN_RIGHT:
                message = "右下";
                break;
            default:
                break;
        }
        return message;
    }
}
