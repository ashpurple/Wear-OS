package com.example.android.wearable.watchface.watchface;

import android.annotation.SuppressLint;
import android.os.Messenger;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MyMqttClient implements MqttCallback, Runnable {

	private Messenger mClient = null;
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_SEND_TO_SERVICE = 3;
	public static final int MSG_SEND_TO_ACTIVITY = 4;
	MqttClient myClient;
	MqttConnectOptions connOpt;
	static final int MAX_QUEUE_LEN = 10;
	static String BROKER_URL = "tcp://15.164.45.229:1883";
	//static final String SBSYS_USERNAME = "";
	//static final String SBSYS_PASSWORD = "";
	int msgCount;
	String from_id;
	static String to_id;
	// send to other users
	String s_topic;
	// receive from other users
	String s_topic2;
	static String msg;
	Boolean subscriber;
	public static String ans="";
	public String revMsg;

	static ArrayList<String> p_topics;
	static ArrayList<String> p_msgs;

	MqttTopic topic;
	@SuppressLint("StaticFieldLeak")
	public static MessageActivity messageActivity;

	public MyMqttClient() {
		super();
	}

	public MyMqttClient(MessageActivity myActivity){
		messageActivity = myActivity;
	}

	public MyMqttClient(String from_id) {
		super();
		this.from_id = from_id;
		this.s_topic = "/sbsys/" + from_id + "/+/+/request/reply";
		//System.out.println("s_topic: " + s_topic);
		this.s_topic2 = "/sbsys/+/+/" + from_id + "/request";
		//System.out.println("s_topic2: " + s_topic2);
		this.p_topics = new ArrayList<String>();
		this.p_msgs=new ArrayList<String>();
		this.subscriber = false;
		this.msgCount = 0;
	}

	@Override
	public void connectionLost(Throwable arg0) {
		System.out.println("Connection lost!");
		subscriber = false;
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		try {
			String myMsg = new String(token.getMessage().getPayload());
			if(!myMsg.equals("OK")){ // reply가 아닐 때
				messageActivity.sendMessage();
			}
			System.out.println("Publish is completed: " + new String(token.getMessage().getPayload()));
		} catch (MqttException e) {
			System.out.println("Error to publish topic");
			e.printStackTrace();
		}
	}

	public void insertNewTopic(String msg) {
		String p_topic;
		msgCount = (msgCount + 1) % 1000;
		p_topic = "/sbsys/" + from_id + "/msg" + msgCount + "/" + to_id + "/request";
		if (p_topics.size()>= MAX_QUEUE_LEN){
			p_topics.remove(0);
			p_msgs.remove(0);}
		p_topics.add(p_topic);
		p_msgs.add(p_topic+msg);
	}

	public void myPublish(String p_topic, String pubMsg) {
		topic = myClient.getTopic(p_topic);
		int pubQoS = 0;
		MqttMessage message = new MqttMessage(pubMsg.getBytes());
		message.setQos(pubQoS);
		message.setRetained(false);

		// Publish the message
		System.out.println("Publishing to topic \"" + p_topic + "\" qos " + pubQoS);
		MqttDeliveryToken token = null;
		try {
			token = topic.publish(message);
		} catch (Exception e) {
			System.out.println("Error in onPublish()!");
			e.printStackTrace();
		}
	}

	@Override
	public void messageArrived(String revTopic, MqttMessage message) throws Exception {

		System.out.println("Topic:" + revTopic);
		revMsg = new String(message.getPayload());
		System.out.println("Arrived Message: " + revMsg);

		String[] splitStr = revTopic.split("/");
		String senderId;
		if(revMsg.equals("OK")){
			senderId = splitStr[4];
		} else{
			senderId = splitStr[2];
		}
		/* Toast Message */
		System.out.println("SENDER ID:" + senderId);
		messageActivity.receiveMessage(senderId, revMsg);

		if (revTopic.contains("/reply")) { // 상대의 수신 확인 메시지
			String reply_topic = revTopic;
			reply_topic = reply_topic.replace("/reply", "");
			System.out.println("Modified topic:" + reply_topic);
			p_topics.remove(reply_topic);
			for(int i=0; i<p_msgs.size();i++){
				if(p_msgs.get(i).contains(reply_topic))
					p_msgs.remove(i);
			}
		}
		else if (revTopic.contains("/request")) { // 상대에게 수신 확인 전송
			String p_topic = revTopic+"/reply";
			System.out.println("Publish " + p_topic + " topic");
			myPublish(p_topic, "OK");
		}

		else
			System.out.println("Not proper message!");
	}


	public static void main(String[] args) {
		String user_id = args[0];
		to_id = args[1];
		final MyMqttClient smc = new MyMqttClient(user_id);
		Thread runThread = new Thread(smc);
		runThread.start();
		System.out.println("Finish initializing MyMqttClient");
		Timer m_timer = new Timer();
		if(!((MessageActivity) MessageActivity.context).isPressed) {
			((MessageActivity) MessageActivity.context).isPressed = true;
		}
		TimerTask m_task = new TimerTask() {
			@Override
			public void run() {
				System.out.println("Called Timer");
				int tmp=0;
				for (String p_topic : p_topics) {
					smc.myPublish(p_topic, p_msgs.get(tmp).substring(29));
					tmp++;
				}
			}
		};
		m_timer.schedule(m_task, 5000, 5000);
	}
	@Override
	public void run() {
		connOpt = new MqttConnectOptions();
		connOpt.setCleanSession(true);
		connOpt.setKeepAliveInterval(30);
		//connOpt.setUserName(SBSYS_USERNAME);
		//connOpt.setPassword(SBSYS_PASSWORD.toCharArray());

		// Connect to Broker
		try {
			myClient = new MqttClient(BROKER_URL, from_id, new MemoryPersistence());
			myClient.setCallback(this);
			myClient.connect(connOpt);
		} catch (MqttException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		System.out.println("Connected to " + BROKER_URL);

		if (myClient.isConnected()) {
			System.out.println("Successfully connected");
			subscriber = true;
		}
		else {
			System.out.println("Error to connect!");
		}

		// subscribe to topic if subscriber
		if (subscriber) {
			try {
				int subQoS = 1;
				myClient.subscribe(s_topic, subQoS);
				myClient.subscribe(s_topic2, subQoS);
				System.out.println(s_topic + "\n" + s_topic2 + " are subscribed" );

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		while (subscriber) {
			try {
				// Publish New topic
				// /sbsys/form_id/msg_id/to_id/request
				if(((MessageActivity)MessageActivity.context).sendFlag) {
					msg=((MessageActivity)MessageActivity.context).selectedAnswer;
					insertNewTopic(msg);
					((MessageActivity)MessageActivity.context).sendFlag = false;
				}
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				System.out.println("Error in sleep()!");
				e.printStackTrace();
			}
		}
		// disconnect
		try {
			myClient.disconnect();
		} catch (MqttException e) {
			System.out.println("Error in disconnect()!");
			e.printStackTrace();
		}

	}

}