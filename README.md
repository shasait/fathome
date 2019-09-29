# fathome

Free At Home Connector

```
FreeAtHomeConfiguration fahConfig = new FreeAtHomeConfiguration();
fahConfig.setXXX...;
FreeAtHome fah = new FreeAtHome();
fah.connect(fahConfig);
fah.getChannel("KitchenLight").switchActuator(true);
```
