package bot.moondev.Buttons;

import bot.moondev.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class OpenTicketButton extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.getButton().getId().equalsIgnoreCase("open-ticket")) return;

        String memberId = Bot.ticketOwners.get(event.getChannelId());

        TextChannel channel = event.getChannel().asTextChannel();

        event.getGuild().retrieveMemberById(memberId).queue(
                member -> {
                    channel.upsertPermissionOverride(member)
                            .grant(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)
                            .queue();
                }
        );

        // Mensaje de confirmación de cierre
        MessageEmbed embed = new EmbedBuilder()
                .setColor(0xbca2fd)
                .setDescription("➦ " + event.getUser().getName() + " ha reabierto el ticket.")
                .build();

        event.replyEmbeds(embed).queue();
    }
}
