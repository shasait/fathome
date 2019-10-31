# fathome

Free At Home Connector

```
FreeAtHomeConfiguration fahConfig = new FreeAtHomeConfiguration();
fahConfig.setXXX...;

FreeAtHome fah = new FreeAtHome();
fah.connect(fahConfig);

fah.getSwitch("KitchenLight").switchOn();
fah.getBlind("FrontWindow").moveUp();
fah.getScene("MovieScene").activate();
```
