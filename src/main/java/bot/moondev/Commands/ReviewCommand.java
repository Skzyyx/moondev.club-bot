package bot.moondev.Commands;

import bot.moondev.Utils.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ReviewCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equalsIgnoreCase("review")) return;

        Member member = event.getMember();

        String product = event.getOption("product").getAsString();
        String message = event.getOption("message").getAsString();
        int stars = event.getOption("stars").getAsInt();

        TextChannel reviewsChannel = event.getJDA().getTextChannelById(Config.getString("reviews-channel_id"));

        MessageEmbed embed = new EmbedBuilder()
                .setAuthor(member.getEffectiveName(), null, member.getEffectiveAvatarUrl())
                .setTitle(product)
                .setDescription(message + "\n\n" + "⭐".repeat(stars))
                .setColor(0xbca2fd)
                .build();

        if (reviewsChannel != null) {
            reviewsChannel.sendMessageEmbeds(embed).queue();
        } else {
            System.out.println("[🌑] No se encontró el canal de reviews.");
        }

        event.reply("Reseña enviada! 🌑").setEphemeral(true).queue();
    }
}
