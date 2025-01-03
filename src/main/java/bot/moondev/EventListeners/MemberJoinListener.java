package bot.moondev.EventListeners;

import bot.moondev.Utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class MemberJoinListener extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        TextChannel channel = event.getGuild().getTextChannelById(Config.getString("joins_channel_id"));

        Member member = event.getMember();

        MessageEmbed embed = new EmbedBuilder()
                .setDescription(String.format("**¡**%s acaba de aterrizar a `moondev.club`**!**", member.getAsMention()))
                .setColor(0xbca2fd)
                .build();

        assert channel != null;
        channel.sendMessageEmbeds(embed).queue();
    }
}
