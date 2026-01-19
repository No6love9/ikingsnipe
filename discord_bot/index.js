const { Client, GatewayIntentBits, EmbedBuilder } = require('discord.js');
const mysql = require('mysql2/promise');

// Configuration
const config = {
    token: 'YOUR_DISCORD_BOT_TOKEN',
    db: {
        host: 'localhost',
        user: 'root',
        password: '',
        database: 'snipes_casino'
    }
};

const client = new Client({
    intents: [
        GatewayIntentBits.Guilds,
        GatewayIntentBits.GuildMessages,
        GatewayIntentBits.MessageContent
    ]
});

let pool;

async function initDb() {
    pool = mysql.createPool(config.db);
    console.log('Connected to Snipes Casino Database');
}

client.once('ready', () => {
    console.log(`Logged in as ${client.user.tag}!`);
});

client.on('messageCreate', async (message) => {
    if (message.author.bot) return;

    const args = message.content.split(' ');
    const command = args[0].toLowerCase();

    if (command === '!balance') {
        const username = args[1];
        if (!username) return message.reply('Usage: !balance <rs_username>');

        try {
            const [rows] = await pool.execute('SELECT balance_gp, total_wagered, total_won FROM snipes_players WHERE username = ?', [username.toLowerCase()]);
            
            if (rows.length === 0) {
                return message.reply(`No data found for player: **${username}**`);
            }

            const player = rows[0];
            const embed = new EmbedBuilder()
                .setTitle(`💰 Balance: ${username}`)
                .setColor(0x00FFFF)
                .addFields(
                    { name: 'Current Balance', value: `${formatGP(player.balance_gp)}`, inline: true },
                    { name: 'Total Wagered', value: `${formatGP(player.total_wagered)}`, inline: true },
                    { name: 'Total Won', value: `${formatGP(player.total_won)}`, inline: true }
                )
                .setFooter({ text: 'SnipesScripts Ultimate Integration' })
                .setTimestamp();

            message.reply({ embeds: [embed] });
        } catch (err) {
            console.error(err);
            message.reply('Error fetching balance from database.');
        }
    }

    if (command === '!link') {
        const username = args[1];
        if (!username) return message.reply('Usage: !link <rs_username>');

        try {
            await pool.execute('UPDATE snipes_players SET discord_id = ? WHERE username = ?', [message.author.id, username.toLowerCase()]);
            message.reply(`Successfully linked Discord to RS account: **${username}**`);
        } catch (err) {
            console.error(err);
            message.reply('Error linking account.');
        }
    }
});

function formatGP(a) {
    if (a >= 1000000) return (a / 1000000).toFixed(1) + 'M';
    if (a >= 1000) return (a / 1000).toFixed(1) + 'K';
    return a.toString() + ' GP';
}

initDb().then(() => client.login(config.token));
