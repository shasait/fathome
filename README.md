# fathome

Free At Home Connector

```
FreeAtHomeConfiguration fahConfig = new FreeAtHomeConfiguration();
fahConfig.setXXX...;
FreeAtHome fah = new FreeAtHome();
fah.connect(fahConfig);
FahProject fahProject = fah.getProject();
fahProject.getChannel("KitchenLight").switchActuator(true);
```
