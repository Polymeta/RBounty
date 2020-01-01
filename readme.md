# RBounty

A simple bounty plugin for Sponge! Works with  any economy that implements Sponge's EconomyService. Tested with the current reccomended 1.12.2 versions of spongeforge, totaleconomy, and forge as of the 1-1-20 (spongeforge-1.12.2-2838-7.1.8.jar, TotalEconomy-1.8.1.jar, and forge-1.12.2-14.23.5.2847-universal.jar)


**MECHANICS:**

Whenever a player is bountied, a serverwide (configurable) broadcast goes off indicating that a bounty has been set on them. When a player with a bounty is killed by another player, a broadcast goes off saying the killer has claimed the victim's bounty and the bounty is credited to the killer's account. 


**COMMANDS:**

/bounty set <user> <non-negative-integer> (rbounty.command.admin) Sets a players bounty without spending any money.

/bounty add <user> <non-negative-integer> (rbounty.command.user) Takes money out of the sender's default currency account and adds it to the specified user's bounty.

/bounty view <user> (rbounty.command.user) Gets a players current bounty. Will get the sender's bounty if no user is specified.

/bounty top <page> (rbounty.command.user) Shows a page of the leaderboard of the highest bountied players on the server, excluding people with bounties of 0. If no page is specified it shows the first page. 

/bounty topOnline <page> (rbounty.command.user) Shows a page of the leaderboard of the highest bountied players currently online, excluding people with bounties of 0. If no page is specified it shows the first page. 


**PERMISSIONS:**

rbounty.command.admin - Grants staff perms to rbounty, which currently only includes being able to set someone's bounty.

rbounty.command.user - Grants basic permissions to rbounty and should be given to the default group on your server. 


**CONFIGURATION:**

Within /config/rbounty/rbounty.conf, there is a true/false setting to enable or disable serverwide broadcasts. 

**TODO:**
Configurable command cooldown to prevent spam.

Permission check for allowing/disallowing bounty claims. 

More configuration/permission customization.

Allow item bounties?

**NOTE: This plugin is completely unrelated to an old Spigot plugin named RBounty that seems to have been abandoned after 1.8**

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

