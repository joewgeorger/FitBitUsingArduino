#include <Wire.h> 
#include "SparkFun_MMA8452Q.h"

MMA8452Q accel;  

unsigned long nextTime = 1000;
unsigned long nextTime2 = 1500;
unsigned long delta = 20;
boolean count = false;

enum State{
  steps,
  sleep,
  sleepIdle,
  stepsIdle,
};

State sc = steps;

void setup() {
  // put your setup code here, to run once:
   Serial.begin(9600);
   sendInfo("HI");
   pinMode( 8, OUTPUT);
   pinMode( 9, OUTPUT);
   pinMode( 7, INPUT_PULLUP);
   digitalWrite( 8, HIGH);
   //Serial.println("MMA8452Q Basic Reading Code!");
   Wire.begin();

  if (accel.begin() == false) {
    //Serial.println("Not Connected. Please check connections and read the hookup guide.");
    while (1);
  }

}

void loop() {
  // put your main code here, to run repeatedly:
  unsigned long time = millis();
  runState();
  Serial.write(0x21);
  Serial.write(0x32);
  sendBitsLong(time);
}

State nextState( State s){
  switch ( s ){
    case steps:
      if( digitalRead(7) == LOW){
        sendBut();
        s = sleepIdle;
      }
      else{
        s = steps;
      }
      break;

     case sleep:
      if( digitalRead(7) == LOW){
        sendBut();
        s = stepsIdle;
      }
      else{
        s = sleep;
      }
      break;

     case stepsIdle:
      if( digitalRead(7) != LOW){
        s = steps;
      }
      else{
        s = stepsIdle;
      }
      break;

     case sleepIdle:
      if( digitalRead(7) != LOW){
        s = sleep;
      }
      else{
        s = sleepIdle;
      }
      break;
  }
  return s;
}

void runState(){
  switch ( sc ){
    case steps:
       digitalWrite( 8, HIGH);
       digitalWrite( 9, LOW);
       if (accel.available()) {      // Wait for new data from accelerometer
      // Acceleration of x, y, and z directions in g units
        Serial.write(0x21);
        Serial.write(0x35);
        int x = abs(accel.getCalculatedX() * 100);
        sendBitsInt(x);
        int y = abs(accel.getCalculatedY() * 100);
        sendBitsInt(y);
        int z = accel.getCalculatedZ() * 100;
        sendBitsInt(z);
      }
      sc = nextState( sc );
      break;
   
    case sleep:
      digitalWrite( 9, HIGH);
      digitalWrite( 8, LOW);
       if (accel.available()) {      // Wait for new data from accelerometer
      // Acceleration of x, y, and z directions in g units
         Serial.write(0x21);
        Serial.write(0x35);
        int x = abs(accel.getCalculatedX() * 100);
        sendBitsInt(x);
        int y = abs(accel.getCalculatedY() * 100);
        sendBitsInt(y);
        int z = accel.getCalculatedZ() * 100;
        sendBitsInt(z);
      }
      sc = nextState( sc );
      break;
          
    case stepsIdle:
       digitalWrite( 8, HIGH);
       digitalWrite( 9, LOW);
       if (accel.available()) {      // Wait for new data from accelerometer
      // Acceleration of x, y, and z directions in g units
        Serial.write(0x21);
        Serial.write(0x35);
        int x = abs(accel.getCalculatedX() * 100);
        sendBitsInt(x);
        int y = abs(accel.getCalculatedY() * 100);
        sendBitsInt(y);
        int z = accel.getCalculatedZ() * 100;
        sendBitsInt(z);
      }
      sc = nextState( sc );
      break;

    case sleepIdle:
      digitalWrite( 9, HIGH);
      digitalWrite( 8, LOW);
       if (accel.available()) {      // Wait for new data from accelerometer
      // Acceleration of x, y, and z directions in g units
         Serial.write(0x21);
        Serial.write(0x35);
        int x = abs(accel.getCalculatedX() * 100);
        sendBitsInt(x);
        int y = abs(accel.getCalculatedY() * 100);
        sendBitsInt(y);
        int z = accel.getCalculatedZ() * 100;
        sendBitsInt(z);
      }
      sc = nextState( sc );
      break;
    
  }
}

void sendBut(){
  Serial.write(0x21);
  Serial.write(0x36);
}

void sendCord( signed int x, signed int y, signed int z){
  Serial.write(0x21);
  Serial.write(0x35);
  Serial.print("\t");
  Serial.println(x);
  Serial.print("\t");
  Serial.println(y);
  Serial.print("\t");
  Serial.println(z);
  Serial.print("\t");
  sendBitsInt(x);
  sendBitsInt(y);
  sendBitsInt(z);
}

void sendInfo(String s){
  Serial.write("!");
  Serial.write(0x30);
  sendBitsInt(s.length());
  Serial.print(s);
}

void sendBitsInt(int a){
  Serial.write(a >> 8);
  Serial.write(a);
}

void sendBitsLong( long e){
  Serial.write(e >> 24);
  Serial.write(e >> 16);
  Serial.write(e >> 8);
  Serial.write(e);
}

void sendError(){
  String error = "High Voltage";
  Serial.write("!");
  Serial.write(0x31);
  sendBitsInt(error.length());
  Serial.print(error);
}
