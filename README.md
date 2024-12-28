# JoinManager
A minecraft plugin for giving specific join priorities to players.

## Installation
1. Download the plugin from the releases page
2. Put the plugin jar into your plugins folder
3. Restart the server

## How it works
- Every player has a join priority.
- Players with a higher join priority can kick players with a lower join priority if the server is full.
- Join priorities are set via the permission `joinmanager.level.<level>`.
- If a player has multiple join priority permission, the highest level will count.
- Priorities must be >= 0 and lower than the maximum level.
- Players with bypass permission can join even if the server is full.

## Commands
| Command                             | Permission                   | Description                                                  |
|-------------------------------------|------------------------------|--------------------------------------------------------------|
| `/joininfo [<player>]`              | `joinmanager.command.info`   | Shows join level and bypass status of player                 |
| `/get-temp-join-bypass`             | `joinmanager.command.info`   | Shows players temporary bypassing the player limit.          |
| `/allow-temp-join-bypass <player>`  | `joinmanager.command.manage` | Allows a player to temporary bypass the player limit.        |
| `/remove-temp-join-bypass <player>` | `joinmanager.command.manage` | Removes the permission to temporary byoass the player limit. |

## Permissions
| Permission                   | Description                                                              |
|------------------------------|--------------------------------------------------------------------------|
| `joinmanager.level.<level>`  | Set a specific join priority. Replace <level> with the priority.         |
| `joinmanager.level.highest`  | Set the join priority to the maximum integer value. Ignores max. level.  |
| `joinmanager.bypass`         | Bypass the player limit on the server.                                   |
| `joinmanager.command.info`   | Grants access to `/joininfo` and `/get-temp-join-bypass`.                |
| `joinmanager.command.bypass` | Grants access to `/allow-temp-join-bypass` and `remove-temp-join-bypass` |

## Configuration
| Value                   | Description                                                                                     |
|-------------------------|-------------------------------------------------------------------------------------------------|
| `max_level`             | The maximum join priority a player can have. Higher priorities than this value will be ignored. |
| `always_bypass`         | If enabled, players with the bypass permission will never kick players when joining.            |
| `kick_message`          | The message a player will see when getting kicked for a player with higher priority.            |
| `no_permission_message` | The message a player will see when trying to run a command without the permission.              |
