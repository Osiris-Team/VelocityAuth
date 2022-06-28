# VelocityAuth
Authenticate players on your velocity server.

## Features
- SQL database support.
- Session based authentication (players only need to login once).
- Pre-configured, auto-installed and auto-started limbo auth-server.
- Not logged in players get automatically forwarded to the limbo auth-server (in spectator mode).
- Blocks all proxy command execution for not logged in players (except the /register and /login commands)
  , by changing the permissions function of the player.  
- Prevents kicking of already connected players (username spoofing / can only happen in offline mode).
- Prevents join blocking (username spoofing / can only happen in offline mode).
- Whitelist mode to completely block not registered players from joining.

## Usage
1. Download `VelocityAuth.jar` from the latest release [here](https://github.com/Osiris-Team/VelocityAuth/releases).
2. Put the jar into your velocity /plugins folder.
3. Start your velocity server/proxy.

## Commands

#### /a_register _username_ _password_
- Registers the provided player. (Should only be used by admins.)
- Permission: `velocityauth.admin.register`

#### /a_unregister _username_
- Unregisters the provided player. (Should only be used by admins.)
- Permission: `velocityauth.admin.unregister`

#### /a_login _username_ _password_
- Logins the provided player. (Should only be used by admins.)
- Permission: `velocityauth.admin.login`

#### /register _password_ _confirm-password_
- Registers the player.
- Permission: `velocityauth.register` (Players have this permission by default when not logged in.)

#### /login _password_
- Logins the player. On success, forwards the player to the first server, restores permissions, creates a session
  so this player can rejoin without needing to login again.
- Permission: `velocityauth.login` (Players have this permission by default when not logged in.)

#### /list sessions _(username)_
- Lists all sessions or the sessions for the specified player. (Should only be used by admins.)
- Permission: `velocityauth.list.sessions`
