#include "HX711.h"

HX711 scale;
void setup(){
  Serial.begin(115200);
  scale.begin(A5, A4);
  scale.set_scale(-21235);
  scale.tare();
}

void loop(){
  Serial.println(scale.get_units(), 4);
}