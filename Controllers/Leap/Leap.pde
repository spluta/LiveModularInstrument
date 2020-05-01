import de.voidplus.leapmotion.*;
import oscP5.*;
import netP5.*;

OscP5 oscP5;
NetAddress myRemoteLocation;
LeapMotion leap;
int lastHandCount;
int port;

void setup() {
  surface.setVisible(false);
  size(100, 100);
  background(255);
  
  if (args != null) {
    print(args);
    port = int(args[0]);
  }else{
    port = 12000;
  }
  oscP5 = new OscP5(this,port);
  
  leap = new LeapMotion(this);
  myRemoteLocation = new NetAddress("127.0.0.1",57120);
  lastHandCount = 0;
}


// ======================================================
// 1. Callbacks

void leapOnInit() {
  // println("Leap Motion Init");
}
void leapOnConnect() {
  // println("Leap Motion Connect");
}
void leapOnFrame() {
  // println("Leap Motion Frame");
}
void leapOnDisconnect() {
  // println("Leap Motion Disconnect");
}
void leapOnExit() {
  // println("Leap Motion Exit");
}


void draw() {
  
  background(255);
  
  OscBundle myBundle = new OscBundle();
  OscMessage myMessage = new OscMessage("/leapOnOff");
  
  int fps = leap.getFrameRate();
  int currentHandCount = leap.countHands();
  
  if(currentHandCount!=lastHandCount){
    
      myMessage.add(currentHandCount);
      myBundle.add(myMessage);
      myBundle.setTimetag(myBundle.now());
      oscP5.send(myBundle, myRemoteLocation);
    }
    lastHandCount=currentHandCount;
  
  if(currentHandCount>0){
    
  for (int i=0; i<1; i=i+1) {
    Hand hand = leap.getHands().get(i);

    
    // ==================================================
    // 2. Hand

    PVector handPosition       = hand.getPosition();
    float   handGrab           = hand.getGrabStrength();
    float   sphereRadius       = hand.getSphereRadius();
    
    
    
    myMessage.clear();
    myMessage.setAddrPattern("/leapContX");
    myMessage.add(constrain(handPosition.x/float(width), 0.0, 1.0));
    myBundle.add(myMessage);
     myMessage.clear();
    myMessage.setAddrPattern("/leapContZ");
    myMessage.add(constrain(1.0-(handPosition.y/float(height)), 0.0, 1.0));
    myBundle.add(myMessage);
    myMessage.clear();
    myMessage.setAddrPattern("/leapContY");
    myMessage.add(constrain((handPosition.z+20.0)/100.0, 0.0, 1.0));
    myBundle.add(myMessage);
    //myMessage.clear();
    //myMessage.setAddrPattern("/leapGrab");
    //myMessage.add(handGrab);
    //myBundle.add(myMessage);
     myMessage.clear();
    myMessage.setAddrPattern("/leapContSphere");
    myMessage.add(constrain(map(sphereRadius, 30.0, 130.0, 0.0, 1.0), 0.0, 1.0));

    myBundle.add(myMessage);

    }
    myBundle.setTimetag(myBundle.now());
    oscP5.send(myBundle, myRemoteLocation);
  }
  
}

void oscEvent(OscMessage theOscMessage) {
  if(theOscMessage.addrPattern().equals("/close")){
    exit();
  }
}
  
