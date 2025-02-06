 #include "gps_module.h"

 void GpsModule::begin() {
  gpsSerial.begin(GPSBaud);
 }

 bool GpsModule::isSignalAvailable() {
  return gps.satellites.value() > 0;
 }


time_t GpsModule::convertDateTimeToTimestamp(int year, int month, int day, int hour, int minute, int second) {
  struct tm gps_date_time;
  gps_date_time.tm_year = year - 1900;
  gps_date_time.tm_mon = month - 1;
  gps_date_time.tm_mday = day;
  gps_date_time.tm_hour = hour;
  gps_date_time.tm_min = minute;
  gps_date_time.tm_sec = second;

  return mktime(&gps_date_time);
}

 GPSData GpsModule::getGpsData() {

  GPSData data;

  while(gpsSerial.available() > 0) {
    char data = gpsSerial.read();
    gps.encode(data);
  }

  if(gps.satellites.value() > 0 && gps.location.isUpdated() && gps.time.isUpdated()) {

    time_t timestamp = convertDateTimeToTimestamp(
      gps.date.year(),
      gps.date.month(),
      gps.date.day(),
      gps.time.hour(),
      gps.time.minute(),
      gps.time.second()
    );

    data.latitude = gps.location.lat();
    data.longitude = gps.location.lng();
    data.speedKmph = gps.speed.kmph();
    data.timestamp = (double)timestamp;
  }

  return data;
 }