#ifndef GPS_MODULE_H
#define GPS_MODULE_H

#include <Arduino.h>
#include <SoftwareSerial.h>
#include <TinyGPS++.h>
#include <cmath>
#include <ctime>


struct GPSData {
  double timestamp = NAN;
  double latitude = NAN;
  double longitude = NAN;
  double speedKmph = NAN;
};

class GpsModule {

  public:
    void begin();
    bool isSignalAvailable();
    GPSData getGpsData();

  private:
    const int RXPin = D5;      
    const int TXPin = D6;               
    const int GPSBaud = 9600; 
    SoftwareSerial gpsSerial = SoftwareSerial(RXPin,TXPin);
    TinyGPSPlus gps;
    
    time_t convertDateTimeToTimestamp(int year, int month, int day, int hour, int minute, int second);
    
};

#endif