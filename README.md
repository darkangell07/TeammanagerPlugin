# TeamManager

A comprehensive team management plugin for Minecraft Paper servers (1.20+).

## Features

- **Team Management**: Create, disband, and manage teams with different roles (Owner, Recruit, Member)
- **Team Invites**: Invite players to your team and accept invites
- **Team PvP Control**: Toggle friendly-fire within your team
- **Team Chat**: Chat privately with your team members
- **Persistence**: Team data is saved and loaded automatically

## Commands

### Team Commands

- `/team create <name>`: Creates a team, makes the executor the **Owner**
- `/team disband`: (Owner only) Deletes the team
- `/team invite <player>`: (Owner/Recruit) Invites a player to join the team
- `/team join <name>`: Accepts an invite to join a team
- `/team leave`: Leaves your current team
- `/team kick <player>`: (Owner/Recruit) Removes a member from the team
- `/team pvp [on|off]`: (Owner only) Toggles friendly-fire for that team
- `/team info [name]`: Shows team details (Owner, Recruits, Members, PvP status)
- `/team list`: Lists all existing teams
- `/team help`: Shows help information

### Team Chat

- `/tc <message>`: Sends a message only to your team members

## Permissions

- `teammanager.team.create`: Create teams
- `teammanager.team.disband`: Disband teams (as owner)
- `teammanager.team.invite`: Invite players to teams (as owner/recruit)
- `teammanager.team.join`: Join teams with invites
- `teammanager.team.leave`: Leave teams
- `teammanager.team.kick`: Kick players from teams (as owner/recruit)
- `teammanager.team.pvp`: Toggle team PvP (as owner)
- `teammanager.team.info`: View team information
- `teammanager.team.list`: List all teams
- `teammanager.team.chat`: Use team chat

## Role Permissions

- **Owner**: Full control (disband, pvp toggle, invite, kick)
- **Recruit**: Can invite or kick, but not disband or toggle PvP
- **Member**: No management permissions

## Installation

1. Download the TeamManager.jar file
2. Place it in your server's plugins folder
3. Restart your server
4. Configure the plugin (optional)

## Configuration

The plugin creates two configuration files:

- `config.yml`: Contains general plugin settings
- `teams.yml`: Contains team data (automatically managed by the plugin)

## Support

Created by darkangel.
If you want to use it contact me on darka9962@gmail.com.
## License

This project is licensed under the MIT License - see the LICENSE file for details. 