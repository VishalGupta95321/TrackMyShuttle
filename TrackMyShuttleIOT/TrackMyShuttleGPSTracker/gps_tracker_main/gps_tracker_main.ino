#include <Ticker.h>
#include <ESP8266WiFi.h>
#include <Preferences.h>
#include <ESPAsyncWebServer.h>
#include <ArduinoJson.h>
#include "gps_module.h"
#include "aws_mqtt_module.h"
#include <TimeLib.h>



// Constants
const int AP_AND_BUS_REGISTRATION_INDICATOR_LED_PIN = D4;
const int WIFI_INDICATOR_LED_PIN = D2;
const int DEVICE_READY_INDICATOR_LED_PIN = D1;
const int OPEN_AP_BUTTON_INDICATOR = 0;
const int GPS_SIGNAL_INDICATOR_PIN = D7;
const int AWS_MQTT_BROKER_CONNECTION_PIN = D8;


const char* ACCESS_POINT_NAME = "TrackMyShuttleRegistration";
const char* ACCESS_POINT_PASSWORD = "TrackMyShuttle@Pass";
const int SERVER_PORT = 80;

const char*  TIME_ZONE = "Asia/Kolkata";


// Parameter names
const char* PARAM_BUSID = "busid";
const char* PARAM_SSID = "ssid";
const char* PARAM_PASS = "pass";

// Error messages
const char* MESSAGE_ERROR_MAX_CHARACTER_EXCEED = "Error: Max Length Reached";
const char* MESSAGE_ERROR_MISSING_PARAM_OR_VALUE = "Error: Missing Param(s) or Value(s)";
const char* MESSAGE_SUCCESS_BUS_REGISTRATION = "Bus Registration Successfully";
const char* SOMETHING_WENT_WRONG = "Something Went Wrong";

// Maximum lengths
const size_t maxBusIdLength = 36;
const size_t maxSSIDLength = 32;
const size_t maxPassLength = 64;

// Structure for bus data
struct BusData {
  bool isRegistered;
  char busId[maxBusIdLength]; 
  char ssid[maxSSIDLength];
  char password[maxPassLength];
};

Ticker timer;
BusData busData;
Preferences prefs;
AsyncWebServer server(SERVER_PORT);

// Flags
bool isDeviceReady = false; /// renmae -> it will indicate if the device is ready to send gps signals.
bool isBusRegistered = false;
bool isWifiConnected = false;
bool isRegistering = false;
bool isGpsSignalAvailable = false;
bool isAwsMqttBrokerConnected = false;

// Function prototypes
bool isValidInput(AsyncWebServerRequest* request, const char* paramName);
bool isValidInputLength(const char* input, size_t maxLength);

void connectToWifi(const char* ssid, const char* pass);
bool checkIfWifiConnected();
bool checkIfBusRegistered();

void startAccessPoint();
void openBusRegistrationEndpoint(
  std::function<void(AsyncWebServerRequest*)> onSuccess, 
    std::function<void(AsyncWebServerRequest*, const char*)> onFailure
  );
void closeBusRegistrationAccessPoint();
void registerButtonHandler();

void storeBusData(BusData data);
BusData readBusData();

void controlLED(int ledPin, bool condition);
void initializePins();

void setInternalClock();


auto handleRegistrationSuccess = [](AsyncWebServerRequest* request) {
  Serial.println("Bus registered successfully!");
  int addr = 0;
  request->send(200, "text/plain", MESSAGE_SUCCESS_BUS_REGISTRATION);
  isBusRegistered = true;
  isRegistering = false;
  connectToWifi(busData.ssid,busData.password);


  storeBusData(busData);
  Serial.print("BusId: ");
  Serial.println(readBusData().busId);

 
};


auto handleRegistrationFailure = [](AsyncWebServerRequest* request, const char* errorMessage) {
  Serial.println("Bus registration failed or incomplete.");
  Serial.println(errorMessage);
  request->send(400, "text/plain", errorMessage);
};













AwsMqttModule awsMqttCLient(readBusData().busId);
GpsModule gpsClient;

bool isBrokerCOnnected = false;


void setup() {
  

  Serial.begin(115200);
  initializePins();
  
  if(checkIfBusRegistered()){
    Serial.println("Bus is already registered. ");
    isBusRegistered = true;
    busData = readBusData();
    connectToWifi(busData.ssid,busData.password);
  } else {
    Serial.println("Bus is not registered. ");
    startAccessPoint();
    openBusRegistrationEndpoint(handleRegistrationSuccess, handleRegistrationFailure);
  }

  while(WiFi.status() != WL_CONNECTED) {
    delay(500);
  }

  setInternalClock();

  ///////////////////////////////////


  awsMqttCLient.connectToAwsMqttBroker(isBrokerCOnnected);
  
  Serial.println("Connectiong to broker....");
  while(!isBrokerCOnnected) {
    delay(1000);
  }
  Serial.println("Connected to broker");

  Serial.println("Searching for gps signal....... ");

  gpsClient.begin();

  while(!gpsClient.isSignalAvailable()) {
    delay(1000);
  }


  Serial.println("GPS Signal found.");


  Serial.println("BUS ID:");
  Serial.println(readBusData().busId);

/////////////////////////////////////////////
}

void loop() {


  
  ////////////////////////////////////////
  GPSData d;
  d = gpsClient.getGpsData();


  DynamicJsonDocument jsonDoc(2048);
  jsonDoc["busId"] = "wed" ;
  jsonDoc["lat"] = d.latitude;
  jsonDoc["lng"] = d.longitude;
  jsonDoc["timestamp"] = d.timestamp;


  String jsonString = jsonDoc.as<String>();



  awsMqttCLient.publish(jsonString.c_str());

////////////////////////////////


  delay(3000);

  registerButtonHandler();  // Registering Button Handler
  controlLED(AP_AND_BUS_REGISTRATION_INDICATOR_LED_PIN,!(isBusRegistered && !isRegistering)); // Access Point Indicator
  controlLED(WIFI_INDICATOR_LED_PIN,!isWifiConnected); // WIFI indicator
 // controlLED(GPS_SIGNAL_INDICATOR_PIN,!isGpsSignalAvailable); // GPS Signal Indicator 
  //controlLED(AWS_MQTT_BROKER_CONNECTION_PIN,!isAwsMqttBrokerConnected); // AWS MQTT Broker Connection Indicator
 // controlLED(DEVICE_READY_INDICATOR_LED_PIN,isDeviceReady); // Device Ready Indicator


  // Serial.println("Bus Registration Status: ");
  // Serial.println(isBusRegistered);

  // Serial.println("Wifi Status: ");
  // Serial.println(isWifiConnected);

  // Serial.println("GPS Status: ");
  // Serial.println(isGpsSignalAvailable);

  // Serial.println("AWS MQTT Connection Status: ");
  // Serial.println(isAwsMqttBrokerConnected);

  // Serial.println("Is Device Ready: ");
  // Serial.println(isDeviceReady);


  if (isBusRegistered && !isRegistering) closeBusRegistrationAccessPoint();
  isWifiConnected = checkIfWifiConnected();
  isDeviceReady = isBusRegistered && isWifiConnected && isGpsSignalAvailable && isAwsMqttBrokerConnected;

  


  if(isDeviceReady){
    /// Send Gps data 
  }
  
}




















void initializePins() {
  pinMode(AP_AND_BUS_REGISTRATION_INDICATOR_LED_PIN, OUTPUT);
  pinMode(WIFI_INDICATOR_LED_PIN, OUTPUT);
  pinMode(DEVICE_READY_INDICATOR_LED_PIN, OUTPUT);
  pinMode(GPS_SIGNAL_INDICATOR_PIN,OUTPUT);
  pinMode(AWS_MQTT_BROKER_CONNECTION_PIN,OUTPUT);
}

void controlLED(int ledPin, bool condition) {
  digitalWrite(ledPin, condition ? HIGH : LOW);
}

void registerButtonHandler(){
 if (digitalRead(OPEN_AP_BUTTON_INDICATOR) == LOW) {
    isRegistering = true;
    startAccessPoint();
    openBusRegistrationEndpoint(handleRegistrationSuccess, handleRegistrationFailure);
  }
}

bool checkIfWifiConnected() {
  return WiFi.status() == WL_CONNECTED;
}

bool checkIfBusRegistered(){
  return readBusData().isRegistered;
}

void connectToWifi(const char* ssid, const char* pass) {
  WiFi.begin(ssid, pass);
}

void startAccessPoint() {
  WiFi.softAP(ACCESS_POINT_NAME, ACCESS_POINT_PASSWORD);
  Serial.println("Access Point Created. Connect to TrackMyShuttleRegistration ");
  Serial.print("Access Point IP Address: ");
  Serial.println(WiFi.softAPIP());
}

void openBusRegistrationEndpoint(
  std::function<void(AsyncWebServerRequest*)> onSuccess, 
  std::function<void(AsyncWebServerRequest*, const char*)> onFailure
  ) {

  server.on("/submit", HTTP_POST, [onSuccess, onFailure](AsyncWebServerRequest* request) {
    if (isValidInput(request, PARAM_BUSID) && isValidInput(request, PARAM_SSID) && isValidInput(request, PARAM_PASS)) {
      strcpy(busData.busId, request->getParam(PARAM_BUSID, true)->value().c_str());
      strcpy(busData.ssid, request->getParam(PARAM_SSID, true)->value().c_str());
      strcpy(busData.password, request->getParam(PARAM_PASS, true)->value().c_str());

      if (isValidInputLength(busData.busId, maxBusIdLength) && 
          isValidInputLength(busData.password, maxPassLength) && 
          isValidInputLength(busData.ssid, maxSSIDLength)) {
        onSuccess(request);
      } else {
        onFailure(request,MESSAGE_ERROR_MAX_CHARACTER_EXCEED);
      }
    } else {
      onFailure(request,MESSAGE_ERROR_MISSING_PARAM_OR_VALUE);
    }
  });

  server.onNotFound([](AsyncWebServerRequest* request) {
    request->send(404, "text/plain", "Not Found");
  });

  timer.attach(50,closeBusRegistrationAccessPoint);

  server.begin();
}

bool isValidInput(AsyncWebServerRequest* request, const char* paramName) {
  if (request->hasParam(paramName, true)) {
    String paramValue = request->getParam(paramName, true)->value();
    return !paramValue.isEmpty();
  }
  return false;
}

bool isValidInputLength(const char* input, size_t maxLength) {
  return strlen(input) <= maxLength;
}

bool checkBusRegistration() {
  BusData busData = readBusData();
  return busData.isRegistered;
}

void storeBusData(BusData data) {
  prefs.begin("bus_data", false);
  prefs.putBool("isRegistered", true);
  prefs.putString("busId", data.busId);
  prefs.putString("ssid", data.ssid);
  prefs.putString("password", data.password);
  prefs.end();
}

BusData readBusData() {
  BusData data;
  prefs.begin("bus_data", false);
  data.isRegistered = prefs.getBool("isRegistered", false);
  strcpy(data.busId, prefs.getString("busId", "").c_str());
  strcpy(data.ssid, prefs.getString("ssid", "").c_str());
  strcpy(data.password, prefs.getString("password", "").c_str());
  prefs.end();
  return data;
}

void closeBusRegistrationAccessPoint() {
  isRegistering = false;
  WiFi.softAPdisconnect(true);
}

// Seting ESP8266 Internal Clock

void setInternalClock() {

  configTime(TIME_ZONE,"pool.ntp.org", "time.nist.gov");
  
  time_t now = time(nullptr);
  while (now < SECS_YR_2000) {
    delay(500);
    now = time(nullptr);
  }

  setTime(now);
}

