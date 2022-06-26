# VelocityAuth
Authenticate players on your velocity server.

## Features
- SQL database support.
- Blocks players with the same username from joining and prevents kicking of already connected players (useful in offline mode).
- Whitelist mode to completely block not registered players from joining.

## Commands

#### /aregister _username_ _password_
- Registers the provided player.
- Permission: `velocityauth.admin.register`

#### /alogin _username_ _password_
- Logins the provided player.
- Permission: `velocityauth.admin.login`

#### /register _password_ _confirm-password_
- Registers the provided player.
- Permission: `velocityauth.register`

#### /login _password_
- Logins the provided player.
- Permission: `velocityauth.login`