# TeamManager

A team management plugin for Paper Minecraft servers that I've been working on since August 2023.

## Features

- **Team Management**: Create, disband, and manage teams with different roles
- **Team Chat**: Private chat for team members with `/tc`
- **Team Homes**: Teleport to team's home location
- **Team Colors**: Each team can have its own color
- **Team Levels**: Teams can level up to get more members
- **Team Alliances**: Form alliances with other teams

## Commands

### Main Commands
- `/team create <name>` - Create a new team
- `/team disband` - Delete your team (owner only, requires confirmation)
- `/team invite <player>` - Invite someone to your team
- `/team join <name>` - Accept an invite
- `/team leave` - Leave your current team
- `/team promote <player>` - Promote a member to recruiter
- `/team kick <player>` - Remove someone from your team
- `/team info [name]` - View team info
- `/team list` - Show all teams

### Home Commands
- `/team sethome` - Set your team's home
- `/team home` - Teleport to team home

### Customization
- `/team color <color>` - Set team color
- `/team ally <team>` - Request an alliance

### Team Chat
- `/tc <message>` - Send a message to team
- `/team tctoggle` - Toggle team chat on/off

## Installation

1. Drop the .jar in your plugins folder
2. Restart your server
3. Configure as needed

## Development Notes

This has been a fun project to work on! Had some issues with team chat at first but got it working nicely. Team homes and colors were a bit tricky with the serialization, but I'm happy with how it came out.

Still planning to add:
- Team stats tracking
- Team banks/vaults
- Maybe team territories?
- Team events system

## Permissions
All the standard permissions are included - check plugin.yml for details.

## Known Issues
- Sometimes the scoreboard teams get out of sync if the server crashes
- Need to improve invite storage for offline players
- Need to test more with larger servers

## Credits
This was built and tested on Paper 1.20.2
Thanks to my friends for testing! 