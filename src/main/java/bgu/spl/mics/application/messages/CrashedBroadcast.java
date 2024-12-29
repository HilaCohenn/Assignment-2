package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class CrashedBroadcast implements Broadcast {
    private final String sender;

    public CrashedBroadcast(String sender){
    this.sender=sender;
 }
 public String getSender(){
    return this.sender;
 }

}
