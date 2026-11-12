/*
  LabLight · Plan B de instrumentación
  Sensor de luz digital BH1750 + ESP32 (o Arduino Uno/Nano).

  Se usa SOLO si el celular no tiene sensor de luz o si se quiere comparar
  el sensor del celular con un sensor de referencia.

  Conexiones (I2C):
    BH1750 VCC  -> 3V3
    BH1750 GND  -> GND
    BH1750 SDA  -> GPIO 21 (ESP32)  /  A4 (Arduino Uno/Nano)
    BH1750 SCL  -> GPIO 22 (ESP32)  /  A5 (Arduino Uno/Nano)
    BH1750 ADDR -> GND  (dirección 0x23)

  Salida por el Monitor Serie (115200 baudios), en formato CSV:
    tiempo_ms,lux
  Esos datos se pueden pegar en una planilla, igual que el CSV de la app.

  No requiere librerías externas: se habla con el sensor directamente por I2C.
*/

#include <Wire.h>

const uint8_t BH1750_ADDR = 0x23;       // ADDR conectado a GND
const uint8_t CMD_POWER_ON = 0x01;
const uint8_t CMD_CONT_HIGH_RES = 0x10;  // modo continuo, resolución 1 lux, ~120 ms por lectura
const unsigned long PERIODO_MS = 200;    // 5 lecturas por segundo

void enviarComando(uint8_t cmd) {
  Wire.beginTransmission(BH1750_ADDR);
  Wire.write(cmd);
  Wire.endTransmission();
}

float leerLux() {
  Wire.requestFrom(BH1750_ADDR, (uint8_t)2);
  if (Wire.available() < 2) return -1;   // error de lectura
  uint16_t raw = (Wire.read() << 8) | Wire.read();
  return raw / 1.2;                      // conversión de la hoja de datos del BH1750
}

void setup() {
  Serial.begin(115200);
  Wire.begin();
  enviarComando(CMD_POWER_ON);
  enviarComando(CMD_CONT_HIGH_RES);
  delay(180);                            // primera conversión
  Serial.println("tiempo_ms,lux");
}

void loop() {
  float lux = leerLux();
  Serial.print(millis());
  Serial.print(",");
  Serial.println(lux, 1);
  delay(PERIODO_MS);
}
