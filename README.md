# v-premier-scheduled

A bot for Valorant Premier teams that helps query and organize player availability for upcoming matches. This way, the team always knows exactly who can play and when!

## Features

- Automatically sends out a poll where everyone can vote on which days they're available to play
- Team members can vote for the Agents they'd like to play this week

## Emojis

I have uploaded example emojis in the [emojis](/src/main/resources/emojis) directory. You can easily upload these to your bot via the Discord Developer Dashboard.
Once uploaded, you just need to replace the corresponding emoji codes inside the [Agent enum](/src/main/java/io/github/pilzi/discord/enums/Agent.java) in the code.
If you prefer not to do this or if an emoji is missing, the bot will simply use the Agent's name as a fallback.

## ⚙️ Configuration (.env)

Before starting the bot, you need to create a `.env` file in the root directory of the project. Here is an example of what it should look like:

```env
# Your Bot Token (e.g., from the Discord Developer Portal)
DISCORD_API_KEY=<bot secret>

# Henrikdev api Token (https://docs.henrikdev.xyz/valorant/general)
HENRIKDEV_API_KEY=<henrikdev secret>
VALORANT_REGION=<na | latam | br | eu | kr | ap>

# Bot Database Connection
DB_URL=<db url>
DB_USERNAME=<db username>
DB_PASSWORD=<db password>

# Postgres data for docker compose initialization
POSTGRES_DB=v_premier_bot
POSTGRES_USER=v_premier_bot
POSTGRES_PASSWORD=<password>

LANGUAGE=<de | en>
POLL_CHANNEL_NAME=<discord channel name>
```

## 🚀 Installation & Start (Docker Compose)

The easiest way to run the bot is via Docker Compose. Ensure that [Docker](https://docs.docker.com/get-docker/) and [Docker Compose](https://docs.docker.com/compose/install/) are installed on your system.

1. Clone this repository.
2. (Optional) Replace emojis as described in the Emojis section.
3. Create and configure your `.env` file (see above).
4. Open your terminal in the project directory and start the bot in the background with the following command:

```bash
docker-compose up -d
```

To check the bot's logs and verify if it started successfully, you can use the following command:

```bash
docker-compose logs -f
```

To stop the bot, simply run:

```bash
docker-compose down
```