package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

public class CrashedBroadcast implements Broadcast {
    private final String sender;
    private final String description; 

    public CrashedBroadcast(String sender, String description){
        this.sender=sender;
        this.description=description;
    } 
 public String getSender(){
    return this.sender;
 }

   public String getDescription(){
      return this.description;
   }

}
