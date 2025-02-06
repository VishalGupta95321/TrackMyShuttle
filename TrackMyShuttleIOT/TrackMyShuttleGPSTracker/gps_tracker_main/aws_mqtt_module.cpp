#include "aws_mqtt_module.h"

AwsMqttModule::AwsMqttModule(const char* busID) 
  : cert(cacert),
    client_crt(client_cert),
    key(privkey),
    mqttClient(net) {

       MQTT_PUB_TOPIC = std::string("busdata/") + busID;
}

void AwsMqttModule::connectToAwsMqttBroker(bool& isAwsMqttBrokerConnected) {

  net.setTrustAnchors(&cert);
  net.setClientRSACert(&client_crt, &key);

  mqttClient.setServer(AWS_MQTT_HOST,AWS_MQTT_PORT);
  
  Serial.println(" Connecting To AWS Mqtt Broker:");

  while(!mqttClient.connect(THING_NAME)){ 
    delay(500);
  }

  Serial.println(" Connected To AWS Mqtt Broker:");

  isAwsMqttBrokerConnected = true;
}

bool AwsMqttModule::getConnectionState() {
  return mqttClient.state() == 0;
}

void AwsMqttModule::publish(const char* gpsData){
  mqttClient.publish(MQTT_PUB_TOPIC.c_str(), gpsData);
}


void AwsMqttModule::loop() {
  mqttClient.loop();
}