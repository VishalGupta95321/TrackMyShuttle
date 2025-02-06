#ifndef AWS_MQTT_MODULE
#define AWS_MQTT_MODULE

#include <WiFiClientSecure.h>
#include <PubSubClient.h>
#include <string>
#include "credentials.h"

class AwsMqttModule {
  public:
    AwsMqttModule(const char* busID);
    void connectToAwsMqttBroker(bool& isAwsMqttBrokerConnected);
    void publish(const char* gpsData);
    bool getConnectionState();
    void loop();

  private:
    WiFiClientSecure net;
    PubSubClient mqttClient;
    BearSSL::X509List cert;
    BearSSL::X509List client_crt;
    BearSSL::PrivateKey key;
    std::string MQTT_PUB_TOPIC;
};

#endif