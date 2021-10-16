M_Gine ("mission engine") is a framework that will procedurally generate missions. Players can accept these missions, usually provided by NPC factions, and do them alone or in groups. The mod is fully multiplayer/server capable.

THIS IS AN ALPHA VERSION, BUGS MIGHT OCCUR

A mission consists of 1 or more MissionTasks, once all tasks are complete, the mission is successfull and the reward is payed. If at least one mandatory task is failed or the time runs out, the mission will fail.

Right now 3 types of missions exist:
- patrol: fly to a bunch of sectors
- scout: scan a bunch of sectors
- transport: load at station A, bring to station B

More types of missions are planned/in developement.
Press left-control + m to open the mission GUI.

Inspecting missions:
- all missions in state "OPEN" (unclaimed+public) are marked with a questiontag on the map.
- if you are within 16 sectors, you can see the mission in detail, it will also show in the GUI list "available"

- left click marker to center on it/select mission
- right click to set waypoint to the marker


Accepting mission:
- you have to be in the missions start sector (marked with questiontag)
- you can not be at war with the faction that offers the mission
- the mission has to be unclaimed
- for transport: you can not be at war with the faction that receives the cargo


Mission cycle:
After 22 to 37,5 minutes, unclaimed missions are deleted and replaced with new ones.


Known limitations:
- some missions require existing NPC stations. In a new universe, only the homebases of the NPCs exist. Do some scout missions to uncover => spawn more stations, to get more variation
- The mod was built with WarpSpace in mind, if you use the vanilla jump mechanic, missions will be quick and probably boring

Known bugs:
- missions do not get saved across server/game restarts
- finished missions do not show up in their GUI list.
- the "invite" and "kick" buttons dont work

Planned for the near future:
- "Search and destroy" mission

- Multiple players per party
- Reputation with factions that influences reward/type of missions/peace+war with faction
- Admin commands for debugging and config control