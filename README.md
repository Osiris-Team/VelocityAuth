# VelocityAuth
Authenticate players on your velocity proxy (1.19 support)

## Features
- Basics
  - SQL database support.
  - Session based authentication (players only need to login once).
  - Pre-configured, auto-installed and auto-started limbo auth-server.
  - Whitelist mode to completely block not registered players from joining.
  - Ban system, to block players from joining your proxy.
- Security
  - Not logged in players get automatically forwarded to the limbo auth-server (in spectator mode).
  - Blocks all proxy command execution for not logged in players (except the /register and /login commands)
  , by changing the permissions function of the player.  
  - Prevents kicking of already connected players (username spoofing / can only happen in offline mode).
  - Prevents join blocking (username spoofing / can only happen in offline mode).
  - Secured against password timing attacks.
  - Secured against password spamming attacks, by temp-banning those players (configurable).
  - Secured against SQL injection.


## Usage
1. Download `VelocityAuth.jar` from the latest release [here](https://github.com/Osiris-Team/VelocityAuth/releases).
2. Put the jar into your velocity /plugins folder.
3. Start your velocity server/proxy.

## Player commands

#### /register _password_ _confirm-password_
- `velocityauth.register`
- Registers the player. (Players have this permission by default when not logged in.)

#### /login _password_
- `velocityauth.login`
- Logins the player. On success, forwards the player to the first server, restores permissions, creates a session
  so this player can rejoin without needing to login again. (Players have this permission by default when not logged in.)
- Details: Failed logins get saved to a table, together with 
  the UUID and IP of the player. If there are more than 5 failed attempts
  in the last hour, the player gets banned for 10 seconds on each
  following failed attempt.

## Admin commands

#### /a_register _username_ _password_
- `velocityauth.admin.register`
- Registers the provided player.

#### /a_unregister _username_
- `velocityauth.admin.unregister`
- Unregisters the provided player.

#### /a_login _username_ _password_
- `velocityauth.admin.login`
- Logins the provided player.

#### /ban _username_ (_hours_) (_reason_)
- `velocityauth.ban`
- Bans the player for 24h, with default reason: Your behavior violated our community guidelines and/or terms of service.
- Details: The UUID and IP of the player get added to 
  the banned players table. On each player join that table gets
  checked and if there is a match for the UUID OR IP,
  the connection is aborted.

#### /list_sessions _(username)_
-`velocityauth.list.sessions`
- Lists all sessions. 
- Lists sessions for the specified player, if username provided.

#### /clear_sessions _(username)_
- `velocityauth.clear.sessions`
- Removes/Clears all sessions from the database.
- Removes/Clears all sessions from the database for the specified player, if username provided.
