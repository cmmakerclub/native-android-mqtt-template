package com.cmmakerclub.nat.mqtt_template;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MQTT_NAT";
    Button mConnectButton, mOnButton, mOffButton;
    TextView mTextView1;

    MqttAndroidClient mqttAndroidClient;
    final String serverUri = "tcp://mqtt.cmmc.io:1883";
    final String clientId = "ExampleAndroidClient";
    final String subscriptionTopic = "CMMC/PLUG-001/$/command";

    final String publishTopic = "CMMC/PLUG-001/$/command";
    String publishMessage = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mConnectButton = findViewById(R.id.button_connect);
        mOnButton = findViewById(R.id.button_on);
        mOffButton = findViewById(R.id.button_off);
        mTextView1 = findViewById(R.id.myTextView1);
        mTextView1.setText("Hello CMMC");

        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView1.setText("Button Clicked.");
                mConnectButton.setEnabled(false);
                mOnButton.setEnabled(true);
                mOffButton.setEnabled(true);
                setupMqttClient();
            }
        });

        mOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishMessage = "ON";
                publishMessage();
            }
        });

        mOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publishMessage = "OFF";
                publishMessage();
            }
        });
    }

    private void setupMqttClient() {
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId+Math.random());
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    addToHistory("[CON] Reconnected to : " + serverURI);
                } else {
                    addToHistory("[CON] Connected to: " + serverURI);
                }
                subscribeToTopic();
            }

            @Override
            public void connectionLost(Throwable cause) {
                addToHistory("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                addToHistory("Incoming message: " + new String(message.getPayload()));
                mTextView1.setText(topic + " => " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);

        try {
            mqttAndroidClient.connect(mqttConnectOptions, null);
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    private void addToHistory(String mainText) {
        Log.d(TAG, "[LOG:] addToHistory: " + mainText);
        mTextView1.setText(mainText);
    }

    public void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    addToHistory("Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    addToHistory("Failed to subscribe");
                }
            });
        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    public void publishMessage() {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(publishMessage.getBytes());
            mqttAndroidClient.publish(publishTopic, message);
            addToHistory("Message Published");
            if (!mqttAndroidClient.isConnected()) {
                addToHistory(mqttAndroidClient.getBufferedMessageCount() + " messages in buffer.");
            }
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
