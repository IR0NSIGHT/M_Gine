# Overview
M_Gine ("mission engine") is a framework that will procedurally generate missions. Players can accept these missions, usually provided by NPC factions, and do them alone or in groups. The mod is fully multiplayer/server capable.

## Basic structure
- Every mission has one or more tasks.
- Tasks can be mandatory or optional
- Tasks can have preconditions: Other tasks that need to be completed first.
- If all mandatory tasks are complete, the mission is successfull
- If at least one mandatory task fails, the mission fails.

## Type of missions
Right now 3 types of missions exist:
- patrol: fly to a bunch of sectors
- scout: scan a bunch of sectors
- transport: load at station A, bring to station B

More types of missions are planned/in developement.

## Interaction with missions
#### Press left-control + m to open the mission GUI.
### Map navigation:
- left click marker to center on it/select mission
- right click to set waypoint to the marker
- 
### Inspecting missions:
- all missions in state "OPEN" (unclaimed+public) are marked with a questiontag on the map.
- if you are within 16 sectors, you can see the mission in detail, it will also show in the GUI list "available"

### Accepting mission:
- you have to be in the missions start sector (marked with questiontag)
- you can not be at war with the faction that offers the mission
- the mission has to be unclaimed
- for transport: you can not be at war with the faction that receives the cargo

## Mission cycle:
- After 22 to 37,5 minutes, unclaimed missions are deleted and replaced with new ones.
- Every mission is update once a second.

## Known limitations:
- some missions require existing NPC stations. In a new universe, only the homebases of the NPCs exist. Do some scout missions to uncover => spawn more stations, to get more variation
- The mod was built with WarpSpace in mind, if you use the vanilla jump mechanic, missions will be quick and probably boring

## Known bugs:
- missions do not get saved across server/game restarts
- finished missions do not show up in their GUI list.
- the "invite" and "kick" buttons dont work
- super rare: missions get desynched between players and 2 players get shown different info.
- 
## Planned for the near future:
- "Search and destroy" mission
- Multiple players per party
- Reputation with factions that influences reward/type of missions/peace+war with faction
- Admin commands for debugging and config control
