package bot.moondev;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

public class CloseTicketButton extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.getButton().getId().equalsIgnoreCase("close-ticket")) return;

        Guild guild = event.getGuild();
        Member member = event.getMember();
        assert guild != null;

        if (!member.getPermissions().contains(Permission.ADMINISTRATOR) || !member.isOwner()) {
            event.reply("No tienes permitida esa acción.").setEphemeral(true).queue();
            return;
        }

        TextChannel channel = event.getChannel().asTextChannel();

        String ownerId = Bot.ticketOwners.get(event.getChannel().asTextChannel().getId());
        System.out.println(ownerId);
        event.getGuild().retrieveMemberById(ownerId).queue(
                member1 -> {
                    channel.upsertPermissionOverride(member1)
                            .deny(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND)
                            .queue();
                }
        );

        MessageEmbed embed = new EmbedBuilder()
                .setColor(Color.RED)
                .setDescription("➦ " + event.getUser().getName() + " ha cerrado el ticket.")
                .build();

        event.replyEmbeds(embed).setActionRow(
                Button.primary("open-ticket", "Reabrir ticket").withEmoji(Emoji.fromFormatted("🔓")),
                Button.danger("delete-ticket", "Eliminar ticket").withEmoji(Emoji.fromFormatted("🧨"))
        ).queue();
    }
}
