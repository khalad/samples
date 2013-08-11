const byte LED_PIN = 13;

void setup()
{
  Serial.begin(9600);

  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, LOW);
}

void loop()
{
  while (Serial.available() > 0)
  {
    char ch = Serial.read();
    executeReceivedCommand(ch);
  }
}

void executeReceivedCommand(char command)
{
  switch (command)
  {
    case '0':
      digitalWrite(LED_PIN, LOW);
      break;
    case '1':
      digitalWrite(LED_PIN, HIGH);
      break;
  }
}

