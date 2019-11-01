# fathome

Free At Home Connector

```
FreeAtHomeConfiguration fahConfig = new FreeAtHomeConfiguration(); // [1]
fahConfig.setXXX...;

FreeAtHome fah = new FreeAtHome(); // [2]
fah.connect(fahConfig);

fah.getSwitch("KitchenLight").switchOn();
fah.getBlind("FrontWindow").moveUp();
fah.getScene("MovieScene").activate();
```

Links
* [`[1] FreeAtHomeConfiguration.java`](src/main/java/de/hasait/fathome/FreeAtHomeConfiguration.java)
* [`[2] FreeAtHome.java`](src/main/java/de/hasait/fathome/FreeAtHome.java)
