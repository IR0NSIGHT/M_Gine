//TODO
#CLIENT
##GUI window
- ~~window that lists mission~~
- ~~button for mission selection~~
- button for mission activation
- button for mission abortion
- button to ask for more time
    
##map
- waypoint
 - ~~lines~~
 - ~~automatic navigation to next task~~
 - button to toggle automatic navigation
 - button to hide waypoints
 - ~~selectable sprites~~
    - ~~give info~~
    - ~~clicking centers on their pos~~
 - sprites 
- ~~coloring waypoints for active/finished/failed~~
   
#SERVER
## logic
- generate missions randomly (seed)
- update missions through centralized timer
- save missions persistently (with minimal fields, recreate through seed?)
- decide where what missions are available for who (npc stations)
- global timer that generates new missions
- experience/popularity level gained through missions, unlock new missions?
- mission UID

## missions
- patrol mission
- escort mission
- search and destroy mission
- defense mission
- transport goods mission
- rescue mission

#NETWORK
- synch missions between client and server
   -update mission server->client
   -send GUI interaction client->server
       
   