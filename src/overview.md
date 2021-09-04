# Mission Generator
this program aims at generating seed-based, random missions.
Based on the seed:
- a subtype of mission is chosen
- the mission is generated
## basic logic structure
a mission is a collection of tasks. Once all mandatory (=non optional) tasks are marked as complete,
the mission is completed. if a mandatory task is failed, the mission is failed as well. all missions
have a timer, once it reaches zero, the mission fails.

### Tasks
Each mission has a list of tasks. These tasks can have preconditions. F.e. Task "deliver the cargo" can only
be activated ("state IN_PROGRESS") once its precondtion task "pick up the cargo" is completed.
each mission update, all tasks get updated again. that means, depending on the tasks code, an unlocked task
 can be locked again, if its precondition's state changes:
 if you drop the cargo, the task "pick up cargo" goes back to IN_PROGRESS, and
 "deliver cargo" gets locked again.
 
 #### task states, mission states
 tasks can be "open"(locked),"in progress"(active and completable), "success" (done), "failed"(failed).
 same goes for mission. see "MissionState" enum
 
 ### Creating unique tasks and missions
 If you want to give a mission a unique task, its advised to instantiate one and overwrite its
 "successCondition" or "failureCondition" methods.
 To reward or punish the player for completed missions, overwrite the "onSuccess" and "onFailure" methods
 
 to make a new mission inheriting from Mission.java availalbe to the generator, add it to the MissionType enum and
 the switch statement in the MissionState.generate method.