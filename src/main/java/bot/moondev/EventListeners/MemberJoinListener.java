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
        String avatarUrl = member.getUser().getEffectiveAvatarUrl();

        MessageEmbed embed = new EmbedBuilder()
                .setAuthor(Config.getString("join-member.author"))
                .setTitle(Config.getString("join-member.title")
                        .replace("{user-name}", member.getEffectiveName()))
                .setDescription(Config.getString("join-member.description"))
                .setFooter(Config.getString("join-member.footer"))
                .setColor(Color.decode(Config.getString("join-member.color")))
                .setThumbnail(avatarUrl)
                .build();

        assert channel != null;
        channel.sendMessage(Config.getString("join-member.message")
                        .replace("{user-mention}", member.getAsMention()))
                .addEmbeds(embed).queue();
    }
}
