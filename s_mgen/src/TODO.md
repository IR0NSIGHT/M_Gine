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
    - ~~ask for more time~~
    - unselect mission
    - show tasks
- party management
    - invite player
    - kick player
    - become captain
    
- GUI scaling
- GUI remake:
    - each mission in 3tab lists:
        - shown with name, reward
        - dropdown reveals briefing
    - active tab
        - show briefing button
        - only display active waypoints? / optional "hide task"
        - show what conditions you have to fulfill to be avle to claim mission
        - show if you can claim mission or not
    
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
 - mark available missions (with small ! marker)
 - when should OPEN mission be visible ?:
    - globally
    - same system/nearby
    
# design choices:
### mission design
- a mission should take between 15 to 30 minutes

### mission markers:
 - open missions are marked with an "!" marker, visible from far away (at least 1 system) : "quest available here"
 - detailed info avaialbe when "in comms range": closeby (within loading range) "get quest infos"
 - claimable when in comms range "accept quest"
 
### UI params:
 every mission has:
 - name (name for easy distincton)
 - briefing (detailed info with lore)
 - requirement list (list of things you need: - cargo space, - personal transporter)

#SERVER
## logic
- ~~generate missions randomly (seed)~~
- generate random lore texts fitting mission
- ~~update missions through centralized timer~~
- save missions persistently (with minimal fields, recreate through seed?)
- ~~decide where what missions are available for who (npc stations)~~
- ~~global timer that generates new missions and scraps old ones~~
- handle finished mission: save, delete, clientside save?
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

# NETWORK
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
        - ~~request delay~~
        - invite
        - kick
        - leave
    
# BUGS
- missions that share the same position have (probably) overlapping quest markers
    - actually the marker system on the server only allows one marker per sector, overlapping missions just dont get shown.
- questiontag sprite has very very light edges
- finished missions dont show up in "finished" GUI window (except transport mission)
- map is clickable through the GUI window.

# Test
- do missions get locked down if they get claimed or are double claims possible?
