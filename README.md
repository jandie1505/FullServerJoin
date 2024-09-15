# FullServerJoin
A minecraft plugin for giving specific join priorities to players.

## Installation
1. Download the plugin from the releases page
2. Put the plugin jar into your plugins folder
3. Restart the server

## Usage
- Every player has a join priority.
- Players with a higher join priority can kick players with a lower join priority if the server is full.
- Join priorities are set via the permission `fullserverjoin.level.<level>`.
- If a player has multiple join priority permission, the highest level will count.
- Priorities must be >= 0 and lower than the maximum level.
- Players with bypass permission can join even if the server is full.

## Commands
With the command `/joininfo [player]`, you can get the join priority of a player.  
The command requires the permission `fullserverjoin.command.joininfo`.

## Permissions
| Permission | Description |
|--|--|
| `fullserverjoin.level.<level>` | Set a specific join priority. Replace <level> with the priority. |
| `fullserverjoin.level.highest` | Set the join priority to the maximum integer value. Ignores max. level. |
| `fullserverjoin.command.joininfo` | Allows the usage of the command `/getjoinpriority` |
| `fullserverjoin.bypass` | Bypass the player limit on the server. |

## Configuration
| Value | Description |
|--|--|
| `max_level` | The maximum join priority a player can have. Higher priorities than this value will be ignored. |
| `kick_message` | The message a player will see when getting kicked for a player with higher priority. |
| `no_permission_message` | The message a player will see when trying to run a command without the permission. |
