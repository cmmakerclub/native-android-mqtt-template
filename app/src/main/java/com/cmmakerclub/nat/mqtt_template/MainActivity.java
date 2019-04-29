package com.cmmakerclub.nat.mqtt_template;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

    public static final String serverUri = "tcp://mqtt.cmmc.io:1883";
    public static final String clientId = "ExampleAndroidClient" + Math.random();
    public static final String subscriptionTopic = "CMMC/DEMO/$/command";
    public static final String publishTopic = "CMMC/DEMO/$/command";

    private Button buttonConnect;
    private Button buttonOn;
    private Button buttonOff;
    private TextView textViewStatus;

    private MqttAndroidClient mqttAndroidClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonConnect = findViewById(R.id.buttonConnect);
        buttonOn = findViewById(R.id.buttonOn);
        buttonOff = findViewById(R.id.buttonOff);
        textViewStatus = findViewById(R.id.textViewStatus);
        textViewStatus.setText(getString(R.string.chiang_mai_maker_club));

        buttonConnect.setOnClickListener(view -> {
            textViewStatus.setText(getString(R.string.status_button_clicked));
            buttonConnect.setEnabled(false);
            buttonOn.setEnabled(true);
            buttonOff.setEnabled(true);
            setupMqttClient();
        });

        buttonOn.setOnClickListener(view -> publishMessage(getString(R.string.status_on)));

        buttonOff.setOnClickListener(view -> publishMessage(getString(R.string.status_off)));
    }

    private void setupMqttClient() {
        mqttAndroidClient = new MqttAndroidClient(this, serverUri, clientId + Math.random());
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverUri) {
                if (reconnect) {
                    addToHistory(getString(R.string.status_reconnected_to, serverUri));
                } else {
                    addToHistory(getString(R.string.status_connected_to, serverUri));
                }
                subscribeToTopic();
            }

            @Override
            public void connectionLost(Throwable cause) {
                addToHistory(getString(R.string.status_connection_lost));
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                addToHistory(getString(R.string.status_incoming_message, new String(message.getPayload())));
                textViewStatus.setText(getString(R.string.status_incoming_topic_and_message, topic, new String(message.getPayload())));
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
        textViewStatus.setText(mainText);
    }

    public void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    addToHistory(getString(R.string.status_subscribed));
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    addToHistory(getString(R.string.status_failed_to_subscribe));
                }
            });
        } catch (MqttException ex) {
            Log.w(TAG, "Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    public void publishMessage(String msg) {
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(msg.getBytes());
            mqttAndroidClient.publish(publishTopic, message);
            addToHistory(getString(R.string.status_message_published));
            if (!mqttAndroidClient.isConnected()) {
                addToHistory(getString(R.string.status_messages_in_buffer, mqttAndroidClient.getBufferedMessageCount()));
            }
        } catch (MqttException e) {
            Log.w(TAG, "Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
