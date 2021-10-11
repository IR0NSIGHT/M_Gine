//TODO
#CLIENT
##HUD
- mission overview
    - currenttask
- HUD markers (HUD Indicators)    
##GUI window
- ~~window that lists mission~~
- mission management
    - ~~select~~
    - ~~accept~~
    - ~~abort~~
    - ask for more time
    - show briefing
- party management
    - invite player
    - kick player
    - become captain
- GUI scaling
    
##map
- waypoint
 - ~~lines~~
 - ~~automatic navigation to next task~~
 - button to hide waypoints [irrelevant]
 - ~~selectable sprites~~
    - ~~give info~~
    - ~~clicking centers on their pos~~
    - ~~set waypoint to sprite (rightclick)~~
 - ~~decent looking waypoint icon~~
- ~~coloring waypoints for active/finished/failed~~
   
#SERVER
## logic
- ~~generate missions randomly (seed)~~
- generate random lore texts fitting mission
- ~~update missions through centralized timer~~
- save missions persistently (with minimal fields, recreate through seed?)
- ~~decide where what missions are available for who (npc stations)~~
- global timer that generates new missions
- experience/popularity level gained through missions, unlock new missions?
- ~~mission UID~~

## missions
### Mp structure:
mission has
    - ~~max. one captain~~
    - ~~can have multiple members ("party")~~
    - captain can invite and kick members
    - ~~mission is either "claimed" or "unclaimed"~~
        
##### guarantee that the mission can be recreated with seed and minimal values!
- ~~patrol mission~~
- ~~scan sectors mission~~
- escort mission
- search and destroy mission
- defense mission
- transport goods mission
- rescue mission

#NETWORK
- synch missions between client and server
   - update mission server->client
    - ~~make missions & tasks serializable~~ 
    - ~~synch on update~~
    - ~~only send relevant missions to each client (joined, owned, avaialbe.)~~
    - ~~only send when a mission is changed. have client do countdown on his own.~~
   - send GUI interaction client->server
    - ~~packet~~
    - buttons
        - ~~accept/abort~~
        - invite
         - kick
         - leave
    
# BUGS
   