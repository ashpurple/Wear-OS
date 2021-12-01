package com.example.android.wearable.watchface.watchface;

import android.content.Context;
import android.widget.Toast;


import java.util.ArrayList;
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


	MqttClient myClient;
    MqttConnectOptions connOpt;
   static final int MAX_QUEUE_LEN = 10;
	static String BROKER_URL = "tcp://15.164.45.229:1883";
//	static final String SBSYS_USERNAME = "";
//	static final String SBSYS_PASSWORD = "";
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
	
    
	static ArrayList<String> p_topics;
	static ArrayList<String> p_msgs;

	MqttTopic topic;
	public static Context context2;

	public MyMqttClient() {
		super();
	}
	
	public MyMqttClient(Context context){
		context2=context;
	}
	public MyMqttClient(String from_id) {
		super();
		this.from_id = from_id;
		this.s_topic = "/sbsys/" + from_id + "/+/+/request/reply";
		//System.out.println("s_topic: " + s_topic);
		this.s_topic2 = "/sbsys/+/+/" + from_id + "/request";
		//System.out.println("s_topic2: " + s_topic2);
		this.p_topics = new ArrayList();
		this.p_msgs=new ArrayList();
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
			System.out.println("Pubish is completed: " + new String(token.getMessage().getPayload()));
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
		String revMsg;
		System.out.println("Topic:" + revTopic);
		revMsg = new String(message.getPayload());
		System.out.println("Message: " + revMsg);
		//Toast.makeText(context2, revTopic.substring(7,11)+" : "+revMsg,Toast.LENGTH_SHORT).show();
		if (revTopic.contains("/reply")) {
			String reply_topic = revTopic;
			reply_topic = reply_topic.replace("/reply", "");
			System.out.println("Modified topic:" + reply_topic);
			p_topics.remove(reply_topic);
			for(int i=0; i<p_msgs.size();i++){
				if(p_msgs.get(i).contains(reply_topic))
					p_msgs.remove(i);
		}
		}
		else if (revTopic.contains("/request")) {
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
		//String user_id = "user001";
		final MyMqttClient smc = new MyMqttClient(user_id);
		Thread runThread = new Thread(smc);
		runThread.start();
		System.out.println("Finish initializing MyMqttClient");
		Timer m_timer = new Timer();
		if(((MessageActivity)MessageActivity.context).presscheck==0) {
			((MessageActivity)MessageActivity.context).presscheck = 1;
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
				if(((MessageActivity)MessageActivity.context).sendcheck==0) {

					msg=((MessageActivity)MessageActivity.context).selectedanswer;
					insertNewTopic(msg);
					((MessageActivity)MessageActivity.context).sendcheck=1;
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