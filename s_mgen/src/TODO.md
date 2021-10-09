//TODO
#CLIENT
##HUD
- mission overview
    - currenttask
- HUD markers (HUD Indicators)    
##GUI window
- ~~window that lists mission~~
- mission management
    - select
    - ~~accept~~
    - ~~abort~~
    - ask for more time
- party management
    - invite player
    - kick player
    - become captain
    
##map
- waypoint
 - ~~lines~~
 - ~~automatic navigation to next task~~
 - button to hide waypoints
 - ~~selectable sprites~~
    - ~~give info~~
    - ~~clicking centers on their pos~~
    - ~~set waypoint to sprite (rightclick)~~
 - decent looking waypoint icon
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
### Mp structure:
mission has
    - max. one captain
    - can have multiple members ("party")
    - captain can invite and kick members
    - mission is either "claimed" or "unclaimed"
        - unclaimed: avaialbe for everyone (nearby)
        - claimed: captain can invite others
        - switch: someone accepts the mission
##### guarantee that the mission can be recreated with seed and minimal values!
- ~~patrol mission~~
- ~~scan sectors mission~~
- scan objects mission
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
    - only send relevant missions to each client (joined, owned, avaialbe.)
   - send GUI interaction client->server
    - ~~packet~~
    - buttons
    
# BUGS
- aborted mission in "active" tab of GUI
- selected mission doesnt show up in "selected tab", only after re-selecting
   